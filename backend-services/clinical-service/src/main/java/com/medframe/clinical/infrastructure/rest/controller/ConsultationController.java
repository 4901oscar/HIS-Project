package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.config.ConsultationPriceConfig;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.domain.port.in.RegisterConsultationUseCase;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.ConsultationRepository;
import com.medframe.clinical.domain.service.AppointmentManager;
import com.medframe.clinical.infrastructure.client.BillingServiceClient;
import com.medframe.clinical.infrastructure.client.dto.ChargeRequest;
import com.medframe.clinical.infrastructure.client.dto.CreateInvoiceRequest;
import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import com.medframe.clinical.infrastructure.rest.dto.request.ConsultationRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.ConsultationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinical/consultations")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ConsultationController {

    private final RegisterConsultationUseCase registerConsultationUseCase;
    private final BillingServiceClient billingServiceClient;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final AppointmentManager appointmentManager;
    private final ConsultationPriceConfig priceConfig;

    @PostMapping
    public ResponseEntity<ConsultationResponse> registerConsultation(
            @Valid @RequestBody ConsultationRequest request,
            @RequestHeader("X-User-Id") String userId) {

        Consultation consultation = registerConsultationUseCase.registerConsultation(
            request.getPatientId(),
            userId,
            request.getAppointmentId(),
            request.getChiefComplaint(),
            request.getSymptoms(),
            request.getPrimaryDiagnosis(),
            request.getSecondaryDiagnoses(),
            request.getMedicalNotes(),
            request.getTreatmentPlan(),
            request.isHasLabOrders(),
            request.isHasPrescription()
        );

        if (request.isHasLabOrders() && request.getAppointmentId() != null) {
            createLabInvoice(request.getPatientId(), request.getAppointmentId(), userId, request.getLabCharges());
        }

        if (request.isHasPrescription() && request.getAppointmentId() != null) {
            createPharmacyInvoice(request.getPatientId(), request.getAppointmentId(), userId, request.getPharmacyCharges());
        }

        // Create follow-up appointment if requested
        if (request.getFollowUpDate() != null && request.getFollowUpTime() != null) {
            createFollowUpAppointment(
                request.getPatientId(),
                userId, // doctorId is the current user (doctor)
                request.getFollowUpDate(),
                request.getFollowUpTime(),
                userId
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(consultation));
    }

    @GetMapping("/by-appointment/{appointmentId}")
    public ResponseEntity<ConsultationResponse> getByAppointment(@PathVariable String appointmentId) {
        return consultationRepository.findByAppointmentId(appointmentId)
                .map(c -> ResponseEntity.ok(mapToResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    private void createFollowUpAppointment(String patientId, String doctorId, 
                                           LocalDate followUpDate, LocalTime followUpTime, 
                                           String userId) {
        try {
            log.info("Creating follow-up appointment for patient {} with doctor {} on {} at {}", 
                     patientId, doctorId, followUpDate, followUpTime);
            
            // 1. Create the follow-up appointment
            Appointment followUpAppointment = appointmentManager.createAppointment(
                patientId,
                doctorId,
                followUpDate,
                followUpTime,
                "Cita de seguimiento",
                userId,
                false, // skipAvailabilityCheck = false (validate availability)
                false  // hasPaid = false (requires payment)
            );
            
            log.info("Follow-up appointment created successfully with ID: {}", followUpAppointment.getId());
            
            // 2. Create invoice for the follow-up appointment
            try {
                ChargeRequest followUpCharge = new ChargeRequest(
                        "CONSULTATION",
                        "Consulta de seguimiento",
                        1,
                        priceConfig.getFollowup() // Use follow-up price from config
                );
                
                CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest(
                        patientId,
                        followUpAppointment.getId(),
                        Collections.singletonList(followUpCharge)
                );
                
                InvoiceResponse invoiceResponse = billingServiceClient.createInvoice(invoiceRequest, userId);
                
                if (invoiceResponse != null) {
                    followUpAppointment.setInvoiceId(invoiceResponse.getId());
                    appointmentRepository.save(followUpAppointment);
                    log.info("Invoice created for follow-up appointment {}. InvoiceId: {}, InvoiceNumber: {}",
                            followUpAppointment.getId(), invoiceResponse.getId(), invoiceResponse.getInvoiceNumber());
                } else {
                    log.warn("Billing Service unavailable — follow-up appointment {} created without invoice",
                            followUpAppointment.getId());
                }
            } catch (Exception e) {
                log.error("Failed to create invoice for follow-up appointment {}: {}", 
                         followUpAppointment.getId(), e.getMessage(), e);
                // Continue - invoice creation failure shouldn't block appointment creation
            }
            
        } catch (Exception e) {
            log.error("Failed to create follow-up appointment for patient {}: {}", 
                     patientId, e.getMessage(), e);
            // Don't throw - follow-up appointment creation failure shouldn't block consultation registration
        }
    }

    private void createLabInvoice(String patientId, String appointmentId, String userId,
                                   List<ConsultationRequest.ServiceCharge> charges) {
        try {
            List<ChargeRequest> chargeList;
            if (charges != null && !charges.isEmpty()) {
                chargeList = charges.stream()
                    .map(c -> new ChargeRequest("LABORATORY", c.getName(), 1, c.getPrice()))
                    .collect(Collectors.toList());
            } else {
                chargeList = Collections.singletonList(
                    new ChargeRequest("LABORATORY", "Exámenes de laboratorio", 1, priceConfig.getLab())
                );
            }

            InvoiceResponse invoice = billingServiceClient.createInvoice(
                new CreateInvoiceRequest(patientId, appointmentId, chargeList), userId);

            if (invoice != null) {
                appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
                    appointment.setLabInvoiceId(invoice.getId());
                    appointmentRepository.save(appointment);
                    log.info("Lab invoice {} created and linked to appointment {}", invoice.getId(), appointmentId);
                });
            } else {
                log.warn("Billing Service unavailable — lab invoice not created for appointment {}", appointmentId);
            }
        } catch (Exception e) {
            log.error("Failed to create lab invoice for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
    }

    private void createPharmacyInvoice(String patientId, String appointmentId, String userId,
                                        List<ConsultationRequest.ServiceCharge> charges) {
        try {
            List<ChargeRequest> chargeList;
            if (charges != null && !charges.isEmpty()) {
                chargeList = charges.stream()
                    .map(c -> new ChargeRequest("MEDICATION", c.getName(), 1, c.getPrice()))
                    .collect(Collectors.toList());
            } else {
                log.warn("No pharmacy charges provided for appointment {} — skipping pharmacy invoice", appointmentId);
                return;
            }

            InvoiceResponse invoice = billingServiceClient.createInvoice(
                new CreateInvoiceRequest(patientId, appointmentId, chargeList), userId);

            if (invoice != null) {
                appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
                    appointment.setPharmacyInvoiceId(invoice.getId());
                    appointmentRepository.save(appointment);
                    log.info("Pharmacy invoice {} created and linked to appointment {}", invoice.getId(), appointmentId);
                });
            } else {
                log.warn("Billing Service unavailable — pharmacy invoice not created for appointment {}", appointmentId);
            }
        } catch (Exception e) {
            log.error("Failed to create pharmacy invoice for appointment {}: {}", appointmentId, e.getMessage(), e);
        }
    }

    private ConsultationResponse mapToResponse(Consultation consultation) {
        return new ConsultationResponse(
            consultation.getId(),
            consultation.getPatientId(),
            consultation.getDoctorId(),
            consultation.getChiefComplaint(),
            consultation.getSymptoms(),
            consultation.getPrimaryDiagnosis(),
            consultation.getSecondaryDiagnoses(),
            consultation.getMedicalNotes(),
            consultation.getTreatmentPlan(),
            consultation.getConsultationDate()
        );
    }
}
