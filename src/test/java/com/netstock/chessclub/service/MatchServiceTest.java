package com.netstock.chessclub.service;

import com.netstock.chessclub.dto.MatchDto;
import com.netstock.chessclub.dto.MemberStatistics;
import com.netstock.chessclub.entity.Match;
import com.netstock.chessclub.entity.Member;
import com.netstock.chessclub.exception.InvalidMatchException;
import com.netstock.chessclub.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// Tests for MatchService business logic
class MatchServiceTest {
    
    @Mock
    private MatchRepository matchRepository;
    
    @Mock
    private MemberService memberService;
    
    @Mock
    private RankingService rankingService;
    
    @InjectMocks
    private MatchService matchService;
    
    private Member player1;
    private Member player2;
    private MatchDto matchDto;
    private Match match;
    
    @BeforeEach
    // Initialize test data with mock members and match objects
    void setUp() {
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
        
        matchDto = MatchDto.builder()
                .player1Id(1L)
                .player2Id(2L)
                .result(Match.MatchResult.PLAYER1_WIN)
                .notes("Great game!")
                .build();
        
        match = Match.builder()
                .id(1L)
                .player1(player1)
                .player2(player2)
                .result(Match.MatchResult.PLAYER1_WIN)
                .player1RankBefore(1)
                .player2RankBefore(2)
                .player1RankAfter(1)
                .player2RankAfter(2)
                .notes("Great game!")
                .matchDate(LocalDateTime.now())
                .build();
    }
    
    @Test
    // Test successfully recording a match between two players
    void testRecordMatch_Success() {
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(memberService.getMemberById(2L)).thenReturn(player2);
        when(rankingService.updateRankingsAfterMatch(any(Match.class))).thenReturn(match);
        when(matchRepository.save(any(Match.class))).thenReturn(match);
        
        Match result = matchService.recordMatch(matchDto);
        
        assertNotNull(result);
        assertEquals(match, result);
        
        verify(memberService).getMemberById(1L);
        verify(memberService).getMemberById(2L);
        verify(rankingService).updateRankingsAfterMatch(any(Match.class));
        verify(memberService).incrementGamesPlayed(1L);
        verify(memberService).incrementGamesPlayed(2L);
        verify(matchRepository).save(any(Match.class));
    }
    
    @Test
    // Test recording a match with custom date
    void testRecordMatch_WithCustomDate() {
        LocalDateTime customDate = LocalDateTime.of(2023, 1, 1, 12, 0);
        matchDto.setMatchDate(customDate);
        
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(memberService.getMemberById(2L)).thenReturn(player2);
        when(rankingService.updateRankingsAfterMatch(any(Match.class))).thenReturn(match);
        when(matchRepository.save(any(Match.class))).thenReturn(match);
        
        Match result = matchService.recordMatch(matchDto);
        
        assertNotNull(result);
        verify(matchRepository).save(any(Match.class));
    }
    
    @Test
    // Test recording match with same player as both players
    void testRecordMatch_SamePlayer() {
        matchDto.setPlayer2Id(1L); // Same as player1Id
        
        assertThrows(InvalidMatchException.class, () -> matchService.recordMatch(matchDto));
        
        verify(memberService, never()).getMemberById(anyLong());
        verify(matchRepository, never()).save(any());
    }
    
    @Test
    // Test recording match when player1 does not exist
    void testRecordMatch_Player1NotFound() {
        when(memberService.getMemberById(1L)).thenThrow(new RuntimeException("Member not found"));
        
        assertThrows(RuntimeException.class, () -> matchService.recordMatch(matchDto));
        
        verify(memberService).getMemberById(1L);
        verify(memberService, never()).getMemberById(2L);
        verify(matchRepository, never()).save(any());
    }
    
