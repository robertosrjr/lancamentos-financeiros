# ADR-001 — Padrão Arquitetural: Serviços Desacoplados com Comunicação Assíncrona

**Status:** Aceito
**Deriva de:** NFR01 (obrigatório), `DESIGN/lancamentos/`, `DESIGN/consolidado-diario/`

## Contexto

O documento de requisitos exige explicitamente que **o serviço de Lançamentos
não fique indisponível se o serviço de Consolidado Diário cair**. Além disso, o
Consolidado Diário precisa suportar picos de 50 req/s com no máximo 5% de perda.
São duas capacidades de negócio distintas (escrita transacional vs. relatório
agregado), com perfis de carga e criticidade diferentes.

## Alternativas consideradas

**A) Monolito modular** — os dois módulos no mesmo processo/deploy.
- Prós: menor complexidade operacional inicial, sem necessidade de broker.
- Contras: **viola diretamente o NFR01** — um problema de recursos (memória,
  threads, GC) causado por carga no módulo de Consolidado pode degradar ou
  derrubar o processo inteiro, levando Lançamentos junto. Deploy único também
  significa que uma release do Consolidado pode causar downtime de Lançamentos.
  **Rejeitada** por incompatibilidade direta com um requisito obrigatório.

**B) Microsserviços com chamada síncrona (REST/gRPC)** entre os serviços.
- Prós: mais simples de implementar que mensageria; consistência mais imediata.
- Contras: ainda existe acoplamento de disponibilidade na direção da chamada.
  Mitigar com circuit breaker/timeout reduz o risco mas não o elimina
  completamente sob picos, e adiciona latência à escrita de lançamentos se a
  chamada for do lado de Lançamentos. **Rejeitada** como padrão principal —
  mantida apenas como opção para chamadas de leitura não críticas, se surgirem.

**C) Microsserviços com comunicação assíncrona orientada a eventos** (escolhida).
- Lançamentos publica eventos de domínio após persistir localmente (Outbox
  Pattern — ver ADR-005); Consolidado Diário consome de forma independente e
  mantém sua própria projeção (CQRS read model).
- Prós: **zero chamadas bloqueantes** entre os serviços — isolamento real de
  falha; cada serviço escala e falha independentemente; picos de leitura no
  Consolidado não afetam a escrita em Lançamentos e vice-versa.
- Contras: consistência eventual (aceitável — ver seção Trade-offs) e maior
  complexidade operacional (broker de mensageria).

## Decisão

Adotar **(C)**: dois serviços independentes, Lançamentos (write model/fonte da
verdade) e Consolidado Diário (read model/projeção CQRS), comunicando-se
exclusivamente via eventos de domínio assíncronos.

## Trade-offs aceitos

- **Consistência eventual** no saldo consolidado: aceitável porque é um
  relatório derivado, não a fonte de verdade transacional do fluxo de caixa. O
  atraso esperado (ordem de segundos) não compromete o valor de negócio do
  relatório de saldo diário.
- **Complexidade operacional adicional** (broker): mitigada escolhendo uma
  tecnologia de mensageria leve para o porte do desafio (ver ADR-002).

## Consequências

- Nenhum endpoint de Lançamentos depende da disponibilidade do Consolidado
  Diário (e vice-versa).
- O Consolidado Diário deve ser projetado para reconstrução (replay) a partir
  do histórico de eventos, caso divirja da fonte de verdade.
- Testes de resiliência devem simular o Consolidado Diário fora do ar e
  confirmar que Lançamentos continua respondendo normalmente.
