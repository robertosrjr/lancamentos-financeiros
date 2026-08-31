# Serviços de Negócio e Oferta de Serviços no TOGAF

**Contexto do projeto:** Controle de Fluxo de Caixa

## 1. Objetivo

Este documento formaliza, no contexto de arquitetura de negócio do TOGAF, a
aplicação dos conceitos de:

- **Business Service (Serviço de Negócio)**: o que a organização faz para entregar
  valor em nível funcional e operacional;
- **Service Offer (Oferta de Serviços)**: a proposta de valor que a organização
  oferece ao cliente, agrupando um conjunto de serviços de negócio em uma
  experiência de entrega.

A intenção é demonstrar como o sistema de controle de fluxo de caixa transforma
capacidade operacional em valor percebido pelo comerciante.

## 2. Conceitos no TOGAF

### 2.1 Business Service (Serviço de Negócio)

Um **Business Service** representa uma capacidade de negócio estável, executada
pela organização para atender objetivos de negócio. Ele está focado no "como a
empresa entrega valor" em termos de competência operacional.

Características principais:

- é orientado para a capacidade de negócio;
- é consumido por partes interessadas internas ou externas;
- é funcionalmente relevante para o processo de negócio;
- define a entrega de valor de forma consistente e reutilizável;
- normalmente é apoiado por processos, pessoas, dados e capacidades de TI.

### 2.2 Service Offer (Oferta de Serviços)

Uma **Service Offer** representa a expressão comercial e de experiência do valor
oferecido ao cliente. Ela organiza um conjunto de serviços de negócio em uma
proposta de entrega, geralmente com foco em benefício, experiência de uso,
acesso e conveniência.

Características principais:

- é orientada ao cliente e ao valor percebido;
- encapsula e comunica uma combinação de serviços de negócio;
- define a forma como o consumidor adquire e usa a solução;
- pode incluir SLAs, acesso, experiência, pacote funcional e diferenciação.

## 3. Relação entre os dois conceitos

A diferença essencial é de abstração:

- o **Business Service** responde ao que a organização faz;
- a **Service Offer** responde ao que a organização vende/entrega ao cliente em
  formato de proposta de valor.

Em outras palavras:

- **Business Service** = capacidade operacional / funcional;
- **Service Offer** = pacote de valor percebido / proposta de negócio.

### Exemplo de relacionamento

Um banco pode ter o Business Service "Processamento de Pagamentos". Mas a sua
Service Offer para um cliente pode ser "Conta Digital com Pagamentos e
Concilição Automática". A oferta combina vários serviços de negócio em uma
experiência final.

## 4. Aplicação ao domínio do projeto

No domínio de **Controle de Fluxo de Caixa**, o sistema é a materialização de
várias capacidades de negócio. Os serviços de negócio mapeados no contexto do
projeto são os seguintes.

### 4.1 Serviços de Negócio (Business Services)

| Serviço de Negócio | Descrição | Suporte no modelo atual |
|---|---|---|
| Gestão de Lançamentos | Registrar débitos e créditos de forma controlada e rastreável | `DESIGN/lancamentos/domain_design.md` |
| Estorno de Lançamento | Correção contábil de um lançamento incorreto sem perder o histórico | `DESIGN/lancamentos/domain_design.md` |
| Consulta de Histórico | Consultar lançamentos por período, tipo e referência | `DESIGN/lancamentos/domain_design.md` |
| Consolidação Diária | Recalcular e manter o saldo diário consolidado | `DESIGN/consolidado-diario/domain_design.md` |
| Visualização de Saldo | Exibir saldo atual e evolução do fluxo de caixa | `ARCHITECTURE/arquitetura_alvo.md` |
| Auditoria e Integridade | Garantir rastreabilidade, idempotência e consistência entre eventos | `ARCHITECTURE/arquitetura_alvo.md` |

Esses serviços representam capacidades práticas de negócio, independentemente da
forma tecnológica de implementação.

### 4.2 Ofertas de Serviços (Service Offers)

As offers abaixo representam como essas capacidades são propostas ao comerciante
como valor de negócio.

| Oferta de Serviço | Descrição | Serviços de Negócio envolvidos |
|---|---|---|
| Gestão de Fluxo de Caixa Digital | Solução para registrar e acompanhar entradas e saídas financeiras | Gestão de Lançamentos, Consulta de Histórico |
| Correção Financeira com Estorno | Permite corrigir erros sem destruir o histórico original | Estorno de Lançamento, Auditoria e Integridade |
| Visão Diária do Saldo | Exibe o saldo consolidado em tempo real ou próximo do real | Consolidação Diária, Visualização de Saldo |
| Painel de Controle Financeiro | Permite acompanhar movimento, saldo e situação financeira operacional | Gestão de Lançamentos, Consolidação Diária, Visualização de Saldo |
| Compliance e Rastreabilidade | Entrega evidência para controle interno e auditoria do histórico financeiro | Auditoria e Integridade |

## 5. Mapeamento do modelo para o projeto

A arquitetura atual do sistema já reflete esse nível de distinção:

- O **Serviço de Lançamentos** implementa o conjunto de capacidades de negócio
  relacionadas à criação, consulta e correção de lançamentos.
- O **Serviço de Consolidado Diário** implementa a capacidade de negocio de
  projeção do saldo e visibilidade diária.
- A **Oferta de Serviços** pode ser entendida como o conjunto de experiências
  entregues ao comerciante no portal ou no aplicativo: controle financeiro,
  consulta de saldo e correções de lançamentos.

### Estrutura conceitual

```text
Oferta de Serviços: "Gestão de Fluxo de Caixa"
    ├─ Business Service: Gestão de Lançamentos
    │   ├─ Registrar débito/crédito
    │   ├─ Consultar histórico
    │   └─ Estornar lançamento
    ├─ Business Service: Consolidação Diária
    │   ├─ Recalcular saldo
    │   └─ Exibir saldo consolidado
    └─ Business Service: Auditoria e Integridade
        ├─ Rastreabilidade
        ├─ Idempotência
        └─ Consistência entre serviços
```

## 6. Por que isso importa em TOGAF

No TOGAF, esse tipo de modelagem ajuda a separar:

- a estrutura de valor da organização;
- as capacidades que a organização precisa sustentar;
- a proposta que ela entrega ao cliente.

Isso permite que a arquitetura de negócio seja desenhada com clareza:

- primeiro, definem-se as capacidades de negócio;
- depois, organizam-se as ofertas de serviço;
- em seguida, mapeiam-se para processos, aplicações e tecnologias.

## 7. Conclusão

No contexto do projeto, os conceitos de **Business Service** e **Service Offer**
permitem distinguir duas camadas essenciais:

1. A camada operacional, composta pelos serviços de negócio que sustentam a
   operação financeira;
2. A camada de valor percebido, composta pelas ofertas que entregam essa
   capacidade ao comerciante.

A organização não vende apenas um sistema; ela vende a capacidade de controlar o
fluxo de caixa com segurança, rastreabilidade e visibilidade diária. Essa é a
proposta de valor representada pela **Service Offer**. Já os **Business Services**
são as capacidades internas que tornam essa proposta possível.

## 8. Rastreabilidade para o projeto

- `aidlc-docs/story-artifacts/user_stories_nfrs_risks.md`
- `DESIGN/lancamentos/domain_design.md`
- `DESIGN/consolidado-diario/domain_design.md`
- `ARCHITECTURE/arquitetura_alvo.md`

Esse documento serve como base para a arquitetura de negócio e para a tradução
posterior para processos, aplicações e serviços tecnológicos.
