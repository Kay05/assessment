package com.netstock.chessclub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a match between two chess club members.
 * Tracks match results and ranking changes.
 */
@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Player 1 is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player1_id", nullable = false)
    private Member player1;
    
    @NotNull(message = "Player 2 is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player2_id", nullable = false)
    private Member player2;
    
    @NotNull(message = "Match result is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchResult result;
    
    // Store the ranks at the time of the match for historical tracking
    @Column(nullable = false)
    private Integer player1RankBefore;
    
    @Column(nullable = false)
    private Integer player2RankBefore;
    
    @Column(nullable = false)
    private Integer player1RankAfter;
    
    @Column(nullable = false)
    private Integer player2RankAfter;
    
    @Column(nullable = false)
    private LocalDateTime matchDate;
    
    @Column(length = 500)
    private String notes;
    
    /**
     * Enum representing possible match outcomes
     */
    public enum MatchResult {
        PLAYER1_WIN("Player 1 Wins"),
        PLAYER2_WIN("Player 2 Wins"),
        DRAW("Draw");
        
        private final String displayName;
        
        MatchResult(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (matchDate == null) {
            matchDate = LocalDateTime.now();
        }
    }
    
    /**
     * Get the winner of the match
     * @return The winning member or null if draw
     */
    public Member getWinner() {
        if (result == MatchResult.PLAYER1_WIN) {
            return player1;
        } else if (result == MatchResult.PLAYER2_WIN) {
            return player2;
        }
        return null;
    }
    
    /**
     * Check if the match resulted in a draw
     * @return true if the match was a draw
     */
    public boolean isDraw() {
        return result == MatchResult.DRAW;
    }
}