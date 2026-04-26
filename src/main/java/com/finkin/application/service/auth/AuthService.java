package com.finkin.application.service.auth;

import com.finkin.domain.exception.CustomerNotFoundException;
import com.finkin.domain.port.input.IAuthenticateUseCase;
import com.finkin.domain.port.input.IRegisterUserUseCase;
import com.finkin.domain.port.output.IAccountRepository;
import com.finkin.domain.port.output.IAuthCredentialsRepository;
import com.finkin.domain.port.output.ICustomerRepository;
import com.finkin.infrastructure.adapter.input.web.auth.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IRegisterUserUseCase, IAuthenticateUseCase {

    private final IAuthCredentialsRepository credentialsRepository;
    private final ICustomerRepository customerRepository;
    private final IAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void register(IRegisterUserUseCase.Command command) {
        // Confirmar que o customer existe antes de criar credenciais
        customerRepository.findById(command.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        String hashed = passwordEncoder.encode(command.rawPassword());
        credentialsRepository.save(command.customerId(), command.email(), hashed);
        log.info("Credenciais criadas para customer={}", command.customerId());
    }

    @Override
    public String authenticate(IAuthenticateUseCase.Command command) {
        var credentials = credentialsRepository.findByEmail(command.email())
            .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(command.rawPassword(), credentials.hashedPassword())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        // Busca IDs de contas para incluir no JWT (evita lookups extras por requisição)
        var accountIds = accountRepository.findByCustomerId(credentials.customerId())
            .stream()
            .map(a -> a.getId())
            .toList();

        // KYC status do customer para autorizar ou bloquear operações de envio
        var customer = customerRepository.findById(credentials.customerId())
            .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        String token = jwtService.generateToken(
            credentials.customerId(),
            customer.getKycStatus().name(),
            accountIds
        );

        log.info("Login realizado: customer={}", credentials.customerId());
        return token;
    }
}
