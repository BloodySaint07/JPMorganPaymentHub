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
        return paymentService.ping();
    }

    @PostMapping("/accounts")
    public Mono<Accounts> getAllAccounts(@RequestBody String requestBody) {
        return paymentService.getAllAccounts(requestBody);
    }
    @PostMapping("/accounts/stream")
    public Flux<Accounts> getAllAccountsStream(@RequestBody String requestBody) {
        return paymentService.getAllAccountsStream(requestBody);
    }


}
