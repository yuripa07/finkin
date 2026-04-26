-- ============================================================
-- V7 — Corrige hashes de senha do seed dev (V6 tinha hashes inválidos)
-- Os hashes anteriores não correspondiam às senhas documentadas.
-- ============================================================

UPDATE auth_credentials
SET password_hash = '$2b$12$znv97WPSKS5qZt2sDp260ePYomvTq8IXi46B17EcNhfxvVPCnMIkm'
WHERE email = 'alice@finkin.dev';
-- Senha: Alice123

UPDATE auth_credentials
SET password_hash = '$2b$12$UPhBjq2QWG0F/idW2lhphOnbGCH4bXk6K2YbawCbI2Sz.tQ.8234q'
WHERE email = 'bob@finkin.dev';
-- Senha: Bob12345
