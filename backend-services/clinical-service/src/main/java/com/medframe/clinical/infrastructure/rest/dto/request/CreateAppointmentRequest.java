package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.Future;
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
    
    @NotBlank(message = "Doctor ID es requerido")
    private String doctorId;
    
    @NotNull(message = "Fecha de cita es requerida")
    @Future(message = "La fecha debe ser futura")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Hora de cita es requerida")
    private LocalTime appointmentTime;
    
    private String notes;
}
