package com.finkin.domain;

import com.finkin.domain.model.account.DaytimeLimit;
import com.finkin.domain.model.account.LimitPolicy;
import com.finkin.domain.model.account.LimitPolicySelector;
import com.finkin.domain.model.account.NighttimeLimit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class LimitPolicySelectorTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    // Horários diurnos: 06h00 a 19h59 em Brasília
    @ParameterizedTest(name = "{0}h deve ser diurno")
    @CsvSource({"06", "10", "12", "15", "19"})
    void shouldSelectDaytimeFor(String hour) {
        ZonedDateTime time = ZonedDateTime.of(2026, 4, 25, Integer.parseInt(hour), 0, 0, 0, SAO_PAULO);
        LimitPolicy policy = LimitPolicySelector.select(time);
        assertThat(policy).isInstanceOf(DaytimeLimit.class);
    }

    // Horários noturnos: 20h00 a 05h59 em Brasília (Resolução BCB nº 1/2020, Art. 20)
    @ParameterizedTest(name = "{0}h deve ser noturno")
    @CsvSource({"20", "21", "23", "00", "01", "03", "05"})
    void shouldSelectNighttimeFor(String hour) {
        ZonedDateTime time = ZonedDateTime.of(2026, 4, 25, Integer.parseInt(hour), 0, 0, 0, SAO_PAULO);
        LimitPolicy policy = LimitPolicySelector.select(time);
        assertThat(policy).isInstanceOf(NighttimeLimit.class);
    }

    @Test
    void shouldSelectNighttimeAtExactlyTwenty() {
        ZonedDateTime time = ZonedDateTime.of(2026, 4, 25, 20, 0, 0, 0, SAO_PAULO);
        assertThat(LimitPolicySelector.select(time)).isInstanceOf(NighttimeLimit.class);
    }

    @Test
    void shouldSelectDaytimeAtExactlySix() {
        ZonedDateTime time = ZonedDateTime.of(2026, 4, 25, 6, 0, 0, 0, SAO_PAULO);
        assertThat(LimitPolicySelector.select(time)).isInstanceOf(DaytimeLimit.class);
    }
}
