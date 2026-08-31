# Estimativa de Custos — Arquitetura Alvo (AWS)

**Diferencial priorizado.** Estimativa de referência (região `us-east-1`,
valores aproximados de tabela pública AWS, para uma carga compatível com o NFR
do desafio: baixo volume médio, picos pontuais de 50 req/s). Objetivo é
demonstrar ordem de grandeza e raciocínio de dimensionamento, não uma cotação
formal.

| Recurso | Dimensionamento sugerido | Estimativa mensal (USD) |
|---|---|---|
| ECS Fargate — Serviço Lançamentos | 2 tasks × 0.5 vCPU / 1GB (HA mínima) | ~15 |
| ECS Fargate — Serviço Consolidado Diário | 2 a 4 tasks × 0.5 vCPU / 1GB (escala no pico) | ~15–30 |
| RDS PostgreSQL — lancamentos_db | db.t4g.micro Multi-AZ | ~25 |
| RDS PostgreSQL — consolidado_db | db.t4g.micro Multi-AZ | ~25 |
| ElastiCache Redis (cache dias fechados) | cache.t4g.micro, 1 nó | ~12 |
| SNS + SQS (mensageria) | baixo volume (bem abaixo do free tier em nº de mensagens) | ~1–3 |
| Application Load Balancer (x2, um por serviço, ou 1 com roteamento por path) | 1 ALB compartilhado | ~18 |
| CloudWatch (Logs + Metrics + Alarms) | volume baixo/médio | ~10 |
| Secrets Manager | poucos segredos | ~2 |
| **Total estimado** | | **~120–150 USD/mês** |

## Racional de dimensionamento

- O pico definido no requisito (50 req/s) é **modesto** para os padrões de
  serviços web — não justifica, por si só, instâncias grandes; a estratégia de
  cache (ADR-006) reduz ainda mais a pressão sobre compute/banco.
- Redundância mínima (2 tasks, RDS Multi-AZ) já atende ao requisito de
  disponibilidade/isolamento de falha sem sobre-dimensionar.
- Custo pode cair significativamente usando Fargate Spot para tasks não
  críticas, ou subir sob demanda via Auto Scaling apenas durante os picos —
  não estimado aqui por ser uma otimização de segunda fase.

## Fora do escopo desta estimativa

- Custos de transferência de dados entre regiões/AZs (baixo, dado o volume).
- Licenciamento — toda a stack escolhida (PostgreSQL, RabbitMQ/SNS/SQS, Spring
  Boot) é open-source ou pay-as-you-go gerenciado, sem licença fixa.
