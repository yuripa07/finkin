package com.finkin.domain.model.transaction;

import com.finkin.domain.model.account.MoneyModel;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidade de domínio que representa uma transação financeira.
 *
 * Imutabilidade por design: transações não são deletadas, apenas têm
 * seu status atualizado. O histórico completo é preservado para auditoria
 * conforme exigido pela regulação bancária brasileira (Resolução BCB nº 4/2020).
 *
 * O endToEndId é gerado no momento de criação e nunca muda — é a identidade
 * global desta transação no sistema de pagamentos.
 */
@Getter
@Builder
public class TransactionModel {

    private final UUID id;

    /**
     * Chave de idempotência enviada pelo cliente.
     * UUID v4 único por operação do cliente. Armazenado em Redis com TTL 24h
     * para evitar processamento duplicado de requisições retransmitidas.
     */
    private final String idempotencyKey;

    private final TransactionType type;
    private TransactionStatus status;

    /** Conta debitada (origem). Null apenas em PIX_RECEBIMENTO de origem externa. */
    private final UUID sourceAccountId;

    /** Conta creditada (destino). Null em falhas antes de identificar destino. */
    private final UUID targetAccountId;

    private final MoneyModel amount;

    /**
     * Identificador fim-a-fim no formato Bacen.
     * Único por transação — é o "número do comprovante" da perspectiva do Bacen.
     */
    private final EndToEndIdModel endToEndId;

    private ZonedDateTime executedAt;
    private String failureReason;

    // Auditoria
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // ── Transições de estado ───────────────────────────────────────────────

    public void markProcessing() {
        assertTransitionFrom(TransactionStatus.PENDENTE);
        this.status = TransactionStatus.PROCESSANDO;
        this.updatedAt = ZonedDateTime.now();
    }

    public void complete() {
        assertTransitionFrom(TransactionStatus.PROCESSANDO);
        this.status = TransactionStatus.CONCLUIDA;
        this.executedAt = ZonedDateTime.now();
        this.updatedAt = executedAt;
    }

    public void fail(String reason) {
        this.status = TransactionStatus.FALHA;
        this.failureReason = reason;
        this.updatedAt = ZonedDateTime.now();
    }

    /** Marca como revertida (apenas registro — a reversão é uma nova transação PIX_DEVOLUCAO). */
    public void markReverted() {
        assertTransitionFrom(TransactionStatus.CONCLUIDA);
        this.status = TransactionStatus.REVERTIDA;
        this.updatedAt = ZonedDateTime.now();
    }

    private void assertTransitionFrom(TransactionStatus expected) {
        if (!expected.equals(this.status)) {
            throw new IllegalStateException(
                "Transição de estado inválida: %s → não esperado de %s".formatted(expected, this.status));
        }
    }
}
