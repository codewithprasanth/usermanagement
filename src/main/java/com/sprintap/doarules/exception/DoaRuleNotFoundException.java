package com.sprintap.doarules.exception;

/**
 * Exception thrown when a DOA rule is not found
 */
public class DoaRuleNotFoundException extends RuntimeException {

    public DoaRuleNotFoundException(String message) {
        super(message);
    }

    public DoaRuleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

