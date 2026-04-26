package com.finkin.domain.model.account;

import java.math.BigDecimal;

/**
 * Pattern: Strategy — define uma família de algoritmos de limite de transferência,
 * encapsula cada um, e os torna intercambiáveis.
 *
 * Por que Strategy aqui: o limite de transferência muda com base no horário
 * (Resolução BCB nº 1/2020, Art. 20). Em vez de um if/else no service,
 * cada política é um objeto independente — facilitando adicionar novas
 * políticas futuras (ex: limite diferenciado por tipo de conta, por KYC level).
 */
public interface LimitPolicy {

    /**
     * Retorna o limite máximo de transferência para este período.
     * Pode ser sobrescrito por configuração por conta (dailyLimitDay/Night na Account).
     */
    BigDecimal getLimit(Account account);

    /** Nome descritivo do período para mensagens de erro. */
    String periodName();
}
