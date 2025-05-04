package com.jpmorgan.JPMorganPaymentHub.model;

import lombok.Data;

import java.math.BigDecimal;
import javax.validation.constraints.*;

@Data
public class TransactionRequest {
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    @NotBlank(message = "Transaction type is required")
    private String transactionType;
    @Size(max = 100, message = "Description must not exceed 100 characters")
    private String description;
    @NotBlank(message = "Reference number is required")
    private String referenceNumber;
}