package com.netstock.chessclub.exception;

/**
 * Exception thrown when attempting to create an invalid match.
 */
public class InvalidMatchException extends RuntimeException {
    
    public InvalidMatchException(String message) {
        super(message);
    }
    
    public InvalidMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}