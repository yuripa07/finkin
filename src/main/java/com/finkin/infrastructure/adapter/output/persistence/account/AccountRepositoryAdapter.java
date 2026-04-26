package com.finkin.infrastructure.adapter.output.persistence.account;

import com.finkin.domain.model.account.AccountModel;
import com.finkin.domain.port.output.IAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements IAccountRepository {

    private final IAccountJpaRepository jpaRepository;
    private final IAccountMapper mapper;

    @Override
    public AccountModel save(AccountModel account) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(account)));
    }

    @Override
    public Optional<AccountModel> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AccountModel> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<AccountModel> findByPixKey(String keyValue) {
        return jpaRepository.findByPixKey(keyValue).map(mapper::toDomain);
    }
}
