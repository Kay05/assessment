package com.netstock.chessclub.controller;

import com.netstock.chessclub.service.MatchService;
import com.netstock.chessclub.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the home page and leaderboard.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    
    private final MemberService memberService;
    private final MatchService matchService;
    
    /**
     * Display the home page with leaderboard
     * @param model Model for view
     * @return Home page view
     */
    @GetMapping("/")
    public String home(Model model) {
        log.debug("Loading home page with leaderboard");
        
        // Get top 10 members for leaderboard
        model.addAttribute("leaderboard", memberService.getLeaderboard(10));
        
        // Get recent matches
        model.addAttribute("recentMatches", matchService.getRecentMatches(5));
        
        // Get total member count
        model.addAttribute("totalMembers", memberService.getAllMembers().size());
        
        return "index";
    }
    
    /**
     * Display the full leaderboard
     * @param model Model for view
     * @param page Current page number (0-based)
     * @param size Page size (10-100)
     * @return Leaderboard view
     */
    @GetMapping("/leaderboard")
    public String leaderboard(Model model, 
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        log.debug("Loading leaderboard page {} with size {}", page, size);
        
        // Validate page size
        if (size < 10) size = 10;
        if (size > 100) size = 100;
        
        var pageResult = memberService.getMembersPaginated(page, size);
        
        model.addAttribute("members", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalElements", pageResult.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("hasNext", pageResult.hasNext());
        model.addAttribute("hasPrevious", pageResult.hasPrevious());
        
        // Add overall statistics (not affected by pagination)
        model.addAttribute("totalMembersCount", memberService.getAllMembers().size());
        
        // Get the actual champion (rank 1) from the database
        memberService.getMemberByRank(1).ifPresent(champion -> {
            model.addAttribute("champion", champion);
            model.addAttribute("championGamesPlayed", champion.getGamesPlayed());
        });
        
        return "leaderboard";
    }
}