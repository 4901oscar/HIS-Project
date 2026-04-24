package com.medframe.clinical.infrastructure.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private String id;
    private String patientId;
    private String patientName;  // NEW: Patient full name
    private String patientDpi;   // NEW: Patient DPI
    private String doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private String qrCodeBase64;  // NEW: QR code for appointment confirmation
}
