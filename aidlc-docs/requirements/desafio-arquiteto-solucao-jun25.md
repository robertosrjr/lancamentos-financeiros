# Desafio Arquiteto de Solução (jun/25) — Transcrição do Documento Fonte

> Fonte original: `.claude/rules/requisitos/desafio-arquiteto-solucao-jun25.pdf`
> Transcrito integralmente para permitir rastreabilidade e versionamento no repositório.

## Papel do Arquiteto de Soluções

- Responsável por compreender e transformar requisitos de negócios (funcionais e
  não-funcionais) em capacidades/competências que gerem valor para a organização.
- Desenha arquiteturas de contexto com distribuições e responsabilidades de
  processos/etapas, isoladas ou não, habilitando a segregação de capacidades.
- Precisa de capacidade analítica para definição de conceitos e desenho de
  processos/soluções que componham a cadeia de valor do negócio.
- Responsável por tornar as soluções escaláveis, reutilizáveis e flexíveis,
  suportando a estratégia de negócio e a arquitetura de referência.
- Deve ter capacidade de comunicação e visão sistêmica para integrar áreas,
  atividades, serviços e sistemas.

## Objetivo do Desafio

Desenvolver uma arquitetura que integre processos e sistemas de forma eficiente,
garantindo entrega de valor. Inclui: definição de contextos, capacidades de
negócio e domínios funcionais; escalabilidade (alta disponibilidade, segurança,
desempenho); comunicação eficaz entre áreas/serviços; seleção de padrões
arquiteturais; integração de tecnologias/frameworks; otimização de requisitos
não-funcionais.

Dimensões avaliadas:

- **Compreensão dos Requisitos de Negócios**
- **Arquitetura Corporativa** (processos, etapas, responsabilidades isoladas/integradas)
- **Escalabilidade** (dimensionamento horizontal, balanceamento de carga, cache)
- **Resiliência** (redundância, failover, monitoramento proativo, recuperação)
- **Segurança** (autenticação, autorização, criptografia, proteção contra ataques)
- **Padrões Arquiteturais** (microsserviços, monolito, SOA, serverless — trade-offs)
- **Integração** (protocolos, formatos de mensagem, ferramentas de integração)
- **Requisitos Não-Funcionais** (desempenho, disponibilidade, confiabilidade — métricas e metas)
- **Documentação** (decisões arquiteturais, diagramas, fluxos de dados)

> *Nota do documento fonte:* não é necessário que todas essas premissas sejam
> apresentadas na codificação, mas nas decisões e representações arquiteturais do
> projeto. A intenção é analisar conhecimento empírico, capacidade de tomada de
> decisão, aplicação de boas práticas e decomposição de domínios/componentes.

## Descritivo da Solução (Intent de negócio)

> Um comerciante precisa controlar o seu fluxo de caixa diário com os lançamentos
> (débitos e créditos), também precisa de um relatório que disponibilize o saldo
> diário consolidado.

## Requisitos de Negócio

- Serviço que faça o controle de lançamentos.
- Serviço do consolidado diário.

## Requisitos Obrigatórios

- Mapeamento de domínios funcionais e capacidades de negócio.
- Refinamento do levantamento de requisitos funcionais e não funcionais.
- Desenho da solução completo (Arquitetura Alvo).
- Justificativa na decisão/escolha de ferramentas/tecnologias e de tipo de arquitetura.
- Pode ser feito na linguagem que o candidato domina.
- Testes.
- Readme com instruções claras de como a aplicação funciona e como rodar localmente.
- Hospedar em repositório público (GitHub).
- Todas as documentações de projeto devem estar no repositório.

> *Nota do documento fonte:* caso os requisitos técnicos obrigatórios não sejam
> minimamente atendidos, o teste será descartado.

## Requisitos Diferenciais

- Desenho da solução da Arquitetura de Transição (se necessária, considerando
  migração de legado).
- Estimativa de custos com infraestrutura e licenças.
- Monitoramento e Observabilidade.
- Critérios de segurança para consumo (integração) de serviços.

## Requisitos Não Funcionais (explícitos no documento)

- O serviço de controle de lançamento **não pode ficar indisponível** se o
  serviço de consolidado diário cair (isolamento de falhas entre os dois serviços).
- Em dias de pico, o serviço de consolidado diário recebe **50 requisições/segundo**,
  com **no máximo 5% de perda de requisições**.

## Observações do Documento Fonte

- Considerar todos os critérios técnicos mencionados, mas não se prender só a eles;
  usar o desafio para demonstrar capacidade de decisão sobre o que é importante.
- São bem-vindas descrições de evoluções futuras / o que se gostaria de ter
  implementado com mais tempo — aproveitar a documentação para isso.
