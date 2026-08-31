# AI-DLC — Plano Detalhado de Construção por Bolt

**Objetivo:** transformar a arquitetura e o design aprovados em execução incremental, por unidade funcional e com ciclos curtos de validação.

**Origem:** `aidlc-docs/plans/fase1_inception.md`  
**Unidades cobertas:** Lançamentos, Consolidado Diário, Cross-cutting / Integração

## Bolt 1 — Lançamentos

### Objetivo
Implementar a atividade principal de negócio da Unit de Lançamentos: registrar,
consultar e estornar lançamentos financeiros com consistência local e publicação
assíncrona de eventos.

### Escopo
- API REST para criação, consulta e estorno de lançamentos.
- Entidade de domínio `Lancamento` com invariantes e value objects.
- Persistência em PostgreSQL.
- Publicação de eventos por Transactional Outbox.
- Validação de autenticação por JWT na API.
- Testes unitários e de integração dos casos principais.

### Entregáveis
- Endpoints: create/list/lookup/estorno.
- Repositório e tabelas de persistência.
- Outbox e poller de publicação.
- Testes de domínio e endpoint.

### Tarefas
- [ ] Modelar persistência do agregado `Lancamento`.
- [ ] Implementar regras de negócio: valor positivo, data não futura, estorno válido.
- [ ] Implementar endpoints REST.
- [ ] Implementar repositório de acesso a dados.
- [ ] Implementar Transactional Outbox.
- [ ] Publicar eventos de domínio para o broker.
- [ ] Validar autenticação via JWT no serviço.
- [ ] Executar testes unitários e de integração.

### Critérios de aceitação
- Lançamentos são persistidos com integridade financeira.
- Estorno cria nova transação e mantém rastreabilidade.
- Eventos são publicados sem acoplamento síncrono ao Consolidado.
- API responde corretamente em cenários felizes e de erro.

---

## Bolt 2 — Consolidado Diário

### Objetivo
Implementar a projeção e consulta de saldo consolidado diário, mantendo
idempotência, performance e consistência eventual com os eventos da Unit de
Lançamentos.

### Escopo
- Processador de eventos de domínio do broker.
- Cálculo e atualização do saldo diário.
- API de consulta do saldo por dia e intervalo.
- Cache para dias fechados / saldo já calculado.
- Validação de tenantId e idempotência por evento.

### Entregáveis
- Consumidor idempotente do broker.
- Projeção de saldo diário em banco próprio.
- API de consulta com performance adequada.
- Cache e estratégia de leitura intensiva.

### Tarefas
- [ ] Definir o modelo de persistência do saldo diário.
- [ ] Implementar consumidor do broker com idempotência.
- [ ] Reprocessar e recalcular saldo em caso de inconsistência.
- [ ] Implementar endpoints de consulta de saldo e histórico.
- [ ] Aplicar cache otimizado para dias fechados.
- [ ] Validar tenantId em todos os fluxos de leitura/escrita.
- [ ] Executar testes de integração e carga leve.

### Critérios de aceitação
- O consolidado reflete os eventos de forma consistente.
- Reprocessamento de eventos não duplica dados.
- Consultas de saldo atendem ao NFR02 em cenário de pico.
- Falha no serviço de Lançamentos não bloqueia o serviço de Consolidado.

---

## Bolt 3 — Integração, Cross-cutting e Infraestrutura

### Objetivo
Entregar a estrutura transversal necessária para operação segura e observável do
sistema: autenticação, mensageria, observabilidade, rede e execução local.

### Escopo
- Configuração de JWT / Authorization Server e validação por middleware.
- Segurança da borda e políticas de rede / VPC.
- Broker e configuração de fila/dead-letter.
- Logs, métricas, health checks e alertas.
- Containerização e ambiente local para execução do sistema.

### Entregáveis
- Middleware de autenticação JWT compartilhado.
- Pipeline de observabilidade operável.
- Configuração de broker com segurança e rastreabilidade.
- Docker Compose / ambiente local mínimo.

### Tarefas
- [ ] Definir o padrão de JWT e claims (`tenantId`, `client_id`, `aud`, `iss`, `exp`).
- [ ] Implementar filtro/interceptor de validação de token nos serviços.
- [ ] Configurar broker, filas e DLQ.
- [ ] Implementar logs estruturados com correlationId e tenantId.
- [ ] Expor métricas do Actuator/Micrometer.
- [ ] Configurar health checks por serviço.
- [ ] Documentar alarmes e erros críticos.
- [ ] Preparar ambiente local com containers e configurações mínimas.

### Critérios de aceitação
- Todas as APIs exigem autenticação válida.
- Logs e métricas permitem rastrear a jornada completa do cliente.
- O sistema é executável localmente em ambiente controlado.
- A infraestrutura de apoio está documentada para operação e evolução.

---

## Sequência de execução recomendada

1. Bolt 1 — Lançamentos
2. Bolt 2 — Consolidado Diário
3. Bolt 3 — Integração & Cross-cutting

Essa ordem reduz risco porque a Unit de Lançamentos é a fonte de verdade e o
ponto de entrada do fluxo principal. O Consolidado depende do evento gerado pela
Unit de Lançamentos, e a infraestrutura transversal deve ser preparada para
apoiar ambos.

## Critério de parada por Bolt

Cada Bolt só é considerado concluído se:
- o código estiver funcionando nos testes relevantes;
- os artefatos de domínio/arquitetura permanecerem coerentes com a implementação;
- houver rastreabilidade entre funcionalidade, regras e documentação;
- a validação humana do passo seja concluída antes do próximo Bolt.

## Estado atual

- [x] Fase 1 concluída
- [x] Deploy da arquitetura e design em artefatos
- [x] Definição de Bolts
- [ ] Início da implementação do Bolt 1
