package com.finkin.application.service.transfer;
import com.finkin.domain.model.transaction.enums.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finkin.application.service.transfer.validator.*;
import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.exception.CustomerNotFoundException;
import com.finkin.domain.model.account.MoneyModel;
import com.finkin.domain.model.transaction.*;
import com.finkin.domain.port.input.IExecuteInternalTransferUseCase;
import com.finkin.domain.port.output.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecuteInternalTransferService implements IExecuteInternalTransferUseCase {

    private final IAccountRepository accountRepository;
    private final ICustomerRepository customerRepository;
    private final ITransactionRepository transactionRepository;
    private final IIdempotencyStore idempotencyStore;
    private final IDomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    // Chain of Responsibility: ordem importa — KYC antes de status, status antes de saldo
    private final KycValidator kycValidator;
    private final AccountStatusValidator accountStatusValidator;
    private final SufficientBalanceValidator balanceValidator;
    private final DailyLimitValidator limitValidator;

    @Override
    @Transactional
    public TransactionModel execute(Command command) {
        // ── 1. Idempotência: retorna resultado anterior se já processado ──────
        var cached = idempotencyStore.get(command.idempotencyKey());
        if (cached.isPresent()) {
            log.debug("Idempotency hit para key={}", command.idempotencyKey());
            return deserialize(cached.get());
        }

        // ── 2. Carregar entidades ──────────────────────────────────────────────
        var source = accountRepository.findById(command.sourceAccountId())
            .orElseThrow(() -> new AccountNotFoundException(command.sourceAccountId()));
        var target = accountRepository.findById(command.targetAccountId())
            .orElseThrow(() -> new AccountNotFoundException(command.targetAccountId()));
        var sourceOwner = customerRepository.findById(source.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(source.getCustomerId()));

        var amount = MoneyModel.of(command.amount());

        // ── 3. Validações em cadeia (Chain of Responsibility) ────────────────
        List.of(kycValidator, accountStatusValidator, balanceValidator, limitValidator)
            .forEach(v -> v.validate(source, target, sourceOwner, amount));

        // ── 4. Criar transação PENDENTE ───────────────────────────────────────
        var now = ZonedDateTime.now();
        var tx = TransactionModel.builder()
            .id(UUID.randomUUID())
            .idempotencyKey(command.idempotencyKey())
            .type(TransactionTypeEnum.TRANSFERENCIA_INTERNA)
            .status(TransactionStatusEnum.PENDENTE)
            .sourceAccountId(command.sourceAccountId())
            .targetAccountId(command.targetAccountId())
            .amount(amount)
            .endToEndId(EndToEndIdModel.generate())
            .createdAt(now)
            .updatedAt(now)
            .build();

        tx.markProcessing();

        // ── 5. Débito / Crédito ───────────────────────────────────────────────
        source.debit(amount);
        target.credit(amount);

        accountRepository.save(source);
        accountRepository.save(target);

        tx.complete();
        var saved = transactionRepository.save(tx);

        // ── 6. Armazenar resultado em Redis (idempotência futura) ─────────────
        idempotencyStore.store(command.idempotencyKey(), serialize(saved));

        // ── 7. Publicar evento de domínio ─────────────────────────────────────
        eventPublisher.publish(new TransactionCompletedEvent(saved));

        log.info("Transferência concluída: e2eId={}, valor={}", saved.getEndToEndId(), amount);
        return saved;
    }

    private String serialize(TransactionModel tx) {
        try {
            return objectMapper.writeValueAsString(tx);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar transação para Redis", e);
            return "{}";
        }
    }

    private TransactionModel deserialize(String json) {
        try {
            return objectMapper.readValue(json, TransactionModel.class);
        } catch (Exception e) {
            log.error("Erro ao deserializar transação do Redis", e);
            throw new RuntimeException("Falha ao recuperar resultado de idempotência", e);
        }
    }
}
