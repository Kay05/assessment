package com.netstock.chessclub.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// Test class for Match entity
class MatchTest {
    
    private Match match;
    private Member player1;
    private Member player2;
    
    @BeforeEach
    void setUp() {
        // Initialize test players and match
        player1 = Member.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .currentRank(1)
                .gamesPlayed(10)
                .build();
        
        player2 = Member.builder()
                .id(2L)
                .name("Jane")
                .surname("Smith")
                .email("jane@example.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .currentRank(2)
                .gamesPlayed(5)
                .build();
        
        match = new Match();
    }
    
    @Test
    void testMatchBuilder() {
        // Test Match builder creates match with all properties
        LocalDateTime matchDate = LocalDateTime.now();
        
        Match builtMatch = Match.builder()
                .player1(player1)
                .player2(player2)
                .result(Match.MatchResult.PLAYER1_WIN)
                .player1RankBefore(1)
                .player2RankBefore(2)
                .player1RankAfter(1)
                .player2RankAfter(2)
                .matchDate(matchDate)
                .notes("Great match!")
                .build();
        
        assertEquals(player1, builtMatch.getPlayer1());
        assertEquals(player2, builtMatch.getPlayer2());
        assertEquals(Match.MatchResult.PLAYER1_WIN, builtMatch.getResult());
        assertEquals(1, builtMatch.getPlayer1RankBefore());
        assertEquals(2, builtMatch.getPlayer2RankBefore());
        assertEquals(1, builtMatch.getPlayer1RankAfter());
        assertEquals(2, builtMatch.getPlayer2RankAfter());
        assertEquals(matchDate, builtMatch.getMatchDate());
        assertEquals("Great match!", builtMatch.getNotes());
    }
    
    @Test
    void testGetWinnerPlayer1Wins() {
        // Test getWinner returns player1 when player1 wins
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setResult(Match.MatchResult.PLAYER1_WIN);
        
        assertEquals(player1, match.getWinner());
    }
    
    @Test
    void testGetWinnerPlayer2Wins() {
        // Test getWinner returns player2 when player2 wins
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setResult(Match.MatchResult.PLAYER2_WIN);
        
        assertEquals(player2, match.getWinner());
    }
    
    @Test
    void testGetWinnerDraw() {
        // Test getWinner returns null for draw
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setResult(Match.MatchResult.DRAW);
        
        assertNull(match.getWinner());
    }
    
    @Test
    void testIsDrawTrue() {
        // Test isDraw returns true for draw result
        match.setResult(Match.MatchResult.DRAW);
        assertTrue(match.isDraw());
    }
    
    @Test
    void testIsDrawFalsePlayer1Win() {
        // Test isDraw returns false when player1 wins
        match.setResult(Match.MatchResult.PLAYER1_WIN);
        assertFalse(match.isDraw());
    }
    
    @Test
    void testIsDrawFalsePlayer2Win() {
        // Test isDraw returns false when player2 wins
        match.setResult(Match.MatchResult.PLAYER2_WIN);
        assertFalse(match.isDraw());
    }
    
    @Test
    void testOnCreateSetsMatchDate() {
        // Test onCreate sets match date to current time
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        match.onCreate();
        
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertNotNull(match.getMatchDate());
        assertTrue(match.getMatchDate().isAfter(before));
        assertTrue(match.getMatchDate().isBefore(after));
    }
    
    @Test
    void testOnCreateDoesNotOverrideExistingMatchDate() {
        // Test onCreate preserves existing match date
        LocalDateTime originalDate = LocalDateTime.of(2023, 1, 1, 12, 0);
        match.setMatchDate(originalDate);
        
        match.onCreate();
        
        assertEquals(originalDate, match.getMatchDate());
    }
    
    @Test
    void testMatchResultEnum() {
        // Test MatchResult enum display names
        assertEquals("Player 1 Wins", Match.MatchResult.PLAYER1_WIN.getDisplayName());
        assertEquals("Player 2 Wins", Match.MatchResult.PLAYER2_WIN.getDisplayName());
        assertEquals("Draw", Match.MatchResult.DRAW.getDisplayName());
    }
    
    @Test
    void testMatchResultValues() {
        // Test MatchResult enum values array
        Match.MatchResult[] results = Match.MatchResult.values();
        assertEquals(3, results.length);
        assertEquals(Match.MatchResult.PLAYER1_WIN, results[0]);
        assertEquals(Match.MatchResult.PLAYER2_WIN, results[1]);
        assertEquals(Match.MatchResult.DRAW, results[2]);
    }
    
    @Test
    void testMatchResultValueOf() {
        // Test MatchResult valueOf method
        assertEquals(Match.MatchResult.PLAYER1_WIN, Match.MatchResult.valueOf("PLAYER1_WIN"));
        assertEquals(Match.MatchResult.PLAYER2_WIN, Match.MatchResult.valueOf("PLAYER2_WIN"));
        assertEquals(Match.MatchResult.DRAW, Match.MatchResult.valueOf("DRAW"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Test equals and hashCode for Match objects
        Match match1 = Match.builder()
                .id(1L)
                .player1(player1)
                .player2(player2)
                .result(Match.MatchResult.DRAW)
                .build();
        
        Match match2 = Match.builder()
                .id(1L)
                .player1(player1)
                .player2(player2)
                .result(Match.MatchResult.DRAW)
                .build();
        
        assertEquals(match1, match2);
        assertEquals(match1.hashCode(), match2.hashCode());
    }
    
    @Test
    void testNotEquals() {
        // Test not equals for different Match objects
        Match match1 = Match.builder()
                .id(1L)
                .result(Match.MatchResult.PLAYER1_WIN)
                .build();
        
        Match match2 = Match.builder()
                .id(2L)
                .result(Match.MatchResult.PLAYER2_WIN)
                .build();
        
        assertNotEquals(match1, match2);
    }
}