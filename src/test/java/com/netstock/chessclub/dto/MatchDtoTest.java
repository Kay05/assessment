package com.netstock.chessclub.dto;

import com.netstock.chessclub.entity.Match;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// Test class for MatchDto data transfer object
class MatchDtoTest {
    
    @Test
    void testBuilder() {
        // Test MatchDto builder creates DTO with all properties
        LocalDateTime matchDate = LocalDateTime.now();
        
        MatchDto matchDto = MatchDto.builder()
                .player1Id(1L)
                .player2Id(2L)
                .result(Match.MatchResult.PLAYER1_WIN)
                .matchDate(matchDate)
                .notes("Great game!")
                .build();
        
        assertEquals(1L, matchDto.getPlayer1Id());
        assertEquals(2L, matchDto.getPlayer2Id());
        assertEquals(Match.MatchResult.PLAYER1_WIN, matchDto.getResult());
        assertEquals(matchDate, matchDto.getMatchDate());
        assertEquals("Great game!", matchDto.getNotes());
    }
    
    @Test
    void testNoArgsConstructor() {
        // Test no-args constructor creates DTO with null values
        MatchDto matchDto = new MatchDto();
        
        assertNull(matchDto.getPlayer1Id());
        assertNull(matchDto.getPlayer2Id());
        assertNull(matchDto.getResult());
        assertNull(matchDto.getMatchDate());
        assertNull(matchDto.getNotes());
    }
    
    @Test
    void testAllArgsConstructor() {
        // Test all-args constructor sets all properties
        LocalDateTime matchDate = LocalDateTime.now();
        
        MatchDto matchDto = new MatchDto(1L, 2L, Match.MatchResult.DRAW, matchDate, "Close match");
        
        assertEquals(1L, matchDto.getPlayer1Id());
        assertEquals(2L, matchDto.getPlayer2Id());
        assertEquals(Match.MatchResult.DRAW, matchDto.getResult());
        assertEquals(matchDate, matchDto.getMatchDate());
        assertEquals("Close match", matchDto.getNotes());
    }
    
    @Test
    void testSettersAndGetters() {
        // Test all setters and getters work correctly
        MatchDto matchDto = new MatchDto();
        LocalDateTime matchDate = LocalDateTime.now();
        
        matchDto.setPlayer1Id(1L);
        matchDto.setPlayer2Id(2L);
        matchDto.setResult(Match.MatchResult.PLAYER2_WIN);
        matchDto.setMatchDate(matchDate);
        matchDto.setNotes("Excellent match");
        
        assertEquals(1L, matchDto.getPlayer1Id());
        assertEquals(2L, matchDto.getPlayer2Id());
        assertEquals(Match.MatchResult.PLAYER2_WIN, matchDto.getResult());
        assertEquals(matchDate, matchDto.getMatchDate());
        assertEquals("Excellent match", matchDto.getNotes());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Test equals and hashCode for MatchDto objects
        LocalDateTime matchDate = LocalDateTime.now();
        
        MatchDto dto1 = MatchDto.builder()
                .player1Id(1L)
                .player2Id(2L)
                .result(Match.MatchResult.DRAW)
                .matchDate(matchDate)
                .notes("Test")
                .build();
        
        MatchDto dto2 = MatchDto.builder()
                .player1Id(1L)
                .player2Id(2L)
                .result(Match.MatchResult.DRAW)
                .matchDate(matchDate)
                .notes("Test")
                .build();
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
    
    @Test
    void testNotEquals() {
        // Test not equals for different MatchDto objects
        MatchDto dto1 = MatchDto.builder()
                .player1Id(1L)
                .player2Id(2L)
                .result(Match.MatchResult.PLAYER1_WIN)
                .build();
        
        MatchDto dto2 = MatchDto.builder()
                .player1Id(1L)
                .player2Id(2L)
                .result(Match.MatchResult.PLAYER2_WIN)
                .build();
        
        assertNotEquals(dto1, dto2);
    }
    
    @Test
    void testToString() {
        // Test toString contains all properties
        MatchDto matchDto = MatchDto.builder()
                .player1Id(1L)
                .player2Id(2L)
                .result(Match.MatchResult.DRAW)
                .notes("Test match")
                .build();
        
        String toString = matchDto.toString();
        
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("2"));
        assertTrue(toString.contains("DRAW"));
        assertTrue(toString.contains("Test match"));
    }
}