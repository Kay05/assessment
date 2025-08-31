package com.netstock.chessclub.service;

import com.netstock.chessclub.dto.MatchDto;
import com.netstock.chessclub.dto.MemberStatistics;
import com.netstock.chessclub.entity.Match;
import com.netstock.chessclub.entity.Member;
import com.netstock.chessclub.exception.InvalidMatchException;
import com.netstock.chessclub.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing chess matches.
 * Handles match recording and statistics tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchService {
    
    private final MatchRepository matchRepository;
    private final MemberService memberService;
    private final RankingService rankingService;
    
    /**
     * Record a new match and update rankings
     * @param matchDto The match data
     * @return The recorded match with updated rankings
     */
    public Match recordMatch(MatchDto matchDto) {
        log.info("Recording match between players {} and {}", 
                matchDto.getPlayer1Id(), matchDto.getPlayer2Id());
        
        // Validate match
        if (matchDto.getPlayer1Id().equals(matchDto.getPlayer2Id())) {
            throw new InvalidMatchException("A player cannot play against themselves");
        }
        
        // Get the players
        Member player1 = memberService.getMemberById(matchDto.getPlayer1Id());
        Member player2 = memberService.getMemberById(matchDto.getPlayer2Id());
        
        // Create the match
        Match match = Match.builder()
                .player1(player1)
                .player2(player2)
                .result(matchDto.getResult())
                .notes(matchDto.getNotes())
                .matchDate(matchDto.getMatchDate() != null ? matchDto.getMatchDate() : LocalDateTime.now())
                .player1RankBefore(player1.getCurrentRank())
                .player2RankBefore(player2.getCurrentRank())
                .build();
        
        // Update rankings based on match result
        match = rankingService.updateRankingsAfterMatch(match);
        
        // Increment games played for both players
        memberService.incrementGamesPlayed(player1.getId());
        memberService.incrementGamesPlayed(player2.getId());
        
        // Save the match
        Match savedMatch = matchRepository.save(match);
        
        log.info("Match recorded successfully with ID: {}", savedMatch.getId());
        
        return savedMatch;
    }
    
    /**
     * Get all matches ordered by date
     * @return List of all matches
     */
    @Transactional(readOnly = true)
    public List<Match> getAllMatches() {
        log.debug("Fetching all matches");
        return matchRepository.findAllByOrderByMatchDateDesc();
    }
    
    /**
     * Get matches for a specific member
     * @param memberId The member ID
     * @return List of matches for the member
     */
    @Transactional(readOnly = true)
    public List<Match> getMatchesForMember(Long memberId) {
        log.debug("Fetching matches for member ID: {}", memberId);
        Member member = memberService.getMemberById(memberId);
        return matchRepository.findMatchesByMember(member);
    }
    
    /**
     * Get recent matches
     * @param limit Number of matches to retrieve
     * @return List of recent matches
     */
    @Transactional(readOnly = true)
    public List<Match> getRecentMatches(int limit) {
        log.debug("Fetching {} recent matches", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by("matchDate").descending());
        return matchRepository.findRecentMatches(pageable);
    }
    
    /**
     * Get match by ID
     * @param id The match ID
     * @return The match
     */
    @Transactional(readOnly = true)
    public Match getMatchById(Long id) {
        log.debug("Fetching match with ID: {}", id);
        return matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found with ID: " + id));
    }
    
    /**
     * Delete a match (admin function - doesn't reverse ranking changes)
     * @param id The match ID to delete
     */
    public void deleteMatch(Long id) {
        log.info("Deleting match with ID: {}", id);
        Match match = getMatchById(id);
        matchRepository.delete(match);
        log.info("Match deleted successfully");
    }
    
    /**
     * Get statistics for a member
     * @param memberId The member ID
     * @return Statistics for the member
     */
    @Transactional(readOnly = true)
    public MemberStatistics getMemberStatistics(Long memberId) {
        log.debug("Calculating statistics for member ID: {}", memberId);
        
        Member member = memberService.getMemberById(memberId);
        List<Object[]> stats = matchRepository.getMemberStatistics(memberId);
        
        if (stats != null && !stats.isEmpty()) {
            Object[] statArray = stats.get(0);
            Long wins = statArray[0] != null ? ((Number) statArray[0]).longValue() : 0L;
            Long losses = statArray[1] != null ? ((Number) statArray[1]).longValue() : 0L;
            Long draws = statArray[2] != null ? ((Number) statArray[2]).longValue() : 0L;
            
            return MemberStatistics.builder()
                    .member(member)
                    .totalMatches(wins + losses + draws)
                    .wins(wins)
                    .losses(losses)
                    .draws(draws)
                    .winRate(calculateWinRate(wins, wins + losses + draws))
                    .build();
        }
        
        return MemberStatistics.builder()
                .member(member)
                .totalMatches(0L)
                .wins(0L)
                .losses(0L)
                .draws(0L)
                .winRate(0.0)
                .build();
    }
    
    /**
     * Calculate win rate percentage
     * @param wins Number of wins
     * @param totalMatches Total matches played
     * @return Win rate as percentage
     */
    private double calculateWinRate(long wins, long totalMatches) {
        if (totalMatches == 0) {
            return 0.0;
        }
        return (double) wins / totalMatches * 100;
    }
    
    /**
     * Get head-to-head record between two members
     * @param member1Id First member ID
     * @param member2Id Second member ID
     * @return List of matches between the two members
     */
    @Transactional(readOnly = true)
    public List<Match> getHeadToHeadRecord(Long member1Id, Long member2Id) {
        log.debug("Fetching head-to-head record between members {} and {}", member1Id, member2Id);
        
        Member member1 = memberService.getMemberById(member1Id);
        Member member2 = memberService.getMemberById(member2Id);
        
        return matchRepository.findMatchesBetweenMembers(member1, member2);
    }
    
    /**
     * Get matches with pagination
     * @param page Page number (0-based)
     * @param size Page size
     * @return Page of matches ordered by date (newest first)
     */
    @Transactional(readOnly = true)
    public Page<Match> getMatchesPaginated(int page, int size) {
        log.debug("Fetching matches page {} with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("matchDate").descending());
        return matchRepository.findAll(pageable);
    }
}