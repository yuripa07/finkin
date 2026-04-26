package com.finkin.application.service.transfer.validator;

import com.finkin.domain.exception.DailyLimitExceededException;
import com.finkin.domain.model.account.Account;
import com.finkin.domain.model.account.LimitPolicySelector;
import com.finkin.domain.model.account.Money;
import com.finkin.domain.model.customer.Customer;
import org.springframework.stereotype.Component;

/**
 * Valida o limite diário (diurno ou noturno) conforme a Resolução BCB nº 1/2020, Art. 20.
 * A seleção de política (Strategy) é delegada ao LimitPolicySelector.
 */
@Component
public class DailyLimitValidator implements TransactionValidator {

    @Override
    public void validate(Account source, Account target, Customer sourceOwner, Money amount) {
        var policy = LimitPolicySelector.select();
        var limit = Money.of(policy.getLimit(source));

        if (amount.isGreaterThan(limit)) {
            throw new DailyLimitExceededException(
                amount.getAmount(),
                limit.getAmount(),
                policy.periodName()
            );
        }
    }
}
