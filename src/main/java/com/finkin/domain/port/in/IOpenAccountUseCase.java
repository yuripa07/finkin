package com.finkin.domain.port.in;

import com.finkin.domain.model.account.AccountModel;
import com.finkin.domain.model.account.AccountType;

import java.util.UUID;

public interface IOpenAccountUseCase {

    /**
     * Abre uma nova conta para o customer.
     * Lança CustomerNotFoundException se o customer não existir.
     * Lança KycNotApprovedException se o KYC não estiver aprovado.
     */
    AccountModel open(Command command);

    record Command(UUID customerId, AccountType type) {}
}
