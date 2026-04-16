package com.medflow.patient.dto;

import com.medflow.patient.model.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreatePatientRequest {

    @NotBlank @Size(min = 13, max = 13, message = "El DPI debe tener 13 digitos")
    private String dpi;

    @Size(max = 20)
    private String nit;

    @NotBlank @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String secondName;

    @NotBlank @Size(max = 100)
    private String firstLastName;

    @Size(max = 100)
    private String secondLastName;

    @NotNull
    @Past(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate birthDate;

    @NotNull
    private Gender gender;

    @NotBlank @Email @Size(max = 100)
    private String email;

    @NotBlank @Pattern(regexp = "\\d{8}", message = "El telefono debe tener 8 digitos")
    private String phone;

    private String department;
    private String municipality;
    private String zone;
    private String address;
}
