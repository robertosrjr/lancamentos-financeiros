# Controle Financeiro

## Visão geral

Este projeto é uma solução de referência para o desafio de arquitetura de solução de fluxo de caixa, com foco em dois domínios de negócio principais:

- controle de lançamentos financeiros (débito e crédito)
- consolidação diária do saldo por dia

O objetivo é demonstrar, de forma prática e documentada, como transformar requisitos de negócio em uma arquitetura coerente, com isolamento de falhas, observabilidade, segurança e documentação de decisão.

## Objetivo do negócio

Permitir que um comerciante:

- registre lançamentos financeiros com tipo, valor, data, categoria e descrição
- consulte os lançamentos cadastrados
- gere um estorno quando necessário
- leia um saldo diário consolidado sem que o serviço de lançamentos fique dependente do serviço de consolidação

## Requisitos atendidos

Este projeto foi organizado para cobrir os requisitos do documento de desafio e da metodologia aplicada:

- mapeamento de domínios funcionais e capacidades de negócio
- refinamento de requisitos funcionais e não funcionais
- desenho da solução completa em arquitetura alvo
- justificativa técnica e de decisão em ADRs
- documentação de decisões arquiteturais e padrões
- código em Java com Spring Boot
- testes automatizados
- README com instruções locais e visão de navegação do repositório
- documentação versionada no próprio repositório

Referência de requisitos: [aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md](aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md)

## Metodologia aplicada: AI-DLC

Este repositório segue a metodologia AI-DLC (AI-Driven Development Lifecycle), que coloca a IA como agente de condução do processo de análise, decomposição, design, implementação e validação, enquanto o humano valida decisões críticas e aprova os pontos de gate.

Em termos práticos, a abordagem adotada aqui foi:

- iniciar com o Intent de negócio
- decompor em user stories, NFRs e riscos
- agrupar em Units funcionais coesas
- documentar Domain Design e Logical Design
- registrar ADRs para decisões arquiteturais
- implementar o código com testes e rastreabilidade

A ideia central é manter a cadeia de rastreabilidade entre requisito, projeto, código e validação.

## Estrutura do repositório

```text
.
├── aidlc-docs/
│   ├── plans/
│   ├── requirements/
│   ├── story-artifacts/
│   ├── design-artifacts/
│   ├── risks/
│   └── prompts.md
├── application/
│   ├── src/
│   ├── pom.xml
│   └── target/
├── ARCHITECTURE/
│   ├── adr/
│   ├── arquitetura_alvo.md
│   ├── estimativa_custos.md
│   ├── observabilidade.md
│   └── seguranca_integracao.md
├── DESIGN/
│   ├── consolidado-diario/
│   └── lancamentos/
├── .claude/
├── README.md
└── ...
```

## Navegação rápida por documentação

### Requisitos e processo

- [aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md](aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md)
- [aidlc-docs/plans/level1_plan.md](aidlc-docs/plans/level1_plan.md)
- [aidlc-docs/plans/units_plan.md](aidlc-docs/plans/units_plan.md)
- [aidlc-docs/story-artifacts/user_stories_nfrs_risks.md](aidlc-docs/story-artifacts/user_stories_nfrs_risks.md)

### Design de domínio

- [DESIGN/lancamentos/domain_design.md](DESIGN/lancamentos/domain_design.md)
- [DESIGN/consolidado-diario/domain_design.md](DESIGN/consolidado-diario/domain_design.md)

### Arquitetura e decisão

- [ARCHITECTURE/arquitetura_alvo.md](ARCHITECTURE/arquitetura_alvo.md)
- [ARCHITECTURE/adr/ADR-010-arquitetura-alvo.md](ARCHITECTURE/adr/ADR-010-arquitetura-alvo.md)
- [ARCHITECTURE/adr/ADR-001-padrao-arquitetural.md](ARCHITECTURE/adr/ADR-001-padrao-arquitetural.md)
- [ARCHITECTURE/adr/ADR-007-seguranca-acesso.md](ARCHITECTURE/adr/ADR-007-seguranca-acesso.md)
- [ARCHITECTURE/adr/ADR-004-persistencia.md](ARCHITECTURE/adr/ADR-004-persistencia.md)
- [ARCHITECTURE/adr/ADR-002-mensageria.md](ARCHITECTURE/adr/ADR-002-mensageria.md)
- [ARCHITECTURE/adr/ADR-005-consistencia-outbox.md](ARCHITECTURE/adr/ADR-005-consistencia-outbox.md)
- [ARCHITECTURE/adr/ADR-006-cache-resiliencia.md](ARCHITECTURE/adr/ADR-006-cache-resiliencia.md)
- [ARCHITECTURE/adr/ADR-008-resiliencia-filas-retry.md](ARCHITECTURE/adr/ADR-008-resiliencia-filas-retry.md)
- [ARCHITECTURE/adr/ADR-003-provedor-nuvem.md](ARCHITECTURE/adr/ADR-003-provedor-nuvem.md)
- [ARCHITECTURE/adr/ADR-009-bff-edge-api.md](ARCHITECTURE/adr/ADR-009-bff-edge-api.md)
- [ARCHITECTURE/filas_retry_dlq.md](ARCHITECTURE/filas_retry_dlq.md)
- [ARCHITECTURE/observabilidade.md](ARCHITECTURE/observabilidade.md)
- [ARCHITECTURE/operacao_monitoramento.md](ARCHITECTURE/operacao_monitoramento.md)
- [ARCHITECTURE/seguranca_integracao.md](ARCHITECTURE/seguranca_integracao.md)
- [ARCHITECTURE/estimativa_custos.md](ARCHITECTURE/estimativa_custos.md)

