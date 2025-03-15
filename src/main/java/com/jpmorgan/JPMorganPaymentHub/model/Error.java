package com.jpmorgan.JPMorganPaymentHub.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Error {
    private String errorMessage;
    private String errorCode;
    private String errorDescription;
}
