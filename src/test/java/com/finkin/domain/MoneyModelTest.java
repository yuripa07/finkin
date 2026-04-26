package com.finkin.domain;

import com.finkin.domain.model.account.MoneyModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyModelTest {

    @Test
    void shouldEnforceScale2() {
        var money = MoneyModel.of("10.999");
        // HALF_EVEN: 10.999 → 11.00 (arredondamento do banqueiro)
        assertThat(money.getAmount()).isEqualByComparingTo("11.00");
        assertThat(money.getAmount().scale()).isEqualTo(2);
    }

    @Test
    void shouldAddCorrectly() {
        var result = MoneyModel.of("100.00").add(MoneyModel.of("50.50"));
        assertThat(result.getAmount()).isEqualByComparingTo("150.50");
    }

    @Test
    void shouldSubtractCorrectly() {
        var result = MoneyModel.of("100.00").subtract(MoneyModel.of("30.00"));
        assertThat(result.getAmount()).isEqualByComparingTo("70.00");
    }

    @Test
    void shouldBeImmutable() {
        var original = MoneyModel.of("100.00");
        original.add(MoneyModel.of("50.00"));
        // original não deve mudar — add retorna novo MoneyModel
        assertThat(original.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldDetectNegative() {
        assertThat(MoneyModel.of("-0.01").isNegative()).isTrue();
        assertThat(MoneyModel.zero().isNegative()).isFalse();
    }

    @Test
    void shouldCompareLessThan() {
        assertThat(MoneyModel.of("99.99").isLessThan(MoneyModel.of("100.00"))).isTrue();
        assertThat(MoneyModel.of("100.00").isLessThan(MoneyModel.of("100.00"))).isFalse();
    }

    @Test
    void shouldUseHalfEvenRounding() {
        // HALF_EVEN (arredondamento do banqueiro): quando o dígito descartado é exatamente 5,
        // arredonda para o vizinho par.
        // 2.505 → último dígito mantido é 0 (par) → arredonda PARA BAIXO → 2.50
        assertThat(MoneyModel.of("2.505").getAmount()).isEqualByComparingTo("2.50");
        // 2.515 → último dígito mantido seria 1 (ímpar) → arredonda PARA CIMA → 2.52 (par)
        assertThat(MoneyModel.of("2.515").getAmount()).isEqualByComparingTo("2.52");
    }
}
