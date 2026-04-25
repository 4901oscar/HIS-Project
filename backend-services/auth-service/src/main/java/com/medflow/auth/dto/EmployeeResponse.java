package com.medflow.auth.dto;

import com.medflow.auth.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private String fullName;
    private String phone;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;

    public static EmployeeResponse fromUser(User user) {
        String role = user.getRoles().stream()
            .findFirst()
            .map(r -> r.getName().name())
            .orElse(null);

        return new EmployeeResponse(
            user.getId().toString(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getSecondName(),
            user.getFirstLastName(),
            user.getSecondLastName(),
            user.getFullName(),
            user.getPhone(),
            role,
            user.isActive(),
            user.getCreatedAt()
        );
    }
}
