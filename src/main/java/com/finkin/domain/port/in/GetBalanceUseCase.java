package com.finkin.domain.port.in;

import com.finkin.domain.model.account.Money;

import java.util.UUID;

public interface GetBalanceUseCase {

    /**
     * Retorna o saldo atual da conta.
     * Lança AccountNotFoundException se a conta não existir.
     * Lança AccessDeniedException (Spring Security) se o customerId do JWT
     * não for o titular da conta — verificação no controller.
     */
    Money getBalance(UUID accountId);
}
