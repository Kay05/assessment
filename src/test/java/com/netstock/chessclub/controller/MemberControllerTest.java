package com.netstock.chessclub.controller;

import com.netstock.chessclub.config.GlobalExceptionHandler;
import com.netstock.chessclub.dto.MemberStatistics;
import com.netstock.chessclub.entity.Match;
import com.netstock.chessclub.entity.Member;
import com.netstock.chessclub.exception.DuplicateEmailException;
import com.netstock.chessclub.exception.MemberNotFoundException;
import com.netstock.chessclub.service.MatchService;
import com.netstock.chessclub.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
// Tests for MemberController web endpoints
class MemberControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MemberService memberService;
    
    @MockBean
    private MatchService matchService;
    
    private Member testMember;
    private List<Member> testMembers;
    
    @BeforeEach
    // Initialize test member data
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .currentRank(1)
                .gamesPlayed(10)
                .build();
        
        testMembers = List.of(testMember);
    }
    
    @Test
    // Test listing members without search term
    void testListMembers_WithoutSearch() throws Exception {
        Page<Member> memberPage = new PageImpl<>(testMembers);
        when(memberService.getMembersPaginated(anyInt(), anyInt())).thenReturn(memberPage);
        
        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/list"))
                .andExpect(model().attributeExists("members"))
                .andExpect(model().attribute("members", testMembers))
                .andExpect(model().attributeDoesNotExist("searchTerm"));
        
        verify(memberService).getMembersPaginated(anyInt(), anyInt());
        verify(memberService, never()).searchMembersPaginated(anyString(), anyInt(), anyInt());
    }
    
    @Test
    // Test listing members with search term
    void testListMembers_WithSearch() throws Exception {
        String searchTerm = "john";
        Page<Member> memberPage = new PageImpl<>(testMembers);
        when(memberService.searchMembersPaginated(eq(searchTerm), anyInt(), anyInt())).thenReturn(memberPage);
        
        mockMvc.perform(get("/members").param("search", searchTerm))
                .andExpect(status().isOk())
                .andExpect(view().name("members/list"))
                .andExpect(model().attributeExists("members"))
                .andExpect(model().attributeExists("searchTerm"))
                .andExpect(model().attribute("members", testMembers))
                .andExpect(model().attribute("searchTerm", searchTerm));
        
        verify(memberService).searchMembersPaginated(eq(searchTerm), anyInt(), anyInt());
        verify(memberService, never()).getMembersPaginated(anyInt(), anyInt());
    }
    
    @Test
    // Test listing members with empty search term
    void testListMembers_WithEmptySearch() throws Exception {
        Page<Member> memberPage = new PageImpl<>(testMembers);
        when(memberService.getMembersPaginated(anyInt(), anyInt())).thenReturn(memberPage);
        
        mockMvc.perform(get("/members").param("search", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("members/list"))
                .andExpect(model().attributeExists("members"))
                .andExpect(model().attribute("members", testMembers))
                .andExpect(model().attributeDoesNotExist("searchTerm"));
        
        verify(memberService).getMembersPaginated(anyInt(), anyInt());
        verify(memberService, never()).searchMembersPaginated(anyString(), anyInt(), anyInt());
    }
    
    @Test
    // Test displaying member creation form
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/members/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().attributeExists("member"));
    }
    
    @Test
    // Test successfully creating a new member
    void testCreateMember_Success() throws Exception {
        when(memberService.createMember(any(Member.class))).thenReturn(testMember);
        
        mockMvc.perform(post("/members")
                .param("name", "John")
                .param("surname", "Doe")
                .param("email", "john@example.com")
                .param("birthday", "1990-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attributeExists("successMessage"));
        
        verify(memberService).createMember(any(Member.class));
    }
    
    @Test
    // Test creating member with validation errors
    void testCreateMember_ValidationErrors() throws Exception {
        mockMvc.perform(post("/members")
                .param("name", "")  // Empty name should cause validation error
                .param("surname", "Doe")
                .param("email", "john@example.com")
                .param("birthday", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().hasErrors());
        
        verify(memberService, never()).createMember(any(Member.class));
    }
    
    @Test
    // Test creating member with duplicate email
    void testCreateMember_DuplicateEmail() throws Exception {
        when(memberService.createMember(any(Member.class)))
                .thenThrow(new DuplicateEmailException("Email already exists"));
        
        mockMvc.perform(post("/members")
                .param("name", "John")
                .param("surname", "Doe")
                .param("email", "john@example.com")
                .param("birthday", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().hasErrors());
        
        verify(memberService).createMember(any(Member.class));
    }
    
    @Test
    // Test viewing member details successfully
    void testViewMember_Success() throws Exception {
        MemberStatistics stats = MemberStatistics.builder()
                .member(testMember)
                .totalMatches(10L)
                .wins(7L)
                .losses(2L)
                .draws(1L)
                .winRate(70.0)
                .build();
        
        when(memberService.getMemberById(1L)).thenReturn(testMember);
        when(matchService.getMemberStatistics(1L)).thenReturn(stats);
        when(matchService.getMatchesForMember(1L)).thenReturn(List.of());
        
        mockMvc.perform(get("/members/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/view"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("statistics"))
                .andExpect(model().attributeExists("matches"))
                .andExpect(model().attribute("member", testMember))
                .andExpect(model().attribute("statistics", stats));
        
        verify(memberService).getMemberById(1L);
        verify(matchService).getMemberStatistics(1L);
        verify(matchService).getMatchesForMember(1L);
    }
    
    @Test
    // Test viewing non-existent member
    void testViewMember_NotFound() throws Exception {
        when(memberService.getMemberById(99L))
                .thenThrow(new MemberNotFoundException("Member not found"));
        
        mockMvc.perform(get("/members/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"));
        
        verify(memberService).getMemberById(99L);
    }
    
    @Test
    // Test displaying member edit form
    void testShowEditForm() throws Exception {
        when(memberService.getMemberById(1L)).thenReturn(testMember);
        
        mockMvc.perform(get("/members/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().attributeExists("member"))
                .andExpect(model().attributeExists("isEdit"))
                .andExpect(model().attribute("member", testMember))
                .andExpect(model().attribute("isEdit", true));
        
        verify(memberService).getMemberById(1L);
    }
    
    @Test
    // Test successfully updating member details
    void testUpdateMember_Success() throws Exception {
        when(memberService.updateMember(eq(1L), any(Member.class))).thenReturn(testMember);
        
        mockMvc.perform(post("/members/1")
                .param("name", "John")
                .param("surname", "Doe")
                .param("email", "john@example.com")
                .param("birthday", "1990-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members/1"))
                .andExpect(flash().attributeExists("successMessage"));
        
        verify(memberService).updateMember(eq(1L), any(Member.class));
    }
    
    @Test
    // Test updating member with validation errors
    void testUpdateMember_ValidationErrors() throws Exception {
        mockMvc.perform(post("/members/1")
                .param("name", "")  // Empty name should cause validation error
                .param("surname", "Doe")
                .param("email", "john@example.com")
                .param("birthday", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().hasErrors());
        
        verify(memberService, never()).updateMember(anyLong(), any(Member.class));
    }
    
    @Test
    // Test updating member with duplicate email
    void testUpdateMember_DuplicateEmail() throws Exception {
        when(memberService.updateMember(eq(1L), any(Member.class)))
                .thenThrow(new DuplicateEmailException("Email already exists"));
        
        mockMvc.perform(post("/members/1")
                .param("name", "John")
                .param("surname", "Doe")
                .param("email", "jane@example.com")
                .param("birthday", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/form"))
                .andExpect(model().hasErrors());
        
        verify(memberService).updateMember(eq(1L), any(Member.class));
    }
    
    @Test
    // Test successfully deleting a member
    void testDeleteMember_Success() throws Exception {
        when(memberService.getMemberById(1L)).thenReturn(testMember);
        doNothing().when(memberService).deleteMember(1L);
        
        mockMvc.perform(post("/members/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attributeExists("successMessage"));
        
        verify(memberService).getMemberById(1L);
        verify(memberService).deleteMember(1L);
    }
    
    @Test
    // Test deleting non-existent member
    void testDeleteMember_NotFound() throws Exception {
        when(memberService.getMemberById(99L))
                .thenThrow(new MemberNotFoundException("Member not found"));
        
        mockMvc.perform(post("/members/99/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attributeExists("errorMessage"));
        
        verify(memberService).getMemberById(99L);
        verify(memberService, never()).deleteMember(anyLong());
    }
    
    @Test
    // Test deleting member with service error
    void testDeleteMember_ServiceError() throws Exception {
        when(memberService.getMemberById(1L)).thenReturn(testMember);
        doThrow(new RuntimeException("Database error")).when(memberService).deleteMember(1L);
        
        mockMvc.perform(post("/members/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attributeExists("errorMessage"));
        
        verify(memberService).getMemberById(1L);
        verify(memberService).deleteMember(1L);
    }
}