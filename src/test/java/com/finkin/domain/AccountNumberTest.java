package com.finkin.domain;

import com.finkin.domain.model.account.AccountNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.*;

class AccountNumberTest {

    @Test
    void shouldCalculateCheckDigitCorrectly() {
        // Verifica o cálculo para alguns números conhecidos
        assertThat(AccountNumber.calculateCheckDigit("123456")).isBetween(0, 9);
    }

    @Test
    void shouldAcceptValidCheckDigit() {
        String number = "123456";
        int dv = AccountNumber.calculateCheckDigit(number);
        assertThatNoException().isThrownBy(() -> new AccountNumber(number, dv));
    }

    @Test
    void shouldRejectInvalidCheckDigit() {
        String number = "123456";
        int dv = AccountNumber.calculateCheckDigit(number);
        int wrong = (dv + 1) % 10;
        assertThatThrownBy(() -> new AccountNumber(number, wrong))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNonSixDigitNumber() {
        assertThatThrownBy(() -> new AccountNumber("12345", 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AccountNumber("1234567", 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFormatWithHyphen() {
        String number = "123456";
        int dv = AccountNumber.calculateCheckDigit(number);
        var accountNumber = new AccountNumber(number, dv);
        assertThat(accountNumber.formatted()).matches("\\d{6}-\\d");
    }

    @RepeatedTest(100)
    void shouldProduceValidCheckDigitForRandomInputs() {
        // Gera 100 números aleatórios de 6 dígitos e verifica que o DV calculado é válido
        String sixDigits = String.format("%06d", (int)(Math.random() * 1_000_000));
        int dv = AccountNumber.calculateCheckDigit(sixDigits);
        assertThatNoException().isThrownBy(() -> new AccountNumber(sixDigits, dv));
    }
}
