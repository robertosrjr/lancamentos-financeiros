# Plano: Transactional Outbox + Idempotência por hash da mensagem

- [x] Revisar a arquitetura e o estado atual do fluxo de registro de lançamentos.
- [x] Confirmar ausência de persistência de evento e geração de chave de idempotência no código em execução.
- [x] Definir estratégia de outbox com retorno do evento persistido e chave calculada via hash SHA-256 do payload.
- [x] Implementar o contrato do repositório para registrar eventos de outbox.
- [x] Persistir um evento de `LancamentoRegistrado` na mesma operação de cadastro do lançamento.
- [ ] Validar com testes focados do caso de uso e ajustar o que for necessário.
- [ ] Revisar impacto em outros testes e confirmar estabilidade do contrato de repositório.

## Observações

- A chave de idempotência será calculada a partir do payload do evento, usando SHA-256, conforme pedido do usuário.
- O objetivo é garantir que o mesmo evento não seja publicado mais de uma vez em cenários de retry ou reprocessamento.
- A implementação atual é um MVP em memória, sem poller assíncrono, mas já preserva o conceito central do outbox e do idempotency key.
