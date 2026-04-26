package com.finkin.domain.model.customer;
import com.finkin.domain.model.customer.enums.KycStatusEnum;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidade de domínio que representa um titular (Pessoa Física) do Finkin.
 *
 * Deliberadamente sem anotações JPA — o domínio não conhece o mecanismo de
 * persistência. A conversão para/de JPA Entity é responsabilidade dos adapters
 * de persistência (CustomerJpaEntity + ICustomerMapper).
 */
@Getter
@Builder
public class CustomerModel {

    private final UUID id;
    private final CpfModel cpf;
    private final String fullName;
    private final LocalDate birthDate;
    private final EmailModel email;
    private final PhoneModel phone;

    private KycStatusEnum kycStatus;

    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;

    /** Aprovação de KYC: transiciona status e registra timestamp de atualização. */
    public void approveKyc() {
        this.kycStatus = KycStatusEnum.APPROVED;
        this.updatedAt = ZonedDateTime.now();
    }

    public void rejectKyc() {
        this.kycStatus = KycStatusEnum.REJECTED;
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean isKycApproved() {
        return KycStatusEnum.APPROVED.equals(kycStatus);
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
