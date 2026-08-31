# Monitoramento e Observabilidade

**Diferencial priorizado.** Atende ao NFR06 (`aidlc-docs/story-artifacts/user_stories_nfrs_risks.md`).

## Pilares

Os três pilares da observabilidade são:

1. **Logs** — responder: o que aconteceu?
2. **Métricas** — responder: quão frequentemente e quão rápido aconteceu?
3. **Tracing distribuído** — responder: em qual etapa do fluxo a requisição ou evento ficou preso?

Juntos, esses pilares permitem diagnósticos rápidos, operação segura e validação de comportamento em produção.

### Logs
- Logs estruturados em JSON (ex.: Logback com encoder JSON) em ambos os
  serviços, incluindo `correlationId`, `tenantId` e `client_id` no contexto do
  request. O `tenantId` deve ser propagado via token JWT e também anexado ao
  evento de domínio publicado, permitindo rastrear uma requisição de ponta a
  ponta (cliente → edge → API → evento → Consolidado).
- Em produção: agregados no CloudWatch Logs (ADR-003).
- Logs da borda (WAF/Load Balancer) e do IdP devem registrar tentativa de acesso,
  rejeições de autenticação e latência de ingestão, ajudando a distinguir falha
  de autenticação de indisponibilidade de backend.

### Métricas
- Expostas via Spring Boot Actuator + Micrometer (`/actuator/prometheus` ou
  CloudWatch em produção):
  - `lancamentos_registrados_total` (contador, por tipo débito/crédito)
  - `lancamentos_estornados_total`
  - `outbox_eventos_pendentes` (gauge — alerta se crescer, indica publicador
    travado)
  - `consolidado_eventos_processados_total` / `consolidado_eventos_duplicados_total`
    (evidencia a idempotência funcionando)
  - `http_server_requests_seconds` (latência/percentis por endpoint, padrão
    Actuator) — usado para validar o NFR02 (50 rps, ≤5% perda)
  - `consolidado_cache_hit_ratio` (valida a eficácia do ADR-006)
  - `waf_blocked_requests_total` (rastreia tentativas bloqueadas na borda)
  - `load_balancer_http_requests_total` (volume por backend e região)
  - `database_replica_lag_seconds` (mede atraso da réplica secundária em AZ-B)
  - `requests_by_tenant_total` (volume e taxa por `tenantId`/`client_id`)

### Health Checks
- `/actuator/health` em cada serviço, com indicadores customizados:
  - Lançamentos: conectividade com seu Postgres, broker e IdP/validação do
    JWT — mas **nunca** dependente da saúde do Consolidado Diário (reforça
    NFR01).
  - Consolidado Diário: conectividade com seu Postgres, cache, consumo do
    broker e validade do token do cliente autenticado.
- Na camada de infraestrutura: health checks do Load Balancer e da borda (WAF/
  firewall) devem indicar se o ingress está saudável, sem depender da
  disponibilidade dos bancos de dados internos.

### Tracing distribuído
- Correlação de requisições entre cliente, borda e serviços via `correlationId`,
  `tenantId` e claims do token JWT (sub/client_id/audience) (MVP, baixo custo).
- Evolução futura: OpenTelemetry + AWS X-Ray (ADR-003) para tracing completo
  incluindo a travessia assíncrona via broker e a duplicação escrita/leitura em
  AZs diferentes.

### Alertas (produção — CloudWatch Alarms)
- Latência p95 do Consolidado Diário acima do limite aceitável.
- Taxa de erro/perda de requisições acima de 5% (limite do NFR02).
- `outbox_eventos_pendentes` crescendo sem publicar (indica outbox
  publisher travado).
- Lag de consumo da fila do Consolidado Diário acima de um limiar (indica
  atraso na consistência eventual).
- `database_replica_lag_seconds` acima do limite aceitável, indicando read
  replica atrasada na AZ-B.
- `waf_blocked_requests_total` acima do baseline, sinalizando ataque ou mau uso
  de edge.
- `requests_by_tenant_total` com padrões anômalos para um `tenantId` específico,
  indicando abuso, pico de carga ou falha de segmentação.

## Fora do escopo do MVP (evolução futura)
- Dashboards prontos (Grafana/CloudWatch Dashboards) — documentar as queries
  principais é suficiente para o desafio.
- Tracing distribuído completo com OpenTelemetry (mencionado como evolução).
- Controle mais granular de quotas por `tenantId` e segmentação de dados em
  nível de banco/particionamento por cliente.
