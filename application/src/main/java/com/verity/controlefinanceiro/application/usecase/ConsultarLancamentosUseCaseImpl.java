package com.verity.controlefinanceiro.application.usecase;

import com.verity.controlefinanceiro.application.port.in.ConsultarLancamentosUseCase;
import com.verity.controlefinanceiro.application.port.out.LancamentoRepository;
import com.verity.controlefinanceiro.domain.model.Lancamento;

import java.util.List;
import java.util.UUID;

public class ConsultarLancamentosUseCaseImpl implements ConsultarLancamentosUseCase {

    private final LancamentoRepository repository;

    public ConsultarLancamentosUseCaseImpl(LancamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Lancamento> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Lancamento buscarPorId(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lançamento não encontrado: " + id));
    }
}
