package com.finkin.domain;

import com.finkin.domain.model.account.AccountNumberModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.*;

class AccountNumberModelTest {

    @Test
    void shouldCalculateCheckDigitCorrectly() {
        // Verifica o cálculo para alguns números conhecidos
        assertThat(AccountNumberModel.calculateCheckDigit("123456")).isBetween(0, 9);
    }

    @Test
    void shouldAcceptValidCheckDigit() {
        String number = "123456";
        int dv = AccountNumberModel.calculateCheckDigit(number);
        assertThatNoException().isThrownBy(() -> new AccountNumberModel(number, dv));
    }

    @Test
    void shouldRejectInvalidCheckDigit() {
        String number = "123456";
        int dv = AccountNumberModel.calculateCheckDigit(number);
        int wrong = (dv + 1) % 10;
        assertThatThrownBy(() -> new AccountNumberModel(number, wrong))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNonSixDigitNumber() {
        assertThatThrownBy(() -> new AccountNumberModel("12345", 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AccountNumberModel("1234567", 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFormatWithHyphen() {
        String number = "123456";
        int dv = AccountNumberModel.calculateCheckDigit(number);
        var accountNumber = new AccountNumberModel(number, dv);
        assertThat(accountNumber.formatted()).matches("\\d{6}-\\d");
    }

    @RepeatedTest(100)
    void shouldProduceValidCheckDigitForRandomInputs() {
        // Gera 100 números aleatórios de 6 dígitos e verifica que o DV calculado é válido
        String sixDigits = String.format("%06d", (int)(Math.random() * 1_000_000));
        int dv = AccountNumberModel.calculateCheckDigit(sixDigits);
        assertThatNoException().isThrownBy(() -> new AccountNumberModel(sixDigits, dv));
    }
}
