package com.jpmorgan.JPMorganPaymentHub.service;

import com.jpmorgan.JPMorganPaymentHub.model.TransactionDetail;
import com.jpmorgan.JPMorganPaymentHub.model.TransactionRequestDTO;
import com.jpmorgan.JPMorganPaymentHub.model.TransactionUpdateRequest;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface TransactionService {
    TransactionDetail createTransaction(TransactionRequestDTO transferRequest);
    TransactionDetail updateTransaction(TransactionUpdateRequest transferUpdateRequest);
    TransactionDetail getTransactionByReferenceNumber(String referenceNumber);
    TransactionDetail getTransactionByReferenceNumberWithEntityManager(String referenceNumber);
    CompletableFuture<String> getTransactionDescriptionByReferenceNumberWithEntityManager(String referenceNumber);

}
