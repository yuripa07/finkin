package com.finkin.infrastructure.adapter.output.idempotency;

import com.finkin.domain.port.output.IIdempotencyStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Implementação de IIdempotencyStore usando Redis.
 *
 * Chave Redis: "idem:tx:<uuid>" (prefixo evita colisões com outras chaves)
 * TTL: 24 horas — alinhado com a janela de retransmissão de clientes HTTP
 * conforme o Manual de Integração do Pix do Bacen.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IIdempotencyStore {

    private static final String KEY_PREFIX = "idem:tx:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<String> get(String key) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + key);
        return Optional.ofNullable(value);
    }

    @Override
    public void store(String key, String resultJson) {
        redisTemplate.opsForValue().set(KEY_PREFIX + key, resultJson, TTL);
        log.debug("Idempotency stored: key={}, ttl={}h", key, TTL.toHours());
    }
}
