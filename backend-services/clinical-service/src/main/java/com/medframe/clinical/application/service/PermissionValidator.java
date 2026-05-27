package com.medframe.clinical.application.service;

import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Application service for validating user permissions based on roles extracted from HTTP headers.
 * 
 * This service extracts the X-User-Roles header from incoming requests and validates
 * that the user has the required role(s) to perform specific operations.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Component
public class PermissionValidator {
    
    private static final String ROLE_HEADER = "X-User-Roles";
    private static final String USER_ID_HEADER = "X-User-Id";
    
    @Autowired
    private HttpServletRequest request;
    
    /**
     * Validates that the current user has at least one of the allowed roles.
     * 
     * @param allowedRoles One or more roles that are allowed to perform the operation
     * @throws UnauthorizedException if no role information is found in the request
     * @throws ForbiddenException if the user doesn't have any of the required roles
     */
    public void requireRole(String... allowedRoles) {
        String userRoles = request.getHeader(ROLE_HEADER);
        
        if (userRoles == null || userRoles.isEmpty()) {
            throw new UnauthorizedException("No se encontró información de roles");
        }
        
        boolean hasPermission = Arrays.stream(allowedRoles)
            .anyMatch(userRoles::contains);
        
        if (!hasPermission) {
            throw new ForbiddenException(
                "No tiene permisos para realizar esta operación. Roles requeridos: " 
                + String.join(", ", allowedRoles));
        }
    }
    
    /**
     * Retrieves the user ID from the X-User-Id header.
     * 
     * @return The user ID, or null if not present
     */
    public String getUserId() {
        return request.getHeader(USER_ID_HEADER);
    }
    
    /**
     * Retrieves the user roles from the X-User-Roles header.
     * 
     * @return The user roles as a comma-separated string, or null if not present
     */
    public String getUserRoles() {
        return request.getHeader(ROLE_HEADER);
    }
}
