package com.netstock.chessclub.dto;

import com.netstock.chessclub.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for member match statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStatistics {
    
    private Member member;
    private Long totalMatches;
    private Long wins;
    private Long losses;
    private Long draws;
    private Double winRate;
    
    /**
     * Get formatted win rate as percentage string
     * @return Win rate formatted as "XX.X%"
     */
    public String getFormattedWinRate() {
        return String.format("%.1f%%", winRate);
    }
}