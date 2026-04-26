package com.finkin.infrastructure.adapter.input.web.account;

import com.finkin.domain.model.account.AccountModel;
import com.finkin.domain.model.account.enums.AccountTypeEnum;
import com.finkin.domain.model.pix.enums.PixKeyTypeEnum;
import com.finkin.domain.model.transaction.TransactionModel;
import com.finkin.domain.port.input.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Contas", description = "Abertura de conta, saldo, extrato e chaves Pix")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final IOpenAccountUseCase openAccountUseCase;
    private final IGetBalanceUseCase getBalanceUseCase;
    private final IGetStatementUseCase getStatementUseCase;
    private final IRegisterPixKeyUseCase registerPixKeyUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abrir conta corrente")
    public AccountResponse openAccount(@Valid @RequestBody OpenAccountRequest request,
                                       Authentication auth) {
        UUID customerId = UUID.fromString(auth.getName());
        var account = openAccountUseCase.open(
            new IOpenAccountUseCase.Command(customerId, request.type())
        );
        return toResponse(account);
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Consultar saldo")
    public BalanceResponse getBalance(@PathVariable UUID id) {
        var balance = getBalanceUseCase.getBalance(id);
        return new BalanceResponse(id.toString(), balance.getAmount().toPlainString(), "BRL");
    }

    @GetMapping("/{id}/statement")
    @Operation(summary = "Extrato paginado", description = "Retorna transações ordenadas por data decrescente.")
    public Page<StatementItem> getStatement(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "executedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return getStatementUseCase.getStatement(id, pageable).map(this::toStatementItem);
    }

    @PostMapping("/{id}/pix-keys")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar chave Pix")
    public PixKeyResponse registerPixKey(@PathVariable UUID id,
                                         @Valid @RequestBody PixKeyRequest request) {
        var key = registerPixKeyUseCase.register(
            new IRegisterPixKeyUseCase.Command(id, request.keyType(), request.keyValue())
        );
        return new PixKeyResponse(key.getId().toString(), key.getKeyType().name(), key.getKeyValue());
    }

    // ── Mappers locais ────────────────────────────────────────────────────

    private AccountResponse toResponse(AccountModel a) {
        return new AccountResponse(
            a.getId().toString(),
            a.getAgency() + "/" + a.getNumber().formatted(),
            a.getNumber().formatted(),
            a.getType().name(),
            a.getStatus().name(),
            a.getBalance().getAmount().toPlainString()
        );
    }

    private StatementItem toStatementItem(TransactionModel t) {
        return new StatementItem(
            t.getId().toString(),
            t.getType().name(),
            t.getStatus().name(),
            t.getAmount().getAmount().toPlainString(),
            t.getEndToEndId().getValue(),
            t.getExecutedAt() != null ? t.getExecutedAt().toString() : null
        );
    }

    // ── DTOs ──────────────────────────────────────────────────────────────

    record OpenAccountRequest(@NotNull AccountTypeEnum type) {}

    record AccountResponse(String id, String fullNumber, String accountNumber,
                           String type, String status, String balance) {}

    record BalanceResponse(String accountId, String balance, String currency) {}

    record StatementItem(String transactionId, String type, String status,
                         String amount, String endToEndId, String executedAt) {}

    record PixKeyRequest(@NotNull PixKeyTypeEnum keyType, String keyValue) {}

    record PixKeyResponse(String id, String keyType, String keyValue) {}
}
