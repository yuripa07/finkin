package com.finkin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Limites operacionais do Finkin Bank.
 * Baseados na Resolução BCB nº 1/2020 que instituiu o Pix e definiu
 * limites diferenciados para períodos diurno (06h–20h) e noturno (20h–06h).
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "finkin.limits")
public class LimitsProperties {

    @NotNull @Positive
    private BigDecimal transferDayBrl;

    @NotNull @Positive
    private BigDecimal transferNightBrl;

    @NotNull @Positive
    private BigDecimal pixNightBrl;

    private String nightWindowStart = "20:00";
    private String nightWindowEnd   = "06:00";
}
