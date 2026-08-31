package com.verity.controlefinanceiro.application.usecase;

import com.verity.controlefinanceiro.application.port.in.EstornarLancamentoUseCase;
import com.verity.controlefinanceiro.application.port.out.LancamentoRepository;
import com.verity.controlefinanceiro.domain.model.Lancamento;

import java.util.UUID;

public class EstornarLancamentoUseCaseImpl implements EstornarLancamentoUseCase {

    private final LancamentoRepository repository;

    public EstornarLancamentoUseCaseImpl(LancamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Lancamento estornar(UUID lancamentoId) {
        Lancamento original = repository.findById(lancamentoId)
            .orElseThrow(() -> new IllegalArgumentException("Lançamento não encontrado: " + lancamentoId));

        if (original.status().name().equals("ESTORNADO")) {
            throw new IllegalStateException("Lançamento já foi estornado");
        }

        Lancamento estorno = original.estornar();
        return repository.save(estorno);
    }
}
