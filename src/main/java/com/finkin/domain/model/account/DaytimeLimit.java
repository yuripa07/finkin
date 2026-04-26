package com.finkin.domain.model.account;

import java.math.BigDecimal;

/**
 * Estratégia de limite diurno (06h–20h).
 * Resolução BCB nº 1/2020, Art. 20.
 */
public class DaytimeLimit implements LimitPolicy {

    @Override
    public BigDecimal getLimit(Account account) {
        return account.getDailyLimitDay();
    }

    @Override
    public String periodName() {
        return "diurno (06h–20h)";
    }
}
