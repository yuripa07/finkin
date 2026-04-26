package com.finkin.domain.model.pix;

/**
 * Tipos de chave Pix suportados pelo Finkin (fase 1).
 * Fonte: Manual de Integração do Pix — Bacen (seção DICT).
 */
public enum PixKeyType {
    CPF,
    EMAIL,
    PHONE,
    RANDOM   // Chave aleatória (UUID v4) — gerada pelo servidor
}
