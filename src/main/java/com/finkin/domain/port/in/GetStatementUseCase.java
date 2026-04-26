package com.finkin.domain.port.in;

import com.finkin.domain.model.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetStatementUseCase {

    /**
     * Retorna o extrato paginado de uma conta.
     * Ordenação padrão: executedAt DESC (mais recente primeiro).
     */
    Page<Transaction> getStatement(UUID accountId, Pageable pageable);
}
