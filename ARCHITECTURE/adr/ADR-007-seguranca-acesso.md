# ADR-007 — Segurança de Acesso: OAuth2/JWT com Authorization Server

**Status:** Aceito

## Contexto

O documento pede proteção contra acesso não autorizado (autenticação,
autorização, criptografia), e a solução precisa permanecer compatível com
crescimento futuro sem reescrever o modelo de segurança. Mesmo no escopo
**single-tenant**, o padrão de acesso por JWT emitido por Authorization Server
oferece melhor evolução para múltiplos usuários, papéis e, eventualmente,
multi-tenant, sem bloquear a entrega do MVP.

Importante: ainda que o MVP tenha apenas um comerciante/cliente principal, a
arquitetura passa a identificar o contexto do cliente por um `client_id` (ou
`tenantId` lógico) no token e em dados de observabilidade. Esse valor não é
apenas um identificador de sessão: ele também fundamenta controle de tráfego,
segmentação lógica de dados, rate limiting por cliente e futura expansão para
multi-tenant sem reescrever o contrato de segurança.

## Alternativas consideradas

- **Sem autenticação**: rejeitada — os dados financeiros nunca devem ficar
  expostos em endpoint aberto.
- **API Key estática por cliente**: simples, mas insuficiente para um modelo de
  acesso mais robusto, sem suporte nativo a scopes, rotação de credenciais,
  revogação e diferenciação de usuários/papéis.
- **OAuth2 + OpenID Connect com Authorization Server** (ex.: Keycloak, Azure AD,
  Auth0) + JWTs como access tokens (escolhida): oferece autenticação do
  comerciante/cliente, escopos, expiração curta e possibilidade futura de
  multi-tenant, múltiplos papéis e auditoria melhor.

## Decisão

- Todos os endpoints públicos do sistema exigem **Bearer JWT** emitido por um
  Authorization Server autorizado no contexto do projeto.
- O Authorization Server é responsável por autenticar o cliente e emitir
  tokens com claims mínimos, como `sub`, `iss`, `aud`, `exp`, `scope`,
  `client_id` e `tenantId` (ou equivalente lógico), mesmo no MVP single-tenant.
- O `client_id`/`tenantId` é usado como critério de segmentação lógica para:
  controlar tráfego por cliente, aplicar rate limiting por parceiro, isolar
  dados do comerciante em consultas e permitir extensão futura para
  multi-tenant sem alterar o formato do token.
- O sistema não exige uma Unit separada de Identidade para isso: a Identidade
  continua sendo um serviço de borda/externo, enquanto o controle de escopo e
  isolamento do dado fica na camada de aplicação e infraestrutura.
- Cada serviço (Lançamentos e Consolidado Diário) valida a assinatura e os
  claims do token localmente, sem depender de chamada síncrona para um serviço
  de identidade em cada requisição.
- Os serviços adotam middleware/filtro compartilhado para validação do token
  (`issuer`, `audience`, `exp`, `algoritmo`, `scope`, `tenantId`),
  centralizando a política de segurança sem transformar a autenticação em uma
  Unit de domínio.
- **HTTPS/TLS obrigatório** em qualquer ambiente que não seja localhost puro.
- Comunicação serviço-a-serviço (ex.: broker RabbitMQ) continua protegida por
  credenciais de serviço específicas, sem compartilhamento de credenciais entre
  consumidores/produzidores; quando houver necessidade de autenticação entre
  serviços, essa comunicação pode evoluir para client credentials ou mTLS.

## Consequências

- O sistema passa a ter um modelo de autenticação padronizado, facilmente
  extensível para papéis e múltiplos comerciantes.
- O custo inicial de setup é maior que API Key, mas o prêmio é maior
  aderência a requisitos de segurança e menor retrabalho de evolução futura.
- A autenticação deixa de ser um detalhe operacional e passa a ser um
  componente transversal bem definido no design da arquitetura.
- Débito técnico explícito permanece baixo, porque a decisão foi tomada de
  forma intencional e com visão de evolução, não como workaround temporário.
