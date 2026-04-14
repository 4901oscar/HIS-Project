package com.medflow.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import com.medflow.auth.domain.User;
import com.medflow.auth.repository.UserRepository;
import com.medflow.auth.service.AuthService;
import com.medflow.auth.service.JwtService;
import com.medflow.auth.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController (RED phase - TDD).
 * 
 * <p>These tests are written BEFORE the controller implementation to follow TDD methodology.
 * All tests should initially FAIL until AuthController is implemented.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>POST /api/auth/login - Login with valid/invalid credentials</li>
 *   <li>POST /api/auth/logout - Logout with valid token</li>
 *   <li>POST /api/auth/refresh - Refresh token</li>
 *   <li>GET /api/auth/validate - Validate token</li>
 *   <li>GET /api/auth/me - Get current user info</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    @MockBean
    private JwtService jwtService;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private TokenBlacklistService blacklistService;
    
    private User testUser;
    private String validToken;
    
    @BeforeEach
    void setUp() {
        // Create test role
        Role doctorRole = Role.builder()
            .id(1L)
            .name(RoleName.DOCTOR)
            .description("Doctor role")
            .build();
        
        // Create test user
        testUser = User.builder()
            .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
            .username("doctor1")
            .password("$2a$10$encodedPasswordHash")
            .email("doctor1@medflow.com")
            .fullName("Dr. Juan Pérez")
            .active(true)
            .roles(Set.of(doctorRole))
            .build();
        
        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTEyMyJ9.signature";
    }
    
    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        // Given: Valid login request
        String loginRequest = """
            {
                "username": "doctor1",
                "password": "password123"
            }
            """;
        
        when(authService.login("doctor1", "password123")).thenReturn(validToken);
        when(jwtService.getExpirationTime()).thenReturn(86400L);
        when(userRepository.findByUsername("doctor1")).thenReturn(Optional.of(testUser));
        
        // When/Then: Should return 200 with token
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(validToken))
            .andExpect(jsonPath("$.expiresIn").value(86400))
            .andExpect(jsonPath("$.user.id").value("550e8400-e29b-41d4-a716-446655440000"))
            .andExpect(jsonPath("$.user.username").value("doctor1"))
            .andExpect(jsonPath("$.user.email").value("doctor1@medflow.com"))
            .andExpect(jsonPath("$.user.fullName").value("Dr. Juan Pérez"))
            .andExpect(jsonPath("$.user.roles[0]").value("DOCTOR"));
        
        verify(authService).login("doctor1", "password123");
    }
    
    @Test
    void shouldReturn401WithInvalidCredentials() throws Exception {
        // Given: Invalid login request
        String loginRequest = """
            {
                "username": "doctor1",
                "password": "wrongPassword"
            }
            """;
        
        when(authService.login("doctor1", "wrongPassword"))
            .thenThrow(new RuntimeException("Invalid credentials"));
        
        // When/Then: Should return 401 Unauthorized
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("Invalid credentials"));
        
        verify(authService).login("doctor1", "wrongPassword");
    }
    
    @Test
    void shouldLogoutSuccessfully() throws Exception {
        // Given: Valid token in Authorization header
        doNothing().when(authService).logout(validToken);
        
        // When/Then: Should return 200
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out successfully"));
        
        verify(authService).logout(validToken);
    }
    
    @Test
    void shouldRefreshToken() throws Exception {
        // Given: Valid token to refresh
        String newToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.newToken.signature";
        
        when(jwtService.isValid(validToken)).thenReturn(true);
        when(jwtService.extractUserId(validToken)).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(userRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(newToken);
        when(jwtService.getExpirationTime()).thenReturn(86400L);
        
        // When/Then: Should return 200 with new token
        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(newToken))
            .andExpect(jsonPath("$.expiresIn").value(86400));
        
        verify(jwtService).isValid(validToken);
        verify(jwtService).extractUserId(validToken);
        verify(jwtService).generateToken(testUser);
    }
    
    @Test
    void shouldValidateToken() throws Exception {
        // Given: Valid token to validate
        when(blacklistService.isBlacklisted(validToken)).thenReturn(false);
        when(jwtService.isValid(validToken)).thenReturn(true);
        when(jwtService.extractUserId(validToken)).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(jwtService.extractUsername(validToken)).thenReturn("doctor1");
        when(jwtService.extractRoles(validToken)).thenReturn("DOCTOR");
        
        // When/Then: Should return 200 with validation response
        mockMvc.perform(get("/api/auth/validate")
                .param("token", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.userId").value("550e8400-e29b-41d4-a716-446655440000"))
            .andExpect(jsonPath("$.username").value("doctor1"))
            .andExpect(jsonPath("$.roles").value("DOCTOR"));
        
        verify(blacklistService).isBlacklisted(validToken);
        verify(jwtService).isValid(validToken);
    }
    
    @Test
    void shouldGetCurrentUser() throws Exception {
        // Given: Valid token in Authorization header
        when(jwtService.isValid(validToken)).thenReturn(true);
        when(jwtService.extractUserId(validToken)).thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(userRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))).thenReturn(Optional.of(testUser));
        
        // When/Then: Should return 200 with user info
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("550e8400-e29b-41d4-a716-446655440000"))
            .andExpect(jsonPath("$.username").value("doctor1"))
            .andExpect(jsonPath("$.email").value("doctor1@medflow.com"))
            .andExpect(jsonPath("$.fullName").value("Dr. Juan Pérez"))
            .andExpect(jsonPath("$.roles[0]").value("DOCTOR"))
            .andExpect(jsonPath("$.active").value(true));
        
        verify(jwtService).isValid(validToken);
        verify(jwtService).extractUserId(validToken);
        verify(userRepository).findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    }
}
