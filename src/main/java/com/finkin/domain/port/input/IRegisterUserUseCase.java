package com.finkin.domain.port.input;

import java.util.UUID;

public interface IRegisterUserUseCase {

    /** Cria credenciais de acesso (email + senha) para um customer já cadastrado. */
    void register(Command command);

    record Command(UUID customerId, String email, String rawPassword) {}
}
