package com.finkin.application.service.account;

import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.model.account.Money;
import com.finkin.domain.port.in.GetBalanceUseCase;
import com.finkin.domain.port.out.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetBalanceService implements GetBalanceUseCase {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public Money getBalance(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(accountId))
            .getBalance();
    }
}
