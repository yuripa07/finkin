package com.finkin.application.service.account;

import com.finkin.domain.model.account.AccountNumber;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Gera números de conta únicos com dígito verificador pelo algoritmo Módulo 10.
 *
 * Pattern: Factory (parte da criação de Account) — a geração de número é
 * complexa o suficiente para merecer uma classe dedicada em vez de ficar
 * no construtor da Account ou no AccountFactory.
 *
 * Em produção: adicionar verificação de unicidade consultando o banco.
 * A probabilidade de colisão com 6 dígitos (1 milhão de combinações) é
 * aceitável para a fase 1 — ampliar para 8 dígitos se necessário.
 */
@Component
public class AccountNumberGenerator {

    /** Gera um AccountNumber aleatório válido. */
    public AccountNumber generate() {
        String sixDigits = String.format("%06d", ThreadLocalRandom.current().nextInt(1, 1_000_000));
        int dv = AccountNumber.calculateCheckDigit(sixDigits);
        return new AccountNumber(sixDigits, dv);
    }
}
