# Domain Design — Unit: Lançamentos

**Deriva de:** `aidlc-docs/plans/units_plan.md` → Unit 1
**Stories:** US01, US02, US03, US04 (`aidlc-docs/story-artifacts/user_stories_nfrs_risks.md`)

## Static Model

### Aggregate Root: `Lancamento`

| Campo | Tipo | Regra |
|---|---|---|
| `id` | UUID | gerado no registro |
| `tipo` | `TipoLancamento` (DEBITO \| CREDITO) | obrigatório |
| `valor` | `Money` (BigDecimal + moeda) | > 0 |
| `data` | LocalDate | não pode ser futura |
| `descricao` | String | obrigatória, curta |
| `categoria` | String | opcional |
| `status` | `StatusLancamento` (ATIVO \| ESTORNADO) | inicia ATIVO |
| `lancamentoOrigemId` | UUID (nullable) | preenchido quando este lançamento é um estorno de outro |
| `criadoEm` | Instant | timestamp de auditoria |

### Value Objects

- **`Money`**: encapsula valor monetário (BigDecimal, escala fixa 2 casas,
  `RoundingMode.HALF_EVEN`) + moeda (fixa `BRL` no MVP). Nunca usar
  `float`/`double` para valores financeiros.
- **`TipoLancamento`**: enum `DEBITO`, `CREDITO`.
- **`StatusLancamento`**: enum `ATIVO`, `ESTORNADO`.

### Invariantes do agregado

1. `valor` deve ser estritamente positivo; o valor do estorno também é positivo
   e o efeito contábil é invertido pelo `tipo`/sentido do lançamento, conforme
   a regra contábil formal.
2. `data` não pode ser posterior à data corrente.
3. Um lançamento com `status = ESTORNADO` não pode ser estornado novamente
   (idempotência de negócio).
4. Lançamentos são **imutáveis** após criados — não existe operação de update;
   correção é sempre um novo `Lancamento` de estorno, com `tipo` invertido em
   relação ao original e `lancamentoOrigemId` apontando para o lançamento
   original (US03).

### Domain Events

- `LancamentoRegistrado { eventId, lancamentoId, tipo, valor, data, ocorreuEm }`
- `LancamentoEstornado { eventId, lancamentoId, lancamentoOrigemId, tipo, valor, data, ocorreuEm }`

Ambos publicados via **padrão Outbox** (gravados na mesma transação do
lançamento, publicados depois por um processo assíncrono) — ver ADR-005. Isso
garante que a Unit Consolidado Diário nunca precise ser chamada de forma
síncrona, atendendo ao NFR01.

### Repository

`LancamentoRepository`
- `salvar(Lancamento): Lancamento`
- `buscarPorId(UUID): Optional<Lancamento>`
- `buscarPorPeriodo(LocalDate inicio, LocalDate fim, TipoLancamento? filtro, Pageable): Page<Lancamento>`

## Dynamic Model — Fluxo "Registrar Lançamento"

1. Cliente autenticado com `Bearer JWT` emitido pelo Authorization Server envia
   `POST /lancamentos`.
2. Aplicação valida invariantes (valor > 0, data não futura).
3. `Lancamento` é persistido + evento `LancamentoRegistrado` gravado na tabela
   outbox, **na mesma transação** (garantia atômica local).
4. Processo assíncrono (poller/CDC) publica o evento da outbox no broker
   (RabbitMQ — ver ADR-002).
5. API responde `201 Created` ao cliente **sem esperar** a publicação do evento
   (desacoplamento total do Consolidado Diário).

## Dynamic Model — Fluxo "Estornar Lançamento" (US03)

1. Cliente envia `POST /lancamentos/{id}/estorno`.
2. Aplicação verifica: lançamento existe, `status = ATIVO`.
3. Cria novo `Lancamento` de estorno com **valor positivo**, mas com `tipo`
   invertido em relação ao original (`DEBITO`↔`CREDITO`), mantendo o mesmo
   valor monetário e `lancamentoOrigemId = id original`.
4. Marca o original como `ESTORNADO` (mesma transação).
5. Publica `LancamentoEstornado` via outbox (mesmo mecanismo do registro).

> Regra contábil formal: o estorno não usa valor negativo; o ajuste é feito pela
> inversão do sentido contábil do lançamento original, preservando a auditoria e
> a imutabilidade do registro financeiro.

## Fora de escopo desta Unit (decisão explícita)

- Cálculo de saldo consolidado — pertence à Unit Consolidado Diário.
- Autenticação/autorização de usuários — tratada como cross-cutting concern
  (ver `aidlc-docs/plans/units_plan.md`), não como regra de domínio desta Unit.
