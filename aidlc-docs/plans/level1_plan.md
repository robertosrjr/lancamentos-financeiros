# Level 1 Plan — Desafio Arquiteto de Solução (Controle de Fluxo de Caixa)

**Origem (Intent):** `aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md`

## Intent (declaração de alto nível)

> Prover a um comerciante o controle do seu fluxo de caixa diário (lançamentos de
> débito/crédito) e um relatório de saldo diário consolidado, através de dois
> serviços fracamente acoplados, com o serviço de lançamentos permanecendo
> disponível mesmo se o serviço de consolidado diário cair, e o consolidado diário
> suportando 50 req/s de pico com até 5% de perda.

Este Intent cobre as duas capacidades de negócio explícitas no descritivo:
1. Controle de Lançamentos (débitos/créditos).
2. Consolidado Diário (relatório de saldo consolidado por dia).

---

## ✅ Decisões validadas pelo usuário (2026-08-29)

| # | Decisão | Resposta |
|---|---|---|
| 1 | Linguagem/stack | **Java / Spring Boot** |
| 2 | Padrão arquitetural | **Avaliar alternativas no Logical Design** antes de fechar (não travar em microsserviços+mensageria de cara — comparar com monolito modular e justificar a escolha final) |
| 3 | Provedor de nuvem | **Delegado ao Claude** — proposta com justificativa no Logical Design |
| 4 | Auth/escopo | **MVP single-tenant com autenticação simples** (API Key ou JWT); multi-tenant/OAuth como evolução futura documentada |
| 5 | Repositório GitHub | **Usuário já possui um repositório** — URL a ser fornecida na etapa de publicação do código |
| 6 | Requisitos diferenciais a priorizar | **Não respondido explicitamente.** Assumindo por padrão: priorizar **Estimativa de custos**, **Monitoramento/Observabilidade** e **Critérios de segurança de integração** (nível de documentação/design, proporcional ao tempo); **Arquitetura de Transição fica fora** por o cenário ser green-field. Revisitável a qualquer momento. |

> Decisão 6 foi assumida como default documentado (conforme previsto na seção
> "Regras de interação" do CLAUDE.md — ambiguidade sinalizada, mas não bloqueante).
> Se quiser mudar a priorização dos diferenciais, é só avisar.

---

## Fase 1 — Inception (Mob Elaboration)

- [x] 1. Construir contexto a partir do documento de requisitos (green-field —
      sem código/sistema legado existente). Artefato:
      `aidlc-docs/requirements/desafio-arquiteto-solucao-jun25.md`.
- [x] 2. Elaborar o Intent com perguntas de esclarecimento (ver seção acima).
- [x] 3. Gerar user stories, NFRs e riscos a partir do Intent →
      `aidlc-docs/story-artifacts/user_stories_nfrs_risks.md`.
- [x] 4. Agrupar as stories em Units coesas e fracamente acopladas →
      `aidlc-docs/plans/units_plan.md`. Decisão: **Unit "Lançamentos"** e
      **Unit "Consolidado Diário"**; autenticação tratada como cross-cutting
      concern, sem Unit de Identidade dedicada (justificativa no documento).
- [x] 5. PRFAQ — **pulado deliberadamente**: escopo pequeno e já bem definido
      pelo próprio descritivo do desafio; não agrega valor aqui.
- [x] 6. Bolts sugeridos → `aidlc-docs/plans/units_plan.md` (Bolt 1
      Lançamentos, Bolt 2 Consolidado Diário, Bolt 3 Integração &
      Cross-cutting).
- [x] 7. Saídas da Fase 1 documentadas; usuário orientou priorizar decisão e
      justificativa em vez de novas rodadas de perguntas (ver nota abaixo) —
      avançando para a Fase 2.

> **Nota sobre o processo:** a partir deste ponto, o usuário instruiu
> explicitamente que o critério mais relevante de avaliação do desafio é a
> capacidade analítica e de tomada de decisão. Por isso, decisões de
> arquitetura passaram a ser tomadas e justificadas diretamente nos artefatos
> (ADRs), em vez de geradas como novas perguntas de aprovação — mantendo,
> ainda assim, o checkpoint formal antes de gerar código (regra do CLAUDE.md
> seção 6).

## Fase 2 — Construction (por Unit, após aprovação da Fase 1)

- [x] 1. Domain Design de cada Unit (DDD: agregados, entidades, value objects,
      eventos de domínio, repositórios) → `DESIGN/lancamentos/domain_design.md`,
      `DESIGN/consolidado-diario/domain_design.md`.
- [x] 2. Logical Design: resolver os NFRs com padrões arquiteturais → 7 ADRs em
      `ARCHITECTURE/adr/` + consolidação em `ARCHITECTURE/arquitetura_alvo.md`,
      `ARCHITECTURE/estimativa_custos.md`, `ARCHITECTURE/observabilidade.md`,
      `ARCHITECTURE/seguranca_integracao.md`.
- [ ] 3. Gerar código e testes unitários a partir do Logical Design aprovado →
      `BACKEND/` (e `FRONTEND/` se aplicável).
- [ ] 4. Gerar testes funcionais (e de segurança/carga na medida do escopo
      acordado).
- [ ] 5. Executar testes, corrigir falhas, aguardar aprovação, re-executar.
- [ ] 6. Empacotar em Deployment Units (Dockerfiles, IaC) → `DEPLOYMENT/`.

## Fase 3 — Operations

- [ ] 1. Documentar estratégia de deploy (sem executar deploy real — fora do
      escopo do desafio, que pede repositório público, não ambiente hospedado).
- [ ] 2. Documentar estratégia de monitoramento/observabilidade (diferencial).
- [ ] 3. Documentar runbooks básicos de incidente (proporcional ao escopo do
      desafio).

## Entregáveis finais obrigatórios (checklist do desafio)

- [ ] Mapeamento de domínios funcionais e capacidades de negócio.
- [ ] Requisitos funcionais e não funcionais refinados.
- [ ] Desenho da solução completo (Arquitetura Alvo).
- [ ] Justificativa das escolhas de tecnologia/arquitetura.
- [ ] Código na linguagem escolhida.
- [ ] Testes.
- [ ] README com instruções de uso e execução local.
- [ ] Publicação em repositório público no GitHub.
- [ ] Todas as documentações versionadas no repositório.

---

**Status:** Fase 1 completa e Fase 2 passos 1–2 (Domain Design + Logical
Design/ADRs) completos. Aguardando sua validação do Domain/Logical Design antes
de gerar código (passo 3), conforme regra obrigatória do CLAUDE.md ("não gerar
código de produção sem Domain Design e Logical Design aprovados").
