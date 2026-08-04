package com.school.exceptions;

public class AccountDisableException extends RuntimeException {
    public AccountDisableException(String message) {
        super(message);
    }
}
