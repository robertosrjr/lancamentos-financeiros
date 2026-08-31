# Domain Design — Unit: Consolidado Diário

**Deriva de:** `aidlc-docs/plans/units_plan.md` → Unit 2
**Stories:** US05, US06, US07 (`aidlc-docs/story-artifacts/user_stories_nfrs_risks.md`)

## Static Model

### Projection (read model): `SaldoDiario`

| Campo | Tipo | Regra |
|---|---|---|
| `data` | LocalDate | chave natural (uma linha por dia) |
| `saldoInicial` | Money | = `saldoFinal` do dia anterior (ou 0 se não existir histórico) |
| `totalCreditos` | Money | soma de créditos ativos do dia |
| `totalDebitos` | Money | soma de débitos ativos do dia |
| `saldoFinal` | Money | `saldoInicial + totalCreditos - totalDebitos` |
| `status` | `ABERTO` \| `FECHADO` | `ABERTO` = dia corrente (ainda pode mudar); `FECHADO` = dias passados (imutável, cacheável — ver ADR-006) |
| `atualizadoEm` | Instant | auditoria |

> Este agregado **não é a fonte da verdade** do fluxo de caixa — é uma
> **projeção derivada e recalculável** a partir dos eventos da Unit Lançamentos.
> Em caso de divergência, pode ser reconstruída via replay de eventos
> (justifica consistência eventual como trade-off aceitável — ver ADR-001).

### Invariante

`saldoFinal = saldoInicial + totalCreditos - totalDebitos` deve valer sempre
após cada evento processado (invariante de recálculo, não de persistência
direta de `saldoFinal`).

> Regra contábil formal aplicada no modelo de cálculo: o estorno é sempre
> registrado com valor positivo, mas com o impacto financeiro invertido em
> relação ao lançamento original. Em outras palavras, a conta de débito/crédito
> do evento de estorno é trocada, sem usar valor negativo no domínio. O saldo
> diário continua sendo calculado pela fórmula tradicional, mas o sinal do
> impacto contábil é derivado do tipo do lançamento e do evento de reversão.

### Domain Events consumidos

- `LancamentoRegistrado` → soma/subtrai do dia correspondente conforme o `tipo`.
- `LancamentoEstornado` → aplica o efeito inverso do lançamento original no dia
  correspondente, mas sem usar valor negativo: o consumidor interpreta o
  `tipo` do evento estorno como a inversão contábil do lançamento original.

### Repository

`SaldoDiarioRepository`
- `buscarPorData(LocalDate): Optional<SaldoDiario>`
- `buscarPorPeriodo(LocalDate inicio, LocalDate fim): List<SaldoDiario>`
- `upsert(SaldoDiario)`

## Dynamic Model — Consumo de evento (US07)

1. Consumidor recebe `LancamentoRegistrado`/`LancamentoEstornado` do broker.
2. **Idempotência**: verifica se `eventId` já foi processado (tabela de
   controle `eventos_processados`); se sim, descarta (at-least-once delivery
   do broker pode reentregar).
3. Recupera (ou cria) o `SaldoDiario` da `data` do evento.
4. Recalcula `totalCreditos`/`totalDebitos`/`saldoFinal`, aplicando a regra de
   estorno contábil: valor positivo e inversão do `tipo` do lançamento original.
5. Se `data` = hoje → `status = ABERTO`; se `data` < hoje → `status = FECHADO`
   (dias fechados não recebem mais eventos em condições normais, mas o
   recálculo continua correto/idempotente se um estorno tardio chegar).
6. Persiste (upsert) e marca `eventId` como processado, **na mesma transação**.

## Dynamic Model — Consulta (US05, US06)

1. `GET /consolidado/{data}` → busca direta na projeção; se dia `FECHADO`,
   resposta pode vir do cache (ver ADR-006); se `ABERTO`, TTL curto ou leitura
   direta do banco.
2. `GET /consolidado?inicio=&fim=` → busca por período, paginada.
3. Se a `data` solicitada não existir na projeção (nenhum lançamento naquele
   dia), o saldo é o `saldoFinal` do último dia anterior com registro (saldo
   permanece constante em dias sem movimentação).

## Resiliência a falhas da Unit Lançamentos

Esta Unit não faz nenhuma chamada de volta para Lançamentos — consome apenas do
broker. Se Lançamentos cair, esta Unit continua servindo o saldo já consolidado
normalmente (fica apenas "desatualizada" até novos eventos chegarem quando
Lançamentos voltar).
