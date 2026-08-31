# ADR-002 — Tecnologia de Mensageria: RabbitMQ (mapeado para SNS/SQS em produção AWS)

**Status:** Aceito
**Depende de:** ADR-001

## Contexto

ADR-001 exige um broker para comunicação assíncrona entre Lançamentos e
Consolidado Diário. É preciso escolher uma tecnologia que: (a) rode facilmente
localmente (requisito obrigatório de README/execução local), (b) seja
proporcional à escala do problema (pico de 50 req/s), (c) tenha caminho claro
para produção em nuvem.

## Alternativas consideradas

- **Apache Kafka**: excelente para alto throughput, replay de longo prazo e
  múltiplos consumidores concorrentes de um mesmo stream. **Rejeitada** para
  este escopo: complexidade operacional (Zookeeper/KRaft, partições, tuning)
  desproporcional a 50 req/s e a dois únicos serviços; pesado para rodar
  localmente apenas para demonstrar o desafio.
- **AWS SQS/SNS diretamente**: ótimo para produção AWS, mas exige AWS real (ou
  LocalStack) para rodar localmente, adicionando fricção ao "rodar localmente"
  exigido no README.
- **RabbitMQ** (escolhida): protocolo AMQP maduro, container único e leve via
  Docker Compose, suporta o padrão exchange/fila necessário para
  publish/subscribe entre os dois serviços, e mapeia conceitualmente 1:1 para
  SNS (exchange fanout/topic) + SQS (fila por consumidor) quando migrado para
  AWS.

## Decisão

Usar **RabbitMQ** via Spring AMQP nos dois serviços, rodando em container no
`docker-compose.yml` do repositório. Um **exchange topic**
(`lancamentos.eventos`) recebe as publicações de Lançamentos; o Consolidado
Diário consome de uma fila própria (`consolidado.lancamentos.queue`) ligada a
esse exchange.

## Equivalência para a Arquitetura Alvo em nuvem (AWS — ver ADR-003)

| Local (Docker Compose) | Produção (AWS) |
|---|---|
| Exchange topic RabbitMQ | Amazon SNS (topic) |
| Fila RabbitMQ por consumidor | Amazon SQS (subscription do SNS) |
| Dead-letter queue RabbitMQ | Dead-letter queue SQS |

## Consequências

- README deve documentar `docker-compose up` subindo RabbitMQ + Postgres (x2)
  + os dois serviços.
- Consumidor implementa idempotência (ADR-005) independente da tecnologia de
  broker, o que mantém a decisão portável para SQS/SNS sem reescrever a lógica
  de negócio.
- A fila e o consumidor devem seguir política explícita de retry, DLQ, backoff
  exponencial e jitter para garantir resiliência sem amplificar picos de carga.
- A ordem de processamento deve ser preservada por chave de cliente, conforme
  explicado em `ARCHITECTURE/filas_retry_dlq.md`.
