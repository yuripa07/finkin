package com.finkin.application.service.transfer.validator;

import com.finkin.domain.exception.DailyLimitExceededException;
import com.finkin.domain.model.account.AccountModel;
import com.finkin.domain.model.account.LimitPolicySelectorModel;
import com.finkin.domain.model.account.MoneyModel;
import com.finkin.domain.model.customer.CustomerModel;
import org.springframework.stereotype.Component;

/**
 * Valida o limite diário (diurno ou noturno) conforme a Resolução BCB nº 1/2020, Art. 20.
 * A seleção de política (Strategy) é delegada ao LimitPolicySelectorModel.
 */
@Component
public class DailyLimitValidator implements ITransactionValidator {

    @Override
    public void validate(AccountModel source, AccountModel target, CustomerModel sourceOwner, MoneyModel amount) {
        var policy = LimitPolicySelectorModel.select();
        var limit = MoneyModel.of(policy.getLimit(source));

        if (amount.isGreaterThan(limit)) {
            throw new DailyLimitExceededException(
                amount.getAmount(),
                limit.getAmount(),
                policy.periodName()
            );
        }
    }
}
