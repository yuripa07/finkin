package com.finkin.domain.port.input;

import com.finkin.domain.model.transaction.TransactionModel;

import java.math.BigDecimal;
import java.util.UUID;

public interface IExecuteInternalTransferUseCase {

    /**
     * Executa uma transferência interna entre duas contas do Finkin.
     *
     * Idempotente: se idempotencyKey já foi processada, retorna a transação original.
     *
     * Validações (Chain of Responsibility):
     * 1. KYC do titular da conta de origem aprovado
     * 2. Conta de origem ATIVA
     * 3. Conta de destino ATIVA ou pelo menos não ENCERRADA
     * 4. Saldo suficiente
     * 5. Dentro do limite diário (diurno ou noturno)
     */
    TransactionModel execute(Command command);

    record Command(
        String idempotencyKey,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String description
    ) {}
}