    @Test
    // Test recording match when player2 does not exist
    void testRecordMatch_Player2NotFound() {
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(memberService.getMemberById(2L)).thenThrow(new RuntimeException("Member not found"));
        
        assertThrows(RuntimeException.class, () -> matchService.recordMatch(matchDto));
        
        verify(memberService).getMemberById(1L);
        verify(memberService).getMemberById(2L);
        verify(matchRepository, never()).save(any());
    }
    
    @Test
    // Test retrieving all matches ordered by date
    void testGetAllMatches() {
        List<Match> expectedMatches = List.of(match);
        when(matchRepository.findAllByOrderByMatchDateDesc()).thenReturn(expectedMatches);
        
        List<Match> result = matchService.getAllMatches();
        
        assertEquals(expectedMatches, result);
        verify(matchRepository).findAllByOrderByMatchDateDesc();
    }
    
    @Test
    // Test retrieving all matches for a specific member
    void testGetMatchesForMember() {
        List<Match> expectedMatches = List.of(match);
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(matchRepository.findMatchesByMember(player1)).thenReturn(expectedMatches);
        
        List<Match> result = matchService.getMatchesForMember(1L);
        
        assertEquals(expectedMatches, result);
        verify(memberService).getMemberById(1L);
        verify(matchRepository).findMatchesByMember(player1);
    }
    
    @Test
    // Test retrieving recent matches with pagination
    void testGetRecentMatches() {
        List<Match> expectedMatches = List.of(match);
        when(matchRepository.findRecentMatches(any(Pageable.class))).thenReturn(expectedMatches);
        
        List<Match> result = matchService.getRecentMatches(10);
        
        assertEquals(expectedMatches, result);
        verify(matchRepository).findRecentMatches(argThat(pageable -> 
            pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
    }
    
    @Test
    // Test retrieving a match by ID successfully
    void testGetMatchById_Success() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        
        Match result = matchService.getMatchById(1L);
        
        assertEquals(match, result);
        verify(matchRepository).findById(1L);
    }
    
    @Test
    // Test retrieving a match by non-existent ID
    void testGetMatchById_NotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> matchService.getMatchById(99L));
        
