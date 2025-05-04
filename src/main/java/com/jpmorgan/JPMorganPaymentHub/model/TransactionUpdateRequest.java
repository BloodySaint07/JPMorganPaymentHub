package com.jpmorgan.JPMorganPaymentHub.model;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class TransactionUpdateRequest {
    @NotBlank(message = "Status is required.")
    private String status;
    @NotBlank(message = "Transaction Reference number is mandatory.")
    private String referenceNumber;
}