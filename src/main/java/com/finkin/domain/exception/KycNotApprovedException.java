package com.finkin.domain.exception;

import java.util.UUID;

/**
 * Lançada quando um customer tenta enviar dinheiro sem KYC aprovado.
 * Sem KYC aprovado, a conta pode apenas receber — não pode enviar.
 */
public class KycNotApprovedException extends DomainException {
    public KycNotApprovedException(UUID customerId) {
        super("CustomerModel %s não possui KYC aprovado. Conta pode apenas receber até aprovação.".formatted(customerId));
    }
}
