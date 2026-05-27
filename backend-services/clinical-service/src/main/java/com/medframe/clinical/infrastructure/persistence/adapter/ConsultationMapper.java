package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.infrastructure.persistence.entity.ConsultationEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Consultation domain entity and ConsultationEntity JPA entity.
 */
@Component
public class ConsultationMapper {

    public ConsultationEntity toEntity(Consultation domain) {
        if (domain == null) {
            return null;
        }

        ConsultationEntity entity = new ConsultationEntity();
        entity.setId(domain.getId());
        entity.setPatientId(domain.getPatientId());
        entity.setDoctorId(domain.getDoctorId());
        entity.setAppointmentId(domain.getAppointmentId());
        entity.setChiefComplaint(domain.getChiefComplaint());
        entity.setSymptoms(domain.getSymptoms());
        entity.setPrimaryDiagnosis(domain.getPrimaryDiagnosis());
        entity.setSecondaryDiagnoses(domain.getSecondaryDiagnoses());
        entity.setMedicalNotes(domain.getMedicalNotes());
        entity.setTreatmentPlan(domain.getTreatmentPlan());
        entity.setConsultationDate(domain.getConsultationDate());
        entity.setPerformedBy(domain.getPerformedBy());

        return entity;
    }

    public Consultation toDomain(ConsultationEntity entity) {
        if (entity == null) {
            return null;
        }

        Consultation domain = new Consultation();
        domain.setId(entity.getId());
        domain.setPatientId(entity.getPatientId());
        domain.setDoctorId(entity.getDoctorId());
        domain.setAppointmentId(entity.getAppointmentId());
        domain.setChiefComplaint(entity.getChiefComplaint());
        domain.setSymptoms(entity.getSymptoms());
        domain.setPrimaryDiagnosis(entity.getPrimaryDiagnosis());
        domain.setSecondaryDiagnoses(entity.getSecondaryDiagnoses());
        domain.setMedicalNotes(entity.getMedicalNotes());
        domain.setTreatmentPlan(entity.getTreatmentPlan());
        domain.setConsultationDate(entity.getConsultationDate());
        domain.setPerformedBy(entity.getPerformedBy());

        return domain;
    }
}
