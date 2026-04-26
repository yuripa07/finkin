package com.finkin.application.service.transfer.validator;

import com.finkin.domain.model.account.Account;
import com.finkin.domain.model.account.Money;
import com.finkin.domain.model.customer.Customer;

/**
 * Pattern: Chain of Responsibility — cada validador é um elo da cadeia.
 *
 * Por que Chain of Responsibility aqui:
 * - Cada validação é independente e pode ser adicionada/removida sem alterar outras.
 * - A cadeia é construída no service (ExecuteInternalTransferService) e pode ser
 *   reordenada para otimização (ex: validar KYC antes de consultar saldo).
 * - Em testes, cada validador pode ser testado isoladamente.
 *
 * Alternativa considerada: @Validated do Spring + Bean Validation no service.
 * Rejeitado porque: regras de negócio (limite diário com horário BCB, KYC) não
 * se encaixam bem em anotações genéricas de validação.
 */
public interface TransactionValidator {

    void validate(Account source, Account target, Customer sourceOwner, Money amount);
}
