package com.finkin.domain.exception;

import java.util.UUID;

public class AccountBlockedException extends DomainException {
    public AccountBlockedException(UUID accountId) {
        super("Conta %s está bloqueada ou inativa e não pode realizar operações.".formatted(accountId));
    }
}
