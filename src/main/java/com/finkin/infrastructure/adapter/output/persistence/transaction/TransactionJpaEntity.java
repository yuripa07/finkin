package com.finkin.infrastructure.adapter.output.persistence.transaction;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions",
    indexes = {
        // Índice para extrato (busca por conta + ordem cronológica decrescente)
        @Index(name = "idx_transactions_source_account_executed_at",
               columnList = "source_account_id, executed_at DESC"),
        @Index(name = "idx_transactions_target_account_executed_at",
               columnList = "target_account_id, executed_at DESC")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "source_account_id", columnDefinition = "uuid")
    private UUID sourceAccountId;

    @Column(name = "target_account_id", columnDefinition = "uuid")
    private UUID targetAccountId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "end_to_end_id", nullable = false, unique = true, length = 32)
    private String endToEndId;

    @Column(name = "executed_at", columnDefinition = "timestamptz")
    private ZonedDateTime executedAt;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private ZonedDateTime updatedAt;
}
