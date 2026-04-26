package com.finkin.application.service.transfer.validator;

import com.finkin.domain.exception.AccountBlockedException;
import com.finkin.domain.model.account.Account;
import com.finkin.domain.model.account.AccountStatus;
import com.finkin.domain.model.account.Money;
import com.finkin.domain.model.customer.Customer;
import org.springframework.stereotype.Component;

@Component
public class AccountStatusValidator implements TransactionValidator {

    @Override
    public void validate(Account source, Account target, Customer sourceOwner, Money amount) {
        if (!source.canDebit()) {
            throw new AccountBlockedException(source.getId());
        }
        // Destino aceita crédito se não estiver ENCERRADA
        if (AccountStatus.ENCERRADA.equals(target.getStatus())) {
            throw new AccountBlockedException(target.getId());
        }
    }
}
