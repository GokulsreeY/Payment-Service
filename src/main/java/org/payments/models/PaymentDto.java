package org.payments.models;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor // Generates the required default constructor
@AllArgsConstructor
public class PaymentDto {

    private String accountId;

    private String merchantId;

    private BigDecimal amount;

    private String currency;

    private String status;

    private LocalDateTime createdAt;
}
