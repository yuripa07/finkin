package com.finkin.domain.exception;

import java.util.UUID;

public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException(UUID accountId) {
        super("Conta não encontrada: %s".formatted(accountId));
    }

    public AccountNotFoundException(String number) {
        super("Conta não encontrada: %s".formatted(number));
    }
}
