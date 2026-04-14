package com.medflow.auth.service;

import com.medflow.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for JWT token generation and validation.
 * 
 * <p>This service provides functionality to:
 * <ul>
 *   <li>Generate JWT tokens for authenticated users</li>
 *   <li>Validate JWT token signatures and expiration</li>
 *   <li>Extract claims and user information from tokens</li>
 * </ul>
 * 
 * <p>Uses JJWT library with HS256 signature algorithm.
 * Tokens include userId, username, and roles as claims.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@Service
public class JwtService {
    
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}")
    private long expirationTime;
    
    /**
     * Generates a JWT token for the specified user.
     * 
     * <p>The generated token includes:
     * <ul>
     *   <li>userId - User's unique identifier</li>
     *   <li>username - User's username</li>
     *   <li>roles - Comma-separated list of user roles</li>
     *   <li>subject - Set to userId</li>
     *   <li>issuedAt - Current timestamp</li>
     *   <li>expiration - Current timestamp + configured expiration time</li>
     * </ul>
     * 
     * @param user the user entity containing user information
     * @return JWT token string signed with HS256 algorithm
     * @throws IllegalArgumentException if user is null
     */
    public String generateToken(User user) {
        log.debug("Generating JWT token for user: {}", user.getUsername());
        
        Map<String, Object> claims = buildClaims(user);
        Date issuedAt = new Date();
        Date expiration = new Date(System.currentTimeMillis() + expirationTime);
        
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId().toString())
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .signWith(SignatureAlgorithm.HS256, getSigningKey())
            .compact();
        
        log.info("JWT token generated successfully for user: {} (expires at: {})", 
                user.getUsername(), expiration);
        
        return token;
    }
    
    /**
     * Validates the JWT token signature and expiration.
     * 
     * <p>A token is considered valid if:
     * <ul>
     *   <li>Signature is valid (signed with correct secret key)</li>
     *   <li>Token has not expired</li>
     *   <li>Token structure is well-formed</li>
     * </ul>
     * 
     * @param token the JWT token string to validate
     * @return true if token is valid, false otherwise
     */
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token);
            
            log.debug("JWT token validation successful");
            return true;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts all claims from the JWT token.
     * 
     * <p>This method parses the token and returns the claims body.
     * The token must be valid (correct signature and not expired).
     * 
     * @param token the JWT token string
     * @return Claims object containing all token claims
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    public Claims extractClaims(String token) {
        log.debug("Extracting claims from JWT token");
        
        return Jwts.parser()
            .setSigningKey(getSigningKey())
            .parseClaimsJws(token)
            .getBody();
    }
    
    /**
     * Extracts the userId claim from the JWT token.
     * 
     * @param token the JWT token string
     * @return userId string from token claims
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    public String extractUserId(String token) {
        return extractClaim(token, CLAIM_USER_ID, String.class);
    }
    
    /**
     * Extracts the username claim from the JWT token.
     * 
     * @param token the JWT token string
     * @return username string from token claims
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    public String extractUsername(String token) {
        return extractClaim(token, CLAIM_USERNAME, String.class);
    }
    
    /**
     * Extracts the roles claim from the JWT token.
     * 
     * @param token the JWT token string
     * @return comma-separated roles string from token claims
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    public String extractRoles(String token) {
        return extractClaim(token, CLAIM_ROLES, String.class);
    }
    
    /**
     * Gets the configured token expiration time in seconds.
     * 
     * @return expiration time in seconds
     */
    public long getExpirationTime() {
        return expirationTime / 1000; // Return in seconds
    }
    
    // ==================== Private Helper Methods ====================
    
    /**
     * Builds the claims map for a user.
     * 
     * @param user the user entity
     * @return map containing userId, username, and roles claims
     */
    private Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, user.getId().toString());
        claims.put(CLAIM_USERNAME, user.getUsername());
        claims.put(CLAIM_ROLES, user.getRolesAsString());
        return claims;
    }
    
    /**
     * Extracts a specific claim from the JWT token.
     * 
     * @param token the JWT token string
     * @param claimName the name of the claim to extract
     * @param claimType the expected type of the claim
     * @param <T> the type parameter
     * @return the claim value cast to the specified type
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    private <T> T extractClaim(String token, String claimName, Class<T> claimType) {
        Claims claims = extractClaims(token);
        return claims.get(claimName, claimType);
    }
    
    /**
     * Gets the signing key as byte array.
     * 
     * @return signing key bytes in UTF-8 encoding
     */
    private byte[] getSigningKey() {
        return secretKey.getBytes(StandardCharsets.UTF_8);
    }
}
