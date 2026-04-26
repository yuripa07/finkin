package com.finkin.domain.port.in;

import com.finkin.domain.model.account.Account;
import com.finkin.domain.model.account.AccountType;

import java.util.UUID;

public interface OpenAccountUseCase {

    /**
     * Abre uma nova conta para o customer.
     * Lança CustomerNotFoundException se o customer não existir.
     * Lança KycNotApprovedException se o KYC não estiver aprovado.
     */
    Account open(Command command);

    record Command(UUID customerId, AccountType type) {}
}