        verify(matchRepository).findById(99L);
    }
    
    @Test
    // Test successfully deleting a match
    void testDeleteMatch_Success() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        
        matchService.deleteMatch(1L);
        
        verify(matchRepository).findById(1L);
        verify(matchRepository).delete(match);
    }
    
    @Test
    // Test deleting a non-existent match
    void testDeleteMatch_NotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> matchService.deleteMatch(99L));
        
        verify(matchRepository).findById(99L);
        verify(matchRepository, never()).delete(any());
    }
    
    @Test
    // Test getting member statistics when matches exist
    void testGetMemberStatistics_WithMatches() {
        Object[] statsData = {5L, 2L, 1L}; // wins, losses, draws
        List<Object[]> stats = new ArrayList<>();
        stats.add(statsData);
        
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(matchRepository.getMemberStatistics(1L)).thenReturn(stats);
        
        MemberStatistics result = matchService.getMemberStatistics(1L);
        
        assertNotNull(result);
        assertEquals(player1, result.getMember());
        assertEquals(8L, result.getTotalMatches()); // 5 + 2 + 1
        assertEquals(5L, result.getWins());
        assertEquals(2L, result.getLosses());
        assertEquals(1L, result.getDraws());
        assertEquals(62.5, result.getWinRate(), 0.01); // 5/8 * 100
        
        verify(memberService).getMemberById(1L);
        verify(matchRepository).getMemberStatistics(1L);
    }
    
    @Test
    // Test getting member statistics when no matches exist
    void testGetMemberStatistics_NoMatches() {
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(matchRepository.getMemberStatistics(1L)).thenReturn(null);
        
        MemberStatistics result = matchService.getMemberStatistics(1L);
        
        assertNotNull(result);
        assertEquals(player1, result.getMember());
        assertEquals(0L, result.getTotalMatches());
        assertEquals(0L, result.getWins());
        assertEquals(0L, result.getLosses());
        assertEquals(0L, result.getDraws());
        assertEquals(0.0, result.getWinRate());
        
        verify(memberService).getMemberById(1L);
        verify(matchRepository).getMemberStatistics(1L);
    }
    
    @Test
    // Test getting member statistics with empty results
    void testGetMemberStatistics_EmptyStats() {
        List<Object[]> stats = new ArrayList<>();
        
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(matchRepository.getMemberStatistics(1L)).thenReturn(stats);
        
        MemberStatistics result = matchService.getMemberStatistics(1L);
        
        assertNotNull(result);
        assertEquals(player1, result.getMember());
        assertEquals(0L, result.getTotalMatches());
        assertEquals(0L, result.getWins());
        assertEquals(0L, result.getLosses());
        assertEquals(0L, result.getDraws());
        assertEquals(0.0, result.getWinRate());
    }
    
    @Test
    // Test getting member statistics with null values
    void testGetMemberStatistics_NullValues() {
        Object[] statsData = {null, null, null}; // All null values
        List<Object[]> stats = new ArrayList<>();
        stats.add(statsData);
        
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(matchRepository.getMemberStatistics(1L)).thenReturn(stats);
        
        MemberStatistics result = matchService.getMemberStatistics(1L);
        
        assertNotNull(result);
        assertEquals(player1, result.getMember());
        assertEquals(0L, result.getTotalMatches());
        assertEquals(0L, result.getWins());
        assertEquals(0L, result.getLosses());
        assertEquals(0L, result.getDraws());
        assertEquals(0.0, result.getWinRate());
    }
    
    @Test
    // Test getting member statistics with perfect win rate
    void testGetMemberStatistics_PerfectWinRate() {
        Object[] statsData = {10L, 0L, 0L}; // 10 wins, 0 losses, 0 draws
        List<Object[]> stats = new ArrayList<>();
        stats.add(statsData);
        
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(matchRepository.getMemberStatistics(1L)).thenReturn(stats);
        
        MemberStatistics result = matchService.getMemberStatistics(1L);
        
        assertEquals(100.0, result.getWinRate());
    }
    
    @Test
    // Test getting head-to-head record between two members
    void testGetHeadToHeadRecord() {
        List<Match> expectedMatches = List.of(match);
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(memberService.getMemberById(2L)).thenReturn(player2);
        when(matchRepository.findMatchesBetweenMembers(player1, player2)).thenReturn(expectedMatches);
        
        List<Match> result = matchService.getHeadToHeadRecord(1L, 2L);
        
        assertEquals(expectedMatches, result);
        verify(memberService).getMemberById(1L);
        verify(memberService).getMemberById(2L);
        verify(matchRepository).findMatchesBetweenMembers(player1, player2);
    }
    
    @Test
    // Test head-to-head record when first member not found
    void testGetHeadToHeadRecord_Member1NotFound() {
        when(memberService.getMemberById(1L)).thenThrow(new RuntimeException("Member not found"));
        
        assertThrows(RuntimeException.class, () -> matchService.getHeadToHeadRecord(1L, 2L));
        
        verify(memberService).getMemberById(1L);
        verify(memberService, never()).getMemberById(2L);
        verify(matchRepository, never()).findMatchesBetweenMembers(any(), any());
    }
    
    @Test
    // Test head-to-head record when second member not found
    void testGetHeadToHeadRecord_Member2NotFound() {
        when(memberService.getMemberById(1L)).thenReturn(player1);
        when(memberService.getMemberById(2L)).thenThrow(new RuntimeException("Member not found"));
        
        assertThrows(RuntimeException.class, () -> matchService.getHeadToHeadRecord(1L, 2L));
        
        verify(memberService).getMemberById(1L);
        verify(memberService).getMemberById(2L);
        verify(matchRepository, never()).findMatchesBetweenMembers(any(), any());
    }
}