package com.finkin.domain.model.customer;

import com.finkin.domain.exception.DomainException;
import lombok.Value;

/**
 * Value Object que representa um CPF válido.
 *
 * O CPF (Cadastro de Pessoas Físicas) é composto por 11 dígitos, sendo os
 * dois últimos os "dígitos verificadores" calculados pelo algoritmo oficial
 * da Receita Federal do Brasil.
 *
 * Algoritmo dos dois dígitos verificadores:
 * 1. Multiplica os 9 primeiros dígitos pelos pesos 10, 9, 8, ..., 2 (1º DV)
 *    ou 11, 10, 9, ..., 2 (2º DV).
 * 2. Soma os produtos e divide o resultado por 11.
 * 3. Se resto < 2, DV = 0; caso contrário, DV = 11 - resto.
 *
 * CPFs com todos os dígitos iguais (111.111.111-11) passam na checagem de
 * comprimento, mas são considerados inválidos — tratados explicitamente.
 *
 * Implementação sem dependências externas para manter o domínio puro.
 */
@Value
public class Cpf {

    String value;

    public Cpf(String raw) {
        String normalized = normalize(raw);
        if (!isValid(normalized)) {
            throw new InvalidCpfException(raw);
        }
        this.value = normalized;
    }

    /** Retorna CPF no formato XXX.XXX.XXX-DV para exibição ao usuário. */
    public String formatted() {
        return "%s.%s.%s-%s".formatted(
            value.substring(0, 3),
            value.substring(3, 6),
            value.substring(6, 9),
            value.substring(9, 11)
        );
    }

    /** Remove pontuação e espaços, mantém apenas dígitos. */
    private static String normalize(String cpf) {
        if (cpf == null) return "";
        return cpf.replaceAll("[^0-9]", "");
    }

    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.length() != 11) return false;
        // CPFs com todos os dígitos iguais são inválidos (ex: 000.000.000-00)
        if (cpf.chars().distinct().count() == 1) return false;
        return checkDigit(cpf, 9) && checkDigit(cpf, 10);
    }

    /**
     * Calcula e verifica o dígito verificador na posição {@code position} (9 ou 10).
     * O peso inicial é (position + 1) e decrementa até 2.
     */
    private static boolean checkDigit(String cpf, int position) {
        int sum = 0;
        for (int i = 0; i < position; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (position + 1 - i);
        }
        int remainder = sum % 11;
        int expected = (remainder < 2) ? 0 : (11 - remainder);
        return expected == Character.getNumericValue(cpf.charAt(position));
    }

    public static class InvalidCpfException extends DomainException {
        public InvalidCpfException(String cpf) {
            super("CPF inválido: [mascarado]");
        }
    }
}
