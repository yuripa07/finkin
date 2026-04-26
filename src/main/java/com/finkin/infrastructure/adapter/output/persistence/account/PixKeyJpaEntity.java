package com.finkin.infrastructure.adapter.output.persistence.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * PixKeyJpaEntity fica no pacote de account porque pix_key pertence funcionalmente
 * à conta (uma pix_key sem conta não faz sentido). Alternativa seria um pacote
 * próprio `pix/` — decisão revisável conforme a fase 2 crescer.
 */
@Entity
@Table(name = "pix_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixKeyJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "account_id", nullable = false, columnDefinition = "uuid")
    private UUID accountId;

    @Column(name = "key_type", nullable = false, length = 20)
    private String keyType;

    @Column(name = "key_value", nullable = false, unique = true)
    private String keyValue;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private ZonedDateTime createdAt;
}
