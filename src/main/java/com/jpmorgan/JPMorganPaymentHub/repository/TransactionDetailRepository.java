package com.jpmorgan.JPMorganPaymentHub.repository;

import com.jpmorgan.JPMorganPaymentHub.model.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
    @Query("SELECT t FROM TransactionDetail t WHERE t.referenceNumber = ?1")
    TransactionDetail findByReferenceNumber(String referenceNumber);
}