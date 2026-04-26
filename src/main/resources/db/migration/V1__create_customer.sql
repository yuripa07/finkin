-- ============================================================
-- V1 — Tabela de customers (titulares)
-- ============================================================
-- CITEXT: case-insensitive text — consultas por CPF/email sem LOWER()
-- timestamptz: timestamp WITH timezone — armazena UTC, exibe em SP
-- ============================================================

CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE customers (
    id          UUID        PRIMARY KEY,
    cpf         CITEXT      NOT NULL UNIQUE,
    full_name   TEXT        NOT NULL,
    birth_date  DATE        NOT NULL,
    email       CITEXT      NOT NULL UNIQUE,
    phone       TEXT        NOT NULL,
    kyc_status  TEXT        NOT NULL DEFAULT 'PENDING',

    -- Auditoria automática (Spring Data JPA Auditing)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by  TEXT,
    updated_by  TEXT,

    -- Soft delete: NULL = ativo, preenchido = excluído (preserva histórico)
    deleted_at  TIMESTAMPTZ,

    CONSTRAINT chk_kyc_status CHECK (kyc_status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_customers_cpf ON customers (cpf) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_email ON customers (email) WHERE deleted_at IS NULL;
