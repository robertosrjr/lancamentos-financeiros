package com.verity.controlefinanceiro.application.usecase;

import com.verity.controlefinanceiro.application.port.in.RegistrarLancamentoUseCase;
import com.verity.controlefinanceiro.application.port.out.LancamentoRepository;
import com.verity.controlefinanceiro.application.port.out.OutboxEvent;
import com.verity.controlefinanceiro.domain.model.Lancamento;
import com.verity.controlefinanceiro.domain.model.Money;
import com.verity.controlefinanceiro.domain.model.StatusLancamento;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Currency;
import java.util.HexFormat;
import java.util.UUID;

public class RegistrarLancamentoUseCaseImpl implements RegistrarLancamentoUseCase {

    private final LancamentoRepository repository;

    public RegistrarLancamentoUseCaseImpl(LancamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Lancamento registrar(RegistrarLancamentoCommand command) {
        Money money = new Money(command.valor(), Currency.getInstance("BRL"));

        String payload = String.format(
            "{\"tipo\":\"%s\",\"valor\":\"%s\",\"data\":\"%s\",\"descricao\":\"%s\",\"categoria\":\"%s\"}",
            command.tipo(),
            command.valor(),
            command.data(),
            command.descricao(),
            command.categoria() == null ? "" : command.categoria()
        );
        String idempotencyKey = hash(payload);

        Lancamento lancamento = new Lancamento(
            UUID.randomUUID(),
            command.tipo(),
            money,
            command.data(),
            command.descricao(),
            command.categoria(),
            command.usuarioId(),
            StatusLancamento.ATIVO,
            null,
            idempotencyKey
        );

        Lancamento saved = repository.save(lancamento);

        repository.saveOutboxEvent(OutboxEvent.create(
            saved.id(),
            "Lancamento",
            "LancamentoRegistrado",
            payload,
            idempotencyKey
        ));

        return saved;
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível no runtime", e);
        }
    }
}
