package com.finkin.application.service.account;

import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.model.transaction.TransactionModel;
import com.finkin.domain.port.input.IGetStatementUseCase;
import com.finkin.domain.port.output.IAccountRepository;
import com.finkin.domain.port.output.ITransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetStatementService implements IGetStatementUseCase {

    private final IAccountRepository accountRepository;
    private final ITransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionModel> getStatement(UUID accountId, Pageable pageable) {
        // Verificar que a conta existe antes de buscar transações
        accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId));

        return transactionRepository.findByAccountId(accountId, pageable);
    }
}
