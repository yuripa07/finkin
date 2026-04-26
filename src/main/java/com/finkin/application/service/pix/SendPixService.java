package com.finkin.application.service.pix;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finkin.application.service.transfer.TransactionCompletedEvent;
import com.finkin.application.service.transfer.validator.*;
import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.exception.CustomerNotFoundException;
import com.finkin.domain.exception.PixKeyNotFoundException;
import com.finkin.domain.model.account.MoneyModel;
import com.finkin.domain.model.transaction.*;
import com.finkin.domain.port.in.ISendPixUseCase;
import com.finkin.domain.port.out.*;
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
public class SendPixService implements ISendPixUseCase {

    private final IAccountRepository accountRepository;
    private final ICustomerRepository customerRepository;
    private final IPixKeyRepository pixKeyRepository;
    private final ITransactionRepository transactionRepository;
    private final IIdempotencyStore idempotencyStore;
    private final IDomainEventPublisher eventPublisher;
    private final IExternalPixGateway externalPixGateway;
    private final ObjectMapper objectMapper;

    private final KycValidator kycValidator;
    private final AccountStatusValidator accountStatusValidator;
    private final SufficientBalanceValidator balanceValidator;
    private final DailyLimitValidator limitValidator;

    @Override
    @Transactional
    public TransactionModel send(Command command) {
        // Idempotência
        var cached = idempotencyStore.get(command.idempotencyKey());
        if (cached.isPresent()) {
            return deserialize(cached.get());
        }

        var source = accountRepository.findById(command.sourceAccountId())
            .orElseThrow(() -> new AccountNotFoundException(command.sourceAccountId()));
        var sourceOwner = customerRepository.findById(source.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(source.getCustomerId()));
        var amount = MoneyModel.of(command.amount());

        // Resolver a chave Pix para encontrar a conta destino
        var pixKey = pixKeyRepository.findByKeyValue(command.targetKeyValue())
            .orElseThrow(() -> new PixKeyNotFoundException(command.targetKeyValue()));

        var target = accountRepository.findById(pixKey.getAccountId())
            .orElseThrow(() -> new AccountNotFoundException(pixKey.getAccountId()));

        // Validações (mesmo chain da transferência interna)
        List.of(kycValidator, accountStatusValidator, balanceValidator, limitValidator)
            .forEach(v -> v.validate(source, target, sourceOwner, amount));

        var now = ZonedDateTime.now();
        var endToEndId = EndToEndIdModel.generate();
        var txEnvio = buildTransaction(command.idempotencyKey(), TransactionType.PIX_ENVIO,
            command.sourceAccountId(), target.getId(), amount, endToEndId, now);
        txEnvio.markProcessing();

        source.debit(amount);
        target.credit(amount);
        accountRepository.save(source);
        accountRepository.save(target);
        txEnvio.complete();

        // Para Pix interno Finkin, gera também a transação de recebimento na conta destino
        var txRecebimento = buildTransaction(
            UUID.randomUUID().toString(), TransactionType.PIX_RECEBIMENTO,
            command.sourceAccountId(), target.getId(), amount, endToEndId, now);
        txRecebimento.markProcessing();
        txRecebimento.complete();

        var savedEnvio = transactionRepository.save(txEnvio);
        transactionRepository.save(txRecebimento);

        idempotencyStore.store(command.idempotencyKey(), serialize(savedEnvio));
        eventPublisher.publish(new TransactionCompletedEvent(savedEnvio));

        log.info("Pix interno concluído: e2eId={}, valor={}", endToEndId, amount);
        return savedEnvio;
    }

    private TransactionModel buildTransaction(String idemKey, TransactionType type,
                                          UUID sourceId, UUID targetId, MoneyModel amount,
                                          EndToEndIdModel e2eId, ZonedDateTime now) {
        return TransactionModel.builder()
            .id(UUID.randomUUID())
            .idempotencyKey(idemKey)
            .type(type)
            .status(TransactionStatus.PENDENTE)
            .sourceAccountId(sourceId)
            .targetAccountId(targetId)
            .amount(amount)
            .endToEndId(e2eId)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    private String serialize(TransactionModel tx) {
        try { return objectMapper.writeValueAsString(tx); }
        catch (JsonProcessingException e) { return "{}"; }
    }

    private TransactionModel deserialize(String json) {
        try { return objectMapper.readValue(json, TransactionModel.class); }
        catch (Exception e) { throw new RuntimeException("Falha ao recuperar idempotência Pix", e); }
    }
}
