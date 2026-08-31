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
- [ARCHITECTURE/adr/ADR-001-padrao-arquitetural.md](ARCHITECTURE/adr/ADR-001-padrao-arquitetural.md)
- [ARCHITECTURE/adr/ADR-002-mensageria.md](ARCHITECTURE/adr/ADR-002-mensageria.md)
- [ARCHITECTURE/adr/ADR-004-persistencia.md](ARCHITECTURE/adr/ADR-004-persistencia.md)
- [ARCHITECTURE/adr/ADR-005-consistencia-outbox.md](ARCHITECTURE/adr/ADR-005-consistencia-outbox.md)
- [ARCHITECTURE/observabilidade.md](ARCHITECTURE/observabilidade.md)
- [ARCHITECTURE/seguranca_integracao.md](ARCHITECTURE/seguranca_integracao.md)
- [ARCHITECTURE/estimativa_custos.md](ARCHITECTURE/estimativa_custos.md)

### Código da aplicação

- [application/src/main/java](application/src/main/java)
- [application/src/test/java](application/src/test/java)

## Visão arquitetural resumida

A solução foi pensada como duas capacidades principais:

1. Lançamentos
   - persistência de movimentos financeiros
   - regras de validação e estorno
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
- foco em disponibilidade do serviço de lançamentos mesmo com falhas do serviço de consolidação
- documentação formal de padrões e trade-offs em ADRs

## Status do projeto

O repositório está estruturado como uma base de arquitetura e implementação em evolução, com documentação, design e código alinhados ao desafio proposto. A leitura do README deve servir como ponto de entrada para navegar pelos artefatos de negócio, de arquitetura e de implementação.

## Referência adicional

Para acompanhar o processo completo de requisitos e planejamento, comece pelos documentos abaixo:

- [aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md](aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md)
- [aidlc-docs/plans/level1_plan.md](aidlc-docs/plans/level1_plan.md)
- [ARCHITECTURE/arquitetura_alvo.md](ARCHITECTURE/arquitetura_alvo.md)
- [DESIGN/lancamentos/domain_design.md](DESIGN/lancamentos/domain_design.md)
