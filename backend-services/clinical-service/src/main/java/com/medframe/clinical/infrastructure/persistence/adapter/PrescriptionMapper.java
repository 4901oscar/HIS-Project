package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.infrastructure.persistence.entity.PrescriptionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Prescription domain entity and PrescriptionEntity JPA entity.
 */
@Component
public class PrescriptionMapper {

    public PrescriptionEntity toEntity(Prescription domain) {
        if (domain == null) {
            return null;
        }

        PrescriptionEntity entity = new PrescriptionEntity();
        entity.setId(domain.getId());
        entity.setConsultationId(domain.getConsultationId());
        entity.setPatientId(domain.getPatientId());
        entity.setDoctorId(domain.getDoctorId());
        entity.setPrescriptionCode(domain.getPrescriptionCode());
        entity.setMedications(domain.getMedications());
        entity.setStatus(domain.getStatus());
        entity.setIssuedAt(domain.getIssuedAt());
        entity.setIssuedBy(domain.getIssuedBy());

        return entity;
    }

    public Prescription toDomain(PrescriptionEntity entity) {
        if (entity == null) {
            return null;
        }

        Prescription domain = new Prescription();
        domain.setId(entity.getId());
        domain.setConsultationId(entity.getConsultationId());
        domain.setPatientId(entity.getPatientId());
        domain.setDoctorId(entity.getDoctorId());
        domain.setPrescriptionCode(entity.getPrescriptionCode());
        domain.setMedications(entity.getMedications());
        domain.setStatus(entity.getStatus());
        domain.setIssuedAt(entity.getIssuedAt());
        domain.setIssuedBy(entity.getIssuedBy());

        return domain;
    }
}
