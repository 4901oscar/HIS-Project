package com.medflow.auth.controller;

import com.medflow.auth.domain.User;
import com.medflow.auth.dto.*;
import com.medflow.auth.repository.UserRepository;
import com.medflow.auth.service.AuthService;
import com.medflow.auth.service.JwtService;
import com.medflow.auth.service.LoginRateLimiter;
import com.medflow.auth.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>POST /api/auth/login - User login</li>
 *   <li>POST /api/auth/logout - User logout</li>
 *   <li>POST /api/auth/refresh - Token refresh</li>
 *   <li>GET /api/auth/validate - Token validation</li>
 *   <li>GET /api/auth/me - Get current user info</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;
    private final LoginRateLimiter rateLimiter;
    
    public AuthController(AuthService authService, 
                         JwtService jwtService,
                         UserRepository userRepository,
                         TokenBlacklistService blacklistService,
                         LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.blacklistService = blacklistService;
        this.rateLimiter = rateLimiter;
    }
    
    /**
     * Login endpoint.
     * 
     * @param request login credentials
     * @param httpRequest HTTP request to extract IP address
     * @return JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("Login attempt for user: {} from IP: {}", request.getUsername(), ipAddress);
        
        // Check rate limit
        rateLimiter.checkRateLimit(ipAddress);
        
        // Authenticate and get token
        String token = authService.login(request.getUsername(), request.getPassword());
        
        // Reset rate limit on successful login
        rateLimiter.resetAttempts(ipAddress);
        
        // Get user info
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Build response
        LoginResponse response = new LoginResponse(
            token,
            jwtService.getExpirationTime(),
            UserResponse.fromUser(user)
        );
        
        log.info("Login successful for user: {} from IP: {}", request.getUsername(), ipAddress);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout endpoint.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        log.info("Logout request received");
        
        authService.logout(token);
        
        log.info("Logout successful");
        return ResponseEntity.ok(new LogoutResponse("Logged out successfully"));
    }
    
    /**
     * Refresh token endpoint.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return new JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        log.info("Token refresh request received");
        
        // Check blacklist first
        if (blacklistService.isBlacklisted(token)) {
            throw new RuntimeException("Token is blacklisted");
        }
        
        // Validate current token
        if (!jwtService.isValid(token)) {
            throw new RuntimeException("Invalid token");
        }
        
        // Extract user from token
        String userId = jwtService.extractUserId(token);
        User user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate new token
        String newToken = jwtService.generateToken(user);
        
        RefreshResponse response = new RefreshResponse(
            newToken,
            jwtService.getExpirationTime()
        );
        
        log.info("Token refresh successful for user: {}", user.getUsername());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate token endpoint.
     * 
     * @param token JWT token to validate
     * @return validation result with claims
     */
    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestParam String token) {
        log.info("Token validation request received");
        
        // Check blacklist
        if (blacklistService.isBlacklisted(token)) {
            return ResponseEntity.ok(new ValidateResponse(false, null, null, null));
        }
        
        // Validate token
        if (!jwtService.isValid(token)) {
            return ResponseEntity.ok(new ValidateResponse(false, null, null, null));
        }
        
        // Extract claims
        String userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        String roles = jwtService.extractRoles(token);
        
        ValidateResponse response = new ValidateResponse(true, userId, username, roles);
        
        log.info("Token validation successful for user: {}", username);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user endpoint.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return current user info
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        log.info("Get current user request received");
        
        // Check blacklist first
        if (blacklistService.isBlacklisted(token)) {
            throw new RuntimeException("Token is blacklisted");
        }
        
        // Validate token
        if (!jwtService.isValid(token)) {
            throw new RuntimeException("Invalid token");
        }
        
        // Extract user from token
        String userId = jwtService.extractUserId(token);
        User user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserResponse response = UserResponse.fromUser(user);
        
        log.info("Get current user successful for user: {}", user.getUsername());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Extracts JWT token from Authorization header.
     * 
     * @param authHeader Authorization header value
     * @return JWT token string
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid Authorization header");
    }
    
    /**
     * Extracts client IP address from HTTP request.
     * Checks X-Forwarded-For header first (for proxied requests),
     * then falls back to remote address.
     * 
     * @param request HTTP request
     * @return client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
