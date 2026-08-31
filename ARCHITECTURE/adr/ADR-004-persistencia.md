# ADR-004 — Persistência: PostgreSQL, banco por serviço

**Status:** Aceito

## Contexto

Cada Unit (Lançamentos, Consolidado Diário) precisa de armazenamento
persistente. É preciso decidir tecnologia e estratégia de compartilhamento (ou
não) de banco entre os serviços.

## Alternativas consideradas

- **Banco compartilhado entre os dois serviços**: mais simples no curto prazo,
  mas **rejeitada** — acopla os serviços por schema, permite que uma migração
  ou lock de um serviço afete o outro, e viola o desacoplamento estabelecido no
  ADR-001 (a indisponibilidade do banco de um serviço poderia, na prática,
  contaminar o outro se fosse a mesma instância).
- **NoSQL (ex.: DynamoDB/MongoDB) para o Consolidado Diário** (read model):
  considerado por ser um modelo de leitura simples (chave = data). Viável, mas
  **não escolhida** para manter uma única tecnologia no MVP, reduzindo
  complexidade operacional; fica registrada como evolução futura válida caso o
  volume de leitura cresça muito além do previsto (dado que é um padrão de
  acesso por chave simples, ótimo para key-value store).
- **PostgreSQL, um banco lógico por serviço** (escolhida): dados financeiros
  exigem ACID e tipos numéricos exatos. Postgres cobre bem tanto o lado
  transacional (Lançamentos) quanto a projeção (Consolidado Diário), sem exigir
  uma segunda tecnologia a operar/testar dentro do prazo do desafio.

## Decisão

Cada serviço tem seu **próprio banco PostgreSQL** (instâncias/schemas
separados, mesmo em produção — "database per service"):

- `lancamentos_db`: tabela `lancamento` + tabela `outbox_evento` (ADR-005).
- `consolidado_db`: tabela `saldo_diario` + tabela `evento_processado`
  (controle de idempotência, ADR-005).

Valores monetários usam `NUMERIC(19,2)` (nunca `FLOAT`/`DOUBLE`), consistente
com o Value Object `Money` do Domain Design.

## Consequências

- Nenhuma junção (`JOIN`) entre dados dos dois serviços é possível por design
  — reforça que toda integração passa pelo contrato de eventos.
- Testes de integração usam um Postgres por serviço (via Testcontainers).
