# Fila, Retry, DLQ, Backoff, Jitter e Ordenação por Cliente

## Objetivo

Este documento consolida os conceitos de processamento assíncrono usados na arquitetura do projeto. A ideia central é garantir que eventos financeiros não sejam perdidos, que falhas transitórias sejam tratadas com retry controlado e que a ordem de processamento seja preservada por cliente quando a regra de negócio exige serialização.

## 1. Fila de mensagens

Uma fila é um buffer de trabalho entre dois componentes. No contexto do projeto, o produtor grava o evento de lançamento no outbox e um processador assíncrono publica ou encaminha esse evento para o consumidor do consolidado diário.

A fila resolve três problemas principais:

- desacoplamento entre produtores e consumidores
- proteção da disponibilidade do serviço de lançamentos
- capacidade de reprocessamento em caso de falha temporária do consumidor

Sem fila, o processo de negócio fica acoplado ao processamento downstream. Com fila, o sistema continua funcionando mesmo que o consumidor esteja lento ou indisponível por alguns minutos.

## 2. Retry

Retry significa tentar novamente uma operação quando ela falha por problema transitório.

Exemplos de falhas transitórias:

- timeout de rede
- indisponibilidade temporária do banco
- 429 Too Many Requests
- falha de conexão com broker
- lock ou concorrência leve em banco de dados

Não devem ser reprocessadas automaticamente falhas permanentes, como:

- payload inválido
- regra de negócio rejeitada
- cliente inexistente ou sem permissão
- schema incompatível

Em sistemas financeiros, o retry deve ser controlado e observável. Ele não pode ser infinito e não pode ser aplicado cegamente a qualquer falha.

## 3. Backoff exponencial

Backoff exponencial é a técnica de aumentar o intervalo entre retries conforme falhas se acumulam.

Exemplo:

- tentativa 1: 1s
- tentativa 2: 2s
- tentativa 3: 4s
- tentativa 4: 8s
- tentativa 5: 16s

A lógica é simples: se a falha continua, o sistema espera mais tempo antes do próximo retry para reduzir a pressão sobre a dependência que falhou.

Quando a dependência é externa e lenta, esse mecanismo reduz congestionamento e evita um efeito de “storm” de reprocessamento simultâneo.

## 4. Jitter

Jitter é a adição de uma variação aleatória ao backoff.

Sem jitter, várias instâncias do mesmo consumidor podem tentar reprocessar na mesma hora, gerando pico de carga e aumentando o risco de colisão. Com jitter, cada tentativa fica ligeiramente deslocada no tempo.

Exemplo prático:

- backoff base: 8s
- jitter: 0 a 2s
- intervalo real: entre 8s e 10s

Isso evita que todos os workers façam retry ao mesmo tempo.

## 5. Dead Letter Queue (DLQ)

DLQ significa fila de mensagens mortas. Quando uma mensagem falha repetidamente, ela não é descartada sem controle. Ela é movida para uma fila de análise, diagnóstico ou replay manual.

A DLQ é essencial para:

- observar falhas persistentes
- preservar o payload original para análise
- permitir replay após correção de causa raiz
- evitar que mensagens problemáticas voltem a bloquear o fluxo principal

Em um sistema financeiro, a DLQ não é um “lixo”; ela é a evidência de falha e a rota de recuperação.

### Exemplo de política

- até 5 retries com backoff exponencial + jitter
- se ainda falhar, mover para DLQ
- registrar: motivo, tentativas, payload original, hora, requestId, clientId, traceId

## 6. Consumo em lotes

Consumo em lotes significa receber várias mensagens de uma vez em uma operação de pull.

Vantagens:

- maior throughput por chamada de I/O
- melhor uso de recursos de rede e CPU
- menor overhead de sincronização

Desvantagens:

- pode aumentar o tempo de latência de uma mensagem individual
- se a fila for misturada sem cuidado, pode quebrar a ordem lógica de eventos por cliente

Portanto, quando a regra de negócio exige ordem, o lote deve ser processado com agrupamento por chave de cliente, e não como um bloco global aleatório.

## 7. Chave de cliente para garantir ordem

Quando há dados por cliente, a ordem deve ser garantida por uma chave de negócio, como `clientId`, `tenantId` ou `usuarioId`.

A regra é:

- todo evento de um mesmo cliente segue para a mesma partição, fila ou grupo de mensagem
- eventos de clientes diferentes podem ser processados em paralelo
- eventos do mesmo cliente são processados em sequência

Isso reduz o risco de:

- order inversion
- inconsistência de saldo
- processamento de eventos fora de ordem para o mesmo usuário

### Em termos práticos

- em Kafka: usar `clientId` como chave da mensagem para a mesma partição
- em RabbitMQ: usar exchange/fila ou routing key por cliente, ou filas dedicadas por cliente
- em SQS FIFO: usar `MessageGroupId = clientId`
- em filas de processamento serial: um worker por cliente ou grupo de clientes

A chave de cliente não é apenas um detalhe operacional; ela é parte da garantia de consistência do fluxo de negócio.

## 8. Política recomendada para o projeto

Para este projeto, a política de processamento recomendada é:

1. o produtor grava o evento no outbox na mesma transação do lançamento
2. o publicador lê eventos pendentes e encaminha para a fila
3. o consumidor tenta processar com retry controlado
4. falhas transitórias usam backoff exponencial + jitter
5. falhas permanentes ou retries excedidos vão para DLQ
6. o processamento é em lotes, mas agrupado por `clientId`
7. a ordem é preservada por cliente, e não globalmente

## 9. Exemplo de decisão operacional

Se um cliente enviar 20 eventos em sequência, o sistema deve garantir que a ordem seja preservada, mesmo que outros clientes continuem sendo processados em paralelo. Isso garante que o saldo e o estado do cliente sejam consistentes e previsíveis.

Em outras palavras:

- “ordem global” é mais simples, mas mais rígida
- “ordem por cliente” é mais escalável e tem melhor desempenho
- para dados financeiros por cliente, ordem por cliente é geralmente a escolha correta

## 10. Conclusão

Retry, DLQ, backoff exponencial, jitter e ordenação por cliente são mecanismos complementares de resiliência. Juntos, eles permitem que o sistema continue funcionando sob falhas transitórias sem perder rastreabilidade nem quebrar a ordem de eventos do cliente.

No contexto do projeto, essa estratégia é especialmente importante porque o domínio financeiro exige consistência, previsibilidade e observabilidade.
