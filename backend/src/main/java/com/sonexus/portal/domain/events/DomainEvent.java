package com.sonexus.portal.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {
    private String eventId = UUID.randomUUID().toString();
    private LocalDateTime occurredAt = LocalDateTime.now();
    private String eventType;

    public DomainEvent(String eventType) {
        this.eventType = eventType;
    }
}
