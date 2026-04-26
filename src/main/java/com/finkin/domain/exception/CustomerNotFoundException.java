package com.finkin.domain.exception;

import java.util.UUID;

public class CustomerNotFoundException extends DomainException {
    public CustomerNotFoundException(UUID customerId) {
        super("CustomerModel não encontrado: %s".formatted(customerId));
    }
}
