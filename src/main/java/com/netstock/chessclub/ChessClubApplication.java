package com.netstock.chessclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Chess Club Administration System.
 * This application manages chess club members, their rankings, and match results.
 */
@SpringBootApplication
public class ChessClubApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ChessClubApplication.class, args);
    }
}