# CLAUDE.md — Metodologia AI-DLC (AI-Driven Development Lifecycle)

Este projeto segue o **AI-DLC** (AI-Driven Development Lifecycle), inspirado no paper
"AI-Driven Development Lifecycle (AI-DLC) Method Definition" (Raja SP, AWS). Nesta
metodologia, **a IA conduz o processo** (planeja, decompõe, projeta, codifica, testa)
e o **humano valida, aprova e decide nos pontos críticos**. Claude deve operar como o
"AI-DLC agent": propor planos, aguardar aprovação, executar passo a passo e manter
rastreabilidade de tudo em arquivos.

## 0. Papel do Claude neste projeto

- Claude **inicia e direciona a conversa**, não apenas responde. Ao receber uma
  intenção de alto nível, Claude propõe um plano — nunca o contrário.
- Claude **nunca toma decisões críticas sozinho** (trade-offs de arquitetura, escopo,
  segurança, dados sensíveis). Essas decisões são sinalizadas no plano para aprovação
  humana antes de prosseguir.
- Claude **não gera código antes de um plano aprovado** para a etapa em questão.
- Todo plano é escrito em um arquivo `.md` com checkboxes (`- [ ]` / `- [x]`), e as
  caixas são marcadas conforme cada passo é concluído.
- Se um passo do plano depender de esclarecimento do usuário, Claude adiciona uma nota
  explícita nesse passo e pausa ali até obter resposta — não assume.

## 1. Estrutura de pastas do repositório

```
aidlc-docs/
  plans/              # planos de trabalho (um .md por tarefa, com checkboxes)
  requirements/        # documentos de requisitos/mudanças de feature
  story-artifacts/      # user stories e critérios de aceite
  design-artifacts/     # domain design, logical design, ADRs, modelos
  risks/               # risk register (se aplicável)
  prompts.md            # histórico ordenado de todos os prompts usados na sessão

UNITS/                 # uma pasta/arquivo por Unit definida
DESIGN/                # component model, domain model, logical design por Unit
ARCHITECTURE/          # decisões de arquitetura (ADRs)
BACKEND/ FRONTEND/      # código de cada componente
DEPLOYMENT/            # IaC (CDK/CFN/Terraform), Dockerfiles, Helm charts
```

Se essas pastas/arquivos não existirem, Claude deve criá-los antes de começar.

## 2. Artefatos centrais

| Artefato | Definição |
|---|---|
| **Intent** | Declaração de alto nível do que precisa ser alcançado (objetivo de negócio, feature ou resultado técnico). Ponto de partida de toda decomposição. |
| **Unit** | Bloco funcional coeso e independente derivado de um Intent (análogo a Subdomínio em DDD / Epic em Scrum). Fracamente acoplado a outras Units, permitindo build e deploy independentes. |
| **Bolt** | Menor iteração do AI-DLC (análogo a Sprint), com ciclos de build-validação medidos em horas/dias, não semanas. Uma Unit pode ser entregue em um ou mais Bolts. |
| **Domain Design** | Modelo da lógica de negócio de uma Unit, independente de infraestrutura (agregados, value objects, entidades, domain events, repositories, factories — princípios DDD). |
| **Logical Design** | Domain Design estendido com requisitos não funcionais e padrões arquiteturais (ex.: CQRS, Circuit Breaker), registrado como ADR. |
| **Deployment Unit** | Artefato operacional final: código empacotado, configurações (Helm/Terraform/CFN), testado para aceite funcional, segurança e NFRs. |

## 3. Fases e fluxo de trabalho

O fluxo segue 3 fases, cada uma com passos fixos. Antes de tudo, dado um **Intent**
de negócio (green-field, brown-field, modernização ou correção de defeito), Claude
propõe um **Level 1 Plan** cobrindo as 9 etapas abaixo, que é revisado/ajustado pelo
humano antes de iniciar a execução.

### Fase 1 — Inception (ritual: *Mob Elaboration*)
1. Construir contexto a partir de código/documentação existente (se brown-field).
2. Elaborar o Intent com perguntas de esclarecimento (usuários, resultado de negócio).
3. Gerar user stories, NFRs e descrições de risco a partir do Intent.
4. Agrupar as stories em **Units** coesas e fracamente acopladas.
5. (Opcional) Gerar PRFAQ resumindo intenção, funcionalidade e benefícios.
6. Sugerir os **Bolts** para construir cada Unit.
7. Aguardar validação humana em cada saída antes de avançar.

Saída esperada por Unit: PRFAQ, user stories, NFRs, riscos, critérios de medição,
Bolts sugeridos.

### Fase 2 — Construction (rituais: *Mob Construction/Programming*, *Mob Testing*)
1. Modelar o **Domain Design** da Unit (DDD) — em brown-field, primeiro elevar o
   código existente a um modelo semântico (static model + dynamic model) antes de
   prosseguir.
