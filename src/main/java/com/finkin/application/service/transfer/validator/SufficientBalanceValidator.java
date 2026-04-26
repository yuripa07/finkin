package com.finkin.application.service.transfer.validator;

import com.finkin.domain.exception.InsufficientBalanceException;
import com.finkin.domain.model.account.AccountModel;
import com.finkin.domain.model.account.MoneyModel;
import com.finkin.domain.model.customer.CustomerModel;
import org.springframework.stereotype.Component;

@Component
public class SufficientBalanceValidator implements ITransactionValidator {

    @Override
    public void validate(AccountModel source, AccountModel target, CustomerModel sourceOwner, MoneyModel amount) {
        if (source.getBalance().isLessThan(amount)) {
            throw new InsufficientBalanceException(
                source.getBalance().getAmount(),
                amount.getAmount()
            );
        }
    }
}
