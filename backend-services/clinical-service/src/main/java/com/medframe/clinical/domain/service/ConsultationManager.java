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
                                              String medicalNotes, String treatmentPlan,
                                              boolean hasLabOrders, boolean hasPrescription) {

        // Upsert: si ya existe consulta para esta cita, actualizarla; si no, crearla
        Consultation consultation = (appointmentId != null && !appointmentId.isBlank())
                ? consultationRepository.findByAppointmentId(appointmentId)
                        .orElse(new Consultation())
                : new Consultation();

        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorId);
        consultation.setAppointmentId(appointmentId);
        consultation.setChiefComplaint(chiefComplaint);
        consultation.setSymptoms(symptoms);
        consultation.setPrimaryDiagnosis(primaryDiagnosis);
        consultation.setSecondaryDiagnoses(secondaryDiagnoses);
        consultation.setMedicalNotes(medicalNotes);
        consultation.setTreatmentPlan(treatmentPlan);
        if (consultation.getConsultationDate() == null) {
            consultation.setConsultationDate(LocalDateTime.now());
        }
        consultation.setPerformedBy(doctorId);

        Consultation saved = consultationRepository.save(consultation);

        if (appointmentId != null && !appointmentId.isBlank()) {
            appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
                try {
                    Appointment.AppointmentStatus status = appointment.getStatus();
                    if (status == Appointment.AppointmentStatus.CONSULTATION) {
                        appointment.registerConsultation(hasLabOrders, hasPrescription);
                    } else if (status == Appointment.AppointmentStatus.RE_EVALUATION) {
                        appointment.completeReEvaluation(hasPrescription);
                    }
                    appointmentRepository.save(appointment);
                } catch (IllegalStateException e) {
                    // Estado inesperado — no interrumpir el flujo
                }
            });
        }

        return saved;
    }
}
