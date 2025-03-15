package com.jpmorgan.JPMorganPaymentHub.enums;

public enum CustomErrorCode {
    ERR_4211("ERR-4211", "An unexpected error occurred.Please contact System Administrator."),
    ERR_4212("ERR-4212", "Invalid argument provided"),
    ERR_4252("ERR-4252", "Bad Request"),
    ERR_4253("ERR-4253", "Resource not found"),
    ERR_4254("ERR-4254", ""),
    ERR_4389("ERR-4389", ""),
    GET_ALL_ACCOUNTS_CB_ERROR("ERR-4255", "Circuit breaker is now OPEN for getAllAccounts, calls blocked"),
    GET_ALL_ACCOUNTS_RL_ERROR("ERR-4256", "System is under heavy Load, limiting calls temporarily"),
    GET_ALL_ACCOUNTS_BH_ERROR("ERR-4257", "System is under heavy Load, too many concurrent calls.Limiting calls temporarily");
    private final String code;
    private final String message;

    CustomErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
