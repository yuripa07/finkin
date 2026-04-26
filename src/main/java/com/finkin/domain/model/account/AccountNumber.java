package com.finkin.domain.model.account;

import lombok.Value;

/**
 * Value Object que representa o número de conta no formato XXXXXX-D.
 *
 * O dígito verificador (D) é calculado pelo algoritmo Módulo 10 (Luhn-like),
 * amplamente usado por bancos brasileiros (Banco do Brasil, Itaú, Bradesco)
 * para validar números de conta.
 *
 * Algoritmo Módulo 10:
 * 1. Multiplica os dígitos alternadamente por 2 e 1, da direita para esquerda.
 * 2. Soma os dígitos individuais dos produtos (ex: 16 → 1+6=7).
 * 3. Subtrai o total do próximo múltiplo de 10.
 * 4. Se o resultado for 10, o DV é 0.
 */
@Value
public class AccountNumber {

    String number;   // 6 dígitos
    int checkDigit;  // 0–9

    public AccountNumber(String number, int checkDigit) {
        if (number == null || !number.matches("\\d{6}")) {
            throw new IllegalArgumentException("Número de conta deve ter 6 dígitos: " + number);
        }
        int expected = calculateCheckDigit(number);
        if (expected != checkDigit) {
            throw new IllegalArgumentException(
                "Dígito verificador inválido para conta %s: esperado %d, recebido %d"
                    .formatted(number, expected, checkDigit));
        }
        this.number = number;
        this.checkDigit = checkDigit;
    }

    /** Formata no padrão exibido ao cliente: XXXXXX-D */
    public String formatted() {
        return "%s-%d".formatted(number, checkDigit);
    }

    /**
     * Calcula o dígito verificador pelo algoritmo Módulo 10.
     * Usado pelo AccountNumberGenerator na criação de novas contas.
     */
    public static int calculateCheckDigit(String sixDigits) {
        int sum = 0;
        int multiplier = 2; // começa com 2, alternando 2-1 da direita para a esquerda
        for (int i = sixDigits.length() - 1; i >= 0; i--) {
            int product = Character.getNumericValue(sixDigits.charAt(i)) * multiplier;
            // Soma os dígitos individuais do produto (ex: 16 → 1+6=7)
            sum += (product >= 10) ? (product / 10 + product % 10) : product;
            multiplier = (multiplier == 2) ? 1 : 2;
        }
        int remainder = sum % 10;
        return (remainder == 0) ? 0 : (10 - remainder);
    }

    @Override
    public String toString() {
        return formatted();
    }
}
