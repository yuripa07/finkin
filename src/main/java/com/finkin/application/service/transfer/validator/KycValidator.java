package com.finkin.application.service.transfer.validator;

import com.finkin.domain.exception.KycNotApprovedException;
import com.finkin.domain.model.account.AccountModel;
import com.finkin.domain.model.account.MoneyModel;
import com.finkin.domain.model.customer.CustomerModel;
import org.springframework.stereotype.Component;

@Component
public class KycValidator implements ITransactionValidator {

    @Override
    public void validate(AccountModel source, AccountModel target, CustomerModel sourceOwner, MoneyModel amount) {
        if (!sourceOwner.isKycApproved()) {
            throw new KycNotApprovedException(sourceOwner.getId());
        }
    }
}
