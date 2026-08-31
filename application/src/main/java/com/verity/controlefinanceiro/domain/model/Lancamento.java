package com.verity.controlefinanceiro.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Lancamento {
    private final UUID id;
    private final TipoLancamento tipo;
    private final Money valor;
    private final LocalDate data;
    private final String descricao;
    private final String categoria;
    private StatusLancamento status;
    private final UUID lancamentoOrigemId;

    public Lancamento(
        UUID id,
        TipoLancamento tipo,
        Money valor,
        LocalDate data,
        String descricao,
        String categoria
    ) {
        this(id, tipo, valor, data, descricao, categoria, StatusLancamento.ATIVO, null);
    }

    public Lancamento(
        UUID id,
        TipoLancamento tipo,
        Money valor,
        LocalDate data,
        String descricao,
        String categoria,
        StatusLancamento status,
        UUID lancamentoOrigemId
    ) {
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do lançamento é obrigatório");
        }
        if (valor == null) {
            throw new IllegalArgumentException("Valor do lançamento é obrigatório");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data do lançamento é obrigatória");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data do lançamento não pode ser futura");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }

        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.data = data;
        this.descricao = descricao;
        this.categoria = categoria;
        this.status = status == null ? StatusLancamento.ATIVO : status;
        this.lancamentoOrigemId = lancamentoOrigemId;
    }

    public Lancamento estornar() {
        if (this.status == StatusLancamento.ESTORNADO) {
            throw new IllegalStateException("Lançamento já foi estornado");
        }

        TipoLancamento tipoEstorno = switch (this.tipo) {
            case DEBITO -> TipoLancamento.CREDITO;
            case CREDITO -> TipoLancamento.DEBITO;
        };

        this.status = StatusLancamento.ESTORNADO;

        return new Lancamento(
            UUID.randomUUID(),
            tipoEstorno,
            this.valor,
            LocalDate.now(),
            "Estorno de " + this.descricao,
            this.categoria,
            StatusLancamento.ATIVO,
            this.id
        );
    }

    public void marcarComoEstornado() {
        this.status = StatusLancamento.ESTORNADO;
    }

    public UUID id() {
        return id;
    }

    public TipoLancamento tipo() {
        return tipo;
    }

    public Money valor() {
        return valor;
    }

    public LocalDate data() {
        return data;
    }

    public String descricao() {
        return descricao;
    }

    public String categoria() {
        return categoria;
    }

    public StatusLancamento status() {
        return status;
    }

    public UUID lancamentoOrigemId() {
        return lancamentoOrigemId;
    }
}
