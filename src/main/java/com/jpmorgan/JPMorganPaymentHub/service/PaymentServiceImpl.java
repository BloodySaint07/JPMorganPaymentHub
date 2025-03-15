package com.jpmorgan.JPMorganPaymentHub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.JPMorganPaymentHub.constant.AppConstants;
import com.jpmorgan.JPMorganPaymentHub.enums.CustomErrorCode;
import com.jpmorgan.JPMorganPaymentHub.model.Accounts;
import com.jpmorgan.JPMorganPaymentHub.utility.PaymentUtility;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("jpmorganWebClient")
    private WebClient webClient;

    @Autowired
    private PaymentUtility paymentUtility;

    @Value("${jpmorgan.ping.url}")
    private String pingUrl;

    @Value("${jpmorgan.accounts.url}")
    private String getAllAccountsUrl;

    @Override
    public Mono<String> ping() {
        return webClient.post()
                .uri(pingUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("Ping Response: {}", response))
                .doOnError(error -> log.error("Ping failed: {}", error.getMessage()));
    }

    @Override
    @Retry(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
    @CircuitBreaker(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
    @RateLimiter(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
    //@Bulkhead(name = "getAllAccounts", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "fallbackForGetAllAccounts")
    public Mono<Accounts> getAllAccounts(String requestBody) {
        return webClient.post()
                .uri(getAllAccountsUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", paymentUtility.getSecret())
                .body(Mono.just(requestBody), String.class)
                .retrieve()
                .bodyToMono(String.class)
                //.timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> log.debug("Raw response: {}", response))
                .flatMap(responseBody -> {
                    log.info("Account Details Received: {}", responseBody);
                    try {
                        Accounts accounts = objectMapper.readValue(responseBody, Accounts.class);
                        paymentUtility.logAccountDetails(accounts.getAccounts());
                        return Mono.just(accounts);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse response body ~ ", e);
                        return Mono.error(new RuntimeException("Failed to parse response body ~ ", e));
                    }
                })
                .doOnError(error -> log.error("GetAllAccounts failed with: {}", error.toString(), error));
    }

    @Override
    @Retry(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
    @CircuitBreaker(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
    @RateLimiter(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
    //@Bulkhead(name = "getAllAccounts", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "fallbackForGetAllAccounts")
    public Flux<Accounts> getAllAccountsStream(String requestBody) {
        return webClient.post()
                .uri(getAllAccountsUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", paymentUtility.getSecret())
                .body(Mono.just(requestBody), String.class)
                .retrieve()
                .bodyToFlux(String.class)
                .onBackpressureBuffer(5)
                .flatMap(responseBody -> {
                    log.info("Account Details Received: {}", responseBody);
                    try {
                        Accounts accounts = objectMapper.readValue(responseBody, Accounts.class);
                        paymentUtility.logAccountDetails(accounts.getAccounts());
                        return Mono.just(accounts);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse response body", e);
                        return Mono.error(new RuntimeException("Failed to parse response body", e));
                    }
                })
                .doOnNext(accounts -> log.debug("Processed accounts: {}", accounts))
                .doOnError(error -> log.error("GetAllAccounts failed with: {}", error.toString(), error));
    }

    private Mono<Accounts> fallbackForGetAllAccounts(String requestBody, CallNotPermittedException ex) {
        log.error("Circuit breaker is now OPEN for getAllAccounts, calls blocked: {} originalRequest: {}", ex.getMessage(), requestBody);
        com.jpmorgan.JPMorganPaymentHub.model.Error error = new com.jpmorgan.JPMorganPaymentHub.model.Error(AppConstants.SERVICE_UNAVAILABLE, CustomErrorCode.GET_ALL_ACCOUNTS_CB_ERROR.getCode(), CustomErrorCode.GET_ALL_ACCOUNTS_CB_ERROR.getMessage());
        return Mono.just(new Accounts(null, error));
    }

    private Mono<Accounts> fallbackForGetAllAccounts(String requestBody, io.github.resilience4j.ratelimiter.RequestNotPermitted ex) {
        log.error("Rate limiter blocked call for getAllAccounts: {} originalRequest: {}", ex.getMessage(), requestBody);
        com.jpmorgan.JPMorganPaymentHub.model.Error error = new com.jpmorgan.JPMorganPaymentHub.model.Error(AppConstants.SERVICE_UNAVAILABLE, CustomErrorCode.GET_ALL_ACCOUNTS_RL_ERROR.getCode(), CustomErrorCode.GET_ALL_ACCOUNTS_RL_ERROR.getMessage());
        return Mono.just(new Accounts(null, error));
    }

    private Mono<Accounts> fallbackForGetAllAccounts(String requestBody, io.github.resilience4j.bulkhead.BulkheadFullException ex) {
        log.error("Bulkhead limit reached for getAllAccounts: {} originalRequest: {}", ex.getMessage(), requestBody);
        com.jpmorgan.JPMorganPaymentHub.model.Error error = new com.jpmorgan.JPMorganPaymentHub.model.Error(AppConstants.SERVICE_UNAVAILABLE, CustomErrorCode.GET_ALL_ACCOUNTS_BH_ERROR.getCode(), CustomErrorCode.GET_ALL_ACCOUNTS_BH_ERROR.getMessage());
        return Mono.just(new Accounts(null, error));
    }

    private Mono<Accounts> fallbackForGetAllAccounts(String requestBody, Throwable ex) {
        log.error("GetAllAccounts fallback triggered due to: {} originalRequest: {}", ex.getMessage(), requestBody);
        return Mono.error(ex);
    }
}
