package com.finkin.application.service.pix;

import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.model.transaction.TransactionModel;
import com.finkin.domain.port.in.IIssueReceiptUseCase;
import com.finkin.domain.port.out.ITransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IssueReceiptService implements IIssueReceiptUseCase {

    private final ITransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public TransactionModel getReceipt(UUID transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new AccountNotFoundException(transactionId)); // reutiliza exceção similar
    }
}
