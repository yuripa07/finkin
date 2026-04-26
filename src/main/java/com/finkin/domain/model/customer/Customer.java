package com.finkin.domain.model.customer;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidade de domínio que representa um titular (Pessoa Física) do Finkin Bank.
 *
 * Deliberadamente sem anotações JPA — o domínio não conhece o mecanismo de
 * persistência. A conversão para/de JPA Entity é responsabilidade dos adapters
 * de persistência (CustomerJpaEntity + CustomerMapper).
 */
@Getter
@Builder
public class Customer {

    private final UUID id;
    private final Cpf cpf;
    private final String fullName;
    private final LocalDate birthDate;
    private final Email email;
    private final Phone phone;

    private KycStatus kycStatus;

    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;

    /** Aprovação de KYC: transiciona status e registra timestamp de atualização. */
    public void approveKyc() {
        this.kycStatus = KycStatus.APPROVED;
        this.updatedAt = ZonedDateTime.now();
    }

    public void rejectKyc() {
        this.kycStatus = KycStatus.REJECTED;
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean isKycApproved() {
        return KycStatus.APPROVED.equals(kycStatus);
    }

    /** Soft delete: marca o customer como excluído sem remover o registro. */
    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
