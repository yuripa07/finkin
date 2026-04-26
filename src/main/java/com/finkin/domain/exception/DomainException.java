package com.finkin.domain.exception;

/**
 * Exceção base para todas as violações de regra de negócio do domínio Finkin.
 * Subclasses representam erros específicos e são mapeadas para HTTP status
 * distintos no GlobalExceptionHandler.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
