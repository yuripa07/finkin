package com.finkin.infrastructure.adapter.output.persistence.transaction;

import com.finkin.domain.model.transaction.TransactionModel;
import com.finkin.domain.port.output.ITransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements ITransactionRepository {

    private final ITransactionJpaRepository jpaRepository;
    private final ITransactionMapper mapper;

    @Override
    public TransactionModel save(TransactionModel transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(transaction)));
    }

    @Override
    public Optional<TransactionModel> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<TransactionModel> findByAccountId(UUID accountId, Pageable pageable) {
        return jpaRepository.findByAccountId(accountId, pageable).map(mapper::toDomain);
    }
}
