package com.finkin.infrastructure.adapter.output.persistence.customer;

import com.finkin.domain.port.output.IAuthCredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthCredentialsRepositoryAdapter implements IAuthCredentialsRepository {

    private final IAuthCredentialsJpaRepository jpaRepository;

    @Override
    public void save(UUID customerId, String email, String hashedPassword) {
        var entity = AuthCredentialsJpaEntity.builder()
            .id(UUID.randomUUID())
            .customerId(customerId)
            .email(email.toLowerCase())
            .passwordHash(hashedPassword)
            .createdAt(ZonedDateTime.now())
            .build();
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Credentials> findByEmail(String email) {
        return jpaRepository.findByEmail(email.toLowerCase())
            .map(e -> new Credentials(e.getCustomerId(), e.getEmail(), e.getPasswordHash()));
    }
}
