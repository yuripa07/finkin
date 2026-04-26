-- ============================================================
-- V5 — Tabela de chaves Pix
--
-- Uma conta pode ter múltiplas chaves de tipos diferentes.
-- key_value é UNIQUE globalmente (uma chave não pode apontar para 2 contas).
-- ============================================================

CREATE TABLE pix_keys (
    id          UUID        PRIMARY KEY,
    account_id  UUID        NOT NULL REFERENCES accounts(id),
    key_type    VARCHAR(20) NOT NULL,
    key_value   TEXT        NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_pix_key_type CHECK (key_type IN ('CPF', 'EMAIL', 'PHONE', 'RANDOM'))
);

CREATE INDEX idx_pix_keys_account_id ON pix_keys (account_id);
CREATE INDEX idx_pix_keys_key_value ON pix_keys (key_value);
