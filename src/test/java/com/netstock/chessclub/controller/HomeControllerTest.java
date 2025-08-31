package com.netstock.chessclub.controller;

import com.netstock.chessclub.entity.Match;
import com.netstock.chessclub.entity.Member;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
// Tests for HomeController web endpoints
class HomeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MemberService memberService;
    
    @MockBean
    private MatchService matchService;
    
    private List<Member> mockMembers;
    private List<Match> mockMatches;
    
    @BeforeEach
    // Initialize mock member and match data for tests
    void setUp() {
        Member member1 = Member.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .currentRank(1)
                .gamesPlayed(10)
                .build();
        
        Member member2 = Member.builder()
                .id(2L)
                .name("Jane")
                .surname("Smith")
                .email("jane@example.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .currentRank(2)
                .gamesPlayed(5)
                .build();
        
        mockMembers = List.of(member1, member2);
        
        Match match = Match.builder()
                .id(1L)
                .player1(member1)
                .player2(member2)
                .result(Match.MatchResult.PLAYER1_WIN)
                .player1RankBefore(1)
                .player2RankBefore(2)
                .player1RankAfter(1)
                .player2RankAfter(2)
                .matchDate(LocalDateTime.now())
                .build();
        
        mockMatches = List.of(match);
    }
    
    @Test
    // Test home page displays leaderboard and recent matches
    void testHomePage() throws Exception {
        when(memberService.getLeaderboard(10)).thenReturn(mockMembers);
        when(matchService.getRecentMatches(5)).thenReturn(mockMatches);
        when(memberService.getAllMembers()).thenReturn(mockMembers);
        
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("leaderboard"))
                .andExpect(model().attributeExists("recentMatches"))
                .andExpect(model().attributeExists("totalMembers"))
                .andExpect(model().attribute("leaderboard", mockMembers))
                .andExpect(model().attribute("recentMatches", mockMatches))
                .andExpect(model().attribute("totalMembers", 2));
    }
    
    @Test
    // Test leaderboard page displays paginated members
    void testLeaderboard() throws Exception {
        Page<Member> memberPage = new PageImpl<>(mockMembers);
        when(memberService.getMembersPaginated(anyInt(), anyInt())).thenReturn(memberPage);
        
        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard"))
                .andExpect(model().attributeExists("members"))
                .andExpect(model().attribute("members", mockMembers));
    }
    
    @Test
    // Test home page with empty data sets
    void testHomePageWithEmptyData() throws Exception {
        when(memberService.getLeaderboard(10)).thenReturn(List.of());
        when(matchService.getRecentMatches(5)).thenReturn(List.of());
        when(memberService.getAllMembers()).thenReturn(List.of());
        
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("leaderboard"))
                .andExpect(model().attributeExists("recentMatches"))
                .andExpect(model().attributeExists("totalMembers"))
                .andExpect(model().attribute("totalMembers", 0));
    }
    
    @Test
    // Test leaderboard page with no members
    void testLeaderboardWithEmptyData() throws Exception {
        Page<Member> emptyPage = new PageImpl<>(List.of());
        when(memberService.getMembersPaginated(anyInt(), anyInt())).thenReturn(emptyPage);
        
        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("leaderboard"))
                .andExpect(model().attributeExists("members"))
                .andExpect(model().attribute("members", List.of()));
    }
}