package com.finkin.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * Template Redis <String, String> para uso no IdempotencyAdapter e no RateLimitService.
     * Chaves e valores são Strings puras — a serialização JSON é feita manualmente
     * em cada adapter (usando o ObjectMapper do JacksonConfig).
     *
     * Por que String/String em vez de String/Object: evita dependências de serializers
     * deprecados do Spring Data Redis. A conversão explícita em cada adapter é mais
     * didática e transparente.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        var template = new RedisTemplate<String, String>();
        template.setConnectionFactory(factory);
        var stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
