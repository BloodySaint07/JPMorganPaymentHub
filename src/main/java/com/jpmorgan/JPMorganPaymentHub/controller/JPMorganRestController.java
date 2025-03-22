package com.jpmorgan.JPMorganPaymentHub.controller;


import com.jpmorgan.JPMorganPaymentHub.model.Accounts;
import com.jpmorgan.JPMorganPaymentHub.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/jpmorgan")
public class JPMorganRestController {
    Logger log = LoggerFactory.getLogger(JPMorganRestController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/ping")
    public Mono<String> ping() {
        log.info("Server Ping Service invoked");
        return paymentService.ping();
    }
    @PostMapping("/git")
    public Disposable gitCheck() {
        return paymentService.checkGit();
    }

    @PostMapping("/accounts")
    public Mono<Accounts> getAllAccounts(@RequestBody String requestBody) {
        log.info("Fetch All Accounts Service invoked with Request: {}", requestBody);
        return paymentService.getAllAccounts(requestBody);
    }
    @PostMapping("/accounts/save")
    public Mono<Accounts> getAndSaveAllAccounts(@RequestBody String requestBody) {
        log.info("Fetch All Accounts & Persist Service invoked with Request: {}", requestBody);
        return paymentService.getAllAccounts(requestBody)
                .flatMap(accounts -> paymentService.saveAccountsToDatabase(accounts)
                        .then(Mono.just(accounts)));
    }
    @PostMapping("/accounts/stream")
    public Flux<Accounts> getAllAccountsStream(@RequestBody String requestBody) {
        return paymentService.getAllAccountsStream(requestBody);
    }


}