### Ordem de prioridade dos ADRs

1. ADR-001 — Padrão arquitetural
2. ADR-007 — Segurança de acesso
3. ADR-004 — Persistência
4. ADR-002 — Mensageria
5. ADR-005 — Consistência por outbox
6. ADR-006 — Cache e resiliência
7. ADR-008 — Retry, DLQ e ordenação por cliente
8. ADR-003 — Provedor de nuvem
9. ADR-009 — Fronteira de entrada e BFF
10. ADR-010 — Arquitetura alvo e premissas

### Código da aplicação

- [application/src/main/java](application/src/main/java)
- [application/src/test/java](application/src/test/java)

## Visão arquitetural resumida

A solução foi pensada como duas capacidades principais:

1. Lançamentos
   - persistência de movimentos financeiros
   - regras de validação e estorno
   - chave de idempotência no domínio e no outbox
   - publicação de eventos para desacoplamento

2. Consolidado Diário
   - processamento de eventos
   - cálculo de saldo por dia
   - isolamento de falhas para evitar indisponibilidade do serviço de lançamentos

Os padrões e decisões de projeto foram documentados para preservar consistência, resiliência e observabilidade.

## Como executar localmente

A aplicação principal está no módulo `application` e usa Java + Spring Boot.

### Pré-requisitos

- Java 21
- Maven

### Execução

```bash
cd application
mvn spring-boot:run
```

### Testes

```bash
cd application
mvn test
```

### Banco local

A configuração atual inclui H2 em memória para simular um ambiente PostgreSQL em desenvolvimento, com console disponível em:

- http://localhost:8080/h2-console

## Decisões-chave de arquitetura

- desacoplamento entre serviços por eventos assíncronos
- uso de outbox para preservar consistência entre persistência e publicação
- tratamento de idempotência por chave derivada do payload do evento
- persistência da chave de idempotência também no próprio `Lancamento` para deduplicação do registro e reprocessamento seguro
- foco em disponibilidade do serviço de lançamentos mesmo com falhas do serviço de consolidação
- política de retry, backoff exponencial, jitter e DLQ para resiliência do consumidor
- ordenação de processamento por chave de cliente para preservar consistência da sequência de eventos
- observabilidade estruturada em pilares de logs, métricas e tracing distribuído
- documentação formal de padrões e trade-offs em ADRs

## Pilares de observabilidade

A observabilidade do sistema foi pensada em três pilares complementares:

### 1. Logs
- registros estruturados do fluxo de lançamento e do processamento de eventos
- correlação por `correlationId`, `tenantId` e `clientId`
- rastreio de falhas de autenticação, publicação e consumo de eventos

### 2. Métricas
- contadores de lançamentos, estornos e eventos processados
- gauges para fila de outbox e latência da API
- indicadores de vazão, erros e hit ratio do cache

### 3. Tracing distribuído
- correlação do ciclo completo desde a requisição até a publicação e consumo do evento
- suporte à análise de latência, gargalo e falha em cada etapa do fluxo

A visão completa está em [ARCHITECTURE/observabilidade.md](ARCHITECTURE/observabilidade.md).

## Status do projeto

O repositório está estruturado como uma base de arquitetura e implementação em evolução, com documentação, design e código alinhados ao desafio proposto. A leitura do README deve servir como ponto de entrada para navegar pelos artefatos de negócio, de arquitetura e de implementação.

## Referência adicional

Para acompanhar o processo completo de requisitos e planejamento, comece pelos documentos abaixo:

- [aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md](aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md)
- [aidlc-docs/plans/level1_plan.md](aidlc-docs/plans/level1_plan.md)
- [ARCHITECTURE/arquitetura_alvo.md](ARCHITECTURE/arquitetura_alvo.md)
- [DESIGN/lancamentos/domain_design.md](DESIGN/lancamentos/domain_design.md)
