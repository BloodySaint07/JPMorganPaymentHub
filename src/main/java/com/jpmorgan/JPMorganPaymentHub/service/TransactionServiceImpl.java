package com.jpmorgan.JPMorganPaymentHub.service;

import com.jpmorgan.JPMorganPaymentHub.model.*;
import com.jpmorgan.JPMorganPaymentHub.repository.AccountEntityRepository;
import com.jpmorgan.JPMorganPaymentHub.repository.PaymentMethodRepository;
import com.jpmorgan.JPMorganPaymentHub.repository.TransactionDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static com.jpmorgan.JPMorganPaymentHub.constant.SQLQueries.FETCH_TRAN_DESC_BY_TRANSACTION_REF;
import static com.jpmorgan.JPMorganPaymentHub.constant.SQLQueries.FETCH_TRAN_DETAILS_BY_TRANSACTION_REF;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private TransactionDetailRepository transactionDetailRepository;
    @Autowired
    private AccountEntityRepository accountEntityRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    @Transactional
    public TransactionDetail createTransaction(TransactionRequestDTO transferRequest) {
        AccountDetails account = accountEntityRepository.findByAccountNumber(transferRequest.getAccountNumber());
        if (account == null) {
            throw new IllegalArgumentException("Account not found with number: " + transferRequest.getAccountNumber());
        }

        PaymentDetails paymentMethod = paymentMethodRepository.findById(transferRequest.getPaymentMethodId()).orElseThrow(() -> new IllegalArgumentException("Payment method not found with ID: " + transferRequest.getPaymentMethodId()));

        TransactionDetail transaction = new TransactionDetail();
        transaction.setAccount(account);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setAmount(transferRequest.getAmount());
        transaction.setTransactionType(transferRequest.getTransactionType());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(transferRequest.getDescription());
        transaction.setStatus("PENDING");
        transaction.setReferenceNumber(transferRequest.getReferenceNumber());

        // Update relationships
        account.getTransactions().add(transaction);
        paymentMethod.getTransactions().add(transaction);

        // Persist transaction
        TransactionDetail savedTransaction = transactionDetailRepository.save(transaction);
        log.info("Transaction created successfully with ID :{}.Request done with Correlation-ID :{} and Session-Key :{}", savedTransaction.getId(), transferRequest.getCorrelationId(), transferRequest.getSessionKey());

        return savedTransaction;
    }

    @Override
    @Transactional
    public TransactionDetail updateTransaction(TransactionUpdateRequest transferUpdateRequest) {
        TransactionDetail transactionDetails = transactionDetailRepository.findByReferenceNumber(transferUpdateRequest.getReferenceNumber());
        if (transactionDetails == null) {
            throw new IllegalArgumentException("Transaction not found with reference number: " + transferUpdateRequest.getReferenceNumber());
        } else {
            transactionDetails.setStatus(transferUpdateRequest.getStatus());
            entityManager.merge(transactionDetails);
        }
        return transactionDetails;
    }


    @Override
    @Transactional
    public TransactionDetail getTransactionByReferenceNumber(String referenceNumber) {
        log.info("Getting transaction by reference number: {}", referenceNumber);
        return transactionDetailRepository.findByReferenceNumber(referenceNumber);
    }

    @Override
    @Transactional
    public TransactionDetail getTransactionByReferenceNumberWithEntityManager(String referenceNumber) {
        log.info("Getting transaction by reference number {}  via EM: {} ", referenceNumber, entityManager.getClass().getSimpleName());
        TypedQuery<TransactionDetail> query = entityManager.createQuery(FETCH_TRAN_DETAILS_BY_TRANSACTION_REF, TransactionDetail.class);
        query.setParameter("transactionReferenceNumber", referenceNumber);
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        return query.getSingleResult();
    }


    @Override
    @Transactional
    public CompletableFuture<String> getTransactionDescriptionByReferenceNumberWithEntityManager(String referenceNumber) {
        log.info("Getting transaction description by reference number {} via EM: {}", referenceNumber, entityManager.getClass().getSimpleName());

        CompletableFuture<String> descriptionFuture = CompletableFuture.supplyAsync(() -> transactionTemplate.execute(status -> fetchTransactionDescription(referenceNumber)));
        CompletableFuture<TransactionDetail> detailFuture = CompletableFuture.supplyAsync(() -> transactionTemplate.execute(status -> fetchTransactionDetail(referenceNumber)));

        return descriptionFuture.thenCombine(detailFuture, (desc, fullDetails) -> {
            if (desc.equalsIgnoreCase(fullDetails.getDescription())) {
                log.info("Description matched for reference number: {}", referenceNumber);
                return desc;
            } else {
                log.error("Error in description for reference number: {}", referenceNumber);
                return null;
            }
        }).exceptionally(throwable -> {
            log.error("Error processing transaction for reference number: {}", referenceNumber, throwable);
            return null;
        });
    }

    public String fetchTransactionDescription(String referenceNumber) {
        TypedQuery<String> query = entityManager.createQuery(FETCH_TRAN_DESC_BY_TRANSACTION_REF, String.class);
        query.setParameter("transactionReferenceNumber", referenceNumber);
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        return query.getSingleResult();
    }

    public TransactionDetail fetchTransactionDetail(String referenceNumber) {
        TypedQuery<TransactionDetail> query = entityManager.createQuery(FETCH_TRAN_DETAILS_BY_TRANSACTION_REF, TransactionDetail.class);
        query.setParameter("transactionReferenceNumber", referenceNumber);
        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        return query.getSingleResult();
    }


}