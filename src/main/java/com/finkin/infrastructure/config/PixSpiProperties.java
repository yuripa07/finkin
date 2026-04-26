package com.finkin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurações do mock do SPI (Sistema de Pagamentos Instantâneos).
 * Em fase 1 o Finkin não integra com o SPI real — toda comunicação é simulada.
 * Em fase 2 este objeto será substituído por um cliente HTTP real (Spring WebClient).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "finkin.pix.spi")
public class PixSpiProperties {

    /** Milissegundos de delay para simular a latência de liquidação no SPI. */
    private long delayMs = 2000;

    /** Probabilidade de falha simulada (0.0 = nunca falha, 1.0 = sempre falha). */
    private double failureRate = 0.0;
}
