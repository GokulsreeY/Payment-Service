package org.payments.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.payments.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "payments")
public class Payment {

    public  Payment(){
        id = UUID.randomUUID();
        status = PaymentStatus.CREATED;
    }
    @Id
    private UUID id;

    private String accountId;

    private String merchantId;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
