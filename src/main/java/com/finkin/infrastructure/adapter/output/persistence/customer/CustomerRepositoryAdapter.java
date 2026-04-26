package com.finkin.infrastructure.adapter.output.persistence.customer;

import com.finkin.domain.model.customer.CpfModel;
import com.finkin.domain.model.customer.CustomerModel;
import com.finkin.domain.port.output.ICustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que conecta o port de saída ICustomerRepository à implementação JPA.
 * O domain e o application nunca veem CustomerJpaEntity — apenas CustomerModel.
 */
@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements ICustomerRepository {

    private final ICustomerJpaRepository jpaRepository;
    private final ICustomerMapper mapper;

    @Override
    public CustomerModel save(CustomerModel customer) {
        var entity = mapper.toEntity(customer);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<CustomerModel> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CustomerModel> findByCpf(CpfModel cpf) {
        return jpaRepository.findByCpf(cpf.getValue()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCpf(CpfModel cpf) {
        return jpaRepository.existsByCpf(cpf.getValue());
    }
}
