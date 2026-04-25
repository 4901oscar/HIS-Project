package com.medframe.clinical.infrastructure.rest.dto.request;

import com.medframe.clinical.infrastructure.rest.validation.FutureAppointment;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FutureAppointment(message = "La cita debe ser al menos 30 minutos en el futuro")
public class CreateAppointmentRequest {
    
    // Optional: If provided, creates appointment for this patient (used by admission)
    // If null, uses the authenticated user's patient ID
    private String patientId;
    
    // Optional: If null, automatic doctor assignment will be used
    private String doctorId;
    
    @NotNull(message = "Fecha de cita es requerida")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Hora de cita es requerida")
    private LocalTime appointmentTime;
    
    private String notes;

    // Optional: releases the temporary hold after a successful booking
    private String sessionId;
}
