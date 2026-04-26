package com.finkin.application.service.account;

import com.finkin.domain.exception.CustomerNotFoundException;
import com.finkin.domain.exception.KycNotApprovedException;
import com.finkin.domain.model.account.*;
import com.finkin.domain.port.in.IOpenAccountUseCase;
import com.finkin.domain.port.out.IAccountRepository;
import com.finkin.domain.port.out.ICustomerRepository;
import com.finkin.infrastructure.config.LimitsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Pattern: Factory aplicado aqui — OpenAccountService é o ponto de entrada
 * para abrir uma conta, encapsulando a geração de número, defaults de limite
 * e validação de KYC. Os detalhes de construção da AccountModel ficam centralizados
 * neste service, não espalhados pelos callers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAccountService implements IOpenAccountUseCase {

    private final IAccountRepository accountRepository;
    private final ICustomerRepository customerRepository;
    private final AccountNumberGenerator numberGenerator;
    private final LimitsProperties limitsProperties;

    @Override
    @Transactional
    public AccountModel open(Command command) {
        var customer = customerRepository.findById(command.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        // Sem KYC aprovado, não é possível abrir conta para envio
        if (!customer.isKycApproved()) {
            throw new KycNotApprovedException(command.customerId());
        }

        var now = ZonedDateTime.now();
        var account = AccountModel.builder()
            .id(UUID.randomUUID())
            .customerId(command.customerId())
            .agency("0001")
            .number(numberGenerator.generate())
            .type(command.type())
            .status(AccountStatus.ATIVA)
            .balance(MoneyModel.zero())
            .dailyLimitDay(limitsProperties.getTransferDayBrl())
            .dailyLimitNight(limitsProperties.getTransferNightBrl())
            .createdAt(now)
            .updatedAt(now)
            .build();

        var saved = accountRepository.save(account);
        log.info("Conta aberta: id={}, number={}, customer={}", saved.getId(), saved.getNumber(), command.customerId());
        return saved;
    }
}
