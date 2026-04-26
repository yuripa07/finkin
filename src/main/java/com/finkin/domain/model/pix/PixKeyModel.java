package com.finkin.domain.model.pix;
import com.finkin.domain.model.pix.enums.PixKeyTypeEnum;

import com.finkin.domain.exception.InvalidPixKeyException;
import com.finkin.domain.model.customer.CpfModel;
import com.finkin.domain.model.customer.EmailModel;
import com.finkin.domain.model.customer.PhoneModel;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Entidade de domínio que representa uma chave Pix registrada.
 *
 * Cada chave pertence a uma conta (accountId) e tem um tipo (PixKeyTypeEnum).
 * A validação do valor da chave é específica por tipo:
 *   - CPF: mesmo algoritmo do CustomerModel.CpfModel
 *   - EMAIL: mesmo algoritmo do CustomerModel.EmailModel
 *   - PHONE: mesmo algoritmo do CustomerModel.PhoneModel
 *   - RANDOM: UUID v4 gerado pelo servidor
 *
 * Uma conta pode ter múltiplas chaves de tipos diferentes, mas cada valor
 * de chave é único no sistema (constraint no banco: pix_keys.key_value UNIQUE).
 */
@Getter
@Builder
public class PixKeyModel {

    private final UUID id;
    private final UUID accountId;
    private final PixKeyTypeEnum keyType;
    private final String keyValue;
    private final ZonedDateTime createdAt;

    /**
     * Factory que cria e valida uma PixKeyModel de acordo com seu tipo.
     * Lança InvalidPixKeyException se o valor não for válido para o tipo informado.
     */
    public static PixKeyModel create(UUID accountId, PixKeyTypeEnum keyType, String rawValue) {
        String validated = validate(keyType, rawValue);
        return PixKeyModel.builder()
            .id(UUID.randomUUID())
            .accountId(accountId)
            .keyType(keyType)
            .keyValue(validated)
            .createdAt(ZonedDateTime.now())
            .build();
    }

    /** Para chave aleatória: o servidor gera o UUID, não aceita do cliente. */
    public static PixKeyModel createRandom(UUID accountId) {
        return PixKeyModel.builder()
            .id(UUID.randomUUID())
            .accountId(accountId)
            .keyType(PixKeyTypeEnum.RANDOM)
            .keyValue(UUID.randomUUID().toString())
            .createdAt(ZonedDateTime.now())
            .build();
    }

    private static String validate(PixKeyTypeEnum type, String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidPixKeyException(type.name(), raw);
        }
        return switch (type) {
            case CPF -> {
                try {
                    yield new CpfModel(raw).getValue();
                } catch (Exception e) {
                    throw new InvalidPixKeyException(type.name(), raw);
                }
            }
            case EMAIL -> {
                try {
                    yield new EmailModel(raw).getValue();
                } catch (Exception e) {
                    throw new InvalidPixKeyException(type.name(), raw);
                }
            }
            case PHONE -> {
                try {
                    yield new PhoneModel(raw).getValue();
                } catch (Exception e) {
                    throw new InvalidPixKeyException(type.name(), raw);
                }
            }
            case RANDOM -> {
                // Chave aleatória criada via createRandom() — não via este método
                throw new InvalidPixKeyException(type.name(), "use createRandom() para chaves aleatórias");
            }
        };
    }
}
