package com.finkin.infrastructure.adapter.in.web.auth;

import com.finkin.domain.port.in.AuthenticateUseCase;
import com.finkin.domain.port.in.RegisterCustomerUseCase;
import com.finkin.domain.port.in.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Autenticação", description = "Registro de customer e autenticação JWT")
public class AuthController {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUseCase authenticateUseCase;

    /**
     * Registro unificado: cria o customer + as credenciais de acesso em uma única chamada.
     * Em dev: KYC auto-aprovado, conta já pode ser aberta na sequência.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar novo customer (PF)", description = "Cria customer e credenciais JWT. KYC auto-aprovado em dev.")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        var customer = registerCustomerUseCase.register(
            new RegisterCustomerUseCase.Command(
                request.cpf(), request.fullName(), request.birthDate(),
                request.email(), request.phone()
            )
        );

        registerUserUseCase.register(
            new RegisterUserUseCase.Command(customer.getId(), request.email(), request.password())
        );

        return RegisterResponse.builder()
            .customerId(customer.getId().toString())
            .kycStatus(customer.getKycStatus().name())
            .message("Customer registrado com sucesso. Use /auth/login para obter o token JWT.")
            .build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Retorna JWT para uso nos demais endpoints.")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authenticateUseCase.authenticate(
            new AuthenticateUseCase.Command(request.email(), request.password())
        );
        return new LoginResponse(token, "Bearer");
    }

    // ── DTOs ──────────────────────────────────────────────────────────────

    record RegisterRequest(
        @NotBlank @Pattern(regexp = "[\\d.\\-]{11,14}", message = "CPF inválido")
        String cpf,

        @NotBlank @Size(min = 3, max = 150) String fullName,

        @NotNull LocalDate birthDate,

        @NotBlank @Email String email,

        @NotBlank @Pattern(regexp = "^\\+55\\d{10,11}$", message = "Use E.164: +55XXYYYYYYYYYY")
        String phone,

        @NotBlank @Size(min = 8, max = 100,
            message = "Senha deve ter entre 8 e 100 caracteres")
        @Pattern(regexp = ".*[0-9].*", message = "Senha deve conter ao menos um dígito")
        @Pattern(regexp = ".*[A-Za-z].*", message = "Senha deve conter ao menos uma letra")
        String password
    ) {}

    record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    @Builder
    record RegisterResponse(String customerId, String kycStatus, String message) {}

    record LoginResponse(String token, String tokenType) {}
}
