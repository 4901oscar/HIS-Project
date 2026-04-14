package com.medflow.auth.dto;

import com.medflow.auth.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for user response.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private String id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private boolean active;
    
    /**
     * Creates a UserResponse from a User entity.
     */
    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.getId().toString(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()),
            user.isActive()
        );
    }
}
