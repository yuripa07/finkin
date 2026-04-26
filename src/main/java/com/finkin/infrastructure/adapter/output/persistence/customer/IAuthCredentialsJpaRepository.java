package com.finkin.infrastructure.adapter.output.persistence.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IAuthCredentialsJpaRepository extends JpaRepository<AuthCredentialsJpaEntity, UUID> {
    Optional<AuthCredentialsJpaEntity> findByEmail(String email);
}
