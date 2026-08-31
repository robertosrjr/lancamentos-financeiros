package com.verity.controlefinanceiro.infrastructure.config;

import com.verity.controlefinanceiro.application.port.in.ConsultarLancamentosUseCase;
import com.verity.controlefinanceiro.application.port.in.EstornarLancamentoUseCase;
import com.verity.controlefinanceiro.application.port.in.RegistrarLancamentoUseCase;
import com.verity.controlefinanceiro.application.port.out.LancamentoRepository;
import com.verity.controlefinanceiro.application.usecase.ConsultarLancamentosUseCaseImpl;
import com.verity.controlefinanceiro.application.usecase.EstornarLancamentoUseCaseImpl;
import com.verity.controlefinanceiro.application.usecase.RegistrarLancamentoUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {

    @Bean
    public RegistrarLancamentoUseCase registrarLancamentoUseCase(LancamentoRepository repository) {
        return new RegistrarLancamentoUseCaseImpl(repository);
    }

    @Bean
    public ConsultarLancamentosUseCase consultarLancamentosUseCase(LancamentoRepository repository) {
        return new ConsultarLancamentosUseCaseImpl(repository);
    }

    @Bean
    public EstornarLancamentoUseCase estornarLancamentoUseCase(LancamentoRepository repository) {
        return new EstornarLancamentoUseCaseImpl(repository);
    }
}
