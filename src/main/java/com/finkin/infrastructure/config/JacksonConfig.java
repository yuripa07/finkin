package com.finkin.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper global do Finkin.
     * - JavaTimeModule: serializa ZonedDateTime/LocalDate como ISO-8601 (não timestamp numérico)
     * - WRITE_DATES_AS_TIMESTAMPS: desabilitado — datas legíveis por humanos
     *
     * O mesmo ObjectMapper é reutilizado no RedisConfig para serializar
     * os valores de idempotência em JSON.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
