# ADR-010 — Arquitetura Alvo: Premissas e Considerações Adotadas

**Status:** Aceito
**Deriva de:** Documento de requisitos, NFRs, `ARCHITECTURE/arquitetura_alvo.md`, ADR-001 a ADR-009

## Contexto

A solução proposta foi concebida a partir de um conjunto de premissas de negócio
 e de engenharia que orientaram a definição da arquitetura e dos padrões de
integração adotados. Essas premissas têm como objetivo preservar a coerência entre
requisitos funcionais, requisitos não funcionais, resiliência operacional e
simplicidade de implementação, sem introduzir complexidade desnecessária no ciclo
inicial do projeto.

## Premissas de negócio

1. O domínio principal do problema é o controle de fluxo de caixa, com foco em
   lançamentos financeiros e consolidação do saldo diário.
2. O serviço de lançamentos é a fonte de verdade para a escrita dos eventos
   financeiros, enquanto o serviço de consolidado diário atua como projeção
   derivada do estado financeiro, calculado a partir dos eventos publicados.
3. O sistema deve continuar atendendo ao cliente mesmo quando o serviço de
   consolidação estiver indisponível, preservando a continuidade do fluxo de
   lançamentos.
4. O saldo diário é crítico para a decisão de negócio, mas pode aceitar
   consistência eventual, desde que a correção e a auditoria do processo sejam
   controladas e rastreáveis.
5. A solução deve priorizar segurança, rastreabilidade, idempotência e
   resiliência sobre complexidade inicial.

## Premissas técnicas

1. Os serviços devem ser desacoplados para que falhas em um módulo não derrubem
   o outro.
2. A comunicação entre serviços deve ser assíncrona, evitando acoplamento
   síncrono crítico e reduzindo a dependência de disponibilidade cruzada.
3. O sistema deve operar com autenticação centralizada, validando identidade e
   tenant a partir do contexto autenticado e não a partir de dados fornecidos
   pelo cliente.
4. O banco de dados deve ser isolado por serviço, evitando dependência
   compartilhada entre módulos de negócio.
5. O modelo de evento deve garantir que o processamento não gere efeitos
   duplicados.
6. A operação de estorno deve seguir regra contábil consistente, sem usar valor
   negativo como representação de reversão.

## Considerações arquiteturais

1. A borda do sistema deve ser protegida por gateway, WAF e autenticação
   externa, com validação do token antes do acesso aos serviços.
2. O cliente não deve fornecer identificadores de usuário ou tenant como fonte
   de verdade; isso deve vir do contexto autenticado.
3. A arquitetura deve seguir o princípio de “escrever primeiro, publicar depois”
   por meio de outbox, para evitar inconsistência entre persistência e evento.
4. O consumidor do evento deve ser idempotente e protegido contra reprocessamento
   duplicado.
5. O BFF não é obrigatório para o cenário atual. A solução base passa pelo
   gateway e utiliza serviços de negócio diretamente com contrato padronizado.
6. O BFF deve ser introduzido apenas quando houver múltiplos clientes com
   necessidades distintas de contrato, composição, privacidade ou experiência de
   usuário.
7. A arquitetura deve priorizar simplicidade no MVP e preservar a possibilidade
   de evolução sem reescrever a base conceitual do sistema.

## Considerações de operação e resiliência

1. O sistema deve tolerar falhas transitórias de mensageria e indisponibilidade
   momentânea de consumidores.
2. A arquitetura adopta retry com backoff exponencial, jitter e fila de falhas
   persistentes para evitar comportamento explosivo sob picos de demanda.
3. A observabilidade é tratada como parte da arquitetura e não como anexação
   opcional; logs, métricas e tracing distribuído são requisitos de operação.
4. A solução deve ser dimensionada para crescimento incremental, sem comprometer
   a clareza do desenho atual.

## Decisão

Adotar uma arquitetura alvo baseada em:

- API Gateway ou Load Balancer na borda;
- autenticação atrelada ao provedor de identidade e ao JWT;
- serviços de negócio desacoplados por responsabilidade;
- comunicação assíncrona orientada a eventos;
- outbox e idempotência como mecanismos de consistência;
- observabilidade e resiliência como requisitos de projeto;
- BFF como evolução opcional, e não como camada obrigatória do MVP.

## Consequências

- O desenho mantém a solução mais simples, econômica e operável no estágio inicial.
- A arquitetura preserva resiliência e modularidade sem introduzir acoplamento
  prematuro entre frontends e serviços de negócio.
- Os serviços continuam com autonomia operacional e escalabilidade independente.
- A solução permite evolução futura para múltiplos canais, integrações e
  contratos diferenciados sem reestruturação profunda da base arquitetural.

## Conclusão

A arquitetura proposta parte do princípio de que segurança, desacoplamento,
consistência e observabilidade são critérios mais importantes do que a adoção
prematura de camadas adicionais. A priorização de um desenho enxuto, porém
resiliente, representa uma decisão racional e alinhada ao objetivo de demonstrar
capacidade de análise arquitetural e de tomada de decisão em um cenário realista
 de solução de software.
