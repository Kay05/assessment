package com.netstock.chessclub.controller;

import com.netstock.chessclub.dto.MatchDto;
import com.netstock.chessclub.entity.Match;
import com.netstock.chessclub.service.MatchService;
import com.netstock.chessclub.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for managing chess matches.
 * Handles match recording and viewing.
 */
@Controller
@RequestMapping("/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {
    
    private final MatchService matchService;
    private final MemberService memberService;
    
    /**
     * Display list of all matches
     * @param model Model for view
     * @param page Current page number (0-based)
     * @param size Page size (10-100)
     * @return Matches list view
     */
    @GetMapping
    public String listMatches(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        log.debug("Listing matches page {} with size {}", page, size);
        
        // Validate page size
        if (size < 10) size = 10;
        if (size > 100) size = 100;
        
        var pageResult = matchService.getMatchesPaginated(page, size);
        
        model.addAttribute("matches", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalElements", pageResult.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasNext", pageResult.hasNext());
        model.addAttribute("hasPrevious", pageResult.hasPrevious());
        
        return "matches/list";
    }
    
    /**
     * Display form for recording a new match
     * @param model Model for view
     * @return Match recording form view
     */
    @GetMapping("/new")
    public String showMatchForm(Model model) {
        log.debug("Showing match recording form");
        
        model.addAttribute("matchDto", new MatchDto());
        model.addAttribute("members", memberService.getAllMembers());
        model.addAttribute("matchResults", Match.MatchResult.values());
        
        return "matches/form";
    }
    
    /**
     * Process match recording
     * @param matchDto The match data
     * @param result Validation result
     * @param redirectAttributes Redirect attributes for success message
     * @param model Model for view
     * @return Redirect to matches list or back to form if errors
     */
    @PostMapping
    public String recordMatch(@Valid @ModelAttribute MatchDto matchDto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        log.info("Recording match between players {} and {}", 
                matchDto.getPlayer1Id(), matchDto.getPlayer2Id());
        
        if (result.hasErrors()) {
            log.warn("Validation errors in match recording");
            model.addAttribute("members", memberService.getAllMembers());
            model.addAttribute("matchResults", Match.MatchResult.values());
            return "matches/form";
        }
        
        try {
            Match recorded = matchService.recordMatch(matchDto);
            
            String message = String.format("Match recorded successfully! Rankings updated - %s: %d → %d, %s: %d → %d",
                    recorded.getPlayer1().getFullName(), 
                    recorded.getPlayer1RankBefore(), 
                    recorded.getPlayer1RankAfter(),
                    recorded.getPlayer2().getFullName(), 
                    recorded.getPlayer2RankBefore(), 
                    recorded.getPlayer2RankAfter());
            
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/matches";
        } catch (Exception e) {
            log.error("Error recording match", e);
            result.reject("error.global", e.getMessage());
            model.addAttribute("members", memberService.getAllMembers());
            model.addAttribute("matchResults", Match.MatchResult.values());
            return "matches/form";
        }
    }
    
    /**
     * View match details
     * @param id Match ID
     * @param model Model for view
     * @return Match details view
     */
    @GetMapping("/{id}")
    public String viewMatch(@PathVariable Long id, Model model) {
        log.debug("Viewing match with ID: {}", id);
        
        Match match = matchService.getMatchById(id);
        model.addAttribute("match", match);
        
        return "matches/view";
    }
    
    /**
     * Delete a match (admin function)
     * @param id Match ID
     * @param redirectAttributes Redirect attributes for success message
     * @return Redirect to matches list
     */
    @PostMapping("/{id}/delete")
    public String deleteMatch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Deleting match ID: {}", id);
        
        try {
            matchService.deleteMatch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Match deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting match", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting match: " + e.getMessage());
        }
        
        return "redirect:/matches";
    }
}