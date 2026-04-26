package com.finkin.domain.model.account;

/**
 * Status do ciclo de vida de uma conta bancária.
 *
 * Transições permitidas:
 * ATIVA → INATIVA (inatividade prolongada, sem movimentação)
 * ATIVA → BLOQUEADA (suspeita de fraude, ordem judicial)
 * ATIVA → ENCERRADA (solicitação do titular)
 * INATIVA → ATIVA (reativação pelo titular)
 * BLOQUEADA → ATIVA (liberação pela instituição)
 *
 * ENCERRADA é terminal — não pode ser revertida.
 */
public enum AccountStatus {
    ATIVA,
    INATIVA,
    BLOQUEADA,
    ENCERRADA
}
