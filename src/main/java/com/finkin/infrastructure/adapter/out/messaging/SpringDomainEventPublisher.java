package com.finkin.infrastructure.adapter.out.messaging;

import com.finkin.domain.port.out.IDomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter que conecta o IDomainEventPublisher (port out) ao ApplicationEventPublisher do Spring.
 *
 * Por que Spring Events em vez de Kafka/Rabbit em fase 1:
 * Transacionalidade simples — o evento é publicado dentro da mesma transação JPA.
 * Em fase 2, trocar por Kafka garante entrega confiável entre microserviços.
 */
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements IDomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(Object event) {
        publisher.publishEvent(event);
    }
}
