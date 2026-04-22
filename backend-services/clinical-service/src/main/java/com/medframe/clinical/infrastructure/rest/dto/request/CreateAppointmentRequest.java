package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {
    
    @NotBlank(message = "Patient ID es requerido")
    private String patientId;
    
    // Optional: If null, automatic doctor assignment will be used
    private String doctorId;
    
    @NotNull(message = "Fecha de cita es requerida")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Hora de cita es requerida")
    private LocalTime appointmentTime;
    
    private String notes;

    // Optional: releases the temporary hold after a successful booking
    private String sessionId;
}
