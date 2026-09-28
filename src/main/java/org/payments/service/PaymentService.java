package org.payments.service;

import jakarta.transaction.Transactional;
import org.payments.entities.FraudProcessedEvent;
import org.payments.entities.Payment;
import org.payments.models.FraudDecisionEvent;
import org.payments.models.PaymentCreatedEvent;
import org.payments.models.PaymentDto;
import org.payments.models.OutboxEvent;
import org.payments.repository.FraudProcessedEventRepository;
import org.payments.repository.OutboxEventRepository;
import org.payments.enums.OutboxStatus;
import org.payments.enums.PaymentStatus;
import org.payments.repository.PaymentRepository;
import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final FraudProcessedEventRepository fraudProcessedEventRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          OutboxEventRepository outboxEventRepository,
                          ObjectMapper objectMapper,
                          FraudProcessedEventRepository fraudProcessedEventRepository){
        this.paymentRepository = paymentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.fraudProcessedEventRepository = fraudProcessedEventRepository;
    }

    @Transactional
    public Payment createTransaction(PaymentDto paymentDto){
        Payment payment = new Payment();
        payment.setAccountId(paymentDto.getAccountId());
        payment.setMerchantId(paymentDto.getMerchantId());
        payment.setStatus(PaymentStatus.PENDING_FRAUD_CHECK);
        payment.setAmount(paymentDto.getAmount());
        payment.setCurrency(paymentDto.getCurrency());
        Payment savedPayment =  paymentRepository.save(payment);

        UUID eventId = UUID.randomUUID();

        PaymentCreatedEvent event = new PaymentCreatedEvent(
                eventId,
                savedPayment.getId(),
                savedPayment.getAccountId(),
                savedPayment.getMerchantId(),
                savedPayment.getAmount(),
                savedPayment.getCurrency(),
                savedPayment.getStatus().name(),
                savedPayment.getCreatedAt()
        );

        try {

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent();

            outboxEvent.setId(eventId);
            outboxEvent.setAggregateType("PAYMENT");
            outboxEvent.setAggregateId(savedPayment.getId());
            outboxEvent.setEventType("PAYMENT_CREATED");
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus(OutboxStatus.PENDING);
            outboxEvent.setCreatedAt(LocalDateTime.now());

            outboxEventRepository.save(outboxEvent);

        } catch (JsonParseException e) {
            throw new RuntimeException(
                    "Failed to serialize payment event", e);
        }
        return savedPayment;
    }

    public List<PaymentDto> getPayments(){
        List<Payment> payments = paymentRepository.findAll();
        List<PaymentDto> paymentDtos = new ArrayList<>();
        for (Payment payment : payments){
            PaymentDto paymentDto = PaymentDto.builder().accountId(payment.getAccountId())
                    .merchantId(payment.getMerchantId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .status(payment.getStatus().name())
                    .createdAt(payment.getCreatedAt())
                    .build();

            paymentDtos.add(paymentDto);

        }
        return paymentDtos;
    }

    public PaymentDto getPayment(UUID id){
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException());

        PaymentDto paymentDto = PaymentDto.builder().accountId(payment.getAccountId())
                    .merchantId(payment.getMerchantId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .status(payment.getStatus().name())
                    .createdAt(payment.getCreatedAt())
                    .build();

        return paymentDto;
    }

    @Transactional
    public void processFraudDecision(FraudDecisionEvent event) {
        if(fraudProcessedEventRepository.existsById(event.eventId())){
            return;
        }
        Payment payment = paymentRepository
                .findById(event.paymentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found: " + event.paymentId()
                        )
                );

        if ("Low Risk".equals(event.riskLevel())) {
            payment.setStatus(PaymentStatus.APPROVED);
        } else {
            payment.setStatus(PaymentStatus.FLAGGED);
        }

        paymentRepository.save(payment);
        fraudProcessedEventRepository.save(new FraudProcessedEvent(event.eventId(),LocalDateTime.now()));
    }

}
