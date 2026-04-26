package com.finkin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "finkin.rate-limit")
public class RateLimitProperties {

    /** Requisições por minuto por IP nos endpoints públicos (/auth/**). */
    private int publicPerMinute = 100;

    /** Requisições por minuto por customerId nos endpoints de transação. */
    private int transactionPerMinute = 30;
}
