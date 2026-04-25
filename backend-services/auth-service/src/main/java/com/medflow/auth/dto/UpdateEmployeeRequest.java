package com.medflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateEmployeeRequest {

    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;

    @Email(message = "Formato de correo inválido")
    private String email;

    @Pattern(regexp = "\\d{8}", message = "El teléfono debe tener 8 dígitos")
    private String phone;

    private String roleName;
}
