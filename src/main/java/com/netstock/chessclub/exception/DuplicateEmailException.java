package com.netstock.chessclub.exception;

/**
 * Exception thrown when attempting to create or update a member with a duplicate email.
 */
public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String message) {
        super(message);
    }
    
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}