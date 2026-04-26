-- ============================================================
-- V4 — Tabela de transações
--
-- Regras de design:
-- - Histórico imutável: sem soft delete, sem DELETE físico permitido
-- - idempotency_key UNIQUE: impede duplo processamento de requisições retransmitidas
-- - end_to_end_id UNIQUE: identificador Bacen único por transação (32 chars)
-- - Índices em (source/target_account_id, executed_at DESC): otimizam o extrato
-- ============================================================

CREATE TABLE transactions (
    id                  UUID            PRIMARY KEY,
    idempotency_key     UUID            NOT NULL UNIQUE,
    type                VARCHAR(30)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    source_account_id   UUID            REFERENCES accounts(id),
    target_account_id   UUID            REFERENCES accounts(id),
    amount              NUMERIC(15, 2)  NOT NULL,
    end_to_end_id       VARCHAR(32)     NOT NULL UNIQUE,
    executed_at         TIMESTAMPTZ,
    failure_reason      TEXT,

    -- Auditoria
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transaction_type CHECK (
        type IN ('TRANSFERENCIA_INTERNA', 'PIX_ENVIO', 'PIX_RECEBIMENTO', 'PIX_DEVOLUCAO')
    ),
    CONSTRAINT chk_transaction_status CHECK (
        status IN ('PENDENTE', 'PROCESSANDO', 'CONCLUIDA', 'FALHA', 'REVERTIDA')
    )
);

-- Índices para extrato eficiente (busca por conta + ordem por data)
CREATE INDEX idx_transactions_source ON transactions (source_account_id, executed_at DESC NULLS LAST);
CREATE INDEX idx_transactions_target ON transactions (target_account_id, executed_at DESC NULLS LAST);
