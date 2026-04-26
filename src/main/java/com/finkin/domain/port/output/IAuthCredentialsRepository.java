package com.finkin.domain.port.output;

import java.util.Optional;
import java.util.UUID;

public interface IAuthCredentialsRepository {

    void save(UUID customerId, String email, String hashedPassword);

    Optional<Credentials> findByEmail(String email);

    record Credentials(UUID customerId, String email, String hashedPassword) {}
}
