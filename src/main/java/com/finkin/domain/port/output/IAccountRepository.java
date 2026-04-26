package com.finkin.domain.port.output;

import com.finkin.domain.model.account.AccountModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountRepository {
    AccountModel save(AccountModel account);
    Optional<AccountModel> findById(UUID id);
    List<AccountModel> findByCustomerId(UUID customerId);
    Optional<AccountModel> findByPixKey(String keyValue);
}
