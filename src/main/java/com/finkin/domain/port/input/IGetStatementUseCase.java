package com.finkin.domain.port.input;

import com.finkin.domain.model.transaction.TransactionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IGetStatementUseCase {

    /**
     * Retorna o extrato paginado de uma conta.
     * Ordenação padrão: executedAt DESC (mais recente primeiro).
     */
    Page<TransactionModel> getStatement(UUID accountId, Pageable pageable);
}
