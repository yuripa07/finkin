package com.finkin.infrastructure.adapter.input.web.common;

import com.finkin.domain.exception.AccountBlockedException;
import com.finkin.domain.exception.AccountNotFoundException;
import com.finkin.domain.exception.CustomerAlreadyExistsException;
import com.finkin.domain.exception.CustomerNotFoundException;
import com.finkin.domain.exception.DailyLimitExceededException;
import com.finkin.domain.exception.DomainException;
import com.finkin.domain.exception.IdempotencyConflictException;
import com.finkin.domain.exception.InsufficientBalanceException;
import com.finkin.domain.exception.InvalidPixKeyException;
import com.finkin.domain.exception.KycNotApprovedException;
import com.finkin.domain.exception.PixKeyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler global de exceções seguindo RFC 7807 (Problem Details for HTTP APIs).
 * O Spring 6+ oferece ProblemDetail nativamente — sem dependência externa necessária.
 *
 * Por que ProblemDetail: formato padronizado e amplamente adotado em APIs bancárias
 * (OpenBanking Brasil e Open Finance BR adotam estrutura similar).
 *
 * Campos do ProblemDetail:
 *   - type: URI que identifica o tipo do problema (prefixo /problems/ é convencional)
 *   - title: descrição curta e legível
 *   - status: código HTTP
 *   - detail: descrição específica da instância do erro
 *   - instance: URI da requisição que originou o erro
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String PROBLEMS_BASE = "https://finkin.dev/problems/";

    // ── Erros de negócio (domínio) ─────────────────────────────────────────

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ProblemDetail handleCustomerAlreadyExists(CustomerAlreadyExistsException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, "customer-already-exists", ex.getMessage(), req);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFound(CustomerNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, "customer-not-found", ex.getMessage(), req);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, "account-not-found", ex.getMessage(), req);
    }

    @ExceptionHandler(AccountBlockedException.class)
    public ProblemDetail handleAccountBlocked(AccountBlockedException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "account-blocked", ex.getMessage(), req);
    }

    @ExceptionHandler(KycNotApprovedException.class)
    public ProblemDetail handleKycNotApproved(KycNotApprovedException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "kyc-not-approved", ex.getMessage(), req);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ProblemDetail handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "insufficient-balance", ex.getMessage(), req);
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    public ProblemDetail handleDailyLimit(DailyLimitExceededException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "daily-limit-exceeded", ex.getMessage(), req);
    }

    @ExceptionHandler(PixKeyNotFoundException.class)
    public ProblemDetail handlePixKeyNotFound(PixKeyNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, "pix-key-not-found", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidPixKeyException.class)
    public ProblemDetail handleInvalidPixKey(InvalidPixKeyException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-pix-key", ex.getMessage(), req);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ProblemDetail handleIdempotencyConflict(IdempotencyConflictException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, "idempotency-conflict", ex.getMessage(), req);
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex, HttpServletRequest req) {
        log.warn("Unhandled domain exception: {}", ex.getMessage());
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "domain-error", ex.getMessage(), req);
    }

    // ── Erros de validação de entrada ──────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> violations = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                f -> f.getDefaultMessage() == null ? "invalid" : f.getDefaultMessage(),
                (a, b) -> a  // em caso de múltiplos erros no mesmo campo, manter o primeiro
            ));

        var problem = problem(HttpStatus.BAD_REQUEST, "validation-error",
            "Um ou mais campos possuem valores inválidos.", req);
        problem.setProperty("violations", violations);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "validation-error", ex.getMessage(), req);
    }

    // ── Erros de segurança ─────────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        // Intencionalmente genérico: não revelar se o problema é usuário ou senha
        return problem(HttpStatus.UNAUTHORIZED, "invalid-credentials",
            "Credenciais inválidas.", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "access-denied",
            "Acesso negado a este recurso.", req);
    }

    // ── Fallback ───────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error processing request to {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
            "Ocorreu um erro interno. Tente novamente.", req);
    }

    // ── Utilitário ─────────────────────────────────────────────────────────

    private ProblemDetail problem(HttpStatus status, String type, String detail, HttpServletRequest req) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEMS_BASE + type));
        problem.setInstance(URI.create(req.getRequestURI()));
        return problem;
    }
}
