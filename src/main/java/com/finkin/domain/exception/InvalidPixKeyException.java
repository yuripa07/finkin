package com.finkin.domain.exception;

public class InvalidPixKeyException extends DomainException {
    public InvalidPixKeyException(String keyType, String value) {
        super("Chave Pix do tipo %s com valor '%s' é inválida.".formatted(keyType, value));
    }
}
