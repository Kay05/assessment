package com.netstock.chessclub.repository;

import com.netstock.chessclub.entity.Match;
import com.netstock.chessclub.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Test class for MatchRepository
class MatchRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private MatchRepository matchRepository;
    
    private Member player1;
    private Member player2;
    private Member player3;
    private Match match1;
    private Match match2;
    private Match match3;
    
    @BeforeEach
    void setUp() {
        // Set up test players and matches
        player1 = Member.builder()
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .currentRank(1)
                .gamesPlayed(10)
                .build();
        
        player2 = Member.builder()
                .name("Jane")
                .surname("Smith")
                .email("jane@example.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .currentRank(2)
                .gamesPlayed(5)
                .build();
        
        player3 = Member.builder()
                .name("Bob")
                .surname("Johnson")
                .email("bob@example.com")
                .birthday(LocalDate.of(1985, 12, 10))
                .currentRank(3)
                .gamesPlayed(15)
                .build();
        
        // Ensure timestamps are set for test entities
        LocalDateTime timestamp = LocalDateTime.now();
        player1.setCreatedAt(timestamp);
        player1.setUpdatedAt(timestamp);
        player2.setCreatedAt(timestamp);
        player2.setUpdatedAt(timestamp);
        player3.setCreatedAt(timestamp);
        player3.setUpdatedAt(timestamp);
        
        entityManager.persistAndFlush(player1);
        entityManager.persistAndFlush(player2);
        entityManager.persistAndFlush(player3);
        
        LocalDateTime now = LocalDateTime.now();
        
        match1 = Match.builder()
                .player1(player1)
                .player2(player2)
                .result(Match.MatchResult.PLAYER1_WIN)
                .player1RankBefore(1)
                .player2RankBefore(2)
                .player1RankAfter(1)
                .player2RankAfter(2)
                .matchDate(now.minusDays(2))
                .notes("Great game")
                .build();
        
        match2 = Match.builder()
                .player1(player2)
                .player2(player3)
                .result(Match.MatchResult.DRAW)
                .player1RankBefore(2)
                .player2RankBefore(3)
                .player1RankAfter(2)
                .player2RankAfter(3)
                .matchDate(now.minusDays(1))
                .build();
        
        match3 = Match.builder()
                .player1(player1)
                .player2(player3)
                .result(Match.MatchResult.PLAYER2_WIN)
                .player1RankBefore(1)
                .player2RankBefore(3)
                .player1RankAfter(2)
                .player2RankAfter(1)
                .matchDate(now)
                .notes("Upset victory")
                .build();
        
        entityManager.persistAndFlush(match1);
        entityManager.persistAndFlush(match2);
        entityManager.persistAndFlush(match3);
    }
    
    @Test
    void testFindMatchesByMember() {
        // Test finding all matches for a member
        List<Match> matches = matchRepository.findMatchesByMember(player1);
        
        assertEquals(2, matches.size());
        assertTrue(matches.contains(match1));
        assertTrue(matches.contains(match3));
        // Should be ordered by date descending
        assertEquals(match3, matches.get(0));
        assertEquals(match1, matches.get(1));
    }
    
    @Test
    void testFindMatchesByMemberNotFound() {
        // Test finding matches for member with no matches
        Member newPlayer = Member.builder()
                .name("Alice")
                .surname("Wonder")
                .email("alice@example.com")
                .birthday(LocalDate.of(1992, 3, 20))
                .currentRank(4)
                .gamesPlayed(0)
                .build();
        entityManager.persistAndFlush(newPlayer);
        
        List<Match> matches = matchRepository.findMatchesByMember(newPlayer);
        
        assertTrue(matches.isEmpty());
    }
    
    @Test
    void testFindMatchesBetweenMembers() {
        // Test finding matches between two members
        List<Match> matches = matchRepository.findMatchesBetweenMembers(player1, player2);
        
        assertEquals(1, matches.size());
        assertEquals(match1, matches.get(0));
    }
    
    @Test
    void testFindMatchesBetweenMembersReversed() {
        List<Match> matches = matchRepository.findMatchesBetweenMembers(player2, player1);
        
        assertEquals(1, matches.size());
        assertEquals(match1, matches.get(0));
    }
    
    @Test
    void testFindMatchesBetweenMembersNoMatches() {
        Member newPlayer = Member.builder()
                .name("Alice")
                .surname("Wonder")
                .email("alice@example.com")
                .birthday(LocalDate.of(1992, 3, 20))
                .currentRank(4)
                .gamesPlayed(0)
                .build();
        entityManager.persistAndFlush(newPlayer);
        
        List<Match> matches = matchRepository.findMatchesBetweenMembers(player1, newPlayer);
        
        assertTrue(matches.isEmpty());
    }
    
    @Test
    void testFindRecentMatches() {
        // Test finding recent matches with pagination
        List<Match> matches = matchRepository.findRecentMatches(PageRequest.of(0, 10));
        
        assertEquals(3, matches.size());
        // Should be ordered by date descending
        assertEquals(match3, matches.get(0));
        assertEquals(match2, matches.get(1));
        assertEquals(match1, matches.get(2));
    }
    
    @Test
    void testFindByMatchDateBetweenOrderByMatchDateDesc() {
        // Test finding matches within date range
        LocalDateTime start = LocalDateTime.now().minusDays(1).minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        
        List<Match> matches = matchRepository.findByMatchDateBetweenOrderByMatchDateDesc(start, end);
        
        assertEquals(2, matches.size());
        assertTrue(matches.contains(match2));
        assertTrue(matches.contains(match3));
        // Should be ordered by date descending
        assertEquals(match3, matches.get(0));
        assertEquals(match2, matches.get(1));
    }
    
    @Test
    void testCountWinsForMember() {
        // Test counting wins for a member
        long wins = matchRepository.countWinsForMember(player1);
        
        assertEquals(1, wins); // player1 won match1
    }
    
    @Test
    void testCountWinsForMemberMultiple() {
        long wins = matchRepository.countWinsForMember(player3);
        
        assertEquals(1, wins); // player3 won match3
    }
    
    @Test
    void testCountWinsForMemberNoWins() {
        long wins = matchRepository.countWinsForMember(player2);
        
        assertEquals(0, wins); // player2 has no wins
    }
    
    @Test
    void testCountLossesForMember() {
        // Test counting losses for a member
        long losses = matchRepository.countLossesForMember(player1);
        
        assertEquals(1, losses); // player1 lost match3
    }
    
    @Test
    void testCountLossesForMemberMultiple() {
        long losses = matchRepository.countLossesForMember(player2);
        
        assertEquals(1, losses); // player2 lost match1
    }
    
    @Test
    void testCountLossesForMemberNoLosses() {
        long losses = matchRepository.countLossesForMember(player3);
        
        assertEquals(0, losses); // player3 won match3, had draw in match2 (draw doesn't count as loss)
    }
    
    @Test
    void testCountDrawsForMember() {
        // Test counting draws for a member
        long draws = matchRepository.countDrawsForMember(player2);
        
        assertEquals(1, draws); // player2 had a draw in match2
    }
    
    @Test
    void testCountDrawsForMemberMultiple() {
        long draws = matchRepository.countDrawsForMember(player3);
        
        assertEquals(1, draws); // player3 had a draw in match2
    }
    
    @Test
    void testCountDrawsForMemberNoDraws() {
        long draws = matchRepository.countDrawsForMember(player1);
        
        assertEquals(0, draws); // player1 has no draws
    }
    
    @Test
    void testGetMemberStatistics() {
        // Test getting complete member statistics
        List<Object[]> stats = matchRepository.getMemberStatistics(player1.getId());
        
        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        
        Object[] statArray = stats.get(0);
        assertEquals(3, statArray.length); // Should return 3 values: [wins, losses, draws]
        
        // Convert to Long for comparison (JPA may return different numeric types)
        Long wins = statArray[0] != null ? ((Number) statArray[0]).longValue() : 0L;
        Long losses = statArray[1] != null ? ((Number) statArray[1]).longValue() : 0L;
        Long draws = statArray[2] != null ? ((Number) statArray[2]).longValue() : 0L;
        
        assertEquals(1L, wins);   // 1 win (match1)
        assertEquals(1L, losses); // 1 loss (match3)
        assertEquals(0L, draws);  // 0 draws
    }
    
    @Test
    void testGetMemberStatisticsWithDraws() {
        // Test member statistics including draws
        List<Object[]> stats = matchRepository.getMemberStatistics(player2.getId());
        
        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        
        Object[] statArray = stats.get(0);
        assertEquals(3, statArray.length); // Should return 3 values: [wins, losses, draws]
        
        Long wins = statArray[0] != null ? ((Number) statArray[0]).longValue() : 0L;
        Long losses = statArray[1] != null ? ((Number) statArray[1]).longValue() : 0L;
        Long draws = statArray[2] != null ? ((Number) statArray[2]).longValue() : 0L;
        
        assertEquals(0L, wins);   // 0 wins
        assertEquals(1L, losses); // 1 loss (match1)
        assertEquals(1L, draws);  // 1 draw (match2)
    }
    
    @Test
    void testFindAllByOrderByMatchDateDesc() {
        // Test finding all matches ordered by date
        List<Match> matches = matchRepository.findAllByOrderByMatchDateDesc();
        
        assertEquals(3, matches.size());
        // Should be ordered by date descending
        assertEquals(match3, matches.get(0));
        assertEquals(match2, matches.get(1));
        assertEquals(match1, matches.get(2));
    }
}