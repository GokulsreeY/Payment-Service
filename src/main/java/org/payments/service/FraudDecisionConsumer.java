package org.payments.service;

import org.payments.models.FraudDecisionEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class FraudDecisionConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public FraudDecisionConsumer(
            ObjectMapper objectMapper,
            PaymentService paymentService) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = "fraud.decisions",
            groupId = "payment-service"
    )
    public void consume(String payload) throws Exception {

        FraudDecisionEvent event =
                objectMapper.readValue(
                        payload,
                        FraudDecisionEvent.class
                );

        paymentService.processFraudDecision(event);
    }
}