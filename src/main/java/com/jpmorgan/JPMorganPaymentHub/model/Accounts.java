package com.jpmorgan.JPMorganPaymentHub.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Accounts {
    private List<Account> accounts;
    private Error errorResult;
}