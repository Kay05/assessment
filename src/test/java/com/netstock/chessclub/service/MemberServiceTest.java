package com.netstock.chessclub.service;

import com.netstock.chessclub.entity.Member;
import com.netstock.chessclub.exception.DuplicateEmailException;
import com.netstock.chessclub.exception.MemberNotFoundException;
import com.netstock.chessclub.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// Tests for MemberService business logic
class MemberServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @InjectMocks
    private MemberService memberService;
    
    private Member testMember;
    
    @BeforeEach
    // Initialize test member object
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
    }
    
    @Test
    // Test successfully creating a new member
    void testCreateMember_Success() {
        Member newMember = Member.builder()
                .name("Jane")
                .surname("Smith")
                .email("jane@example.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .build();
        
        when(memberRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(memberRepository.findMaxRank()).thenReturn(Optional.of(5));
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);
        
        Member created = memberService.createMember(newMember);
        
        assertNotNull(created);
        assertEquals(6, newMember.getCurrentRank()); // Should be max + 1
        assertEquals(0, newMember.getGamesPlayed()); // Should be initialized to 0
        
        verify(memberRepository).existsByEmail("jane@example.com");
        verify(memberRepository).findMaxRank();
        verify(memberRepository).save(newMember);
    }
    
    @Test
    // Test creating the first member in the database
    void testCreateMember_FirstMember() {
        Member newMember = Member.builder()
                .name("Jane")
                .surname("Smith")
                .email("jane@example.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .build();
        
        when(memberRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(memberRepository.findMaxRank()).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);
        
        Member created = memberService.createMember(newMember);
        
        assertEquals(1, newMember.getCurrentRank()); // Should be 1 for first member
        
        verify(memberRepository).existsByEmail("jane@example.com");
        verify(memberRepository).findMaxRank();
        verify(memberRepository).save(newMember);
    }
    
    @Test
    // Test creating member with duplicate email address
    void testCreateMember_DuplicateEmail() {
        Member newMember = Member.builder()
                .name("Jane")
                .surname("Smith")
                .email("john@example.com") // Duplicate email
                .birthday(LocalDate.of(1995, 5, 15))
                .build();
        
        when(memberRepository.existsByEmail("john@example.com")).thenReturn(true);
        
        assertThrows(DuplicateEmailException.class, () -> memberService.createMember(newMember));
        
        verify(memberRepository).existsByEmail("john@example.com");
        verify(memberRepository, never()).save(any());
    }
    
    @Test
    // Test that creating member preserves existing games played
    void testCreateMember_PreservesGamesPlayed() {
        Member newMember = Member.builder()
                .name("Jane")
                .surname("Smith")
                .email("jane@example.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .gamesPlayed(5) // Already set
                .build();
        
        when(memberRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(memberRepository.findMaxRank()).thenReturn(Optional.of(3));
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);
        
        Member created = memberService.createMember(newMember);
        
        assertEquals(5, newMember.getGamesPlayed()); // Should preserve existing value
        
        verify(memberRepository).save(newMember);
    }
    
    @Test
    // Test retrieving all members ordered by rank
    void testGetAllMembers() {
        List<Member> expectedMembers = List.of(testMember);
        when(memberRepository.findAllByOrderByCurrentRankAsc()).thenReturn(expectedMembers);
        
        List<Member> result = memberService.getAllMembers();
        
        assertEquals(expectedMembers, result);
        verify(memberRepository).findAllByOrderByCurrentRankAsc();
    }
    
    @Test
    // Test retrieving a member by ID successfully
    void testGetMemberById_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        
        Member result = memberService.getMemberById(1L);
        
        assertEquals(testMember, result);
        verify(memberRepository).findById(1L);
    }
    
    @Test
    // Test retrieving member by non-existent ID
    void testGetMemberById_NotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(MemberNotFoundException.class, () -> memberService.getMemberById(99L));
        
        verify(memberRepository).findById(99L);
    }
    
    @Test
    // Test successfully updating member details
    void testUpdateMember_Success() {
        Member updatedData = Member.builder()
                .name("Johnny")
                .surname("Smith")
                .email("johnny@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .gamesPlayed(15)
                .build();
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.existsByEmail("johnny@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        
        Member result = memberService.updateMember(1L, updatedData);
        
        assertEquals("Johnny", testMember.getName());
        assertEquals("Smith", testMember.getSurname());
        assertEquals("johnny@example.com", testMember.getEmail());
        assertEquals(15, testMember.getGamesPlayed());
        
        verify(memberRepository).findById(1L);
        verify(memberRepository).existsByEmail("johnny@example.com");
        verify(memberRepository).save(testMember);
    }
    
    @Test
    // Test updating member with same email address
    void testUpdateMember_SameEmail() {
        Member updatedData = Member.builder()
                .name("Johnny")
                .surname("Doe")
                .email("john@example.com") // Same email
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        
        Member result = memberService.updateMember(1L, updatedData);
        
        assertEquals("Johnny", testMember.getName());
        assertEquals("john@example.com", testMember.getEmail());
        
        verify(memberRepository).findById(1L);
        verify(memberRepository, never()).existsByEmail(anyString());
        verify(memberRepository).save(testMember);
    }
    
    @Test
    // Test updating member with duplicate email
    void testUpdateMember_DuplicateEmail() {
        Member updatedData = Member.builder()
                .name("Johnny")
                .surname("Doe")
                .email("jane@example.com") // Different email that already exists
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.existsByEmail("jane@example.com")).thenReturn(true);
        
        assertThrows(DuplicateEmailException.class, () -> memberService.updateMember(1L, updatedData));
        
        verify(memberRepository).findById(1L);
        verify(memberRepository).existsByEmail("jane@example.com");
        verify(memberRepository, never()).save(any());
    }
    
    @Test
    // Test updating member with null games played value
    void testUpdateMember_NullGamesPlayed() {
        Member updatedData = Member.builder()
                .name("Johnny")
                .surname("Doe")
                .email("john@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .gamesPlayed(null) // Null games played
                .build();
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        
        Member result = memberService.updateMember(1L, updatedData);
        
        assertEquals(10, testMember.getGamesPlayed()); // Should preserve original value
        
        verify(memberRepository).save(testMember);
    }
    
    @Test
    // Test successfully deleting a member and updating ranks
    void testDeleteMember_Success() {
        Member member2 = Member.builder()
                .id(2L)
                .name("Jane")
                .surname("Smith")
                .email("jane@example.com")
                .currentRank(3)
                .build();
        
        Member member3 = Member.builder()
                .id(3L)
                .name("Bob")
                .surname("Johnson")
                .email("bob@example.com")
                .currentRank(4)
                .build();
        
        testMember.setCurrentRank(2); // Member to delete has rank 2
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.findByCurrentRankBetweenOrderByCurrentRankAsc(3, Integer.MAX_VALUE))
                .thenReturn(List.of(member2, member3));
        
        memberService.deleteMember(1L);
        
        verify(memberRepository).findById(1L);
        verify(memberRepository).delete(testMember);
        verify(memberRepository).findByCurrentRankBetweenOrderByCurrentRankAsc(3, Integer.MAX_VALUE);
        verify(memberRepository).updateMemberRank(2L, 2); // member2 moves from 3 to 2
        verify(memberRepository).updateMemberRank(3L, 3); // member3 moves from 4 to 3
    }
    
    @Test
    // Test deleting a non-existent member
    void testDeleteMember_NotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(MemberNotFoundException.class, () -> memberService.deleteMember(99L));
        
        verify(memberRepository).findById(99L);
        verify(memberRepository, never()).delete(any());
    }
    
    @Test
    // Test searching members by name or surname term
    void testSearchMembers_WithTerm() {
        List<Member> expectedResults = List.of(testMember);
        when(memberRepository.searchByNameOrSurname("john")).thenReturn(expectedResults);
        
        List<Member> result = memberService.searchMembers("john");
        
        assertEquals(expectedResults, result);
        verify(memberRepository).searchByNameOrSurname("john");
    }
    
    @Test
    // Test searching members with empty search term
    void testSearchMembers_EmptyTerm() {
        List<Member> allMembers = List.of(testMember);
        when(memberRepository.findAllByOrderByCurrentRankAsc()).thenReturn(allMembers);
        
        List<Member> result = memberService.searchMembers("");
        
        assertEquals(allMembers, result);
        verify(memberRepository).findAllByOrderByCurrentRankAsc();
        verify(memberRepository, never()).searchByNameOrSurname(anyString());
    }
    
    @Test
    // Test searching members with null search term
    void testSearchMembers_NullTerm() {
        List<Member> allMembers = List.of(testMember);
        when(memberRepository.findAllByOrderByCurrentRankAsc()).thenReturn(allMembers);
        
        List<Member> result = memberService.searchMembers(null);
        
        assertEquals(allMembers, result);
        verify(memberRepository).findAllByOrderByCurrentRankAsc();
    }
    
    @Test
    // Test searching members trims whitespace from search term
    void testSearchMembers_TrimsWhitespace() {
        List<Member> expectedResults = List.of(testMember);
        when(memberRepository.searchByNameOrSurname("john")).thenReturn(expectedResults);
        
        List<Member> result = memberService.searchMembers("  john  ");
        
        assertEquals(expectedResults, result);
        verify(memberRepository).searchByNameOrSurname("john");
    }
    
    @Test
    // Test retrieving member by their rank
    void testGetMemberByRank() {
        when(memberRepository.findByCurrentRank(1)).thenReturn(Optional.of(testMember));
        
        Optional<Member> result = memberService.getMemberByRank(1);
        
        assertTrue(result.isPresent());
        assertEquals(testMember, result.get());
        verify(memberRepository).findByCurrentRank(1);
    }
    
    @Test
    // Test retrieving top ranked members leaderboard
    void testGetLeaderboard() {
        List<Member> expectedLeaderboard = List.of(testMember);
        when(memberRepository.findTopRankedMembers(any(Pageable.class))).thenReturn(expectedLeaderboard);
        
        List<Member> result = memberService.getLeaderboard(10);
        
        assertEquals(expectedLeaderboard, result);
        verify(memberRepository).findTopRankedMembers(argThat(pageable -> 
            pageable.getPageNumber() == 0 && pageable.getPageSize() == 10));
    }
    
    @Test
    // Test incrementing games played counter for member
    void testIncrementGamesPlayed() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        
        memberService.incrementGamesPlayed(1L);
        
        assertEquals(11, testMember.getGamesPlayed()); // Should be incremented from 10 to 11
        
        verify(memberRepository).findById(1L);
        verify(memberRepository).save(testMember);
    }
    
    @Test
    // Test updating ranks for two members
    void testUpdateMemberRanks() {
        Member member2 = Member.builder()
                .id(2L)
                .name("Jane")
                .surname("Smith")
                .currentRank(2)
                .build();
        
        when(memberRepository.save(testMember)).thenReturn(testMember);
        when(memberRepository.save(member2)).thenReturn(member2);
        
        memberService.updateMemberRanks(testMember, 3, member2, 1);
        
        assertEquals(3, testMember.getCurrentRank());
        assertEquals(1, member2.getCurrentRank());
        
        verify(memberRepository).save(testMember);
        verify(memberRepository).save(member2);
    }
}