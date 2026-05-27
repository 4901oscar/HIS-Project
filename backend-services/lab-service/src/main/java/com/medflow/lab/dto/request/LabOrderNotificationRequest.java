package com.medflow.lab.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderNotificationRequest {

    @NotBlank(message = "El código de orden es obligatorio")
    @Size(min = 8, max = 8, message = "El código de orden debe tener 8 caracteres")
    private String orderCode;

    @NotBlank(message = "El ID del paciente es obligatorio")
    private String patientId;

    @NotBlank(message = "El ID del doctor es obligatorio")
    private String doctorId;

    private String appointmentId;

    @NotEmpty(message = "La lista de exámenes no puede estar vacía")
    private List<String> testNames;
}
