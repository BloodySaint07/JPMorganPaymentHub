package com.jpmorgan.JPMorganPaymentHub.repository;

import com.jpmorgan.JPMorganPaymentHub.model.AccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountEntityRepository extends JpaRepository<AccountDetails, Long> {
    AccountDetails findByAccountNumber(String accountNumber);
}
