package com.jpmorgan.JPMorganPaymentHub.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.random.RandomGenerator;

@Builder
@Data
public class TransactionRequestDTO {
    private String accountNumber;
    private Long paymentMethodId;
    private BigDecimal amount;
    private String transactionType;
    private String description;
    private String referenceNumber;
    private String correlationId;
    private String sessionKey;

    public static TransactionRequestDTO getTransactionRequest(TransactionRequest request) {
        return TransactionRequestDTO.builder()
                .accountNumber(request.getAccountNumber())
                .paymentMethodId(request.getPaymentMethodId())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .referenceNumber(request.getReferenceNumber())
                .correlationId(UUID.randomUUID().toString())
                .sessionKey(RandomGenerator.getDefault().ints(48, 122).filter(i -> Character.isLetterOrDigit(i)).limit(16).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString())
                .build();
    }

}