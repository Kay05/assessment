package com.netstock.chessclub.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// Test class for Member entity
class MemberTest {
    
    private Member member;
    
    @BeforeEach
    void setUp() {
        // Initialize test member
        member = new Member();
    }
    
    @Test
    void testMemberBuilder() {
        // Test Member builder creates member with all properties
        LocalDate birthday = LocalDate.of(1990, 5, 15);
        
        Member builtMember = Member.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .birthday(birthday)
                .gamesPlayed(10)
                .currentRank(5)
                .build();
        
        assertEquals("John", builtMember.getName());
        assertEquals("Doe", builtMember.getSurname());
        assertEquals("john.doe@example.com", builtMember.getEmail());
        assertEquals(birthday, builtMember.getBirthday());
        assertEquals(10, builtMember.getGamesPlayed());
        assertEquals(5, builtMember.getCurrentRank());
    }
    
    @Test
    void testGetFullName() {
        // Test getFullName combines name and surname
        member.setName("Jane");
        member.setSurname("Smith");
        
        assertEquals("Jane Smith", member.getFullName());
    }
    
    @Test
    void testGetFullNameWithNullValues() {
        // Test getFullName handles null values
        member.setName(null);
        member.setSurname("Smith");
        
        assertEquals("null Smith", member.getFullName());
    }
    
    @Test
    void testDefaultGamesPlayed() {
        // Test default games played is zero
        Member newMember = new Member();
        assertEquals(0, newMember.getGamesPlayed());
    }
    
    @Test
    void testOnCreateSetsTimestamps() {
        // Test onCreate sets created and updated timestamps
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        member.onCreate();
        
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertNotNull(member.getCreatedAt());
        assertNotNull(member.getUpdatedAt());
        assertTrue(member.getCreatedAt().isAfter(before));
        assertTrue(member.getCreatedAt().isBefore(after));
        // Check they're very close, but don't require exact equality due to timing
        long timeDiff = java.time.Duration.between(member.getCreatedAt(), member.getUpdatedAt()).toNanos();
        assertTrue(Math.abs(timeDiff) < 1000000); // Less than 1 millisecond difference
    }
    
    @Test
    void testOnUpdateSetsUpdatedTimestamp() {
        // Test onUpdate only updates the updated timestamp
        LocalDateTime originalTime = LocalDateTime.now().minusDays(1);
        member.setCreatedAt(originalTime);
        member.setUpdatedAt(originalTime);
        
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        member.onUpdate();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertEquals(originalTime, member.getCreatedAt());
        assertNotNull(member.getUpdatedAt());
        assertTrue(member.getUpdatedAt().isAfter(before));
        assertTrue(member.getUpdatedAt().isBefore(after));
        assertNotEquals(member.getCreatedAt(), member.getUpdatedAt());
    }
    
    @Test
    void testEqualsAndHashCode() {
        // Test equals and hashCode for Member objects
        Member member1 = Member.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .build();
        
        Member member2 = Member.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .build();
        
        assertEquals(member1, member2);
        assertEquals(member1.hashCode(), member2.hashCode());
    }
    
    @Test
    void testNotEquals() {
        // Test not equals for different Member objects
        Member member1 = Member.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .build();
        
        Member member2 = Member.builder()
                .id(2L)
                .name("Jane")
                .surname("Smith")
                .build();
        
        assertNotEquals(member1, member2);
    }
    
    @Test
    void testToString() {
        // Test toString contains member properties
        member.setName("Test");
        member.setSurname("User");
        member.setEmail("test@example.com");
        
        String toString = member.toString();
        assertTrue(toString.contains("Test"));
        assertTrue(toString.contains("User"));
        assertTrue(toString.contains("test@example.com"));
    }
}