package com.medflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "El primer nombre es requerido")
    private String firstName;

    private String secondName;

    @NotBlank(message = "El primer apellido es requerido")
    private String firstLastName;

    private String secondLastName;

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo inválido")
    private String email;

    @Pattern(regexp = "\\d{8}", message = "El teléfono debe tener 8 dígitos")
    private String phone;

    @NotNull(message = "El rol es requerido")
    private String roleName;
}
