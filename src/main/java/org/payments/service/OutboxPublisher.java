package org.payments.service;

import org.payments.enums.OutboxStatus;
import org.payments.models.OutboxEvent;
import org.payments.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxPublisher {

    private static final String TOPIC = "payments.created";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findByStatusOrderByCreatedAtAsc(
                                OutboxStatus.PENDING);

        for (OutboxEvent event : events) {

            kafkaTemplate.send(
                    TOPIC,
                    event.getAggregateId().toString(),
                    event.getPayload()
            ).join();

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());

            outboxEventRepository.save(event);
        }
    }
}