package com.finkin.infrastructure.adapter.output.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    List<AccountJpaEntity> findByCustomerId(UUID customerId);

    /**
     * Busca uma conta pelo valor de uma chave Pix associada.
     * Join com pix_keys para resolver a chave antes do Pix.
     */
    @Query("""
        SELECT a FROM AccountJpaEntity a
        JOIN PixKeyJpaEntity pk ON pk.accountId = a.id
        WHERE pk.keyValue = :keyValue
        """)
    Optional<AccountJpaEntity> findByPixKey(@Param("keyValue") String keyValue);
}
