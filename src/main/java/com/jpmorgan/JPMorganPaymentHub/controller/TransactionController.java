package com.jpmorgan.JPMorganPaymentHub.controller;


import com.jpmorgan.JPMorganPaymentHub.model.TransactionDetail;
import com.jpmorgan.JPMorganPaymentHub.model.TransactionRequestDTO;
import com.jpmorgan.JPMorganPaymentHub.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/jpmorgan/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/create")
    public TransactionDetail createTransaction(@RequestBody TransactionRequestDTO requestDTO) {
        log.info("Creating transaction for account: {}", requestDTO.getAccountNumber());
        return transactionService.createTransaction(
                requestDTO.getAccountNumber(),
                requestDTO.getPaymentMethodId(),
                requestDTO.getAmount(),
                requestDTO.getTransactionType(),
                requestDTO.getDescription(),
                requestDTO.getReferenceNumber()
        );
    }
}