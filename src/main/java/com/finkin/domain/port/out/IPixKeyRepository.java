package com.finkin.domain.port.out;

import com.finkin.domain.model.pix.PixKeyModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPixKeyRepository {
    PixKeyModel save(PixKeyModel pixKey);
    Optional<PixKeyModel> findByKeyValue(String keyValue);
    List<PixKeyModel> findByAccountId(UUID accountId);
    boolean existsByKeyValue(String keyValue);
}
