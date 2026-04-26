package com.finkin.domain.port.in;

import com.finkin.domain.model.pix.PixKeyType;
import com.finkin.domain.model.transaction.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public interface SendPixUseCase {

    /**
     * Envia Pix para uma chave.
     *
     * Fase 1: apenas chaves internas do Finkin são suportadas (liquidação imediata).
     * Chaves externas disparam o SpiClientMockAdapter (delay configurável).
     *
     * Idempotente via idempotencyKey.
     */
    Transaction send(Command command);

    record Command(
        String idempotencyKey,
        UUID sourceAccountId,
        PixKeyType targetKeyType,
        String targetKeyValue,
        BigDecimal amount,
        String description
    ) {}
}
