-- ============================================================
-- V8 — Corrige dígitos verificadores das contas do seed dev
-- Algoritmo Módulo 10 aplicado aos números 100001 e 200002.
-- ============================================================

UPDATE accounts SET account_number_dv = 7 WHERE account_number = '100001';
-- 100001 → Módulo 10 → DV = 7

UPDATE accounts SET account_number_dv = 4 WHERE account_number = '200002';
-- 200002 → Módulo 10 → DV = 4
