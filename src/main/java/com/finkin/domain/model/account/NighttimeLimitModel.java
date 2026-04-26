package com.finkin.domain.model.account;
import com.finkin.domain.port.input.ILimitPolicy;

import java.math.BigDecimal;

/**
 * Estratégia de limite noturno (20h–06h).
 * Resolução BCB nº 1/2020, Art. 20.
 */
public class NighttimeLimitModel implements ILimitPolicy {

    @Override
    public BigDecimal getLimit(AccountModel account) {
        return account.getDailyLimitNight();
    }

    @Override
    public String periodName() {
        return "noturno (20h–06h)";
    }
}
