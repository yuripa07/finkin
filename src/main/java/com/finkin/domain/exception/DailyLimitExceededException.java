package com.finkin.domain.exception;

import java.math.BigDecimal;

/**
 * Lançada quando uma transação excede o limite diário da conta.
 * Resolução BCB nº 1/2020, Art. 20 — limites diferenciados por período.
 */
public class DailyLimitExceededException extends DomainException {
    public DailyLimitExceededException(BigDecimal requested, BigDecimal limit, String period) {
        super("Valor R$ %.2f excede o limite %s de R$ %.2f.".formatted(requested, period, limit));
    }
}
