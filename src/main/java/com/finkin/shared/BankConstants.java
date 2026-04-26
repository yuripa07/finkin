package com.finkin.shared;

/**
 * Constantes globais do Finkin.
 * Todas as constantes que referenciam normas do Banco Central incluem
 * a citação da resolução para rastreabilidade regulatória.
 */
public final class BankConstants {

    private BankConstants() {}

    /** ISPB fictício do Finkin (bancos reais têm ISPB de 8 dígitos emitido pelo BCB). */
    public static final String ISPB = "99999999";

    /** Agência padrão única do Finkin na fase 1. */
    public static final String DEFAULT_AGENCY = "0001";

    /**
     * Horário de início do período noturno (inclusive).
     * Fonte: Resolução BCB nº 1/2020, Art. 20 — limites de valor noturno para Pix.
     */
    public static final int NIGHT_HOUR_START = 20;

    /**
     * Horário de fim do período noturno (exclusive — a partir das 6h é diurno).
     * Fonte: Resolução BCB nº 1/2020, Art. 20.
     */
    public static final int NIGHT_HOUR_END = 6;

    /** Timezone oficial brasileiro para todas as operações (evitar offset sem timezone). */
    public static final String TIMEZONE_BR = "America/Sao_Paulo";

    /** Comprimento exato do endToEndId no formato Bacen (E + ISPB8 + datetime14 + rand9 = 32 chars). */
    public static final int END_TO_END_ID_LENGTH = 32;

    /** Agência zerada à esquerda para 4 dígitos. */
    public static final int AGENCY_LENGTH = 4;

    /** Parte numérica do número de conta (6 dígitos antes do dígito verificador). */
    public static final int ACCOUNT_NUMBER_DIGITS = 6;

    /** Prefixo do endToEndId definido pelo Bacen. */
    public static final String END_TO_END_PREFIX = "E";
}
