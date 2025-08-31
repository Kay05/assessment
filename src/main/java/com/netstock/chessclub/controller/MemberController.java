package com.netstock.chessclub.controller;

import com.netstock.chessclub.dto.MemberStatistics;
import com.netstock.chessclub.entity.Member;
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
 * Controller for managing chess club members.
 * Handles web requests for member CRUD operations.
 */
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    
    private final MemberService memberService;
    private final MatchService matchService;
    
    /**
     * Display list of all members
     * @param model Model for view
     * @param search Optional search term
     * @param page Current page number (0-based)
     * @param size Page size (10-100)
     * @return Members list view
     */
    @GetMapping
    public String listMembers(Model model, 
                            @RequestParam(required = false) String search,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        log.debug("Listing members with search term: {}, page: {}, size: {}", search, page, size);
        
        // Validate page size
        if (size < 10) size = 10;
        if (size > 100) size = 100;
        
        if (search != null && !search.trim().isEmpty()) {
            var pageResult = memberService.searchMembersPaginated(search, page, size);
            model.addAttribute("members", pageResult.getContent());
            model.addAttribute("searchTerm", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pageResult.getTotalPages());
            model.addAttribute("totalElements", pageResult.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("hasNext", pageResult.hasNext());
            model.addAttribute("hasPrevious", pageResult.hasPrevious());
        } else {
            var pageResult = memberService.getMembersPaginated(page, size);
            model.addAttribute("members", pageResult.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pageResult.getTotalPages());
            model.addAttribute("totalElements", pageResult.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("hasNext", pageResult.hasNext());
            model.addAttribute("hasPrevious", pageResult.hasPrevious());
        }
        
        return "members/list";
    }
    
    /**
     * Display form for creating a new member
     * @param model Model for view
     * @return Member creation form view
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.debug("Showing member creation form");
        model.addAttribute("member", new Member());
        return "members/form";
    }
    
    /**
     * Process member creation
     * @param member The member to create
     * @param result Validation result
     * @param redirectAttributes Redirect attributes for success message
     * @return Redirect to members list or back to form if errors
     */
    @PostMapping
    public String createMember(@Valid @ModelAttribute Member member, 
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        log.info("Creating new member: {} {}", member.getName(), member.getSurname());
        
        if (result.hasErrors()) {
            log.warn("Validation errors in member creation");
            return "members/form";
        }
        
        try {
            Member created = memberService.createMember(member);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Member " + created.getFullName() + " created successfully with rank #" + created.getCurrentRank());
            return "redirect:/members";
        } catch (Exception e) {
            log.error("Error creating member", e);
            result.reject("error.global", e.getMessage());
            return "members/form";
        }
    }
    
    /**
     * Display member details
     * @param id Member ID
     * @param model Model for view
     * @return Member details view
     */
    @GetMapping("/{id}")
    public String viewMember(@PathVariable Long id, Model model) {
        log.debug("Viewing member with ID: {}", id);
        
        Member member = memberService.getMemberById(id);
        MemberStatistics stats = matchService.getMemberStatistics(id);
        
        model.addAttribute("member", member);
        model.addAttribute("statistics", stats);
        model.addAttribute("matches", matchService.getMatchesForMember(id));
        
        return "members/view";
    }
    
    /**
     * Display form for editing a member
     * @param id Member ID
     * @param model Model for view
     * @return Member edit form view
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Showing edit form for member ID: {}", id);
        
        Member member = memberService.getMemberById(id);
        model.addAttribute("member", member);
        model.addAttribute("isEdit", true);
        
        return "members/form";
    }
    
    /**
     * Process member update
     * @param id Member ID
     * @param member Updated member data
     * @param result Validation result
     * @param redirectAttributes Redirect attributes for success message
     * @return Redirect to member view or back to form if errors
     */
    @PostMapping("/{id}")
    public String updateMember(@PathVariable Long id,
                               @Valid @ModelAttribute Member member,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        log.info("Updating member ID: {}", id);
        
        if (result.hasErrors()) {
            log.warn("Validation errors in member update");
            return "members/form";
        }
        
        try {
            Member updated = memberService.updateMember(id, member);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Member " + updated.getFullName() + " updated successfully");
            return "redirect:/members/" + id;
        } catch (Exception e) {
            log.error("Error updating member", e);
            result.reject("error.global", e.getMessage());
            return "members/form";
        }
    }
    
    /**
     * Delete a member
     * @param id Member ID
     * @param redirectAttributes Redirect attributes for success message
     * @return Redirect to members list
     */
    @PostMapping("/{id}/delete")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Deleting member ID: {}", id);
        
        try {
            Member member = memberService.getMemberById(id);
            memberService.deleteMember(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Member " + member.getFullName() + " deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting member", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting member: " + e.getMessage());
        }
        
        return "redirect:/members";
    }
}