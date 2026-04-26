package com.finkin.domain.model.customer;

import com.finkin.domain.exception.DomainException;
import lombok.Value;

import java.util.regex.Pattern;

/**
 * Value Object para endereço de e-mail.
 * Validação baseada em RFC 5322 simplificada — cobre 99% dos casos reais
 * sem a complexidade da regex completa da RFC.
 */
@Value
public class EmailModel {

    // Cobre: usuario@dominio.tld, usuario+tag@sub.dominio.com.br, etc.
    private static final Pattern PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    String value;

    public EmailModel(String raw) {
        if (raw == null || !PATTERN.matcher(raw.strip()).matches()) {
            throw new InvalidEmailException(raw);
        }
        this.value = raw.strip().toLowerCase();
    }

    public static class InvalidEmailException extends DomainException {
        public InvalidEmailException(String email) {
            super("E-mail inválido: %s".formatted(email));
        }
    }
}
