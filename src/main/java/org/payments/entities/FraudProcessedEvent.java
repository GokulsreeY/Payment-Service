package org.payments.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "fraud_processed_events")
public class FraudProcessedEvent {

    @Id
    private UUID eventId;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    public FraudProcessedEvent() {}

    public FraudProcessedEvent(UUID eventId, LocalDateTime processedAt) {
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    // getters/setters
}
