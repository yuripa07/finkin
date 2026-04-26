package com.finkin.application.service.account;

import com.finkin.domain.model.account.AccountNumberModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Gera números de conta únicos com dígito verificador pelo algoritmo Módulo 10.
 *
 * Pattern: Factory (parte da criação de AccountModel) — a geração de número é
 * complexa o suficiente para merecer uma classe dedicada em vez de ficar
 * no construtor da AccountModel ou no AccountFactory.
 *
 * Em produção: adicionar verificação de unicidade consultando o banco.
 * A probabilidade de colisão com 6 dígitos (1 milhão de combinações) é
 * aceitável para a fase 1 — ampliar para 8 dígitos se necessário.
 */
@Component
public class AccountNumberGenerator {

    /** Gera um AccountNumberModel aleatório válido. */
    public AccountNumberModel generate() {
        String sixDigits = String.format("%06d", ThreadLocalRandom.current().nextInt(1, 1_000_000));
        int dv = AccountNumberModel.calculateCheckDigit(sixDigits);
        return new AccountNumberModel(sixDigits, dv);
    }
}
