package com.finkin.application.service.pix;

import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.model.transaction.Transaction;
import com.finkin.domain.port.in.IssueReceiptUseCase;
import com.finkin.domain.port.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IssueReceiptService implements IssueReceiptUseCase {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Transaction getReceipt(UUID transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new AccountNotFoundException(transactionId)); // reutiliza exceção similar
    }
}
