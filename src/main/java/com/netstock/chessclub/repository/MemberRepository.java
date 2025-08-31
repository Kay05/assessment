package com.netstock.chessclub.repository;

import com.netstock.chessclub.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Member entity operations.
 * Provides CRUD operations and custom queries for chess club members.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    /**
     * Find a member by email address
     * @param email The email to search for
     * @return Optional containing the member if found
     */
    Optional<Member> findByEmail(String email);
    
    /**
     * Check if a member exists with the given email
     * @param email The email to check
     * @return true if a member exists with this email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all members ordered by their current rank (ascending)
     * @return List of members sorted by rank
     */
    List<Member> findAllByOrderByCurrentRankAsc();
    
    /**
     * Find a member by their current rank
     * @param rank The rank to search for
     * @return Optional containing the member if found
     */
    Optional<Member> findByCurrentRank(Integer rank);
    
    /**
     * Find members with ranks between two values (inclusive)
     * @param startRank The starting rank (inclusive)
     * @param endRank The ending rank (inclusive)
     * @return List of members within the rank range
     */
    List<Member> findByCurrentRankBetweenOrderByCurrentRankAsc(Integer startRank, Integer endRank);
    
    /**
     * Get the maximum rank value (lowest ranked player)
     * @return The highest rank number, or null if no members exist
     */
    @Query("SELECT MAX(m.currentRank) FROM Member m")
    Optional<Integer> findMaxRank();
    
    /**
     * Set a member's rank to a specific value by ID
     * Used for precise rank updates without constraint violations
     * @param memberId The ID of the member
     * @param newRank The new rank value
     */
    @Modifying
    @Query("UPDATE Member m SET m.currentRank = :newRank WHERE m.id = :memberId")
    void updateMemberRank(@Param("memberId") Long memberId, @Param("newRank") Integer newRank);
    
    /**
     * Set a member's rank to a temporary value to avoid constraints
     * @param memberId The ID of the member
     * @param tempRank The temporary rank value (should be negative to avoid conflicts)
     */
    @Modifying
    @Query("UPDATE Member m SET m.currentRank = :tempRank WHERE m.id = :memberId")
    void setTemporaryRank(@Param("memberId") Long memberId, @Param("tempRank") Integer tempRank);
    
    /**
     * Search members by name or surname (case-insensitive)
     * @param searchTerm The term to search for
     * @return List of matching members
     */
    @Query("SELECT m FROM Member m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Member> searchByNameOrSurname(@Param("searchTerm") String searchTerm);
    
    /**
     * Get top ranked members (leaderboard)
     * @param pageable The pageable object to limit results
     * @return List of top ranked members
     */
    @Query("SELECT m FROM Member m ORDER BY m.currentRank ASC")
    List<Member> findTopRankedMembers(Pageable pageable);
    
    /**
     * Count members with more games played than a given threshold
     * @param gamesThreshold The minimum number of games
     * @return Count of active members
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.gamesPlayed >= :gamesThreshold")
    long countActiveMembers(@Param("gamesThreshold") Integer gamesThreshold);
    
    /**
     * Search members by name or surname with pagination (case-insensitive)
     * @param searchTerm The term to search for
     * @param pageable The pagination information
     * @return Page of matching members
     */
    @Query("SELECT m FROM Member m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Member> searchByNameOrSurnamePaginated(@Param("searchTerm") String searchTerm, Pageable pageable);
}