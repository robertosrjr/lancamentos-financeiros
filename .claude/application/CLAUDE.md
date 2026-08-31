# CLAUDE.md

Memória de projeto para o Claude Code. Mantenha este arquivo enxuto — regras detalhadas ficam em `.claude/rules/`.

## Stack

- **Linguagem**: Java 21 (LTS) — priorizar records, sealed classes, pattern matching for switch, virtual threads, text blocks.
- **Framework**: Spring Boot 4.1.1
- **Arquitetura**: Hexagonal (Ports & Adapters) + DDD
- **Build**: Maven (ajustar se o projeto usar Gradle)

## Estrutura do projeto

```
src/main/java/com/empresa/projeto
 ├── domain          → Entidades, Value Objects, Domain Services, Events (sem dependências externas)
 ├── application      → Ports (in/out) + Use Cases (orquestração)
 └── infrastructure   → Adapters (web, persistence, messaging, client) + config
```

## Regras fundamentais (sempre válidas)

- Dependência sempre aponta para dentro: `infrastructure → application → domain`. `domain` nunca importa Spring, JPA ou HTTP.
- Controllers nunca chamam repositórios diretamente — sempre via um caso de uso.
- Nunca retornar `null`: usar `Optional<T>`, coleção vazia ou exceção de domínio.
- Toda mudança *breaking* de contrato de API exige nova versão, nunca altera um endpoint existente silenciosamente.
- TDD é o padrão: escrever o teste antes da implementação (Red → Green → Refactor).

## Comandos

- Build: `./mvnw clean install`
- Rodar testes: `./mvnw test`
- Subir localmente: `./mvnw spring-boot:run`
- Gerar spec OpenAPI: acessar `/v3/api-docs` com a aplicação rodando

## Regras detalhadas (consultar conforme a tarefa)

| Ao trabalhar em... | Consulte |
|---|---|
| Estrutura de pacotes, camadas, modelagem de domínio (DDD) | `.claude/rules/architecture.md` |
| Qualidade de código: SOLID, Clean Code, Design Patterns | `.claude/rules/code-quality.md` |
| Endpoints REST, contratos, erros, documentação OpenAPI/Swagger | `.claude/rules/api-conventions.md` |
| Configuração, deploy, 12-factor, decisão MVC vs WebFlux | `.claude/rules/platform-practices.md` |
| Testes unitários, integração, TDD | `.claude/rules/testing.md` |

## Referências

- [The Twelve-Factor App (PT-BR)](https://12factor.net/pt_br/)
- [Reactive Manifesto](https://www.reactivemanifesto.org/)
- Eric Evans — *Domain-Driven Design*
- Robert C. Martin — *Clean Code* / *Clean Architecture*
