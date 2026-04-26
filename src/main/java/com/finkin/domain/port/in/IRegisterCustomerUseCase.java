package com.finkin.domain.port.in;

import com.finkin.domain.model.customer.CustomerModel;
import com.finkin.domain.model.customer.KycStatus;

import java.time.LocalDate;

public interface IRegisterCustomerUseCase {

    /**
     * Cadastra um novo cliente PF.
     * Lança CustomerAlreadyExistsException se o CPF já existir.
     * Em dev (kyc.auto-approve=true): retorna com KycStatus.APPROVED.
     */
    CustomerModel register(Command command);

    record Command(
        String cpf,
        String fullName,
        LocalDate birthDate,
        String email,
        String phone
    ) {}
}
