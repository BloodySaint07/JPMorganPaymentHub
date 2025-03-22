package com.jpmorgan.JPMorganPaymentHub.repository;

import com.jpmorgan.JPMorganPaymentHub.model.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentDetails, Long> {
}