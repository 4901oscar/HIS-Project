package com.medflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePatientAccountResponse {
    private String userId;
    private String patientId;
    private String username;
    private String temporaryPassword;
    private String message;
}
