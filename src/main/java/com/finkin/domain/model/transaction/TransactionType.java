package com.finkin.domain.model.transaction;

/**
 * Tipos de transação suportados na fase 1.
 * Em fases futuras: BOLETO_PAGAMENTO, CARTAO_CREDITO, INVESTIMENTO_APLICACAO, etc.
 */
public enum TransactionType {
    /** Transferência entre contas do Finkin (TED interna simulada). */
    TRANSFERENCIA_INTERNA,

    /** Envio de Pix originado nesta conta. */
    PIX_ENVIO,

    /** Recebimento de Pix nesta conta. */
    PIX_RECEBIMENTO,

    /**
     * Devolução de Pix (D+90).
     * Gera nova transação — o comprovante original é referenciado pelo endToEndId.
     */
    PIX_DEVOLUCAO
}