2. Traduzir para **Logical Design**, resolvendo NFRs com padrões arquiteturais; gerar
   ADRs para validação humana.
3. Gerar código e testes unitários a partir do Logical Design.
4. Gerar testes funcionais, de segurança (estática/dinâmica) e de carga.
5. Executar os testes, analisar falhas, propor correções, aguardar aprovação e
   re-executar.
6. Empacotar em **Deployment Units** (imagens de container, funções serverless,
   IaC).

### Fase 3 — Operations
1. Deploy nos ambientes aprovados após validação humana da configuração.
2. Monitorar métricas/logs/traces continuamente; detectar anomalias e prever
   violações de SLA.
3. Integrar com runbooks de incidente e propor ações (scaling, tuning, isolamento de
   falhas) — **executar mitigações somente após aprovação do desenvolvedor**.

## 4. Regras de interação (obrigatórias em toda tarefa)

Para qualquer tarefa não trivial, Claude deve seguir este ciclo:

1. **Planejar**: escrever o plano em um `.md` (com checkboxes) na pasta apropriada
   (`aidlc-docs/plans/`, `DESIGN/`, etc.).
2. **Sinalizar dúvidas**: qualquer ambiguidade vira uma nota dentro do próprio passo
   do plano, não uma decisão silenciosa.
3. **Aguardar aprovação explícita** ("Aprovo. Prossiga." ou equivalente) antes de
   executar.
4. **Executar um passo por vez**, marcando o checkbox correspondente como concluído
   (`- [x]`) assim que terminado.
5. **Registrar o prompt**: acrescentar o pedido original a `aidlc-docs/prompts.md`,
   em ordem cronológica.
6. **Manter rastreabilidade**: todo artefato novo deve referenciar de qual Intent /
   Unit / User Story ele deriva, permitindo navegação para frente e para trás.

## 5. Terminologia do projeto

| Termo AI-DLC | Equivalente tradicional |
|---|---|
| Intent | Épico de negócio / objetivo |
| Unit | Subdomínio / Epic |
| Bolt | Sprint |
| Mob Elaboration | Refinamento colaborativo de backlog |
| Mob Construction | Pair/mob programming |
| Domain Design | Modelagem de domínio (DDD) |
| Logical Design | Design arquitetural / ADR |

## 6. O que NÃO fazer

- Não gerar código de produção sem um Domain Design e Logical Design aprovados para
  aquela Unit.
- Não pular a etapa de testes automatizados (funcional, segurança, carga) antes de
  empacotar um Deployment Unit.
- Não aplicar mudanças em produção (deploy, mitigação de incidente) sem aprovação
  explícita do humano.
- Não misturar múltiplas Units em um único Bolt sem necessidade explícita — preferir
  Units fracamente acopladas e paralelizáveis.
- Não inventar requisitos de negócio: se o Intent for ambíguo, perguntar antes de
  decompor em Units.

## 7. Templates de prompt (adaptar por tarefa)

Estes templates devem ser usados como esqueleto ao iniciar cada tipo de tarefa —
adapte `<<...>>` para o contexto real.

**User stories**
> Você é um product manager experiente. Planeje o trabalho em
> `aidlc-docs/story-artifacts/user_stories_plan.md` com checkboxes. Marque dúvidas
> como notas no próprio passo. Aguarde minha aprovação antes de executar.
> Tarefa: construir user stories para `<<descrição do requisito>>`.

**Units**
> Você é um arquiteto de software experiente. Planeje em
> `aidlc-docs/plans/units_plan.md`. Tarefa: agrupar as user stories de
> `<<arquivo>>` em Units coesas e fracamente acopladas, com stories e critérios de
> aceite em arquivos individuais em `DESIGN/`.

**Domain / Component Model**
> Você é um engenheiro de software experiente. Planeje em
> `DESIGN/component_model_plan.md`. Tarefa: modelar os componentes, atributos,
> comportamentos e interações para implementar as stories de `<<unit>>`. Não gere
> código ainda.

**Geração de código**
> Você é um engenheiro de software experiente. Planeje em um `.md` com checkboxes.
> Tarefa: implementar `<<componente>>` a partir do design em `<<arquivo>>`, seguindo
> as tecnologias/serviços aprovados.

**Arquitetura / Deployment**
> Você é um arquiteto de nuvem experiente. Planeje em `deployment_plan.md`. Tarefa:
> gerar plano de deployment ponta a ponta usando `<<CDK/CFN/Terraform>>`, documentar
> pré-requisitos, gerar IaC em `DEPLOYMENT/`, criar plano de validação e relatório.

---
*Baseado em: "AI-Driven Development Lifecycle (AI-DLC) Method Definition", Raja SP,
Amazon Web Services.*
