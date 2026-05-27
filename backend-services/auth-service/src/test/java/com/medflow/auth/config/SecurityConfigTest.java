package com.medflow.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for SecurityConfig.
 * 
 * <p>Verifies that:
 * <ul>
 *   <li>PasswordEncoder bean is properly configured</li>
 *   <li>BCrypt encoder is used with strength 10</li>
 *   <li>Password encoding and matching works correctly</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    void shouldInjectPasswordEncoderBean() {
        // Then: PasswordEncoder bean should be available
        assertThat(passwordEncoder).isNotNull();
    }
    
    @Test
    void shouldUseBCryptPasswordEncoder() {
        // Then: Should be BCryptPasswordEncoder instance
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }
    
    @Test
    void shouldEncodePassword() {
        // Given: A raw password
        String rawPassword = "password123";
        
        // When: Encoding the password
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Then: Should produce a BCrypt hash
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEmpty();
        assertThat(encodedPassword).startsWith("$2a$10$"); // BCrypt with strength 10
        assertThat(encodedPassword).hasSize(60); // BCrypt hash length
    }
    
    @Test
    void shouldMatchEncodedPassword() {
        // Given: A raw password and its encoded version
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // When/Then: Should match correctly
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }
    
    @Test
    void shouldNotMatchWrongPassword() {
        // Given: A raw password and its encoded version
        String rawPassword = "password123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // When/Then: Should not match wrong password
        assertThat(passwordEncoder.matches(wrongPassword, encodedPassword)).isFalse();
    }
    
    @Test
    void shouldProduceDifferentHashesForSamePassword() {
        // Given: Same password encoded twice
        String rawPassword = "password123";
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);
        
        // Then: Hashes should be different (due to salt)
        assertThat(hash1).isNotEqualTo(hash2);
        
        // But both should match the original password
        assertThat(passwordEncoder.matches(rawPassword, hash1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, hash2)).isTrue();
    }
}
