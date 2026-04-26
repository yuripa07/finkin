package com.finkin.application.service.transfer.validator;

import com.finkin.domain.exception.InsufficientBalanceException;
import com.finkin.domain.model.account.Account;
import com.finkin.domain.model.account.Money;
import com.finkin.domain.model.customer.Customer;
import org.springframework.stereotype.Component;

@Component
public class SufficientBalanceValidator implements TransactionValidator {

    @Override
    public void validate(Account source, Account target, Customer sourceOwner, Money amount) {
        if (source.getBalance().isLessThan(amount)) {
            throw new InsufficientBalanceException(
                source.getBalance().getAmount(),
                amount.getAmount()
            );
        }
    }
}
