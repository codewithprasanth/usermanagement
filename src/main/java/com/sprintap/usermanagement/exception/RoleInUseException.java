package com.sprintap.usermanagement.exception;

public class RoleInUseException extends RuntimeException {
    public RoleInUseException(String message) {
        super(message);
    }
}

