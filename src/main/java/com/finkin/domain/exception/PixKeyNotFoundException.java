package com.finkin.domain.exception;

public class PixKeyNotFoundException extends DomainException {
    public PixKeyNotFoundException(String key) {
        super("Chave Pix não encontrada: %s".formatted(key));
    }
}
