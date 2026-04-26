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
 * Imutável: cada operação retorna um novo MoneyModel.
 */
@Value
public class MoneyModel {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    BigDecimal amount;

    public MoneyModel(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Amount não pode ser nulo");
        this.amount = amount.setScale(SCALE, ROUNDING);
    }

    public static MoneyModel of(BigDecimal amount) {
        return new MoneyModel(amount);
    }

    public static MoneyModel of(String amount) {
        return new MoneyModel(new BigDecimal(amount));
    }

    public static MoneyModel zero() {
        return new MoneyModel(BigDecimal.ZERO);
    }

    public MoneyModel add(MoneyModel other) {
        return new MoneyModel(this.amount.add(other.amount));
    }

    public MoneyModel subtract(MoneyModel other) {
        return new MoneyModel(this.amount.subtract(other.amount));
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isLessThan(MoneyModel other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(MoneyModel other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    @Override
    public String toString() {
        return "R$ " + amount.toPlainString();
    }
}
