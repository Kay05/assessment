package com.netstock.chessclub.repository;

import com.netstock.chessclub.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// Tests for MemberRepository database operations
class MemberRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private MemberRepository memberRepository;
    
    private Member member1;
    private Member member2;
    private Member member3;
    
    @BeforeEach
    // Initialize test data with member entities
    void setUp() {
        member1 = Member.builder()
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .currentRank(1)
                .gamesPlayed(10)
                .build();
        
        member2 = Member.builder()
                .name("Jane")
                .surname("Smith")
                .email("jane@example.com")
                .birthday(LocalDate.of(1995, 5, 15))
                .currentRank(2)
                .gamesPlayed(5)
                .build();
        
        member3 = Member.builder()
                .name("Bob")
                .surname("Johnson")
                .email("bob@example.com")
                .birthday(LocalDate.of(1985, 12, 10))
                .currentRank(3)
                .gamesPlayed(15)
                .build();
        
        // Ensure timestamps are set for test entities
        LocalDateTime now = LocalDateTime.now();
        member1.setCreatedAt(now);
        member1.setUpdatedAt(now);
        member2.setCreatedAt(now);
        member2.setUpdatedAt(now);
        member3.setCreatedAt(now);
        member3.setUpdatedAt(now);
        
        entityManager.persistAndFlush(member1);
        entityManager.persistAndFlush(member2);
        entityManager.persistAndFlush(member3);
    }
    
    @Test
    // Test finding a member by email address
    void testFindByEmail() {
        Optional<Member> found = memberRepository.findByEmail("jane@example.com");
        
        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getName());
        assertEquals("Smith", found.get().getSurname());
    }
    
    @Test
    // Test finding a member by non-existent email
    void testFindByEmailNotFound() {
        Optional<Member> found = memberRepository.findByEmail("nonexistent@example.com");
        
        assertFalse(found.isPresent());
    }
    
    @Test
    // Test checking if member exists by email
    void testExistsByEmail() {
        assertTrue(memberRepository.existsByEmail("john@example.com"));
        assertFalse(memberRepository.existsByEmail("nonexistent@example.com"));
    }
    
    @Test
    // Test finding all members ordered by rank ascending
    void testFindAllByOrderByCurrentRankAsc() {
        List<Member> members = memberRepository.findAllByOrderByCurrentRankAsc();
        
        assertEquals(3, members.size());
        assertEquals(1, members.get(0).getCurrentRank());
        assertEquals(2, members.get(1).getCurrentRank());
        assertEquals(3, members.get(2).getCurrentRank());
        assertEquals("John", members.get(0).getName());
        assertEquals("Jane", members.get(1).getName());
        assertEquals("Bob", members.get(2).getName());
    }
    
    @Test
    // Test finding a member by their current rank
    void testFindByCurrentRank() {
        Optional<Member> found = memberRepository.findByCurrentRank(2);
        
        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getName());
        assertEquals(2, found.get().getCurrentRank());
    }
    
    @Test
    // Test finding a member by non-existent rank
    void testFindByCurrentRankNotFound() {
        Optional<Member> found = memberRepository.findByCurrentRank(10);
        
        assertFalse(found.isPresent());
    }
    
    @Test
    // Test finding members within a rank range
    void testFindByCurrentRankBetweenOrderByCurrentRankAsc() {
        List<Member> members = memberRepository.findByCurrentRankBetweenOrderByCurrentRankAsc(2, 3);
        
        assertEquals(2, members.size());
        assertEquals(2, members.get(0).getCurrentRank());
        assertEquals(3, members.get(1).getCurrentRank());
    }
    
    @Test
    // Test finding the highest rank in the database
    void testFindMaxRank() {
        Optional<Integer> maxRank = memberRepository.findMaxRank();
        
        assertTrue(maxRank.isPresent());
        assertEquals(3, maxRank.get());
    }
    
    @Test
    // Test finding max rank when database is empty
    void testFindMaxRankEmptyDatabase() {
        memberRepository.deleteAll();
        entityManager.flush();
        
        Optional<Integer> maxRank = memberRepository.findMaxRank();
        
        assertFalse(maxRank.isPresent());
    }
    
    @Test
    // Test updating a member's rank
    void testUpdateMemberRank() {
        memberRepository.updateMemberRank(member1.getId(), 5);
        entityManager.flush();
        entityManager.clear();
        
        Member updated = entityManager.find(Member.class, member1.getId());
        assertEquals(5, updated.getCurrentRank());
    }
    
    @Test
    // Test setting a temporary rank for a member
    void testSetTemporaryRank() {
        memberRepository.setTemporaryRank(member2.getId(), -1000);
        entityManager.flush();
        entityManager.clear();
        
        Member updated = entityManager.find(Member.class, member2.getId());
        assertEquals(-1000, updated.getCurrentRank());
    }
    
    @Test
    // Test searching members by name or surname
    void testSearchByNameOrSurname() {
        List<Member> results = memberRepository.searchByNameOrSurname("john");
        
        assertEquals(2, results.size()); // Should find "John" and "Johnson"
        assertTrue(results.stream().anyMatch(m -> m.getName().equals("John")));
        assertTrue(results.stream().anyMatch(m -> m.getSurname().equals("Johnson")));
    }
    
    @Test
    // Test case insensitive search by name or surname
    void testSearchByNameOrSurnameCaseInsensitive() {
        List<Member> results = memberRepository.searchByNameOrSurname("JANE");
        
        assertEquals(1, results.size());
        assertEquals("Jane", results.get(0).getName());
    }
    
    @Test
    // Test partial match search by name or surname
    void testSearchByNameOrSurnamePartialMatch() {
        List<Member> results = memberRepository.searchByNameOrSurname("Smi");
        
        assertEquals(1, results.size());
        assertEquals("Smith", results.get(0).getSurname());
    }
    
    @Test
    // Test search with no matching results
    void testSearchByNameOrSurnameNoResults() {
        List<Member> results = memberRepository.searchByNameOrSurname("xyz");
        
        assertTrue(results.isEmpty());
    }
    
    @Test
    // Test finding top ranked members with pagination
    void testFindTopRankedMembers() {
        List<Member> topMembers = memberRepository.findTopRankedMembers(PageRequest.of(0, 2));
        
        assertEquals(2, topMembers.size()); // Should limit to 2 results
        assertEquals(1, topMembers.get(0).getCurrentRank());
        assertEquals(2, topMembers.get(1).getCurrentRank());
    }
    
    @Test
    // Test counting members with games above threshold
    void testCountActiveMembers() {
        long count = memberRepository.countActiveMembers(8);
        
        assertEquals(2, count); // member1 has 10 games, member3 has 15 games
    }
    
    @Test
    // Test counting active members with higher games threshold
    void testCountActiveMembersWithHigherThreshold() {
        long count = memberRepository.countActiveMembers(12);
        
        assertEquals(1, count); // Only member3 has 15 games
    }
    
    @Test
    // Test counting active members with no matching results
    void testCountActiveMembersNoResults() {
        long count = memberRepository.countActiveMembers(20);
        
        assertEquals(0, count);
    }
}