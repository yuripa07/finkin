package com.finkin.domain.port.in;

public interface AuthenticateUseCase {

    /**
     * Valida credenciais e retorna um JWT assinado.
     * Lança BadCredentialsException se inválidas.
     */
    String authenticate(Command command);

    record Command(String email, String rawPassword) {}
}
