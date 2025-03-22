package com.jpmorgan.JPMorganPaymentHub.service;

import com.jpmorgan.JPMorganPaymentHub.model.Accounts;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentService {

    Mono<String> ping();

    Mono<Accounts> getAllAccounts(String requestBody);
    Flux<Accounts> getAllAccountsStream(String requestBody);
    Disposable checkGit();
    Mono<Accounts> saveAccountsToDatabase(Accounts accounts);
}
