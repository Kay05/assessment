package com.netstock.chessclub.dto;

import com.netstock.chessclub.entity.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Test class for MemberStatistics DTO
class MemberStatisticsTest {
    
    @Test
    void testBuilder() {
        // Test MemberStatistics builder creates statistics with all properties
        Member member = Member.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .currentRank(1)
                .build();
        
        MemberStatistics statistics = MemberStatistics.builder()
                .member(member)
                .totalMatches(10L)
                .wins(7L)
                .losses(2L)
                .draws(1L)
                .winRate(70.0)
                .build();
        
        assertEquals(member, statistics.getMember());
        assertEquals(10L, statistics.getTotalMatches());
        assertEquals(7L, statistics.getWins());
        assertEquals(2L, statistics.getLosses());
        assertEquals(1L, statistics.getDraws());
        assertEquals(70.0, statistics.getWinRate());
    }
    
    @Test
    void testGetFormattedWinRate() {
        // Test formatted win rate returns percentage string
        MemberStatistics statistics = MemberStatistics.builder()
                .winRate(75.5)
                .build();
        
        String formatted = statistics.getFormattedWinRate();
        assertTrue(formatted.contains("75") && formatted.contains("5") && formatted.contains("%"));
    }
    
    @Test
    void testGetFormattedWinRate_ZeroWinRate() {
        // Test formatted win rate for 0% win rate
        MemberStatistics statistics = MemberStatistics.builder()
                .winRate(0.0)
                .build();
        
        String formatted = statistics.getFormattedWinRate();
        assertTrue(formatted.contains("0") && formatted.contains("%"));
    }
    
    @Test
    void testGetFormattedWinRate_PerfectWinRate() {
        // Test formatted win rate for 100% win rate
        MemberStatistics statistics = MemberStatistics.builder()
                .winRate(100.0)
                .build();
        
        String formatted = statistics.getFormattedWinRate();
        assertTrue(formatted.contains("100") && formatted.contains("%"));
    }
    
    @Test
    void testGetFormattedWinRate_RoundedValue() {
        // Test formatted win rate rounds decimal values
        MemberStatistics statistics = MemberStatistics.builder()
                .winRate(66.666)
                .build();
        
        String formatted = statistics.getFormattedWinRate();
        assertTrue(formatted.contains("66") && formatted.contains("7") && formatted.contains("%"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Test equals and hashCode for MemberStatistics objects
        Member member = Member.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .build();
        
        MemberStatistics stats1 = MemberStatistics.builder()
                .member(member)
                .totalMatches(10L)
                .wins(7L)
                .losses(2L)
                .draws(1L)
                .winRate(70.0)
                .build();
        
        MemberStatistics stats2 = MemberStatistics.builder()
                .member(member)
                .totalMatches(10L)
                .wins(7L)
                .losses(2L)
                .draws(1L)
                .winRate(70.0)
                .build();
        
        assertEquals(stats1, stats2);
        assertEquals(stats1.hashCode(), stats2.hashCode());
    }
    
    @Test
    void testNotEquals() {
        // Test not equals for different MemberStatistics objects
        Member member1 = Member.builder().id(1L).name("John").build();
        Member member2 = Member.builder().id(2L).name("Jane").build();
        
        MemberStatistics stats1 = MemberStatistics.builder()
                .member(member1)
                .totalMatches(10L)
                .build();
        
        MemberStatistics stats2 = MemberStatistics.builder()
                .member(member2)
                .totalMatches(10L)
                .build();
        
        assertNotEquals(stats1, stats2);
    }
}