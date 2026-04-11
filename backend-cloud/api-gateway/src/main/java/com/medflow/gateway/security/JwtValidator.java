package com.medflow.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT Validator component for validating and parsing JWT tokens.
 * 
 * <p>This component validates JWT tokens by:
 * <ul>
 *   <li>Verifying the signature using the configured secret key</li>
 *   <li>Checking token expiration</li>
 *   <li>Extracting claims (userId, roles, exp)</li>
 * </ul>
 * 
 * <p>The validator uses JJWT library (version 0.11.5) for token parsing and validation.
 * All validation failures are logged and wrapped in {@link JwtException}.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @see JwtException
 */
@Component
public class JwtValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    /**
     * Validates a JWT token and extracts its claims.
     * 
     * <p>This method performs the following validations:
     * <ol>
     *   <li>Parses the JWT token structure</li>
     *   <li>Verifies the signature using the configured secret key</li>
     *   <li>Checks if the token has expired</li>
     *   <li>Extracts and returns the claims</li>
     * </ol>
     * 
     * @param token the JWT token to validate (must not be null)
     * @return Claims object containing token claims (userId, roles, exp)
     * @throws JwtException if token is invalid, expired, or malformed
     */
    public Claims validateAndGetClaims(String token) {
        try {
            logger.debug("Validating JWT token: {}...", token.substring(0, Math.min(token.length(), 10)));
            
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            logger.debug("JWT validation successful for userId: {}", claims.get("userId"));
            return claims;
            
        } catch (ExpiredJwtException e) {
            return handleExpiredToken(e);
        } catch (SignatureException e) {
            return handleInvalidSignature(e);
        } catch (MalformedJwtException e) {
            return handleMalformedToken(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }
    
    /**
     * Handles expired JWT token exceptions.
     * 
     * @param e the ExpiredJwtException thrown during validation
     * @return never returns, always throws JwtException
     * @throws JwtException with "Token expired" message
     */
    private Claims handleExpiredToken(ExpiredJwtException e) {
        logger.warn("JWT validation failed: Token expired at {}", e.getClaims().getExpiration());
        throw new JwtException("Token expired", e);
    }
    
    /**
     * Handles invalid signature exceptions.
     * 
     * @param e the SignatureException thrown during validation
     * @return never returns, always throws JwtException
     * @throws JwtException with "Invalid token signature" message
     */
    private Claims handleInvalidSignature(SignatureException e) {
        logger.warn("JWT validation failed: Invalid signature");
        throw new JwtException("Invalid token signature", e);
    }
    
    /**
     * Handles malformed JWT token exceptions.
     * 
     * @param e the MalformedJwtException thrown during validation
     * @return never returns, always throws JwtException
     * @throws JwtException with "Malformed token" message
     */
    private Claims handleMalformedToken(MalformedJwtException e) {
        logger.warn("JWT validation failed: Malformed token structure");
        throw new JwtException("Malformed token", e);
    }
    
    /**
     * Handles generic validation errors.
     * 
     * @param e the Exception thrown during validation
     * @return never returns, always throws JwtException
     * @throws JwtException with "Invalid token" message
     */
    private Claims handleGenericError(Exception e) {
        logger.error("JWT validation failed: Unexpected error", e);
        throw new JwtException("Invalid token", e);
    }
}
