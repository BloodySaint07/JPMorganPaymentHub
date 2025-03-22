package com.jpmorgan.JPMorganPaymentHub.repository;

import com.jpmorgan.JPMorganPaymentHub.model.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
}