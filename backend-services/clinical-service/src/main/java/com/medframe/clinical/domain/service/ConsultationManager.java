package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.ConsultationRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ConsultationManager {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;

    public ConsultationManager(ConsultationRepository consultationRepository,
                                AppointmentRepository appointmentRepository) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Consultation registerConsultation(String patientId, String doctorId,
                                              String appointmentId, String chiefComplaint,
                                              String symptoms, String primaryDiagnosis,
                                              List<String> secondaryDiagnoses,
                                              String medicalNotes, String treatmentPlan) {

        Consultation consultation = new Consultation();
        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorId);
        consultation.setAppointmentId(appointmentId);
        consultation.setChiefComplaint(chiefComplaint);
        consultation.setSymptoms(symptoms);
        consultation.setPrimaryDiagnosis(primaryDiagnosis);
        consultation.setSecondaryDiagnoses(secondaryDiagnoses);
        consultation.setMedicalNotes(medicalNotes);
        consultation.setTreatmentPlan(treatmentPlan);
        consultation.setConsultationDate(LocalDateTime.now());
        consultation.setPerformedBy(doctorId);

        Consultation saved = consultationRepository.save(consultation);

        // Complete associated appointment if provided
        if (appointmentId != null && !appointmentId.isBlank()) {
            appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
                try {
                    appointment.complete();
                    appointmentRepository.save(appointment);
                } catch (IllegalStateException e) {
                    // Appointment may already be in a different state — log but don't fail
                }
            });
        }

        return saved;
    }
}
