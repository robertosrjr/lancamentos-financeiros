package com.verity.controlefinanceiro.infrastructure.adapter.out.persistence;

import com.verity.controlefinanceiro.application.port.out.LancamentoRepository;
import com.verity.controlefinanceiro.application.port.out.OutboxEvent;
import com.verity.controlefinanceiro.domain.model.Lancamento;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLancamentoRepository implements LancamentoRepository {

    private final Map<UUID, Lancamento> storage = new ConcurrentHashMap<>();
    private final Map<String, OutboxEvent> outboxByKey = new ConcurrentHashMap<>();

    @Override
    public Lancamento save(Lancamento lancamento) {
        storage.put(lancamento.id(), lancamento);
        return lancamento;
    }

    @Override
    public Optional<Lancamento> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Lancamento> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public OutboxEvent saveOutboxEvent(OutboxEvent event) {
        outboxByKey.put(event.idempotencyKey(), event);
        return event;
    }

    @Override
    public Optional<OutboxEvent> findOutboxEventByIdempotencyKey(String idempotencyKey) {
        return Optional.ofNullable(outboxByKey.get(idempotencyKey));
    }
}
