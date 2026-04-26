-- ============================================================
-- V6 — Dados de seed para ambiente dev (profile: dev)
-- IMPORTANTE: Este script só deve rodar em dev/test — nunca em produção
-- ============================================================

-- Customer 1: Alice (KYC aprovado, conta corrente com R$10.000)
INSERT INTO customers (id, cpf, full_name, birth_date, email, phone, kyc_status, created_at, updated_at)
VALUES (
    'a0000001-0000-0000-0000-000000000001',
    '52998224725',
    'Alice Financeira',
    '1990-01-15',
    'alice@finkin.dev',
    '+5511999990001',
    'APPROVED',
    NOW(), NOW()
);

INSERT INTO auth_credentials (id, customer_id, email, password_hash, created_at)
VALUES (
    'c0000001-0000-0000-0000-000000000001',
    'a0000001-0000-0000-0000-000000000001',
    'alice@finkin.dev',
    -- Senha: "Alice123" hasheada com BCrypt custo 12
    -- Para regenerar: python3 -c "import bcrypt; print(bcrypt.hashpw(b'Alice123', bcrypt.gensalt(12)).decode())"
    '$2b$12$znv97WPSKS5qZt2sDp260ePYomvTq8IXi46B17EcNhfxvVPCnMIkm',
    NOW()
);

INSERT INTO accounts (id, customer_id, agency, account_number, account_number_dv, type, status, balance, daily_limit_day, daily_limit_night, created_at, updated_at)
VALUES (
    'b0000001-0000-0000-0000-000000000001',
    'a0000001-0000-0000-0000-000000000001',
    '0001', '100001', 7,   -- DV calculado pelo algoritmo módulo 10
    'CORRENTE', 'ATIVA',
    10000.00, 5000.00, 1000.00,
    NOW(), NOW()
);

INSERT INTO pix_keys (id, account_id, key_type, key_value, created_at)
VALUES (
    'f0000001-0000-0000-0000-000000000001',
    'b0000001-0000-0000-0000-000000000001',
    'CPF', '52998224725', NOW()
),
(
    'f0000002-0000-0000-0000-000000000002',
    'b0000001-0000-0000-0000-000000000001',
    'EMAIL', 'alice@finkin.dev', NOW()
);

-- Customer 2: Bob (KYC aprovado, conta corrente com R$5.000)
INSERT INTO customers (id, cpf, full_name, birth_date, email, phone, kyc_status, created_at, updated_at)
VALUES (
    'a0000002-0000-0000-0000-000000000002',
    '11144477735',
    'Bob Desenvolvedor',
    '1985-06-20',
    'bob@finkin.dev',
    '+5521988880002',
    'APPROVED',
    NOW(), NOW()
);

INSERT INTO auth_credentials (id, customer_id, email, password_hash, created_at)
VALUES (
    'c0000002-0000-0000-0000-000000000002',
    'a0000002-0000-0000-0000-000000000002',
    'bob@finkin.dev',
    -- Senha: "Bob12345" hasheada com BCrypt custo 12
    '$2b$12$UPhBjq2QWG0F/idW2lhphOnbGCH4bXk6K2YbawCbI2Sz.tQ.8234q',
    NOW()
);

INSERT INTO accounts (id, customer_id, agency, account_number, account_number_dv, type, status, balance, daily_limit_day, daily_limit_night, created_at, updated_at)
VALUES (
    'b0000002-0000-0000-0000-000000000002',
    'a0000002-0000-0000-0000-000000000002',
    '0001', '200002', 4,
    'CORRENTE', 'ATIVA',
    5000.00, 5000.00, 1000.00,
    NOW(), NOW()
);

INSERT INTO pix_keys (id, account_id, key_type, key_value, created_at)
VALUES (
    'f0000003-0000-0000-0000-000000000003',
    'b0000002-0000-0000-0000-000000000002',
    'CPF', '11144477735', NOW()
),
(
    'f0000004-0000-0000-0000-000000000004',
    'b0000002-0000-0000-0000-000000000002',
    'PHONE', '+5521988880002', NOW()
);
