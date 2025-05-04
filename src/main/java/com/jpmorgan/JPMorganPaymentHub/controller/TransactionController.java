package com.jpmorgan.JPMorganPaymentHub.controller;


import com.jpmorgan.JPMorganPaymentHub.model.TransactionDetail;
import com.jpmorgan.JPMorganPaymentHub.model.TransactionRequest;
import com.jpmorgan.JPMorganPaymentHub.model.TransactionRequestDTO;
import com.jpmorgan.JPMorganPaymentHub.model.TransactionUpdateRequest;
import com.jpmorgan.JPMorganPaymentHub.service.TransactionService;
import com.jpmorgan.JPMorganPaymentHub.validation.TransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/jpmorgan/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionValidator transactionValidator;

    @PostMapping("/create")
    public TransactionDetail createTransaction(@Valid @RequestBody TransactionRequest request) {
        log.info("Creating transaction for account: {}", request.getAccountNumber());
        TransactionRequestDTO requestDTO = TransactionRequestDTO.getTransactionRequest(request);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(requestDTO, "transactionRequestDTO");
        transactionValidator.validate(requestDTO, bindingResult);
        if (!bindingResult.hasErrors()) {
            return transactionService.createTransaction(requestDTO);
        } else {
            log.error("We are sorry. Transaction processing failed for transaction-id :{}. Errors :{}", requestDTO.getReferenceNumber(), bindingResult.getAllErrors());
            throw new IllegalArgumentException("Invalid Form Details - " + bindingResult.getAllErrors());
        }
    }
    @PatchMapping("/update/status")
    public TransactionDetail updateTransactionStatus(@Valid @RequestBody TransactionUpdateRequest request) {
        log.info("Updating transaction :{}", request.getReferenceNumber());
        return transactionService.updateTransaction(request);
    }

    @GetMapping("/transaction/{referenceNumber}")
    public TransactionDetail getTransactionByReferenceNumber(@PathVariable String referenceNumber) {
        log.info("Fetching transaction by reference number: {}", referenceNumber);
        return transactionService.getTransactionByReferenceNumber(referenceNumber);
    }

    @GetMapping("/transaction/v2/{referenceNumber}")
    public TransactionDetail getTransactionByReferenceNumberEnhanced(@PathVariable String referenceNumber) {
        log.info("Fetching transaction by reference number via Enhanced Path: {}", referenceNumber);
        return transactionService.getTransactionByReferenceNumberWithEntityManager(referenceNumber);
    }

    @GetMapping("/transaction/v3/{referenceNumber}")
    public String getTransactionByReferenceNumberEnhancedV3(@PathVariable String referenceNumber) {
        log.info("Fetching transaction by reference number via Enhanced Async Path: {}", referenceNumber);
        try {
            return transactionService.getTransactionDescriptionByReferenceNumberWithEntityManager(referenceNumber).get();
        } catch (InterruptedException e) {
            log.error("Thread interrupted while fetching transaction description: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            log.error("Error fetching transaction description: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}