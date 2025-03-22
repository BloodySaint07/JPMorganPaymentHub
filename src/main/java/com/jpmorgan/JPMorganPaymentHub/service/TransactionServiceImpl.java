package com.jpmorgan.JPMorganPaymentHub.service;

import com.jpmorgan.JPMorganPaymentHub.model.AccountDetails;
import com.jpmorgan.JPMorganPaymentHub.model.PaymentDetails;
import com.jpmorgan.JPMorganPaymentHub.model.TransactionDetail;
import com.jpmorgan.JPMorganPaymentHub.repository.AccountEntityRepository;
import com.jpmorgan.JPMorganPaymentHub.repository.PaymentMethodRepository;
import com.jpmorgan.JPMorganPaymentHub.repository.TransactionDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.jpmorgan.JPMorganPaymentHub.constant.SQLQueries.FETCH_TRAN_DETAILS_BY_TRANSACTION_REF;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Autowired
    private AccountEntityRepository accountEntityRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public TransactionDetail createTransaction(String accountNumber, Long paymentMethodId, BigDecimal amount,
                                               String transactionType, String description, String referenceNumber) {
        AccountDetails account = accountEntityRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found with number: " + accountNumber);
        }

        PaymentDetails paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found with ID: " + paymentMethodId));

        TransactionDetail transaction = new TransactionDetail();
        transaction.setAccount(account);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(description);
        transaction.setStatus("PENDING");
        transaction.setReferenceNumber(referenceNumber);

        // Update relationships
        account.getTransactions().add(transaction);
        paymentMethod.getTransactions().add(transaction);

        // Persist transaction
        TransactionDetail savedTransaction = transactionDetailRepository.save(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getId());

        return savedTransaction;
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



}