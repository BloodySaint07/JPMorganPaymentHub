package com.jpmorgan.JPMorganPaymentHub.validation;


import com.jpmorgan.JPMorganPaymentHub.model.TransactionRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TransactionValidator implements Validator {
    private static final List<String> TRANSACTION_TYPE_LIST = List.of("DEBIT","CREDIT");
    @Override
    public boolean supports(Class<?> clazz) {
        return TransactionRequestDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TransactionRequestDTO requestDTO = (TransactionRequestDTO) target;
        if (requestDTO.getAmount() == null || requestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.rejectValue("amount", "amount.invalid", "Amount must be greater than zero");
        }
        if (requestDTO.getReferenceNumber() == null || requestDTO.getReferenceNumber().isEmpty()) {
            errors.rejectValue("referenceNumber", "referenceNumber.empty", "Reference number cannot be empty");
        }
        if (requestDTO.getDescription() == null || requestDTO.getDescription().isEmpty()) {
            errors.rejectValue("description", "description.empty", "Description cannot be empty");
        }
        if (!TRANSACTION_TYPE_LIST.contains(requestDTO.getTransactionType())) {
            errors.rejectValue("transactionType", "transactionType.invalid", "Transaction Type must be either DEBIT or CREDIT");
        }

    }
}
