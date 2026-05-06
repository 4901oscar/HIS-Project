package com.medflow.auth.service;

import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import com.medflow.auth.domain.User;
import com.medflow.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for AuthService (RED phase - TDD).
 * 
 * <p>These tests are written BEFORE the implementation to follow TDD methodology.
 * All tests should initially FAIL until AuthService is implemented.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>Successful login with valid credentials</li>
 *   <li>Rejection of invalid passwords</li>
 *   <li>Rejection of non-existent users</li>
 *   <li>Rejection of inactive users</li>
 *   <li>JWT token generation on successful login</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private TokenBlacklistService blacklistService;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private Role doctorRole;
    
    @BeforeEach
    void setUp() {
        // Create test role
        doctorRole = Role.builder()
            .id(1L)
            .name(RoleName.DOCTOR)
            .description("Doctor role")
            .build();
        
        // Create test user with encoded password
        testUser = User.builder()
            .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
            .username("doctor1")
            .password("$2a$10$encodedPasswordHash") // BCrypt encoded
            .email("doctor1@medflow.com")
            .firstName("Juan")
            .firstLastName("Pérez")
            .active(true)
            .roles(Set.of(doctorRole))
            .build();
    }
    
    @Test
    void shouldLoginWithValidCredentials() {
        // Given: Valid username and password
        String username = "doctor1";
        String rawPassword = "password123";
        String expectedToken = "jwt.token.here";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(rawPassword, testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(expectedToken);
        
        // When: Attempting to login
        String token = authService.login(username, rawPassword);
        
        // Then: Should return JWT token
        assertThat(token).isNotNull();
        assertThat(token).isEqualTo(expectedToken);
        
        // Verify interactions
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, testUser.getPassword());
        verify(jwtService).generateToken(testUser);
    }
    
    @Test
    void shouldRejectInvalidPassword() {
        // Given: Valid username but wrong password
        String username = "doctor1";
        String wrongPassword = "wrongPassword";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, testUser.getPassword())).thenReturn(false);
        
        // When/Then: Should throw exception for invalid credentials
        assertThatThrownBy(() -> authService.login(username, wrongPassword))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid credentials");
        
        // Verify password was checked but token was not generated
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(wrongPassword, testUser.getPassword());
        verify(jwtService, never()).generateToken(any());
    }
    
    @Test
    void shouldRejectNonExistentUser() {
        // Given: Non-existent username
        String username = "nonexistent";
        String password = "password123";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // When/Then: Should throw exception for user not found
        assertThatThrownBy(() -> authService.login(username, password))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid credentials");
        
        // Verify repository was called but password was not checked
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }
    
    @Test
    void shouldRejectInactiveUser() {
        // Given: Valid credentials but inactive user
        String username = "doctor1";
        String password = "password123";
        
        testUser.setActive(false); // Make user inactive
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
        
        // When/Then: Should throw exception for inactive account
        assertThatThrownBy(() -> authService.login(username, password))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Account is disabled");
        
        // Verify password was checked but token was not generated
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, testUser.getPassword());
        verify(jwtService, never()).generateToken(any());
    }
    
    @Test
    void shouldReturnJwtOnSuccessfulLogin() {
        // Given: Valid credentials
        String username = "doctor1";
        String password = "password123";
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTEyMyJ9.signature";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(expectedToken);
        
        // When: Login is successful
        String actualToken = authService.login(username, password);
        
        // Then: Should return a valid JWT token format
        assertThat(actualToken).isNotNull();
        assertThat(actualToken).isNotEmpty();
        assertThat(actualToken).isEqualTo(expectedToken);
        assertThat(actualToken).contains("."); // JWT has dots separating parts
        
        // Verify JWT service was called to generate token
        verify(jwtService).generateToken(testUser);
    }
    
    @Test
    void shouldLogoutSuccessfully() {
        // Given: A valid JWT token
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTEyMyJ9.signature";
        
        // When: User logs out
        authService.logout(token);
        
        // Then: Token should be added to blacklist
        verify(blacklistService).addToBlacklist(token);
    }
    
    @Test
    void shouldAddTokenToBlacklistOnLogout() {
        // Given: A JWT token from a logged-in user
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        
        // When: Logout is called
        authService.logout(token);
        
        // Then: TokenBlacklistService should be called exactly once
        verify(blacklistService, times(1)).addToBlacklist(token);
    }
    
    @Test
    void shouldHandleMultipleLogouts() {
        // Given: Multiple tokens from different sessions
        String token1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.session1.token";
        String token2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.session2.token";
        String token3 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.session3.token";
        
        // When: Multiple logouts occur
        authService.logout(token1);
        authService.logout(token2);
        authService.logout(token3);
        
        // Then: All tokens should be blacklisted
        verify(blacklistService).addToBlacklist(token1);
        verify(blacklistService).addToBlacklist(token2);
        verify(blacklistService).addToBlacklist(token3);
    }
}
