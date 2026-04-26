package com.finkin.infrastructure.adapter.input.web.common;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Converter Logback que mascara dados sensíveis nas mensagens de log.
 *
 * Por que: compliance com LGPD (Lei 13.709/2018) e boas práticas de segurança
 * do BCB — CPF e número de conta completos nunca devem aparecer em logs de sistema.
 *
 * Padrões mascarados:
 * - CPF formatado: 123.456.789-09 → ***.***.***-**
 * - CPF numérico: 12345678909 → ***.***.***-**
 *
 * Configurado em logback-spring.xml via <conversionRule>.
 */
public class MaskingConverter extends ClassicConverter {

    // CPF formatado: DDD.DDD.DDD-DD
    private static final Pattern CPF_FORMATTED =
        Pattern.compile("\\b\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}\\b");

    // CPF numérico: 11 dígitos contíguos (evitar mascarar telefones/outros)
    private static final Pattern CPF_NUMERIC =
        Pattern.compile("(?<!\\d)(\\d{11})(?!\\d)");

    private static final String MASK = "***.***.***.***-**";

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null) return "";
        message = CPF_FORMATTED.matcher(message).replaceAll("***.***.***-**");
        message = CPF_NUMERIC.matcher(message).replaceAll("***.***.***-**");
        return message;
    }
}
