package com.medflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta del patient-service al crear un paciente.
 * Contiene los datos básicos del paciente creado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private String id;
    private String dpi;
    private String fullName;
    private String email;
    private String authUserId;
    private Boolean active;
}
