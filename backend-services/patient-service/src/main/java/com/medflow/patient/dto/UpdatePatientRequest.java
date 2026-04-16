package com.medflow.patient.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdatePatientRequest {

    @Email @Size(max = 100)
    private String email;

    @Pattern(regexp = "\\d{8}", message = "El telefono debe tener 8 digitos")
    private String phone;

    private String department;
    private String municipality;
    private String zone;
    private String address;
}
