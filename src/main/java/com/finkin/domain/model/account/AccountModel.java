package com.finkin.domain.model.account;

import com.finkin.domain.exception.AccountBlockedException;
import com.finkin.domain.exception.InsufficientBalanceException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entidade de domínio que representa uma conta bancária do Finkin.
 *
 * Invariantes garantidas por esta classe:
 * - Saldo nunca fica negativo (sem cheque especial na fase 1).
 * - Débito só é permitido em contas ATIVAS.
 *
 * Os limites diários são por conta — o titular pode solicitar alteração
 * (dentro do máximo definido nas LimitsProperties).
 */
@Getter
@Builder
public class AccountModel {

    private final UUID id;
    private final UUID customerId;
    private final String agency;
    private final AccountNumberModel number;
    private final AccountType type;

    private AccountStatus status;
    private MoneyModel balance;

    /**
     * Limite de transferência diurno (06h–20h), configurável por conta.
     * Padrão: R$ 5.000 (finkin.limits.transfer-day-brl).
     * Resolução BCB nº 1/2020.
     */
    private BigDecimal dailyLimitDay;

    /**
     * Limite de transferência noturno (20h–06h), configurável por conta.
     * Padrão: R$ 1.000 (finkin.limits.transfer-night-brl).
     * Resolução BCB nº 1/2020, Art. 20.
     */
    private BigDecimal dailyLimitNight;

    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime deletedAt;

    /**
     * Debita o valor da conta.
     * Lança exceção de domínio se a conta não estiver ativa ou saldo for insuficiente.
     * A validação de limite diário é responsabilidade do DailyLimitValidator (Chain of Responsibility).
     */
    public void debit(MoneyModel amount) {
        assertActive();
        if (balance.isLessThan(amount)) {
            throw new InsufficientBalanceException(balance.getAmount(), amount.getAmount());
        }
        this.balance = balance.subtract(amount);
        this.updatedAt = ZonedDateTime.now();
    }

    /** Credita o valor na conta. Qualquer status pode receber (exceto ENCERRADA — verificar no service). */
    public void credit(MoneyModel amount) {
        this.balance = balance.add(amount);
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean canDebit() {
        return AccountStatus.ATIVA.equals(status);
    }

    private void assertActive() {
        if (!AccountStatus.ATIVA.equals(status)) {
            throw new AccountBlockedException(id);
        }
    }

    public void softDelete() {
        this.deletedAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
        this.status = AccountStatus.ENCERRADA;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
