package com.medflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El DPI es requerido")
    @Size(min = 13, max = 13, message = "El DPI debe tener 13 dígitos")
    @Pattern(regexp = "\\d{13}", message = "El DPI debe contener solo dígitos")
    private String dpi;

    @NotBlank(message = "El NIT es requerido")
    private String nit;

    @NotBlank(message = "El primer nombre es requerido")
    private String firstName;

    private String secondName;

    @NotBlank(message = "El primer apellido es requerido")
    private String firstLastName;

    private String secondLastName;

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "El teléfono es requerido")
    @Pattern(regexp = "\\d{8}", message = "El teléfono debe tener 8 dígitos")
    private String phone;

    private String address;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}
