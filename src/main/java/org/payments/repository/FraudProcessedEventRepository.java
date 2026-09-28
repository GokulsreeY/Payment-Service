package org.payments.repository;

import org.payments.entities.FraudProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FraudProcessedEventRepository extends JpaRepository<FraudProcessedEvent, UUID> {
}
