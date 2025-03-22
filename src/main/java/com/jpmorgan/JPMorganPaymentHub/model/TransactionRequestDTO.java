package com.jpmorgan.JPMorganPaymentHub.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequestDTO {
    private String accountNumber;
    private Long paymentMethodId;
    private BigDecimal amount;
    private String transactionType;
    private String description;
    private String referenceNumber;
}