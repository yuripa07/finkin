package com.finkin.domain.model.account;

/**
 * Tipos de conta suportados pelo Finkin Bank.
 * Baseado na Resolução CMN nº 4.753/2019 e na Resolução BCB nº 80/2021
 * que definem as modalidades de contas para pessoa física.
 */
public enum AccountType {
    /** Conta Corrente: movimentação livre, sem limite de saques. */
    CORRENTE,
    /** Conta Poupança: remunerada pela TR + 0,5% a.m. (simulado, sem rendimento real). */
    POUPANCA,
    /** Conta de Pagamento: para recebimento de pagamentos e Pix. */
    PAGAMENTO
}
