package com.medflow.auth.dto;

import com.medflow.auth.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private String fullName;
    private String phone;
    private List<String> roles;
    private boolean active;

    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.getId().toString(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getSecondName(),
            user.getFirstLastName(),
            user.getSecondLastName(),
            user.getFullName(),
            user.getPhone(),
            user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()),
            user.isActive()
        );
    }
}
