package com.netstock.chessclub.service;

import com.netstock.chessclub.entity.Member;
import com.netstock.chessclub.exception.DuplicateEmailException;
import com.netstock.chessclub.exception.MemberNotFoundException;
import com.netstock.chessclub.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing chess club members.
 * Handles business logic for member CRUD operations and ranking management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {
    
    private final MemberRepository memberRepository;
    
    /**
     * Create a new member with the lowest rank (new members start at the bottom)
     * @param member The member to create
     * @return The created member
     */
    public Member createMember(Member member) {
        log.info("Creating new member: {} {}", member.getName(), member.getSurname());
        
        // Check for duplicate email
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new DuplicateEmailException("A member with email " + member.getEmail() + " already exists");
        }
        
        // Set the rank to be one more than the current maximum (lowest rank)
        Optional<Integer> maxRank = memberRepository.findMaxRank();
        member.setCurrentRank(maxRank.orElse(0) + 1);
        
        // Initialize games played if not set
        if (member.getGamesPlayed() == null) {
            member.setGamesPlayed(0);
        }
        
        Member savedMember = memberRepository.save(member);
        log.info("Member created successfully with ID: {} and rank: {}", 
                savedMember.getId(), savedMember.getCurrentRank());
        
        return savedMember;
    }
    
    /**
     * Get all members ordered by rank
     * @return List of all members sorted by rank
     */
    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        log.debug("Fetching all members ordered by rank");
        return memberRepository.findAllByOrderByCurrentRankAsc();
    }
    
    /**
     * Get a member by ID
     * @param id The member ID
     * @return The member
     * @throws MemberNotFoundException if member not found
     */
    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        log.debug("Fetching member with ID: {}", id);
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with ID: " + id));
    }
    
    /**
     * Update an existing member
     * @param id The member ID
     * @param updatedMember The updated member data
     * @return The updated member
     */
    public Member updateMember(Long id, Member updatedMember) {
        log.info("Updating member with ID: {}", id);
        
        Member existingMember = getMemberById(id);
        
        // Check for email conflicts if email is being changed
        if (!existingMember.getEmail().equals(updatedMember.getEmail()) 
            && memberRepository.existsByEmail(updatedMember.getEmail())) {
            throw new DuplicateEmailException("Email " + updatedMember.getEmail() + " is already in use");
        }
        
        // Update fields (preserve rank and games played unless explicitly changed)
        existingMember.setName(updatedMember.getName());
        existingMember.setSurname(updatedMember.getSurname());
        existingMember.setEmail(updatedMember.getEmail());
        existingMember.setBirthday(updatedMember.getBirthday());
        
        // Only update games played if provided
        if (updatedMember.getGamesPlayed() != null) {
            existingMember.setGamesPlayed(updatedMember.getGamesPlayed());
        }
        
        Member savedMember = memberRepository.save(existingMember);
        log.info("Member updated successfully: {}", savedMember.getId());
        
        return savedMember;
    }
    
    /**
     * Delete a member and adjust ranks accordingly
     * @param id The member ID to delete
     */
    public void deleteMember(Long id) {
        log.info("Deleting member with ID: {}", id);
        
        Member memberToDelete = getMemberById(id);
        Integer deletedRank = memberToDelete.getCurrentRank();
        
        // Delete the member
        memberRepository.delete(memberToDelete);
        
        // Adjust ranks of all members below the deleted member (move them up by 1)
        List<Member> membersToAdjust = memberRepository.findByCurrentRankBetweenOrderByCurrentRankAsc(
                deletedRank + 1, Integer.MAX_VALUE);
        
        for (Member member : membersToAdjust) {
            Integer newRank = member.getCurrentRank() - 1;
            memberRepository.updateMemberRank(member.getId(), newRank);
            member.setCurrentRank(newRank);
        }
        
        log.info("Member deleted and ranks adjusted");
    }
    
    /**
     * Search members by name or surname
     * @param searchTerm The search term
     * @return List of matching members
     */
    @Transactional(readOnly = true)
    public List<Member> searchMembers(String searchTerm) {
        log.debug("Searching members with term: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllMembers();
        }
        
        return memberRepository.searchByNameOrSurname(searchTerm.trim());
    }
    
    /**
     * Get member by rank
     * @param rank The rank to search for
     * @return The member with the specified rank
     */
    @Transactional(readOnly = true)
    public Optional<Member> getMemberByRank(Integer rank) {
        log.debug("Fetching member with rank: {}", rank);
        return memberRepository.findByCurrentRank(rank);
    }
    
    /**
     * Get leaderboard (top ranked members)
     * @param limit Number of top members to retrieve
     * @return List of top ranked members
     */
    @Transactional(readOnly = true)
    public List<Member> getLeaderboard(int limit) {
        log.debug("Fetching top {} members for leaderboard", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by("currentRank").ascending());
        return memberRepository.findTopRankedMembers(pageable);
    }
    
    /**
     * Increment games played for a member
     * @param memberId The member ID
     */
    public void incrementGamesPlayed(Long memberId) {
        Member member = getMemberById(memberId);
        member.setGamesPlayed(member.getGamesPlayed() + 1);
        memberRepository.save(member);
        log.debug("Incremented games played for member {}: now {}", memberId, member.getGamesPlayed());
    }
    
    /**
     * Update member ranks (used by RankingService)
     * @param member1 First member to update
     * @param newRank1 New rank for first member
     * @param member2 Second member to update
     * @param newRank2 New rank for second member
     */
    @Transactional
    public void updateMemberRanks(Member member1, Integer newRank1, Member member2, Integer newRank2) {
        log.info("Updating ranks - Member {}: {} -> {}, Member {}: {} -> {}", 
                member1.getId(), member1.getCurrentRank(), newRank1,
                member2.getId(), member2.getCurrentRank(), newRank2);
        
        member1.setCurrentRank(newRank1);
        member2.setCurrentRank(newRank2);
        
        memberRepository.save(member1);
        memberRepository.save(member2);
    }
    
    /**
     * Get members with pagination
     * @param page Page number (0-based)
     * @param size Page size
     * @return Page of members ordered by rank
     */
    @Transactional(readOnly = true)
    public Page<Member> getMembersPaginated(int page, int size) {
        log.debug("Fetching members page {} with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("currentRank").ascending());
        return memberRepository.findAll(pageable);
    }
    
    /**
     * Search members with pagination
     * @param searchTerm Search term for name or surname
     * @param page Page number (0-based)
     * @param size Page size
     * @return Page of matching members
     */
    @Transactional(readOnly = true)
    public Page<Member> searchMembersPaginated(String searchTerm, int page, int size) {
        log.debug("Searching members with term: {} on page {} with size {}", searchTerm, page, size);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getMembersPaginated(page, size);
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("currentRank").ascending());
        return memberRepository.searchByNameOrSurnamePaginated(searchTerm.trim(), pageable);
    }
}