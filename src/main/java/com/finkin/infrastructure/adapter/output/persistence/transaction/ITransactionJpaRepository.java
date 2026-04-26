package com.finkin.infrastructure.adapter.output.persistence.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ITransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    /**
     * Busca transações onde a conta é origem OU destino, ordenado por executed_at DESC.
     * Usado para o extrato da conta.
     */
    @Query("""
        SELECT t FROM TransactionJpaEntity t
        WHERE t.sourceAccountId = :accountId OR t.targetAccountId = :accountId
        ORDER BY t.executedAt DESC NULLS LAST, t.createdAt DESC
        """)
    Page<TransactionJpaEntity> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);
}
