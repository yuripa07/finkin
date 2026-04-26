package com.finkin.domain.exception;

public class CustomerAlreadyExistsException extends DomainException {
    public CustomerAlreadyExistsException(String cpf) {
        super("Já existe um customer cadastrado com o CPF informado.");
    }
}
