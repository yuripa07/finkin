package com.finkin.application.service.account;

import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.model.account.MoneyModel;
import com.finkin.domain.port.input.IGetBalanceUseCase;
import com.finkin.domain.port.output.IAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetBalanceService implements IGetBalanceUseCase {

    private final IAccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public MoneyModel getBalance(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId))
            .getBalance();
    }
}
