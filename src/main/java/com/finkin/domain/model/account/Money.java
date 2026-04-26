package com.finkin.domain.model.account;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object que representa um valor monetário em BRL.
 *
 * Por que wrapper e não BigDecimal puro:
 * - Garante escala 2 e arredondamento HALF_EVEN (bancário) em todo o domínio.
 * - Evita operações acidentais sem a escala correta (ex: BigDecimal("1.0") + BigDecimal("2.00")).
 * - HALF_EVEN (arredondamento do banqueiro) minimiza viés acumulado em grandes volumes
 *   de transações — padrão adotado pelo sistema financeiro.
 *
 * Imutável: cada operação retorna um novo Money.
 */
@Value
public class Money {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    BigDecimal amount;

    public Money(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Amount não pode ser nulo");
        this.amount = amount.setScale(SCALE, ROUNDING);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    @Override
    public String toString() {
        return "R$ " + amount.toPlainString();
    }
}
