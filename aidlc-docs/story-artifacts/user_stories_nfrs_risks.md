# User Stories, NFRs e Riscos — Controle de Fluxo de Caixa

**Deriva de (Intent):** `aidlc-docs/plans/level1_plan.md` → Intent
**Requisito fonte:** `aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md`

## Decisão de domínio assumida (a validar)

Lançamentos financeiros, uma vez criados, **não são editados diretamente** —
correções são feitas por **lançamento de estorno** (reversal), preservando
trilha de auditoria e imutabilidade do livro-caixa. Isso é prática padrão em
domínios financeiros e simplifica a consistência do saldo consolidado.
*(Sinalizado para validação — se preferir permitir edição direta, ajusto as
stories US03/US04.)*

---

## User Stories — Capacidade "Controle de Lançamentos"

- **US01** — Como comerciante, quero registrar um lançamento de **débito** ou
  **crédito** (valor, data, descrição, categoria opcional) para manter meu
  fluxo de caixa diário atualizado.
  - Critérios de aceite: valor > 0; tipo obrigatório (DEBITO/CREDITO); data não
    pode ser futura; lançamento recebe ID único e timestamp de criação.
- **US02** — Como comerciante, quero consultar/listar meus lançamentos filtrando
  por período (data início/fim) e tipo, para conferência.
  - Critérios de aceite: paginação; ordenação por data; filtro por tipo opcional.
- **US03** — Como comerciante, quero estornar um lançamento incorreto, para
  corrigir meu fluxo de caixa sem perder o histórico original.
  - Critérios de aceite: cria um lançamento de estorno vinculado ao original,
    com valor positivo e sentido financeiro invertido (débito↔crédito), sem
    uso de valor negativo; original marcado como "estornado", nunca apagado.
- **US04** — Como comerciante, quero consultar o detalhe de um lançamento
  específico, incluindo se foi estornado e por qual lançamento.

## User Stories — Capacidade "Consolidado Diário"

- **US05** — Como comerciante, quero consultar o saldo consolidado de um dia
  específico (créditos − débitos + saldo inicial do dia), para saber minha
  posição de caixa.
- **US06** — Como comerciante, quero consultar o histórico de saldos
  consolidados em um intervalo de datas, para análise de tendência.
- **US07** — Como sistema, quero calcular o saldo consolidado do dia a partir
  dos lançamentos e do saldo final do dia anterior, de forma assíncrona e
  independente da disponibilidade do serviço de Lançamentos no momento do
  cálculo.

## User Stories — Transversais

- **US08** — Como sistema, quero autenticar todas as chamadas às APIs com
  JWT emitido por um Authorization Server (OAuth2/OIDC) para proteger os dados
  financeiros de acesso não autorizado.
- **US09** (evolução futura, fora do MVP) — Multi-tenant (múltiplos
  comerciantes/lojas), exportação de relatórios (CSV/PDF), notificações de
  saldo negativo, integração contábil externa.

---

## Requisitos Não-Funcionais (refinados)

| ID | NFR | Origem | Métrica/Meta |
|---|---|---|---|
| NFR01 | Isolamento de falha: serviço de Lançamentos permanece disponível se o Consolidado Diário cair | Obrigatório (documento) | Lançamentos com disponibilidade independente; nenhuma chamada síncrona bloqueante de Lançamentos → Consolidado |
| NFR02 | Throughput do Consolidado Diário em pico | Obrigatório (documento) | 50 req/s, ≤5% de perda de requisições |
| NFR03 | Escalabilidade horizontal | Objetivo do desafio | Serviços stateless, escaláveis independentemente |
| NFR04 | Segurança de acesso | Objetivo do desafio | Autenticação em 100% dos endpoints; TLS em trânsito |
| NFR05 | Consistência e auditabilidade financeira | Decisão de domínio | Lançamentos imutáveis; toda alteração é um novo evento (estorno) |
| NFR06 | Observabilidade (diferencial priorizado) | Diferencial | Logs estruturados, métricas de latência/erro, health checks |
| NFR07 | Testabilidade | Obrigatório (documento) | Cobertura de testes unitários nas regras de cálculo de saldo e nos endpoints críticos |
| NFR08 | Documentação | Objetivo do desafio | ADRs, diagramas C4/fluxo de dados versionados no repositório |

---

## Riscos

| ID | Risco | Impacto | Mitigação proposta |
|---|---|---|---|
| R01 | Comunicação síncrona entre Lançamentos e Consolidado propaga indisponibilidade | Viola NFR01 (obrigatório) | Comunicação assíncrona (fila/eventos) ou, se síncrona, circuit breaker + degradação graciosa |
| R02 | Concorrência em lançamentos simultâneos gera saldo consolidado incorreto | Integridade financeira | Transações atômicas na escrita; cálculo do consolidado idempotente e recalculável |
| R03 | Perda de mensagens/eventos entre serviços | Saldo consolidado divergente do livro de lançamentos | Padrão Outbox + idempotência no consumidor; reprocessamento por replay de eventos |
| R04 | Pico de 50 req/s no Consolidado Diário degrada latência | Viola NFR02 | Cache do saldo já consolidado (dias fechados são imutáveis), rate limiting, escalonamento horizontal |
| R05 | Modelo de autenticação inadequado para usuários/papéis e multi-tenant | Segurança | Definir Authorization Server e validar claims do JWT para evitar crescimento sem rework |

---

**Status:** aguardando sua validação para prosseguir ao agrupamento em Units
(Fase 1, passo 4).
