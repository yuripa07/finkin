package com.finkin.domain.port.out;

import com.finkin.domain.model.customer.CustomerModel;
import com.finkin.domain.model.customer.CpfModel;

import java.util.Optional;
import java.util.UUID;

public interface ICustomerRepository {
    CustomerModel save(CustomerModel customer);
    Optional<CustomerModel> findById(UUID id);
    Optional<CustomerModel> findByCpf(CpfModel cpf);
    boolean existsByCpf(CpfModel cpf);
}
