# ADR-008 — Resiliência em Filas: Retry, DLQ, Backoff, Jitter e Ordenação por Cliente

**Status:** Aceito
**Atende a:** NFRs de confiabilidade, consistência e disponibilidade do fluxo de lançamentos

## Contexto

A arquitetura do projeto usa comunicação assíncrona entre os serviços de Lançamentos e Consolidado Diário. No PoC, isso é representado por um outbox e por um pipeline de publicação/consumo em memória, mas a decisão prática precisa ser válida também quando a solução evoluir para fila real, como RabbitMQ, SQS ou Kafka.

O problema principal não é apenas “enviar mensagem”, mas garantir que:

- a mensagem não seja perdida;
- falhas temporárias sejam tratadas de forma controlada;
- mensagens problemáticas não bloqueiem o fluxo principal;
- o consumidor preserve a ordem de eventos do mesmo cliente;
- o sistema continue confiável mesmo sob retries e picos de carga.

Sem uma política explícita de resiliência, qualquer falha transitória pode causar perda de consistência, duplicação de processamento ou reordenação de eventos.

## Alternativas consideradas

- **Sem retry e sem DLQ**: simples de implementar, mas causa perda de mensagens e não oferece diagnóstico para falhas persistentes.
- **Retry infinito sem controle**: aumenta a pressão sobre o sistema e pode gerar tempestade de reprocessamento.
- **Backoff fixo sem jitter**: melhora a situação em relação a retry sem controle, mas ainda sincroniza várias instâncias e tende a concentrar carga em momentos específicos.
- **Processamento global em ordem**: reduz complexidade conceitual, mas limita escala e cria gargalo para todos os clientes.
- **Ordenação por cliente com retry + DLQ** (escolhida): balanceia consistência, disponibilidade e escalabilidade.

## Decisão

1. **O produtor grava o evento no outbox na mesma transação do lançamento**.
   - isso evita o clássico problema do dual write;
   - o evento só existe se o lançamento foi persistido corretamente.

2. **O publicador usa retry controlado para tentativas de envio**.
   - retries são usados apenas para falhas transitórias;
   - falhas permanentes devem ser tratadas como erro de domínio ou de contrato.

3. **A política de retry será baseada em backoff exponencial com jitter**.
   - exemplo: 1s, 2s, 4s, 8s, 16s, com variação aleatória para evitar sincronização entre workers;
   - a lógica reduz a pressão sobre componentes dependentes e melhora a estabilidade do sistema em condições de pico.

4. **Mensagens que excederem o limite de retries serão movidas para DLQ**.
   - a DLQ guarda a mensagem original para inspeção e replay manual;
   - ela evita que uma falha persistente bloqueie toda a fila normal;
   - em ambiente financeiro, DLQ é um mecanismo de controle e auditoria, não um “lixo”.

5. **O processamento em lote será usado apenas quando a regra de negócio permitir**.
   - lotes são úteis para throughput;
   - porém o lote não pode quebrar a ordem de eventos do mesmo cliente.

6. **A ordem será preservada por chave de cliente**.
   - a chave de cliente pode ser `clientId`, `tenantId`, `usuarioId` ou outra identidade de domínio;
   - eventos do mesmo cliente devem seguir para o mesmo grupo, partição ou fila;
   - clientes diferentes podem ser processados em paralelo.

7. **O consumo por cliente substitui a ordem global como regra operacional prioritária**.
   - isso reduz risco de inversão de sequência;
   - preserva consistência do saldo e do estado de negócio do cliente;
   - aumenta a capacidade de processamento horizontal sem quebrar regras de negócio.

## Consequências

- O sistema se torna resiliente a falhas temporárias sem perder rastreabilidade.
- Retries agressivos não levam a picos de carga simultâneos, graças ao jitter.
- Mensagens problemáticas são isoladas em DLQ para análise e replay posterior.
- A ordem dos eventos é garantida para um mesmo cliente, que é o ponto crítico de negócio no cenário financeiro.
- A solução continua adequada ao PoC e mantém a mesma estratégia quando migrada para uma infraestrutura real de fila.

## Trade-offs

- A ordenação por cliente exige modelagem mais cuidadosa da chave de particionamento.
- O jitter e o backoff aumentam a latência de recuperação em caso de falha.
- A DLQ exige processo de monitoramento e triagem, mas evita perda silenciosa.
- O consumo em lotes não é sempre ideal para eventos críticos de baixa latência; por isso a estratégia deve ser revisada por carga real.

## Observação de escopo

Esta decisão é uma decisão de arquitetura de resiliência e não substitui a implementação real de broker, worker ou infraestrutura de observabilidade. O PoC apenas prova o padrão e o comportamento desejado. A implementação operacional real pode variar conforme a fila escolhida (RabbitMQ, SQS, Kafka, etc.), mas a política de resilência permanece a mesma.
