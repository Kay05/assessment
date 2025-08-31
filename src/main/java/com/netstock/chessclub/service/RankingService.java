package com.netstock.chessclub.service;

import com.netstock.chessclub.entity.Match;
import com.netstock.chessclub.entity.Member;
import com.netstock.chessclub.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing chess club ranking system.
 * Implements the ranking rules as specified:
 * - Higher-ranked player wins: no rank change
 * - Draw: lower-ranked player gains one position (unless adjacent)
 * - Lower-ranked player wins: higher-ranked moves down one, lower-ranked moves up by half the difference
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RankingService {
    
    private final MemberRepository memberRepository;
    
    /**
     * Update rankings based on match result
     * @param match The match with result
     * @return Updated match with new rankings
     */
    public Match updateRankingsAfterMatch(Match match) {
        Member player1 = match.getPlayer1();
        Member player2 = match.getPlayer2();
        
        // Store original ranks
        Integer rank1Before = player1.getCurrentRank();
        Integer rank2Before = player2.getCurrentRank();
        
        log.info("Processing match result - Player1: {} (Rank {}), Player2: {} (Rank {}), Result: {}",
                player1.getFullName(), rank1Before,
                player2.getFullName(), rank2Before,
                match.getResult());
        
        // Determine higher and lower ranked players
        boolean player1IsHigherRanked = rank1Before < rank2Before;
        Member higherRankedPlayer = player1IsHigherRanked ? player1 : player2;
        Member lowerRankedPlayer = player1IsHigherRanked ? player2 : player1;
        Integer higherRank = higherRankedPlayer.getCurrentRank();
        Integer lowerRank = lowerRankedPlayer.getCurrentRank();
        
        // Apply ranking rules based on match result
        switch (match.getResult()) {
            case PLAYER1_WIN:
                if (player1IsHigherRanked) {
                    // Higher-ranked player wins - no change
                    log.info("Higher-ranked player wins - no rank changes");
                } else {
                    // Lower-ranked player wins
                    processLowerRankedPlayerWin(higherRankedPlayer, lowerRankedPlayer);
                }
                break;
                
            case PLAYER2_WIN:
                if (!player1IsHigherRanked) {
                    // Higher-ranked player wins - no change
                    log.info("Higher-ranked player wins - no rank changes");
                } else {
                    // Lower-ranked player wins
                    processLowerRankedPlayerWin(higherRankedPlayer, lowerRankedPlayer);
                }
                break;
                
            case DRAW:
                processDraw(higherRankedPlayer, lowerRankedPlayer);
                break;
        }
        
        // Update match with new rankings
        match.setPlayer1RankBefore(rank1Before);
        match.setPlayer2RankBefore(rank2Before);
        match.setPlayer1RankAfter(player1.getCurrentRank());
        match.setPlayer2RankAfter(player2.getCurrentRank());
        
        log.info("Rankings updated - Player1: {} -> {}, Player2: {} -> {}",
                rank1Before, player1.getCurrentRank(),
                rank2Before, player2.getCurrentRank());
        
        return match;
    }
    
    /**
     * Process a draw result
     * Lower-ranked player gains one position unless players are adjacent
     */
    private void processDraw(Member higherRankedPlayer, Member lowerRankedPlayer) {
        Integer higherRank = higherRankedPlayer.getCurrentRank();
        Integer lowerRank = lowerRankedPlayer.getCurrentRank();
        
        // Check if players are adjacent
        if (lowerRank - higherRank == 1) {
            log.info("Draw between adjacent players (ranks {} and {}) - no changes", 
                    higherRank, lowerRank);
            return;
        }
        
        // Lower-ranked player moves up one position
        Integer newLowerRank = lowerRank - 1;
        
        // Find the member currently at the target rank and swap
        memberRepository.findByCurrentRank(newLowerRank).ifPresent(displacedMember -> {
            log.info("Draw - {} moves from rank {} to {}, {} moves from rank {} to {}",
                    lowerRankedPlayer.getFullName(), lowerRank, newLowerRank,
                    displacedMember.getFullName(), newLowerRank, lowerRank);
            
            // Use direct database updates only
            memberRepository.updateMemberRank(displacedMember.getId(), lowerRank);
            memberRepository.updateMemberRank(lowerRankedPlayer.getId(), newLowerRank);
            
            // Update entity objects for match recording
            displacedMember.setCurrentRank(lowerRank);
            lowerRankedPlayer.setCurrentRank(newLowerRank);
        });
    }
    
    /**
     * Process when lower-ranked player beats higher-ranked player
     * Higher-ranked moves down one, lower-ranked moves up by half the difference
     */
    private void processLowerRankedPlayerWin(Member higherRankedPlayer, Member lowerRankedPlayer) {
        Integer higherRank = higherRankedPlayer.getCurrentRank();
        Integer lowerRank = lowerRankedPlayer.getCurrentRank();
        
        // Calculate rank changes
        Integer rankDifference = lowerRank - higherRank;
        Integer newLowerRank;
        Integer newHigherRank;
        
        if (rankDifference == 1) {
            // Adjacent ranks: simple swap to maintain ranking integrity
            newHigherRank = lowerRank;
            newLowerRank = higherRank;
            log.info("Adjacent rank upset - {} and {} swap positions", 
                    higherRankedPlayer.getFullName(), lowerRankedPlayer.getFullName());
        } else {
            // Non-adjacent ranks: use original formula
            Integer lowerPlayerMoveUp = rankDifference / 2;
            newLowerRank = lowerRank - lowerPlayerMoveUp;
            newHigherRank = higherRank + 1;
        }
        
        log.info("Lower-ranked player wins - {} moves from {} to {}, {} moves from {} to {}",
                lowerRankedPlayer.getFullName(), lowerRank, newLowerRank,
                higherRankedPlayer.getFullName(), higherRank, newHigherRank);
        
        // Create a map to store the new rank assignments
        Map<Long, Integer> newRankAssignments = new HashMap<>();
        
        // Get all members sorted by rank
        List<Member> allMembers = memberRepository.findAllByOrderByCurrentRankAsc();
        
        // Assign new ranks for all members
        for (Member member : allMembers) {
            Integer currentRank = member.getCurrentRank();
            Integer newRankForMember;
            
            if (member.getId().equals(higherRankedPlayer.getId())) {
                // Higher-ranked player moves down to newHigherRank
                newRankForMember = newHigherRank;
            } else if (member.getId().equals(lowerRankedPlayer.getId())) {
                // Lower-ranked player moves up to newLowerRank
                newRankForMember = newLowerRank;
            } else {
                // Determine displacement for other members based on their position relative to the changes
                if (currentRank > higherRank && currentRank < newLowerRank) {
                    // Members between higher player's original rank and lower player's new rank move up by 1
                    newRankForMember = currentRank - 1;
                } else if (currentRank >= newLowerRank && currentRank < lowerRank) {
                    // Members between lower player's new rank and original rank get pushed down by 1  
                    newRankForMember = currentRank + 1;
                } else {
                    // All other members keep their current rank
                    newRankForMember = currentRank;
                }
            }
            
            newRankAssignments.put(member.getId(), newRankForMember);
        }
        
        // Step 1: Move all affected members to safe temporary ranks first
        int tempRankCounter = -1000;
        for (Member member : allMembers) {
            Integer newRankForMember = newRankAssignments.get(member.getId());
            if (!newRankForMember.equals(member.getCurrentRank())) {
                memberRepository.updateMemberRank(member.getId(), tempRankCounter);
                member.setCurrentRank(tempRankCounter);
                tempRankCounter--;
                log.debug("Moved {} to temporary rank {}", member.getFullName(), member.getCurrentRank());
            }
        }
        
        // Step 2: Assign final ranks
        for (Member member : allMembers) {
            Integer newRankForMember = newRankAssignments.get(member.getId());
            if (member.getCurrentRank() < 0) {  // Only update members in temporary ranks
                memberRepository.updateMemberRank(member.getId(), newRankForMember);
                member.setCurrentRank(newRankForMember);
                log.debug("Set {} to final rank {}", member.getFullName(), newRankForMember);
            }
        }
        
        log.info("Rank adjustments completed successfully");
    }
    
    /**
     * Validate that all members have unique sequential ranks from 1 to n
     * @return true if ranking is valid
     */
    @Transactional(readOnly = true)
    public boolean validateRankingIntegrity() {
        List<Member> members = memberRepository.findAllByOrderByCurrentRankAsc();
        
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getCurrentRank() != i + 1) {
                log.error("Ranking integrity check failed: Member {} has rank {} but should be {}",
                        members.get(i).getFullName(), members.get(i).getCurrentRank(), i + 1);
                return false;
            }
        }
        
        log.info("Ranking integrity check passed - {} members with ranks 1 to {}", 
                members.size(), members.size());
        return true;
    }
    
    /**
     * Fix ranking gaps or duplicates if they occur
     */
    @Transactional
    public void repairRankings() {
        log.info("Starting ranking repair process");
        
        List<Member> members = memberRepository.findAllByOrderByCurrentRankAsc();
        
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            Integer expectedRank = i + 1;
            
            if (!member.getCurrentRank().equals(expectedRank)) {
                log.info("Fixing rank for {}: {} -> {}", 
                        member.getFullName(), member.getCurrentRank(), expectedRank);
                member.setCurrentRank(expectedRank);
            }
        }
        
        log.info("Ranking repair completed");
    }
}