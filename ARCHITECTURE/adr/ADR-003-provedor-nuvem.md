# ADR-003 — Provedor de Nuvem de Referência: AWS

**Status:** Aceito

## Contexto

O desafio pede uma Arquitetura Alvo e, como diferencial, estimativa de custos
de infraestrutura. É necessário fixar um provedor de referência para tornar
essas decisões concretas (mesmo sem deploy real ser exigido pelo desafio).

## Alternativas consideradas

- **Azure**: equivalente em maturidade (App Service, Service Bus, Azure SQL,
  Monitor); escolha igualmente válida.
- **GCP**: bom para workloads de dados/ML, ecossistema de mensageria (Pub/Sub)
  também adequado, porém menor familiaridade de mercado brasileiro em vagas de
  arquitetura corporativa tradicional (contexto do desafio).
- **AWS** (escolhida): cobre 1:1 todos os componentes já decididos nas ADRs
  anteriores com serviços gerenciados maduros, tem a documentação de custos
  mais acessível (AWS Pricing Calculator) para o diferencial de estimativa, e é
  o provedor mais comum em desafios/mercado de arquitetura no Brasil — reduz
  risco de suposições exóticas na avaliação.

## Decisão

Adotar **AWS** como provedor de referência da Arquitetura Alvo, mapeando:

| Necessidade arquitetural | Serviço AWS |
|---|---|
| Compute dos serviços (stateless, escala horizontal) | ECS Fargate (containers, sem gestão de servidor) |
| Persistência transacional (Lançamentos e Consolidado) | RDS PostgreSQL (Multi-AZ) |
| Mensageria assíncrona (ADR-002, produção) | SNS (publish) + SQS (subscribe) |
| Balanceamento de carga + auto scaling (pico 50 rps) | Application Load Balancer + ECS Service Auto Scaling |
| Cache do saldo consolidado (ADR-006) | ElastiCache (Redis) |
| Observabilidade | CloudWatch (Logs, Metrics, Alarms) + X-Ray (tracing) |
| Segredos (credenciais de banco, API keys) | Secrets Manager |
| Borda / proteção | API Gateway (rate limiting) + WAF |

### Observação de arquitetura: BFF não é obrigatório

- A arquitetura base usa **API Gateway + serviços de negócio** como camada de
  entrada e proteção.
- A decisão de introduzir um **BFF** é opcional e depende de necessidades de
  experiência do cliente, agregação de dados e diferenciação de contratos
  entre frontends.
- Para o MVP e para o cenário atual do desafio, não há justificativa para
  forçar um BFF; a complexidade adicional não traz valor proporcional.
- O BFF somente entra como evolução quando existir mais de um tipo de cliente
  com regras distintas de UX, composição ou autorização.

## Consequências

- A estimativa de custos (diferencial) usa os preços destes serviços como
  referência (ver `ARCHITECTURE/estimativa_custos.md`).
- IaC de exemplo (diferencial, se produzido) usa Terraform ou AWS CDK sobre
  este mapeamento.
- Esta escolha não é bloqueante para rodar o desafio localmente — Docker
  Compose local independe de nuvem.
