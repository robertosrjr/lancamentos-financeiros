# ADR-006 — Cache e Resiliência para o Pico de 50 req/s no Consolidado Diário

**Status:** Aceito
**Atende a:** NFR02 (50 req/s de pico, ≤5% de perda)

## Contexto

O Consolidado Diário é majoritariamente um serviço de **leitura**. O documento
de requisitos fixa uma meta explícita de capacidade (50 req/s, ≤5% de perda),
que precisa ser endereçada com uma estratégia concreta, não apenas "escalar
mais servidores".

## Observação de domínio que orienta a decisão

Um `SaldoDiario` com `status = FECHADO` (dia passado) é **imutável por
definição** — não recebe mais lançamentos em condições normais. Isso torna o
resultado dessas consultas um caso ideal de cache de longa duração.

## Alternativas consideradas

- **Escalonamento horizontal puro (mais réplicas do serviço), sem cache**:
  funciona, mas exige mais réplicas/banco dimensionado para pico, custo maior
  para o mesmo resultado, e ainda concentra carga de leitura repetida no
  Postgres a cada requisição idêntica.
- **Cache de todos os dias (inclusive o dia corrente `ABERTO`) com TTL único**:
  simples, mas arrisca servir saldo desatualizado do dia corrente por mais
  tempo que o aceitável para o usuário.
- **Cache diferenciado por status do dia** (escolhida): dias `FECHADO` cacheados
  por longo prazo (ou até invalidação explícita por evento tardio, ex. estorno
  retroativo); dia `ABERTO` com TTL curto (poucos segundos) ou leitura direta.

## Decisão

1. **Cache-aside com Redis** (ElastiCache em produção — ADR-003) na frente do
   `SaldoDiarioRepository`:
   - `status = FECHADO` → cache sem expiração automática, invalidado somente
     se um evento tardio (estorno) recalcular aquele dia.
   - `status = ABERTO` → TTL curto (ex.: 5s) — equilíbrio entre atualidade e
     redução de carga no banco durante o pico do dia corrente.
2. **Rate limiting no ponto de entrada** (API Gateway em produção, ou filtro na
   aplicação localmente) como proteção final, não como estratégia primária —
   evita que picos anômalos (acima do esperado) derrubem a instância; requisições
   excedentes retornam `429`, contabilizadas dentro da margem de perda aceitável
   de 5%.
3. **Escalonamento horizontal** do serviço (stateless, múltiplas réplicas atrás
   de um load balancer) como camada adicional, não como única defesa.

## Consequências

- A meta de 50 req/s com ≤5% de perda é atendida principalmente por **reduzir a
  necessidade de tocar o banco** a cada requisição (cache), com escalonamento
  horizontal e rate limiting como camadas de defesa complementares.
- Testes de carga (diferencial "Monitoramento e Observabilidade" /
  obrigatório "Testes") devem validar esse comportamento simulando o mix de
  consultas a dias fechados vs. dia corrente.
