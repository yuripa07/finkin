package com.finkin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurações do processo KYC (Know Your CustomerModel).
 * Em dev/test: auto-approve=true para facilitar testes de fluxo completo.
 * Em produção: auto-approve=false — KYC real (fase 2) exigirá integração com bureau.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "finkin.kyc")
public class KycProperties {

    private boolean autoApprove = false;
}
