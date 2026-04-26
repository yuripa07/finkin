package com.finkin.domain.port.in;

import com.finkin.domain.model.transaction.TransactionModel;

import java.util.UUID;

public interface IIssueReceiptUseCase {

    /**
     * Retorna os dados completos de uma transação para geração de comprovante.
     * Lança AccountNotFoundException/TransactionNotFoundException se não encontrada.
     * A transação deve pertencer ao customerId do JWT (verificação no controller).
     */
    TransactionModel getReceipt(UUID transactionId);
}
