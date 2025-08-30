package com.netstock.chessclub.exception;

/**
 * Exception thrown when a member is not found.
 */
public class MemberNotFoundException extends RuntimeException {
    
    public MemberNotFoundException(String message) {
        super(message);
    }
    
    public MemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}