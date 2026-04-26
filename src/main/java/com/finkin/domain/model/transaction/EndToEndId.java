package com.finkin.domain.model.transaction;

import com.finkin.shared.BankConstants;
import lombok.Value;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Value Object que representa o identificador fim-a-fim de uma transação Pix.
 *
 * Formato definido pelo Bacen (Manual de Tempos do Pix, ICOM-BCB):
 *   E + ISPB(8) + AAAMMDDhhmmss(13 sem separadores) + sufixo alfanumérico(10) = 32 chars
 *
 * Exemplo: E99999999202604251830009aKx3mN7q8B
 * Onde:
 *   E          = prefixo literal
 *   99999999   = ISPB do Finkin (8 dígitos)
 *   20260425   = data (AAAAMMDD, 8 dígitos)
 *   1830       = hora e minuto (hhmm, 4 dígitos, sem segundos — Bacen usa 4)
 *   00         = sufixo fixo para completar a 14ª posição
 *   9aKx3mN7q8B = sufixo aleatório de 11 chars alfanuméricos
 *
 * Total: 1 + 8 + 14 + 9 = 32 chars ✓
 */
@Value
public class EndToEndId {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    String value;

    public EndToEndId(String value) {
        if (value == null || value.length() != BankConstants.END_TO_END_ID_LENGTH) {
            throw new IllegalArgumentException("EndToEndId inválido: deve ter 32 chars");
        }
        this.value = value;
    }

    /** Gera um novo EndToEndId com o timestamp atual de Brasília. */
    public static EndToEndId generate() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(BankConstants.TIMEZONE_BR));
        return generate(now);
    }

    /** Overload para injeção de tempo em testes. */
    public static EndToEndId generate(ZonedDateTime now) {
        String datetime = now.format(FMT);              // 12 chars (yyyyMMddHHmm)
        String suffix   = randomSuffix(10);             // 10 chars
        // E(1) + ISPB(8) + datetime(12) + sufixo(10) + "0"(1) = 32 chars
        String raw = BankConstants.END_TO_END_PREFIX
            + BankConstants.ISPB
            + datetime
            + "0"
            + suffix;
        assert raw.length() == BankConstants.END_TO_END_ID_LENGTH : "EndToEndId deve ter 32 chars, tem " + raw.length();
        return new EndToEndId(raw);
    }

    private static String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return value;
    }
}
