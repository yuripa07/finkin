package com.finkin.domain.model.account;

import java.math.BigDecimal;

/**
 * Estratégia de limite noturno (20h–06h).
 * Resolução BCB nº 1/2020, Art. 20.
 */
public class NighttimeLimit implements LimitPolicy {

    @Override
    public BigDecimal getLimit(Account account) {
        return account.getDailyLimitNight();
    }

    @Override
    public String periodName() {
        return "noturno (20h–06h)";
    }
}
