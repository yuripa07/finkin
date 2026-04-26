package com.finkin.domain.port.in;

import com.finkin.domain.model.customer.Customer;
import com.finkin.domain.model.customer.KycStatus;

import java.time.LocalDate;

public interface RegisterCustomerUseCase {

    /**
     * Cadastra um novo cliente PF.
     * Lança CustomerAlreadyExistsException se o CPF já existir.
     * Em dev (kyc.auto-approve=true): retorna com KycStatus.APPROVED.
     */
    Customer register(Command command);

    record Command(
        String cpf,
        String fullName,
        LocalDate birthDate,
        String email,
        String phone
    ) {}
}
