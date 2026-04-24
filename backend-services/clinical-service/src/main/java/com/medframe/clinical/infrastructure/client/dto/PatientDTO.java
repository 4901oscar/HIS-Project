package com.medframe.clinical.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private String id;
    private String dpi;
    private String nit;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private LocalDate birthDate;
    private String gender;
    private String email;
    private String phone;
    private String department;
    private String municipality;
    private String zone;
    private String address;
    private String authUserId;
    private Boolean active;
    
    // Computed field for full name
    public String getFullName() {
        StringBuilder name = new StringBuilder(firstName);
        if (secondName != null && !secondName.isEmpty()) {
            name.append(" ").append(secondName);
        }
        name.append(" ").append(firstLastName);
        if (secondLastName != null && !secondLastName.isEmpty()) {
            name.append(" ").append(secondLastName);
        }
        return name.toString();
    }
}
