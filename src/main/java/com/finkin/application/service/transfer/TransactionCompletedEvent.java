package com.finkin.application.service.transfer;

import com.finkin.domain.model.transaction.Transaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado após uma transação ser concluída com sucesso.
 * Consumidores: NotificationListener (fase 2 — stub), AuditListener (futuro).
 *
 * Pattern: Observer/Event — o TransferService não precisa saber quem reage ao evento.
 */
@Getter
public class TransactionCompletedEvent extends ApplicationEvent {

    private final Transaction transaction;

    public TransactionCompletedEvent(Transaction transaction) {
        super(transaction);
        this.transaction = transaction;
    }
}
