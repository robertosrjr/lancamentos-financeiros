# Operação e Monitoramento

## Objetivo

Este documento complementa a arquitetura de observabilidade e define como o sistema deve ser operado em ambiente de teste, homologação e produção. O foco é permitir diagnóstico rápido de falhas, visualizar o comportamento do fluxo financeiro e garantir que a consistência do sistema seja sustentada por sinais confiáveis.

A operação do sistema deve responder a três perguntas fundamentais:

1. O que aconteceu?
2. Em que volume e com que velocidade?
3. Onde no fluxo a falha ou lentidão ocorreu?

## 1. Pilares da observabilidade

### 1.1 Logs

Logs estruturados devem ser produzidos em todos os pontos críticos do fluxo:

- criação de lançamento
- gravação no outbox
- publicação do evento
- retry de publicação
- consumo do evento
- item rejeitado em DLQ
- estorno de lançamento
- erro de validação de domínio
- falha de autenticação/autorização

Cada evento de log deve conter, no mínimo:

- `timestamp`
- `level`
- `service`
- `correlationId`
- `tenantId` ou `clientId`
- `usuarioId`
- `lancamentoId`
- `eventId`
- `status`
- `message`

Logs sem contexto são quase inúteis em sistemas distribuídos. Portanto, a correlação entre requisição, evento e processamento assíncrono é uma exigência operacional.

### 1.2 Métricas

As métricas devem cobrir três domínios: negócio, infraestrutura e resiliência.

#### Métricas de negócio
- `lancamentos_total`
- `lancamentos_por_tipo_total{tipo=DEBITO|CREDITO}`
- `estornos_total`
- `saldo_diario_consultas_total`
- `saldo_diario_cache_hit_total`

#### Métricas de infraestrutura
- `http_requests_total`
- `http_requests_duration_seconds`
- `database_connections_active`
- `queue_messages_pending_total`
- `queue_messages_in_dlq_total`
- `consumer_processing_duration_seconds`

#### Métricas de resiliência
- `outbox_pending_messages`
- `retry_attempts_total`
- `retry_backoff_seconds`
- `dead_letter_messages_total`
- `duplicate_events_rejected_total`

Essas métricas permitem diagnosticar: pico de carga, falha de publicador, fila presa, consumidor lento e consumo duplicado.

### 1.3 Tracing distribuído

O tracing deve permitir seguir a execução de uma operação completa, mesmo quando ela percorre múltiplas etapas e serviços.

Exemplo de ciclo:

- request HTTP do cliente
- autenticação no serviço de lançamentos
- gravação do lançamento
- persistência do evento no outbox
- publicação do evento na fila
- consumo do evento pelo serviço de consolidação
- atualização do saldo diário

Cada etapa deve receber um `traceId` e spans com nomes claros, como:

- `POST /api/v1/lancamentos`
- `lancamento.persist`
- `outbox.write`
- `outbox.publish`
- `event.consume`
- `saldo_diario.update`

Com isso, a operação passa a ser rastreável mesmo em cenários de falha assíncrona.

## 2. Regras de operação

### 2.1 Correlação

Toda operação deve possuir:

- `correlationId` para a requisição original
- `traceId` para o fluxo completo
- `clientId` para o cliente ou tenant
- `usuarioId` para o ator que executou a ação

### 2.2 Nível de log

- `INFO`: eventos de negócio e início/fim de transações
- `WARN`: retries, falhas transitórias, processamento retriado
- `ERROR`: falha final, erro inaceitável, mensagem em DLQ
- `DEBUG`: detalhes de diagnóstico somente em ambientes de desenvolvimento ou depuração

### 2.3 Retenção

Os logs e métricas devem ser retidos por período suficiente para diagnóstico operacional, mas com política clara de arquivamento. A retenção deve respeitar regras de conformidade e custo.

## 3. Alertas recomendados

### Alertas críticos
- fila de outbox crescendo sem publicação
- DLQ com mensagens acumuladas
- latência p95 acima do limite aceitável
- taxa de erro acima do limite de SLA
- consumidor parado ou sem progresso por período definido

### Alertas de atenção
- retry em alta frequência por um cliente específico
- cache hit ratio caindo abaixo do esperado
- picos anômalos de volume por tenant
- uso de CPU ou memória fora da faixa esperada

## 4. Runbook operacional

### Caso 1: outbox crescendo

Sintoma:
- `outbox_pending_messages` aumenta continuamente

Ação:
- verificar se o publicador está vivo
- checar tempo de processamento do broker
- verificar se há retry em loop
- confirmar se a dependência externa está disponível

### Caso 2: DLQ com mensagens

Sintoma:
- `dead_letter_messages_total` cresce

Ação:
- inspecionar motivo da falha
- validar payload e contrato do evento
- corrigir causa raiz
- replay manual da fila após correção

### Caso 3: latência alta no serviço de consolidação

Sintoma:
- p95 do endpoint acima do limite
- aumento de eventos pendentes

Ação:
- verificar consumo em lote
- verificar limite de throughput por cliente
- checar cache e uso de banco
- confirmar se o processamento por cliente está preservando ordem

### Caso 4: eventos duplicados

Sintoma:
- `duplicate_events_rejected_total` aumenta

Ação:
- validar implementação da chave de idempotência
- confirmar que a mesma chave está sendo usada na publicação e no consumidor
- verificar se retries duplicam a mesma mensagem

## 5. Critérios de aceite do PoC

O PoC é considerado adequado se a operação puder responder, com evidência clara, às perguntas abaixo:

- qual foi o cliente que causou a operação?
- qual foi o lançamento relacionado?
- qual foi o evento gerado?
- houve retry? por quê?
- a mensagem foi processada ou chegou à DLQ?
- a ordem foi preservada por cliente?
- a latência ou a falha foi identificada sem inspeção manual do banco?

## 6. Conclusão

A operação do sistema não pode depender apenas de “checar o banco” ou “olhar os logs do console”. O projeto precisa operar com contexto distribuído, métricas de negócio e indicadores de resiliência. Isso é o que torna a solução diagnóstica, sustentável e confiável em cenários financeiros.
