package com.finkin.domain.model.account;

import com.finkin.shared.BankConstants;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Seleciona a ILimitPolicy correta com base no horário atual de Brasília.
 *
 * Por que separado da Strategy: o seletor encapsula a lógica de decisão
 * (qual strategy usar) sem poluir as strategies com conhecimento do relógio.
 * Em testes, pode-se injetar um ZonedDateTime fixo para testar comportamentos
 * de limite noturno sem depender do horário real.
 */
public class LimitPolicySelectorModel {

    private static final LocalTime NIGHT_START = LocalTime.of(BankConstants.NIGHT_HOUR_START, 0);
    private static final LocalTime NIGHT_END   = LocalTime.of(BankConstants.NIGHT_HOUR_END,   0);

    private LimitPolicySelectorModel() {}

    public static ILimitPolicy select() {
        return select(ZonedDateTime.now(ZoneId.of(BankConstants.TIMEZONE_BR)));
    }

    /** Overload para injeção de tempo em testes. */
    public static ILimitPolicy select(ZonedDateTime now) {
        LocalTime time = now.toLocalTime();
        // Período noturno: das 20h até as 06h do dia seguinte
        boolean isNight = !time.isBefore(NIGHT_START) || time.isBefore(NIGHT_END);
        return isNight ? new NighttimeLimitModel() : new DaytimeLimitModel();
    }
}
