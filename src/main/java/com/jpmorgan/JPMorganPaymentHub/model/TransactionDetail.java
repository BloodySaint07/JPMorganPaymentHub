package com.jpmorgan.JPMorganPaymentHub.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_details")
@Data
@NoArgsConstructor
public class TransactionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference
    private AccountDetails account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    @JsonBackReference
    private PaymentDetails paymentMethod;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String transactionType; // e.g., DEBIT, CREDIT

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(length = 100)
    private String description;

    @Column(nullable = false)
    private String status; // e.g., PENDING, COMPLETED, FAILED

    @Column(length = 50)
    private String referenceNumber;
}