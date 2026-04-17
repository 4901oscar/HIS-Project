package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.infrastructure.persistence.entity.TriageEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Triage domain entity and TriageEntity JPA entity.
 */
@Component
public class TriageMapper {

    public TriageEntity toEntity(Triage domain) {
        if (domain == null) {
            return null;
        }

        TriageEntity entity = new TriageEntity();
        entity.setId(domain.getId());
        entity.setPatientId(domain.getPatientId());
        entity.setDoctorId(domain.getDoctorId());
        entity.setMotifId(domain.getMotifId());
        entity.setDiscriminatorIds(domain.getDiscriminatorIds());
        entity.setPriorityLevel(domain.getPriorityLevel());
        entity.setMaxWaitTimeMinutes(domain.getMaxWaitTimeMinutes());
        entity.setPerformedAt(domain.getPerformedAt());
        entity.setPerformedBy(domain.getPerformedBy());

        return entity;
    }

    public Triage toDomain(TriageEntity entity) {
        if (entity == null) {
            return null;
        }

        Triage domain = new Triage();
        domain.setId(entity.getId());
        domain.setPatientId(entity.getPatientId());
        domain.setDoctorId(entity.getDoctorId());
        domain.setMotifId(entity.getMotifId());
        domain.setDiscriminatorIds(entity.getDiscriminatorIds());
        domain.setPriorityLevel(entity.getPriorityLevel());
        domain.setMaxWaitTimeMinutes(entity.getMaxWaitTimeMinutes());
        domain.setPerformedAt(entity.getPerformedAt());
        domain.setPerformedBy(entity.getPerformedBy());

        return domain;
    }
}
