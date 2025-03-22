package com.jpmorgan.JPMorganPaymentHub.service;

import com.jpmorgan.JPMorganPaymentHub.model.TransactionDetail;

import java.math.BigDecimal;

public interface TransactionService {
    TransactionDetail createTransaction(String accountNumber, Long paymentMethodId, BigDecimal amount,
                                        String transactionType, String description, String referenceNumber);
}
