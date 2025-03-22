package com.jpmorgan.JPMorganPaymentHub.utility;

import com.jpmorgan.JPMorganPaymentHub.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class PaymentUtility {
    Logger log = LoggerFactory.getLogger(PaymentUtility.class);

    public String getSecret() {
        try {
            Path filePath = Paths.get(new ClassPathResource("secret.txt").getURI());
            String token = Files.readString(filePath);
            log.info("File content: {}", token);
            return token;
        } catch (IOException e) {
            log.error("Failed to read secret file", e);
            e.printStackTrace();
        }
        return "";
    }

    public void logAccountDetails(List<Account> accounts) {
        accounts.forEach(account -> log.info(
                "Account Details fetched for Holder: {} with Account Number: {} from Country: {}",
                account.getAccountName(), account.getAccountNumber(), account.getCountry()
        ));
    }
    public String guessProvider(String cardNumber) {
        if (cardNumber.startsWith("4")) return "Visa";
        if (cardNumber.startsWith("5")) return "MasterCard";
        return "Unknown";
    }

}
