package com.finkin.application.service.account;

import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.model.transaction.Transaction;
import com.finkin.domain.port.in.GetStatementUseCase;
import com.finkin.domain.port.out.AccountRepository;
import com.finkin.domain.port.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetStatementService implements GetStatementUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getStatement(UUID accountId, Pageable pageable) {
        // Verificar que a conta existe antes de buscar transações
        accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));

        return transactionRepository.findByAccountId(accountId, pageable);
    }
}
