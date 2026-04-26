package com.finkin.domain;

import com.finkin.domain.model.account.DaytimeLimitModel;
import com.finkin.domain.port.input.ILimitPolicy;
import com.finkin.domain.model.account.LimitPolicySelectorModel;
import com.finkin.domain.model.account.NighttimeLimitModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class LimitPolicySelectorModelTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    // Horários diurnos: 06h00 a 19h59 em Brasília
    @ParameterizedTest(name = "{0}h deve ser diurno")
    @CsvSource({"06", "10", "12", "15", "19"})
    void shouldSelectDaytimeFor(String hour) {
        ZonedDateTime time = ZonedDateTime.of(2026, 4, 25, Integer.parseInt(hour), 0, 0, 0, SAO_PAULO);
        ILimitPolicy policy = LimitPolicySelectorModel.select(time);
        assertThat(policy).isInstanceOf(DaytimeLimitModel.class);
    }

    // Horários noturnos: 20h00 a 05h59 em Brasília (Resolução BCB nº 1/2020, Art. 20)
    @ParameterizedTest(name = "{0}h deve ser noturno")
    @CsvSource({"20", "21", "23", "00", "01", "03", "05"})
    void shouldSelectNighttimeFor(String hour) {
        ZonedDateTime time = ZonedDateTime.of(2026, 4, 25, Integer.parseInt(hour), 0, 0, 0, SAO_PAULO);
        ILimitPolicy policy = LimitPolicySelectorModel.select(time);
        assertThat(policy).isInstanceOf(NighttimeLimitModel.class);
    }

    @Test
    void shouldSelectNighttimeAtExactlyTwenty() {
        ZonedDateTime time = ZonedDateTime.of(2026, 4, 25, 20, 0, 0, 0, SAO_PAULO);
        assertThat(LimitPolicySelectorModel.select(time)).isInstanceOf(NighttimeLimitModel.class);
    }

    @Test
    void shouldSelectDaytimeAtExactlySix() {
        ZonedDateTime time = ZonedDateTime.of(2026, 4, 25, 6, 0, 0, 0, SAO_PAULO);
        assertThat(LimitPolicySelectorModel.select(time)).isInstanceOf(DaytimeLimitModel.class);
    }
}
