# Units Plan — Controle de Fluxo de Caixa

**Deriva de:** `aidlc-docs/story-artifacts/user_stories_nfrs_risks.md`

## Units definidas

### Unit 1 — Lançamentos
Bounded context responsável por registrar, consultar e estornar lançamentos de
débito/crédito. É a **fonte da verdade** do fluxo de caixa (write model).

- Stories cobertas: US01, US02, US03, US04.
- Independência: deve permanecer disponível mesmo com o Consolidado Diário fora
  do ar (NFR01) — não faz nenhuma chamada síncrona bloqueante para o Consolidado.
- Publica eventos de domínio para quem quiser consumir (hoje: Consolidado Diário).

### Unit 2 — Consolidado Diário
Bounded context responsável por manter e servir a projeção de saldo diário
consolidado (read model), construída a partir dos eventos publicados pela Unit
Lançamentos.

- Stories cobertas: US05, US06, US07.
- É um **CQRS read side**: não é a fonte da verdade, é recalculável a partir do
  histórico de lançamentos em caso de divergência.
- Precisa suportar 50 req/s de pico com ≤5% de perda (NFR02) — ver ADR-006 (cache).

## Decisão: autenticação via OAuth2/JWT, sem Unit dedicada de Identidade de negócio

Autenticação (US08) é tratada como **capacidade transversal (cross-cutting)**,
implementada por um filtro/middleware de validação de JWT em cada serviço, com
um Authorization Server externo emitindo os tokens.

**Justificativa:** no escopo MVP single-tenant a autenticação segue uma
abordagem padronizada com OAuth2/OIDC, evitando o acoplamento a uma solução
ad hoc de API Key. Isso mantém a segurança alinhada com boas práticas e reduz
o custo de evolução para multi-tenant, múltiplos papéis e auditoria por
usuário, sem transformar autenticação em uma Unit de domínio.

## Relação entre Units

```
[Comerciante] --HTTP(S) + OAuth2 JWT (Bearer)--> [Lançamentos] --evento (RabbitMQ)--> [Consolidado Diário]
                                                   |                                        |
                                             PostgreSQL (write)                    PostgreSQL (read) + cache
```

As duas Units são **fracamente acopladas**: acopladas apenas pelo contrato de
evento (schema do evento `LancamentoRegistrado`/`LancamentoEstornado`), nunca por
chamada de API direta síncrona. Podem ter build, deploy e escala independentes.

## Bolts sugeridos

- **Bolt 1 — Lançamentos**: Domain Design, API REST, persistência, publicação de
  eventos (outbox), testes unitários/integração.
- **Bolt 2 — Consolidado Diário**: Domain Design, consumidor de eventos
  idempotente, projeção de saldo, API de consulta, cache, testes.
- **Bolt 3 — Integração & Cross-cutting**: broker de mensageria, autenticação
  (API Key), observabilidade (logs/métricas/health checks), containerização
  (Docker Compose para rodar localmente), IaC/estimativa de custos AWS,
  documentação final (README, diagramas), testes de carga do Consolidado Diário.

Cada Bolt é pequeno o suficiente para ciclos de build-validação em horas, não
semanas, conforme a definição de Bolt no AI-DLC.
