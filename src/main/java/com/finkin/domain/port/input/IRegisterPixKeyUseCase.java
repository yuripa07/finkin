package com.finkin.domain.port.input;

import com.finkin.domain.model.pix.PixKeyModel;
import com.finkin.domain.model.pix.enums.PixKeyTypeEnum;

import java.util.UUID;

public interface IRegisterPixKeyUseCase {

    /**
     * Registra uma nova chave Pix para a conta.
     * Para chave do tipo RANDOM, o valor é gerado pelo servidor (não aceito do cliente).
     */
    PixKeyModel register(Command command);

    record Command(UUID accountId, PixKeyTypeEnum keyType, String keyValue) {}
}
