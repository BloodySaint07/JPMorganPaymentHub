package com.jpmorgan.JPMorganPaymentHub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.JPMorganPaymentHub.constant.AppConstants;
import com.jpmorgan.JPMorganPaymentHub.enums.AccountServiceType;
import com.jpmorgan.JPMorganPaymentHub.enums.CustomErrorCode;
import com.jpmorgan.JPMorganPaymentHub.model.*;
import com.jpmorgan.JPMorganPaymentHub.repository.AccountEntityRepository;
import com.jpmorgan.JPMorganPaymentHub.repository.AccountServicesRepository;
import com.jpmorgan.JPMorganPaymentHub.repository.PaymentMethodRepository;
import com.jpmorgan.JPMorganPaymentHub.utility.PaymentUtility;
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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private AccountEntityRepository accountEntityRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private AccountServicesRepository accountServicesRepository;


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

    public Disposable checkGit() {
        return webClient.get()
                .uri("https://api.github.com")
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("GitHub Response: {}", response))
                .doOnError(error -> log.error("GitHub Error: {}", error.getMessage(), error))
                .subscribe();
    }
    @Override

    @CircuitBreaker(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
    @RateLimiter(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
    @Retry(name = "getAllAccounts", fallbackMethod = "fallbackForGetAllAccounts")
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
                .timeout(Duration.ofSeconds(5))
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
    @Transactional
    public Mono<Accounts> saveAccountsToDatabase(Accounts accounts) {
        return Mono.fromCallable(() -> {
            if (accounts == null || accounts.getAccounts() == null || accounts.getAccounts().isEmpty()) {
                log.warn("No accounts to save");
                return accounts; // Return original accounts even if empty
            }

            List<AccountDetails> accountEntities = new ArrayList<>();
            List<PaymentDetails> paymentMethods = new ArrayList<>();
            List<AccountService> accountServices = new ArrayList<>();

            for (Account account : accounts.getAccounts()) {
                AccountDetails accountEntity = new AccountDetails();
                accountEntity.setAccountName(account.getAccountName());
                accountEntity.setAccountNumber(account.getAccountNumber());
                accountEntity.setBalance(account.getBalance());
                accountEntity.setCreditCardNumber(account.getCreditCardNumber());
                accountEntity.setDebitCardNumber(account.getDebitCardNumber());
                accountEntity.setAccountType(account.getAccountType());
                accountEntity.setCity(account.getCity());
                accountEntity.setState(account.getState());
                accountEntity.setCountry(account.getCountry());

                List<AccountService> services = setServices(account);
                accountEntity.setServices(services);
                accountServices.addAll(services);

                accountEntities.add(accountEntity);

                // Extract PaymentMethod from creditCardNumber
                if (account.getCreditCardNumber() != null && !account.getCreditCardNumber().isEmpty()) {
                    PaymentDetails creditCard = new PaymentDetails();
                    creditCard.setMethodType("CREDIT_CARD");
                    creditCard.setCardNumber(account.getCreditCardNumber());
                    creditCard.setProvider(paymentUtility.guessProvider(account.getCreditCardNumber()));
                    creditCard.setCardHolderName(account.getAccountName());
                    paymentMethods.add(creditCard);
                }

                // Extract PaymentMethod from debitCardNumber
                if (account.getDebitCardNumber() != null && !account.getDebitCardNumber().isEmpty()) {
                    PaymentDetails debitCard = new PaymentDetails();
                    debitCard.setMethodType("DEBIT_CARD");
                    debitCard.setCardNumber(account.getDebitCardNumber());
                    debitCard.setProvider(paymentUtility.guessProvider(account.getDebitCardNumber()));
                    debitCard.setCardHolderName(account.getAccountName());
                    paymentMethods.add(debitCard);
                }
            }

            // Save all entities
            accountServicesRepository.saveAll(accountServices);
            accountEntityRepository.saveAll(accountEntities);
            paymentMethodRepository.saveAll(paymentMethods);

            log.info("Saved {} accounts, {} payment methods, and {} account services to database", accountEntities.size(), paymentMethods.size(), accountServices.size());
            return accounts; // Return the original Accounts object
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()); // Run blocking code on a suitable scheduler
    }
    private List<AccountService> setServices(Account account) {
        List<AccountService> services = new ArrayList<>();
        AccountService service= new AccountService();
        if(Objects.nonNull(account)){
            if(account.getBalance()>0 && account.getBalance()<100) {
                service.setServiceType(AccountServiceType.STANDARD_BANKING);
                service.setDescription("Standard Banking Services");
                services.add(service);
            }else if(account.getBalance()>=100 && account.getBalance()<500){
                service.setServiceType(AccountServiceType.BUSINESS_BANKING);
                service.setDescription("Premium Banking Services");
                services.add(service);
            }
            else if(account.getBalance()>=500 && account.getBalance()<1000){
                service.setServiceType(AccountServiceType.DEBIT_CARD_SERVICES);
                service.setDescription("Premium Banking Services");
                services.add(service);
            }
            else if(account.getBalance()>=1000 && account.getBalance()<5000){
                service.setServiceType(AccountServiceType.PREMIUM_BANKING);
                service.setDescription("Premium Banking Services");
                services.add(service);

            }
            else if(account.getBalance()>=5000){
                service.setServiceType(AccountServiceType.LOAN_SERVICES);
                service.setDescription("Premium Banking Services");
                services.add(service);

            }
        }
        return services;
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
