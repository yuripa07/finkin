package com.finkin.infrastructure.adapter.out.persistence.customer;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * JPA Entity separada da domain entity CustomerModel.
 * Motivo: manter o domain puro (sem @Entity, sem @Column etc.).
 *
 * Soft delete:
 * - @SQLDelete: substitui o DELETE físico por UPDATE set deleted_at = now()
 * - @SQLRestriction: adiciona WHERE deleted_at IS NULL em todo SELECT
 *   (Hibernate 7 — substitui @Where que foi removido)
 */
@Entity
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE customers SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    // CITEXT no Postgres: case-insensitive sem necessidade de LOWER() nas queries
    @Column(nullable = false, unique = true, columnDefinition = "text")
    private String cpf;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "kyc_status", nullable = false)
    private String kycStatus;

    // ── Auditoria automática via Spring Data JPA ──────────────────────────
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private ZonedDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at", columnDefinition = "timestamptz")
    private ZonedDateTime deletedAt;
}
