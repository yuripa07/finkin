package com.finkin.application.service.transfer.validator;

import com.finkin.domain.exception.AccountBlockedException;
import com.finkin.domain.model.account.AccountModel;
import com.finkin.domain.model.account.enums.AccountStatusEnum;
import com.finkin.domain.model.account.MoneyModel;
import com.finkin.domain.model.customer.CustomerModel;
import org.springframework.stereotype.Component;

@Component
public class AccountStatusValidator implements ITransactionValidator {

    @Override
    public void validate(AccountModel source, AccountModel target, CustomerModel sourceOwner, MoneyModel amount) {
        if (!source.canDebit()) {
            throw new AccountBlockedException(source.getId());
        }
        // Destino aceita crédito se não estiver ENCERRADA
        if (AccountStatusEnum.ENCERRADA.equals(target.getStatus())) {
            throw new AccountBlockedException(target.getId());
        }
    }
}
