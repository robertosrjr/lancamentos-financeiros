package com.verity.controlefinanceiro.infrastructure.adapter.in.web;

import com.verity.controlefinanceiro.application.port.in.ConsultarLancamentosUseCase;
import com.verity.controlefinanceiro.application.port.in.EstornarLancamentoUseCase;
import com.verity.controlefinanceiro.application.port.in.RegistrarLancamentoUseCase;
import com.verity.controlefinanceiro.domain.model.Lancamento;
import com.verity.controlefinanceiro.domain.model.TipoLancamento;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lancamentos")
public class LancamentoController {

    private final RegistrarLancamentoUseCase registrarLancamentoUseCase;
    private final ConsultarLancamentosUseCase consultarLancamentosUseCase;
    private final EstornarLancamentoUseCase estornarLancamentoUseCase;

    public LancamentoController(
        RegistrarLancamentoUseCase registrarLancamentoUseCase,
        ConsultarLancamentosUseCase consultarLancamentosUseCase,
        EstornarLancamentoUseCase estornarLancamentoUseCase
    ) {
        this.registrarLancamentoUseCase = registrarLancamentoUseCase;
        this.consultarLancamentosUseCase = consultarLancamentosUseCase;
        this.estornarLancamentoUseCase = estornarLancamentoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoResponse registrar(@Valid @RequestBody LancamentoRequest request) {
        Lancamento lancamento = registrarLancamentoUseCase.registrar(
            new RegistrarLancamentoUseCase.RegistrarLancamentoCommand(
                request.tipo(),
                request.valor(),
                request.data(),
                request.descricao(),
                request.categoria()
            )
        );

        return LancamentoResponse.from(lancamento);
    }

    @GetMapping
    public List<LancamentoResponse> listar() {
        return consultarLancamentosUseCase.listarTodos().stream()
            .map(LancamentoResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public LancamentoResponse buscarPorId(@PathVariable UUID id) {
        return LancamentoResponse.from(consultarLancamentosUseCase.buscarPorId(id));
    }

    @PostMapping("/{id}/estorno")
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoResponse estornar(@PathVariable UUID id) {
        return LancamentoResponse.from(estornarLancamentoUseCase.estornar(id));
    }

    public record LancamentoRequest(
        TipoLancamento tipo,
        BigDecimal valor,
        LocalDate data,
        String descricao,
        String categoria
    ) {}

    public record LancamentoResponse(
        UUID id,
        TipoLancamento tipo,
        BigDecimal valor,
        LocalDate data,
        String descricao,
        String categoria,
        String status,
        UUID lancamentoOrigemId
    ) {
        public static LancamentoResponse from(Lancamento lancamento) {
            return new LancamentoResponse(
                lancamento.id(),
                lancamento.tipo(),
                lancamento.valor().amount(),
                lancamento.data(),
                lancamento.descricao(),
                lancamento.categoria(),
                lancamento.status().name(),
                lancamento.lancamentoOrigemId()
            );
        }
    }
}
