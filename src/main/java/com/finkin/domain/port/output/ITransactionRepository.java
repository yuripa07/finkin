package com.finkin.domain.port.output;

import com.finkin.domain.model.transaction.TransactionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ITransactionRepository {
    TransactionModel save(TransactionModel transaction);
    Optional<TransactionModel> findById(UUID id);
    Page<TransactionModel> findByAccountId(UUID accountId, Pageable pageable);
}
