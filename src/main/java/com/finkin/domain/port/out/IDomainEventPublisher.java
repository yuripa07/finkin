package com.finkin.domain.port.out;

/**
 * Port de saída para publicação de eventos de domínio.
 *
 * Padrão: Observer/Event — desacopla o domínio dos consumidores de eventos.
 * Implementado por SpringDomainEventPublisher que usa ApplicationEventPublisher.
 *
 * Em fase 2: pode ser substituído por publicação em Kafka/RabbitMQ
 * para arquitetura orientada a eventos entre microserviços.
 */
public interface IDomainEventPublisher {

    void publish(Object event);
}
