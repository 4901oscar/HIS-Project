package com.medflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token validation response.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResponse {
    
    private boolean valid;
    private String userId;
    private String username;
    private String roles;
}
