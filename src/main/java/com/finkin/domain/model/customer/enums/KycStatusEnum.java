package com.finkin.domain.model.customer.enums;

/**
 * Status do processo KYC (Know Your CustomerModel).
 *
 * PENDING: documentos ainda não revisados (estado inicial)
 * APPROVED: KYC aprovado — conta pode enviar e receber
 * REJECTED: KYC reprovado — conta bloqueada para envio e recebimento
 *
 * Em dev (finkin.kyc.auto-approve=true): PENDING → APPROVED automaticamente no cadastro.
 * Em produção (fase 2): integração com bureau de crédito e revisão manual.
 */
public enum KycStatusEnum {
    PENDING,
    APPROVED,
    REJECTED
}
