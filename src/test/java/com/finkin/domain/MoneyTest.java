package com.finkin.domain;

import com.finkin.domain.model.account.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldEnforceScale2() {
        var money = Money.of("10.999");
        // HALF_EVEN: 10.999 → 11.00 (arredondamento do banqueiro)
        assertThat(money.getAmount()).isEqualByComparingTo("11.00");
        assertThat(money.getAmount().scale()).isEqualTo(2);
    }

    @Test
    void shouldAddCorrectly() {
        var result = Money.of("100.00").add(Money.of("50.50"));
        assertThat(result.getAmount()).isEqualByComparingTo("150.50");
    }

    @Test
    void shouldSubtractCorrectly() {
        var result = Money.of("100.00").subtract(Money.of("30.00"));
        assertThat(result.getAmount()).isEqualByComparingTo("70.00");
    }

    @Test
    void shouldBeImmutable() {
        var original = Money.of("100.00");
        original.add(Money.of("50.00"));
        // original não deve mudar — add retorna novo Money
        assertThat(original.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldDetectNegative() {
        assertThat(Money.of("-0.01").isNegative()).isTrue();
        assertThat(Money.zero().isNegative()).isFalse();
    }

    @Test
    void shouldCompareLessThan() {
        assertThat(Money.of("99.99").isLessThan(Money.of("100.00"))).isTrue();
        assertThat(Money.of("100.00").isLessThan(Money.of("100.00"))).isFalse();
    }

    @Test
    void shouldUseHalfEvenRounding() {
        // HALF_EVEN (arredondamento do banqueiro): quando o dígito descartado é exatamente 5,
        // arredonda para o vizinho par.
        // 2.505 → último dígito mantido é 0 (par) → arredonda PARA BAIXO → 2.50
        assertThat(Money.of("2.505").getAmount()).isEqualByComparingTo("2.50");
        // 2.515 → último dígito mantido seria 1 (ímpar) → arredonda PARA CIMA → 2.52 (par)
        assertThat(Money.of("2.515").getAmount()).isEqualByComparingTo("2.52");
    }
}
