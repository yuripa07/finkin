-- ============================================================
-- V2 — Credenciais de autenticação
-- Separada de customers para isolar dados de segurança (hash de senha)
-- da tabela principal de negócio. Facilita auditorias de segurança.
-- ============================================================

CREATE TABLE auth_credentials (
    id             UUID        PRIMARY KEY,
    customer_id    UUID        NOT NULL UNIQUE REFERENCES customers(id),
    email          CITEXT      NOT NULL UNIQUE,
    password_hash  TEXT        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auth_credentials_email ON auth_credentials (email);
