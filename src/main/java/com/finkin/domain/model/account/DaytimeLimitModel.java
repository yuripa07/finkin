package com.finkin.domain.model.account;

import java.math.BigDecimal;

/**
 * Estratégia de limite diurno (06h–20h).
 * Resolução BCB nº 1/2020, Art. 20.
 */
public class DaytimeLimitModel implements ILimitPolicy {

    @Override
    public BigDecimal getLimit(AccountModel account) {
        return account.getDailyLimitDay();
    }

    @Override
    public String periodName() {
        return "diurno (06h–20h)";
    }
}
