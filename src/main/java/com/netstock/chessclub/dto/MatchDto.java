package com.netstock.chessclub.dto;

import com.netstock.chessclub.entity.Match;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for match creation and updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDto {
    
    @NotNull(message = "Player 1 is required")
    private Long player1Id;
    
    @NotNull(message = "Player 2 is required")
    private Long player2Id;
    
    @NotNull(message = "Match result is required")
    private Match.MatchResult result;
    
    private LocalDateTime matchDate;
    
    private String notes;
}