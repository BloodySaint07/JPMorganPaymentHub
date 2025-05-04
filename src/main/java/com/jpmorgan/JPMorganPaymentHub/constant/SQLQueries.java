package com.jpmorgan.JPMorganPaymentHub.constant;

public class SQLQueries {
    public static final String FETCH_TRAN_DETAILS_BY_TRANSACTION_REF="SELECT td FROM TransactionDetail td WHERE td.referenceNumber = :transactionReferenceNumber";
    public static final String FETCH_TRAN_DESC_BY_TRANSACTION_REF="SELECT td.description FROM TransactionDetail td WHERE td.referenceNumber = :transactionReferenceNumber";
}
