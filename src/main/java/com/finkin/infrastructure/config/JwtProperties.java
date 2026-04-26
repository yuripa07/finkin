package com.finkin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "finkin.jwt")
public class JwtProperties {

    /** Chave secreta HMAC-SHA256. Em produção: mínimo 256 bits (32 bytes). */
    @NotBlank
    private String secret;

    /** Tempo de vida do token em minutos. */
    @Min(1)
    private int ttlMinutes = 60;
}
