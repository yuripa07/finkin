package com.finkin.domain.port.output;

import java.util.Optional;

/**
 * Port de saída para armazenamento de idempotência.
 * Implementado pelo RedisIdempotencyAdapter.
 *
 * Garante que uma operação com a mesma chave retorne o mesmo resultado
 * sem processamento duplicado — exigência do BCB para endpoints Pix.
 */
public interface IIdempotencyStore {

    /**
     * Verifica se a chave já foi processada.
     * Retorna o resultado em JSON se já existir; empty caso contrário.
     */
    Optional<String> get(String key);

    /**
     * Armazena o resultado associado à chave com TTL de 24h.
     * @param key chave de idempotência (UUID v4 enviado pelo cliente)
     * @param resultJson resultado serializado em JSON
     */
    void store(String key, String resultJson);
}
