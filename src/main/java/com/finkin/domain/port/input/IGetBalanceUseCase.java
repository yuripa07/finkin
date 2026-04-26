package com.finkin.domain.port.input;

import com.finkin.domain.model.account.MoneyModel;

import java.util.UUID;

public interface IGetBalanceUseCase {

    /**
     * Retorna o saldo atual da conta.
     * Lança AccountNotFoundException se a conta não existir.
     * Lança AccessDeniedException (Spring Security) se o customerId do JWT
     * não for o titular da conta — verificação no controller.
     */
    MoneyModel getBalance(UUID accountId);
}
