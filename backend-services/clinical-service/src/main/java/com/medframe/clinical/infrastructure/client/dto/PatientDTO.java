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
    private String firstName;
    private String lastName;
    private String documentType;
    private String documentNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private String phone;
    private String address;
}
