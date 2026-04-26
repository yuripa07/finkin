package com.finkin.domain.port.in;

import com.finkin.domain.model.pix.PixKey;
import com.finkin.domain.model.pix.PixKeyType;

import java.util.UUID;

public interface RegisterPixKeyUseCase {

    /**
     * Registra uma nova chave Pix para a conta.
     * Para chave do tipo RANDOM, o valor é gerado pelo servidor (não aceito do cliente).
     */
    PixKey register(Command command);

    record Command(UUID accountId, PixKeyType keyType, String keyValue) {}
}
