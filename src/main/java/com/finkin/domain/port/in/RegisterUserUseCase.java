package com.finkin.domain.port.in;

import java.util.UUID;

public interface RegisterUserUseCase {

    /** Cria credenciais de acesso (email + senha) para um customer já cadastrado. */
    void register(Command command);

    record Command(UUID customerId, String email, String rawPassword) {}
}
