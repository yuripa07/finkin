package com.finkin.domain.model.transaction;

/**
 * Ciclo de vida de uma transação bancária.
 *
 * Regra de imutabilidade: transações CONCLUIDAS e REVERTIDAS não podem
 * mudar de status — o histórico é preservado para auditoria regulatória.
 *
 * Transições:
 * PENDENTE → PROCESSANDO → CONCLUIDA
 *                        → FALHA
 * CONCLUIDA → REVERTIDA (apenas via nova transação PIX_DEVOLUCAO, não alteração deste registro)
 */
public enum TransactionStatus {
    PENDENTE,
    PROCESSANDO,
    CONCLUIDA,
    FALHA,
    REVERTIDA
}
