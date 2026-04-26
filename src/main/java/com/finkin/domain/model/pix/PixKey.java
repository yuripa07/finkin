package com.finkin.domain.model.pix;

import com.finkin.domain.exception.InvalidPixKeyException;
import com.finkin.domain.model.customer.Cpf;
import com.finkin.domain.model.customer.Email;
import com.finkin.domain.model.customer.Phone;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Entidade de domínio que representa uma chave Pix registrada.
 *
 * Cada chave pertence a uma conta (accountId) e tem um tipo (PixKeyType).
 * A validação do valor da chave é específica por tipo:
 *   - CPF: mesmo algoritmo do Customer.Cpf
 *   - EMAIL: mesmo algoritmo do Customer.Email
 *   - PHONE: mesmo algoritmo do Customer.Phone
 *   - RANDOM: UUID v4 gerado pelo servidor
 *
 * Uma conta pode ter múltiplas chaves de tipos diferentes, mas cada valor
 * de chave é único no sistema (constraint no banco: pix_keys.key_value UNIQUE).
 */
@Getter
@Builder
public class PixKey {

    private final UUID id;
    private final UUID accountId;
    private final PixKeyType keyType;
    private final String keyValue;
    private final ZonedDateTime createdAt;

    /**
     * Factory que cria e valida uma PixKey de acordo com seu tipo.
     * Lança InvalidPixKeyException se o valor não for válido para o tipo informado.
     */
    public static PixKey create(UUID accountId, PixKeyType keyType, String rawValue) {
        String validated = validate(keyType, rawValue);
        return PixKey.builder()
            .id(UUID.randomUUID())
            .accountId(accountId)
            .keyType(keyType)
            .keyValue(validated)
            .createdAt(ZonedDateTime.now())
            .build();
    }

    /** Para chave aleatória: o servidor gera o UUID, não aceita do cliente. */
    public static PixKey createRandom(UUID accountId) {
        return PixKey.builder()
            .id(UUID.randomUUID())
            .accountId(accountId)
            .keyType(PixKeyType.RANDOM)
            .keyValue(UUID.randomUUID().toString())
            .createdAt(ZonedDateTime.now())
            .build();
    }

    private static String validate(PixKeyType type, String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidPixKeyException(type.name(), raw);
        }
        return switch (type) {
            case CPF -> {
                try {
                    yield new Cpf(raw).getValue();
                } catch (Exception e) {
                    throw new InvalidPixKeyException(type.name(), raw);
                }
            }
            case EMAIL -> {
                try {
                    yield new Email(raw).getValue();
                } catch (Exception e) {
                    throw new InvalidPixKeyException(type.name(), raw);
                }
            }
            case PHONE -> {
                try {
                    yield new Phone(raw).getValue();
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
