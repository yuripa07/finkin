package com.finkin.domain.port.input;

public interface IAuthenticateUseCase {

    /**
     * Valida credenciais e retorna um JWT assinado.
     * Lança BadCredentialsException se inválidas.
     */
    String authenticate(Command command);

    record Command(String email, String rawPassword) {}
}
