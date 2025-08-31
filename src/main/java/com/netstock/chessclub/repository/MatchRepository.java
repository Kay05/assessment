package com.netstock.chessclub.repository;

import com.netstock.chessclub.entity.Match;
import com.netstock.chessclub.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Match entity operations.
 * Provides CRUD operations and custom queries for chess matches.
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    /**
     * Find all matches for a specific member (as either player1 or player2)
     * @param member The member to find matches for
     * @return List of matches involving the member
     */
    @Query("SELECT m FROM Match m WHERE m.player1 = :member OR m.player2 = :member " +
           "ORDER BY m.matchDate DESC")
    List<Match> findMatchesByMember(@Param("member") Member member);
    
    /**
     * Find matches between two specific members
     * @param member1 First member
     * @param member2 Second member
     * @return List of matches between these two members
     */
    @Query("SELECT m FROM Match m WHERE " +
           "(m.player1 = :member1 AND m.player2 = :member2) OR " +
           "(m.player1 = :member2 AND m.player2 = :member1) " +
           "ORDER BY m.matchDate DESC")
    List<Match> findMatchesBetweenMembers(@Param("member1") Member member1, 
                                          @Param("member2") Member member2);
    
    /**
     * Find recent matches ordered by date
     * @param pageable The pageable object to limit results
     * @return List of recent matches
     */
    @Query("SELECT m FROM Match m ORDER BY m.matchDate DESC")
    List<Match> findRecentMatches(Pageable pageable);
    
    /**
     * Find matches within a date range
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of matches within the date range
     */
    List<Match> findByMatchDateBetweenOrderByMatchDateDesc(LocalDateTime startDate, 
                                                           LocalDateTime endDate);
    
    /**
     * Count total wins for a member
     * @param member The member to count wins for
     * @return Number of wins
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE " +
           "(m.player1 = :member AND m.result = 'PLAYER1_WIN') OR " +
           "(m.player2 = :member AND m.result = 'PLAYER2_WIN')")
    long countWinsForMember(@Param("member") Member member);
    
    /**
     * Count total losses for a member
     * @param member The member to count losses for
     * @return Number of losses
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE " +
           "(m.player1 = :member AND m.result = 'PLAYER2_WIN') OR " +
           "(m.player2 = :member AND m.result = 'PLAYER1_WIN')")
    long countLossesForMember(@Param("member") Member member);
    
    /**
     * Count total draws for a member
     * @param member The member to count draws for
     * @return Number of draws
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE " +
           "(m.player1 = :member OR m.player2 = :member) AND m.result = 'DRAW'")
    long countDrawsForMember(@Param("member") Member member);
    
    /**
     * Get match statistics for a member
     * @param memberId The ID of the member
     * @return List containing one Object array with [wins, losses, draws]
     */
    @Query("SELECT " +
           "SUM(CASE WHEN (m.player1.id = :memberId AND m.result = 'PLAYER1_WIN') OR " +
           "(m.player2.id = :memberId AND m.result = 'PLAYER2_WIN') THEN 1 ELSE 0 END) as wins, " +
           "SUM(CASE WHEN (m.player1.id = :memberId AND m.result = 'PLAYER2_WIN') OR " +
           "(m.player2.id = :memberId AND m.result = 'PLAYER1_WIN') THEN 1 ELSE 0 END) as losses, " +
           "SUM(CASE WHEN (m.player1.id = :memberId OR m.player2.id = :memberId) AND " +
           "m.result = 'DRAW' THEN 1 ELSE 0 END) as draws " +
           "FROM Match m WHERE m.player1.id = :memberId OR m.player2.id = :memberId")
    List<Object[]> getMemberStatistics(@Param("memberId") Long memberId);
    
    /**
     * Find all matches ordered by date descending
     * @return List of all matches sorted by date
     */
    List<Match> findAllByOrderByMatchDateDesc();
}