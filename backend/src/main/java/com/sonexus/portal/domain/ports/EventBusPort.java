package com.sonexus.portal.domain.ports;

import com.sonexus.portal.domain.events.DomainEvent;

/**
 * Port for publishing domain events.
 * Implementations: InMemoryEventBusAdapter (local), KafkaEventBusAdapter (cloud/prod)
 */
public interface EventBusPort {

    /**
     * Publish domain event
     */
    void publish(DomainEvent event);

    /**
     * Subscribe to domain events (for internal event handlers)
     */
    void subscribe(String eventType, EventHandler handler);

    @FunctionalInterface
    interface EventHandler {
        void handle(DomainEvent event);
    }
}
