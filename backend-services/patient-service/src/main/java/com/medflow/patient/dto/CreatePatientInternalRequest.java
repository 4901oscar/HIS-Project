package com.medflow.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir peticiones internas de creación de pacientes desde auth-service.
 * Este endpoint es solo para comunicación interna entre microservicios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatientInternalRequest {

    @NotBlank(message = "El ID es requerido")
    private String id;

    @NotBlank(message = "El DPI es requerido")
    @Size(min = 13, max = 13, message = "El DPI debe tener 13 dígitos")
    private String dpi;

    private String nit;

    @NotBlank(message = "El primer nombre es requerido")
    private String firstName;

    private String secondName;

    @NotBlank(message = "El primer apellido es requerido")
    private String firstLastName;

    private String secondLastName;

    @NotBlank(message = "La fecha de nacimiento es requerida")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Formato de fecha inválido (YYYY-MM-DD)")
    private String birthDate;

    @NotBlank(message = "El género es requerido")
    @Pattern(regexp = "M|F", message = "El género debe ser M o F")
    private String gender;

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "\\d{8}", message = "El teléfono debe tener 8 dígitos")
    private String phone;

    private String department;
    private String municipality;
    private String zone;
    private String address;

    @NotBlank(message = "El auth_user_id es requerido")
    private String authUserId;

    @NotNull(message = "El estado activo es requerido")
    private Boolean active;
}
