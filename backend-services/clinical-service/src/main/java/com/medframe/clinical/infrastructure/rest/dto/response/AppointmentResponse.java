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
    private String patientName;
    private String patientDpi;
    private String doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private String qrCodeBase64;
    private String invoiceId;
    private String labInvoiceId;
    private String pharmacyInvoiceId;
    private Boolean isPriority;
}
