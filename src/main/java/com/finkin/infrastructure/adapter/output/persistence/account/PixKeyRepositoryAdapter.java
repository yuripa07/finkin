package com.finkin.infrastructure.adapter.output.persistence.account;

import com.finkin.domain.model.pix.PixKeyModel;
import com.finkin.domain.port.output.IPixKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PixKeyRepositoryAdapter implements IPixKeyRepository {

    private final IPixKeyJpaRepository jpaRepository;
    private final IPixKeyMapper mapper;

    @Override
    public PixKeyModel save(PixKeyModel pixKey) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(pixKey)));
    }

    @Override
    public Optional<PixKeyModel> findByKeyValue(String keyValue) {
        return jpaRepository.findByKeyValue(keyValue).map(mapper::toDomain);
    }

    @Override
    public List<PixKeyModel> findByAccountId(UUID accountId) {
        return jpaRepository.findByAccountId(accountId).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByKeyValue(String keyValue) {
        return jpaRepository.existsByKeyValue(keyValue);
    }
}
