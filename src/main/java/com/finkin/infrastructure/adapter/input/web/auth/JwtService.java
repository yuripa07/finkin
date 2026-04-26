package com.finkin.infrastructure.adapter.input.web.auth;

import com.finkin.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de geração e validação de JWT (JSON Web Tokens).
 *
 * Algoritmo: HMAC-SHA256 (HS256).
 * Por que HS256 e não RS256: em fase 1, serviço único sem necessidade de verificação
 * por terceiros. Em fase 2 (múltiplos serviços ou exposição ao Open Finance),
 * migrar para RS256 com JWK Set público.
 *
 * Claims personalizados:
 * - sub: customerId (UUID)
 * - kyc: status KYC do customer
 * - accountIds: lista de IDs de contas do customer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;

    /** Gera um JWT assinado com os claims do customer. */
    public String generateToken(UUID customerId, String kycStatus, List<UUID> accountIds) {
        SecretKey key = signingKey();
        Instant now = Instant.now();

        return Jwts.builder()
            .subject(customerId.toString())
            .claims(Map.of(
                "kyc", kycStatus,
                "accountIds", accountIds.stream().map(UUID::toString).toList()
            ))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(properties.getTtlMinutes() * 60L)))
            .signWith(key)
            .compact();
    }

    /**
     * Extrai e valida os claims do token.
     * Retorna Optional.empty() se o token for inválido ou expirado —
     * nunca lança exceção (tratamento no filtro).
     */
    public Optional<Claims> validateAndExtract(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT inválido: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private SecretKey signingKey() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
