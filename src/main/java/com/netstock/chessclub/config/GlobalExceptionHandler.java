package com.netstock.chessclub.config;

import com.netstock.chessclub.exception.DuplicateEmailException;
import com.netstock.chessclub.exception.InvalidMatchException;
import com.netstock.chessclub.exception.MemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler for the chess club application.
 * Handles exceptions and provides user-friendly error messages.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handle member not found exceptions
     * @param ex The exception
     * @param redirectAttributes Redirect attributes for error message
     * @return Redirect to members list
     */
    @ExceptionHandler(MemberNotFoundException.class)
    public String handleMemberNotFound(MemberNotFoundException ex, RedirectAttributes redirectAttributes) {
        log.error("Member not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/members";
    }
    
    /**
     * Handle duplicate email exceptions
     * @param ex The exception
     * @param redirectAttributes Redirect attributes for error message
     * @return Redirect to members list
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public String handleDuplicateEmail(DuplicateEmailException ex, RedirectAttributes redirectAttributes) {
        log.error("Duplicate email error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/members";
    }
    
    /**
     * Handle invalid match exceptions
     * @param ex The exception
     * @param redirectAttributes Redirect attributes for error message
     * @return Redirect to match form
     */
    @ExceptionHandler(InvalidMatchException.class)
    public String handleInvalidMatch(InvalidMatchException ex, RedirectAttributes redirectAttributes) {
        log.error("Invalid match error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/matches/new";
    }
    
    /**
     * Handle general runtime exceptions
     * @param ex The exception
     * @param redirectAttributes Redirect attributes for error message
     * @return Redirect to home page
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("errorMessage", 
            "An unexpected error occurred. Please try again or contact support if the problem persists.");
        return "redirect:/";
    }
}