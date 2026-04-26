package com.finkin.infrastructure.adapter.in.web.transfer;

import com.finkin.domain.model.transaction.Transaction;
import com.finkin.domain.port.in.ExecuteInternalTransferUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Transferências", description = "Transferência interna entre contas Finkin")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final ExecuteInternalTransferUseCase executeTransferUseCase;

    /**
     * Endpoint de transferência interna.
     *
     * Header obrigatório Idempotency-Key: UUID v4 gerado pelo cliente.
     * Reenviando a mesma requisição com a mesma chave em até 24h retorna o resultado original.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Transferência interna",
        description = "Transfere entre contas Finkin. Use Idempotency-Key UUID para evitar duplicatas."
    )
    public TransferResponse transfer(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        var tx = executeTransferUseCase.execute(
            new ExecuteInternalTransferUseCase.Command(
                idempotencyKey,
                request.sourceAccountId(),
                request.targetAccountId(),
                request.amount(),
                request.description()
            )
        );

        return toResponse(tx);
    }

    private TransferResponse toResponse(Transaction tx) {
        return new TransferResponse(
            tx.getId().toString(),
            tx.getStatus().name(),
            tx.getAmount().getAmount().toPlainString(),
            tx.getEndToEndId().getValue(),
            tx.getExecutedAt() != null ? tx.getExecutedAt().toString() : null
        );
    }

    record TransferRequest(
        @NotNull UUID sourceAccountId,
        @NotNull UUID targetAccountId,
        @NotNull @Positive BigDecimal amount,
        String description
    ) {}

    record TransferResponse(
        String transactionId, String status, String amount,
        String endToEndId, String executedAt
    ) {}
}
