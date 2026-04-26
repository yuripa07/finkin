package com.finkin.domain.exception;

public class IdempotencyConflictException extends DomainException {
    public IdempotencyConflictException(String key) {
        super("Idempotency-Key '%s' já foi usada com parâmetros diferentes.".formatted(key));
    }
}
