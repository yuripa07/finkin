package com.finkin.domain.model.customer;

import com.finkin.domain.exception.DomainException;
import lombok.Value;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Value Object para número de telefone brasileiro no formato E.164.
 *
 * Formato aceito: +55XXYYYYYYYYYY
 * - +55: DDI do Brasil
 * - XX: DDD válido (lista oficial Anatel)
 * - 8 ou 9 dígitos (celular com 9 ou fixo com 8)
 *
 * Fontes: Plano de Numeração Telefônica (ANATEL, Resolução 263/2001 e atualizações)
 */
@Value
public class PhoneModel {

    // DDDs válidos no Brasil (Resolução ANATEL 263/2001, atualizado)
    private static final Set<String> VALID_DDDS = Set.of(
        "11","12","13","14","15","16","17","18","19", // SP
        "21","22","24",                               // RJ + ES
        "27","28",                                    // ES
        "31","32","33","34","35","37","38",           // MG
        "41","42","43","44","45","46",                // PR
        "47","48","49",                               // SC
        "51","53","54","55",                          // RS
        "61",                                         // DF
        "62","64",                                    // GO
        "63",                                         // TO
        "65","66",                                    // MT
        "67",                                         // MS
        "68",                                         // AC
        "69",                                         // RO
        "71","73","74","75","77",                     // BA
        "79",                                         // SE
        "81","87",                                    // PE
        "82",                                         // AL
        "83",                                         // PB
        "84",                                         // RN
        "85","88",                                    // CE
        "86","89",                                    // PI
        "91","93","94",                               // PA
        "92","97",                                    // AM
        "95",                                         // RR
        "96",                                         // AP
        "98","99"                                     // MA
    );

    // +55 + DDD(2) + número(8 ou 9 dígitos)
    private static final Pattern PATTERN = Pattern.compile("^\\+55(\\d{2})(\\d{8,9})$");

    String value;

    public PhoneModel(String raw) {
        if (raw == null) throw new InvalidPhoneException(null);
        String stripped = raw.strip().replaceAll("[\\s()-]", "");
        var matcher = PATTERN.matcher(stripped);
        if (!matcher.matches() || !VALID_DDDS.contains(matcher.group(1))) {
            throw new InvalidPhoneException(raw);
        }
        this.value = stripped;
    }

    public static class InvalidPhoneException extends DomainException {
        public InvalidPhoneException(String phone) {
            super("Telefone inválido. Use E.164 com DDI +55 e DDD válido: %s".formatted(phone));
        }
    }
}
