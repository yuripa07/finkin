package com.finkin.infrastructure.adapter.output.persistence.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ICustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    Optional<CustomerJpaEntity> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
