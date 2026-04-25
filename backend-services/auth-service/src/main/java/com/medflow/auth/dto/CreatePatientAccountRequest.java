package com.medflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePatientAccountRequest {

    @NotBlank(message = "El DPI es requerido")
    @Size(min = 13, max = 13, message = "El DPI debe tener 13 dígitos")
    private String dpi;

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "El primer nombre es requerido")
    private String firstName;

    private String secondName;

    @NotBlank(message = "El primer apellido es requerido")
    private String firstLastName;

    private String secondLastName;

    @Pattern(regexp = "\\d{8}", message = "El teléfono debe tener 8 dígitos")
    private String phone;

    // Campos médicos obligatorios
    @NotBlank(message = "La fecha de nacimiento es requerida")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Formato de fecha inválido (YYYY-MM-DD)")
    private String birthDate;

    @NotBlank(message = "El género es requerido")
    @Pattern(regexp = "M|F", message = "El género debe ser M o F")
    private String gender;

    // Campos médicos opcionales
    private String nit;
    private String department;
    private String municipality;
    private String zone;
    private String address;
}
