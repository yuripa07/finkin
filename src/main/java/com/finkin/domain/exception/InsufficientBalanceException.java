package com.finkin.domain.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends DomainException {
    public InsufficientBalanceException(BigDecimal balance, BigDecimal requested) {
        super("Saldo insuficiente. Disponível: R$ %.2f, solicitado: R$ %.2f.".formatted(balance, requested));
    }
}
