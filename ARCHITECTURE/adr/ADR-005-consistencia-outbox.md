# ADR-005 — Consistência entre Serviços: Transactional Outbox + Idempotent Consumer

**Status:** Aceito
**Depende de:** ADR-001, ADR-002

## Contexto

Com comunicação assíncrona (ADR-001), surgem dois riscos clássicos de
sistemas distribuídos (registrados como R01/R03 em
`aidlc-docs/story-artifacts/user_stories_nfrs_risks.md`):

1. **Dual write**: persistir o `Lancamento` no banco e publicar o evento no
   broker não são atômicos por padrão — uma falha entre os dois passos perde o
   evento (o lançamento existe, mas o Consolidado nunca fica sabendo).
2. **Entrega duplicada**: brokers com garantia *at-least-once* podem reentregar
   a mesma mensagem, processando o mesmo lançamento duas vezes no Consolidado.

## Alternativas consideradas

- **Publicar o evento diretamente no fluxo da requisição, antes/depois do
  commit** (sem outbox): simples, mas sujeito a perda de evento em caso de
  crash entre o commit do banco e a publicação. **Rejeitada** — viola a
  confiabilidade exigida para dados financeiros.
- **Change Data Capture (CDC)** direto na tabela `lancamento` (ex.: Debezium):
  elimina a tabela outbox dedicada, mas adiciona infraestrutura (conector CDC)
  desproporcional ao escopo do desafio. Registrada como evolução futura.
- **Transactional Outbox** (escolhida): grava o evento numa tabela
  `outbox_evento` **na mesma transação** do `Lancamento`; um publicador
  assíncrono (poller ou scheduled task) lê a tabela e publica no broker,
  marcando como enviado.

## Decisão

1. **Lado produtor (Lançamentos)**: Transactional Outbox. Toda escrita de
   `Lancamento` grava também uma linha em `outbox_evento` na mesma transação.
   Um processo separado publica as linhas pendentes no RabbitMQ e marca como
   publicadas (retry automático em caso de falha de publicação).
2. **Lado consumidor (Consolidado Diário)**: Idempotent Consumer. Cada evento
   carrega um `eventId` único; antes de aplicar o efeito no `SaldoDiario`, o
   consumidor verifica a tabela `evento_processado`. Se já processado,
   descarta silenciosamente (log de auditoria, sem erro).

## Consequências

- Garante **at-least-once delivery com efeito exactly-once** no domínio
  (idempotência absorve duplicatas).
- Se o Consolidado Diário divergir da fonte de verdade por qualquer motivo, é
  possível **reprocessar todo o histórico de eventos** (replay) para reconstruir
  a projeção do zero — capacidade explicitamente prevista no Domain Design do
  Consolidado Diário.
- Custo: uma tabela extra por serviço e um processo de publicação assíncrona
  (implementável com `@Scheduled` no MVP; evolução futura para Debezium se o
  volume justificar).
