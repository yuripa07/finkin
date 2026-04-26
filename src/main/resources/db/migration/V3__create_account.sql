-- ============================================================
-- V3 — Tabela de contas bancárias
--
-- Limites referenciados pela Resolução BCB nº 1/2020, Art. 20:
-- - daily_limit_day: limite diurno (06h–20h), padrão R$5.000
-- - daily_limit_night: limite noturno (20h–06h), padrão R$1.000
-- ============================================================

CREATE TABLE accounts (
    id                  UUID            PRIMARY KEY,
    customer_id         UUID            NOT NULL REFERENCES customers(id),
    agency              VARCHAR(4)      NOT NULL DEFAULT '0001',
    account_number      VARCHAR(6)      NOT NULL,
    account_number_dv   SMALLINT        NOT NULL,
    type                VARCHAR(20)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ATIVA',
    balance             NUMERIC(15, 2)  NOT NULL DEFAULT 0.00,
    daily_limit_day     NUMERIC(15, 2)  NOT NULL DEFAULT 5000.00,
    daily_limit_night   NUMERIC(15, 2)  NOT NULL DEFAULT 1000.00,

    -- Auditoria
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          TEXT,
    updated_by          TEXT,

    -- Soft delete
    deleted_at          TIMESTAMPTZ,

    CONSTRAINT uq_account_number UNIQUE (account_number, account_number_dv),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_account_type CHECK (type IN ('CORRENTE', 'POUPANCA', 'PAGAMENTO')),
    CONSTRAINT chk_account_status CHECK (status IN ('ATIVA', 'INATIVA', 'BLOQUEADA', 'ENCERRADA'))
);

CREATE INDEX idx_accounts_customer_id ON accounts (customer_id) WHERE deleted_at IS NULL;
