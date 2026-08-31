# ADR-009 — Fronteira de Entrada: API Gateway sem BFF obrigatório

**Status:** Aceito
**Deriva de:** NFR04 (segurança), arquitetura alvo do desafio, `ARCHITECTURE/arquitetura_alvo.md`

## Contexto

O sistema expõe APIs de negócio e precisa atender requisitos de segurança,
isolamento lógico, proteção na borda e evolução futura com baixo acoplamento.
Neste cenário, o ponto de entrada deve ser definido com clareza:

- garantir autenticação e autorização consistentes;
- proteger os serviços contra abuso e saturação;
- manter a arquitetura simples no MVP;
- permitir evolução sem introduzir camada desnecessária cedo demais.

O problema central é decidir se a arquitetura base deve incluir um BFF (Backend
for Frontend) como camada obrigatória ou se a solução adequada é o uso de um
API Gateway / Load Balancer + serviços de negócio diretamente.

## Alternativas consideradas

### A) API Gateway + serviços de negócio diretamente (escolhida)

- Prós:
  - menor complexidade arquitetural;
  - menos componentes para operar e monitorar;
  - contratos de API mais simples e padronizados;
  - mantém a autonomia dos serviços e a clareza da separação de responsabilidades;
  - atende ao MVP sem introduzir abstração desnecessária.
- Contras:
  - um frontend mais complexo pode precisar reforçar lógica de composição;
  - clientes muito diferentes podem exigir adaptação manual de payloads.

### B) BFF como camada obrigatória na frente dos serviços

- Prós:
  - otimiza a experiência para cada cliente;
  - permite adaptar payloads, perfis e regras por tipo de frontend;
  - ajuda quando existem web, mobile e admin com contratos muito diferentes.
- Contras:
  - aumenta complexidade operacional;
  - introduz mais código, mais manutenção e mais pontos de falha;
  - torna a arquitetura mais pesada no início;
  - não traz retorno proporcional no cenário de um cliente ou de contratos banais.

### C) Híbrido: gateway na borda + BFF apenas por necessidade futura

- Prós:
  - preserva a arquitetura base enxuta;
  - mantém espaço para evolução sem acoplamento prematuro;
  - reduz custo de entrega e complexidade inicial.
- Contras:
  - exige disciplina para não criar BFF sem necessidade real;
  - exige clareza no desenho para evitar duplicação de lógica.

## Decisão

Adotar a solução **A** como padrão da arquitetura alvo:

- API Gateway / Load Balancer na borda;
- autenticação centralizada via IdP / OAuth2 / JWT;
- serviços de negócio com contratos padronizados;
- BFF apenas como **evolução opcional** quando houver necessidade concreta de
  diferenciação por cliente.

Em outras palavras, o BFF não é obrigatório. Ele será introduzido somente quando
houver múltiplos frontends com necessidades diferentes de composição,
privacidade, UX ou autorização contextual.

## Trade-offs aceitos

- **Simplicidade inicial acima de prematuridade**: não introduzimos BFF sem
  evidência de necessidade.
- **Compatibilidade com crescimento futuro**: a arquitetura permite evoluir para
  BFF sem reescrever o modelo de negócios.
- **Menor esforço de operação**: menos camadas intermediárias significa menos
  manutenção e menos risco de falhas de integração.

## Consequências

- O gateway e o IdP ficam como pontos de entrada e controle da segurança do
  sistema.
- Os serviços continuam com interfaces bem definidas e facilmente testáveis.
- A arquitetura permanece adequada a um PoC e a uma solução de produção de
  porte mediano.
- Se o produto evoluir para múltiplos clientes com necessidades de UX ou
  contrato diferentes, a introdução do BFF será justificada e não será vista
  como uma correção de uma arquitetura inadequada, mas como uma extensão
  consciente.

## Critério de decisão prática

O BFF deve entrar somente quando a regra abaixo for verdade:

- existem 2 ou mais tipos de cliente com contratos de API distintos ou com
  necessidade de composição específica; e/ou
- há requisitos de experiência de usuário que exigem adaptação de dados na
  camada de entrada; e/ou
- a segurança ou privacidade do cliente exigem filtro e agregação antes da
  resposta entregue ao frontend.

Se isso não ocorrer, a API Gateway + serviços de negócio é a solução mais
robusta e mais econômica.
