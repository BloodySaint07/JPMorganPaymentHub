package com.jpmorgan.JPMorganPaymentHub.repository;

import com.jpmorgan.JPMorganPaymentHub.model.AccountService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountServicesRepository extends JpaRepository<AccountService,Long> {
}
