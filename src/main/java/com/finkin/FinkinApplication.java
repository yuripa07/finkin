package com.finkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Ponto de entrada do Finkin Bank.
 *
 * @EnableJpaAuditing — ativa @CreatedDate, @LastModifiedDate, @CreatedBy nas entidades JPA.
 *   A implementação de AuditorAware<String> está em AuditConfig.
 *
 * @ConfigurationPropertiesScan — detecta automaticamente todas as classes anotadas com
 *   @ConfigurationProperties sem precisar de @EnableConfigurationProperties em cada @Configuration.
 *
 * @EnableAsync — necessário para publicação assíncrona de eventos de domínio (TransactionCompletedEvent).
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@ConfigurationPropertiesScan("com.finkin")
@EnableAsync
public class FinkinApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinkinApplication.class, args);
    }
}
