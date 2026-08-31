# Critérios de Segurança para Consumo/Integração de Serviços

**Diferencial priorizado.** Complementa ADR-007 (foco em acesso do cliente
externo); este documento foca na integração **entre** os dois serviços e com a
infraestrutura.

## Edge, rede e isolamento
- O cliente se autentica em um **Provedor de Identidade (IdP) externo**, que
  emite um token JWT. A validação do access token é feita pelos serviços após
  o tráfego chegar ao Load Balancer / API Gateway na borda.
- O tráfego de entrada passa por **firewall/WAF** antes de chegar aos serviços,
  garantindo controle de origem e filtragem de requisições maliciosas.
- Lançamentos, Consolidado Diário, broker e bancos ficam em **sub-redes
  privadas** dentro de uma **VPC**, com isolamento por zona de disponibilidade
  (AZ-A e AZ-B). Apenas os endpoints expostos ao cliente passam pelo ingress
  controlado.
- Security Groups com regra de **menor privilégio**: cada serviço só acessa a
  porta do seu próprio banco e do broker — nenhuma comunicação direta
  Lançamentos ↔ Consolidado Diário é permitida na rede (reforça, na camada de
  infraestrutura, o desacoplamento decidido no ADR-001).
- A topologia de AZs é aplicada por serviço. Ou seja, cada serviço possui sua
  própria primary/secondary, na mesma lógica de failover e leitura em réplica,
  e não um único banco compartilhado do ecossistema.

## Identidade do cliente e isolamento lógico
- Mesmo no MVP single-tenant, o cliente possui um `client_id`/`tenantId` lógico
  associado ao token JWT; esse identificador é usado para controle de tráfego,
  segmentação por cliente e isolamento lógico dos dados na camada de aplicação.
- Todas as consultas e gravações devem validar o `tenantId` do contexto do
  usuário contra o escopo do payload autenticado, evitando vazamento de dados e
  permitindo futura expansão para multi-tenant sem mudança do contrato do token.
- O `tenantId` deve ser persistido junto aos dados relevantes e validado em cada
  leitura/escrita, funcionando como uma chave de segregação lógica do cliente.
- Em cenários de volume, essa chave também pode ser usada para rate limiting,
  métricas por cliente e distinção clara entre cargas de diferentes parceiros.
- Em um desenho mais estrito, o banco pode usar campo `tenant_id` em cada tabela
  e/ou particionamento por tenant, mesmo quando o ambiente atual é single-tenant.

## Autenticação entre serviço e broker
- Cada serviço (produtor e consumidor) usa **credenciais próprias** no
  RabbitMQ/SQS — nunca uma credencial compartilhada — permitindo revogação
  independente e auditoria de qual serviço publicou/consumiu.
- Em produção AWS: acesso ao SNS/SQS via **IAM Role por serviço** (task role do
  ECS), sem chaves de acesso estáticas.

## Integridade e validação do contrato de evento
- Eventos de domínio (`LancamentoRegistrado`, `LancamentoEstornado`) seguem um
  **schema versionado** (ex.: JSON Schema ou Avro); o consumidor rejeita e
  envia para dead-letter queue qualquer mensagem que não valide contra o
  schema esperado — evita que uma mudança incompatível em Lançamentos quebre o
  Consolidado Diário silenciosamente.
- Cada evento carrega um `eventId` (UUID) — base da idempotência (ADR-005) e
  também rastreabilidade de auditoria (quem publicou o quê, quando).

## Segredos e credenciais
- Nenhuma credencial (banco, broker, API Key) em código ou arquivo versionado;
  em produção via AWS Secrets Manager (ADR-003), localmente via variáveis de
  ambiente carregadas de um `.env` **não versionado** (`.gitignore`).
- Rotação de credenciais do broker/banco tratada como responsabilidade
  operacional documentada (fora do escopo de implementação do desafio).

## Criptografia
- TLS em trânsito: entre cliente e serviços (HTTPS), entre serviços e banco
  (`sslmode=require`), entre serviços e broker (AMQPS/TLS em produção).
- Dados em repouso: criptografia nativa do RDS/EBS habilitada (padrão AWS).

## Fora do escopo do MVP (evolução futura)
- mTLS entre os serviços (justificável se a rede deixar de ser totalmente
  privada/confiável, ex. multi-cloud ou multi-conta).
- Assinatura criptográfica dos eventos (além da validação de schema), para
  cenários com múltiplos produtores não totalmente confiáveis.
