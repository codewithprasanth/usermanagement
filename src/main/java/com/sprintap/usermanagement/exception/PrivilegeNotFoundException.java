package com.sprintap.usermanagement.exception;

public class PrivilegeNotFoundException extends RuntimeException {
    public PrivilegeNotFoundException(String message) {
        super(message);
    }
}

