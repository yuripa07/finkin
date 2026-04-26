package com.finkin.application.service.transfer.validator;

import com.finkin.domain.exception.KycNotApprovedException;
import com.finkin.domain.model.account.Account;
import com.finkin.domain.model.account.Money;
import com.finkin.domain.model.customer.Customer;
import org.springframework.stereotype.Component;

@Component
public class KycValidator implements TransactionValidator {

    @Override
    public void validate(Account source, Account target, Customer sourceOwner, Money amount) {
        if (!sourceOwner.isKycApproved()) {
            throw new KycNotApprovedException(sourceOwner.getId());
        }
    }
}
