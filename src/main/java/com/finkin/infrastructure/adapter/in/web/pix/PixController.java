package com.finkin.infrastructure.adapter.in.web.pix;

import com.finkin.domain.model.pix.PixKeyType;
import com.finkin.domain.model.transaction.Transaction;
import com.finkin.domain.port.in.IssueReceiptUseCase;
import com.finkin.domain.port.in.SendPixUseCase;
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
@RequestMapping("/pix")
@RequiredArgsConstructor
@Validated
@Tag(name = "Pix", description = "Envio de Pix por chave e comprovante de transação")
@SecurityRequirement(name = "bearerAuth")
public class PixController {

    private final SendPixUseCase sendPixUseCase;
    private final IssueReceiptUseCase issueReceiptUseCase;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Enviar Pix por chave",
        description = "Fase 1: apenas chaves internas do Finkin. Liquidação imediata."
    )
    public PixResponse sendPix(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody PixRequest request) {

        var tx = sendPixUseCase.send(
            new SendPixUseCase.Command(
                idempotencyKey,
                request.sourceAccountId(),
                request.targetKeyType(),
                request.targetKeyValue(),
                request.amount(),
                request.description()
            )
        );

        return toResponse(tx);
    }

    @GetMapping("/transactions/{id}/receipt")
    @Operation(summary = "Comprovante da transação")
    public ReceiptResponse getReceipt(@PathVariable UUID id) {
        var tx = issueReceiptUseCase.getReceipt(id);
        return new ReceiptResponse(
            tx.getId().toString(),
            tx.getType().name(),
            tx.getStatus().name(),
            tx.getAmount().getAmount().toPlainString(),
            "BRL",
            tx.getEndToEndId().getValue(),
            tx.getSourceAccountId() != null ? tx.getSourceAccountId().toString() : null,
            tx.getTargetAccountId() != null ? tx.getTargetAccountId().toString() : null,
            tx.getExecutedAt() != null ? tx.getExecutedAt().toString() : null
        );
    }

    private PixResponse toResponse(Transaction tx) {
        return new PixResponse(
            tx.getId().toString(),
            tx.getStatus().name(),
            tx.getAmount().getAmount().toPlainString(),
            tx.getEndToEndId().getValue(),
            tx.getExecutedAt() != null ? tx.getExecutedAt().toString() : null
        );
    }

    record PixRequest(
        @NotNull UUID sourceAccountId,
        @NotNull PixKeyType targetKeyType,
        @NotBlank String targetKeyValue,
        @NotNull @Positive BigDecimal amount,
        String description
    ) {}

    record PixResponse(String transactionId, String status, String amount,
                       String endToEndId, String executedAt) {}

    record ReceiptResponse(String transactionId, String type, String status,
                           String amount, String currency, String endToEndId,
                           String sourceAccountId, String targetAccountId, String executedAt) {}
}
