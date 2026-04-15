package com.medflow.auth.service;

import com.medflow.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>User login with username/password validation</li>
 *   <li>JWT token generation for authenticated users</li>
 *   <li>Account status verification</li>
 *   <li>Token invalidation (logout)</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService blacklistService;
    
    public AuthService(UserRepository userRepository, 
                      JwtService jwtService, 
                      PasswordEncoder passwordEncoder,
                      TokenBlacklistService blacklistService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.blacklistService = blacklistService;
    }
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username the username
     * @param password the raw password
     * @return JWT token string
     * @throws RuntimeException if credentials are invalid or account is disabled
     */
    public String login(String username, String password) {
        // 1. Find user by username
        var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        // 2. Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        // 3. Check if user is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is disabled");
        }
        
        // 4. Generate JWT token
        String token = jwtService.generateToken(user);
        
        // 5. Return the token
        return token;
    }
    
    /**
     * Logs out a user by adding their token to the blacklist.
     * 
     * <p>Once a token is blacklisted, it cannot be used for authentication
     * even if it hasn't expired yet.
     * 
     * @param token the JWT token to invalidate
     */
    public void logout(String token) {
        blacklistService.addToBlacklist(token);
    }
}
