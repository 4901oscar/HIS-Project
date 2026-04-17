package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.infrastructure.persistence.entity.LabOrderEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between LabOrder domain entity and LabOrderEntity JPA entity.
 */
@Component
public class LabOrderMapper {

    public LabOrderEntity toEntity(LabOrder domain) {
        if (domain == null) {
            return null;
        }

        LabOrderEntity entity = new LabOrderEntity();
        entity.setId(domain.getId());
        entity.setConsultationId(domain.getConsultationId());
        entity.setPatientId(domain.getPatientId());
        entity.setDoctorId(domain.getDoctorId());
        entity.setOrderCode(domain.getOrderCode());
        entity.setTestNames(domain.getTestNames());
        entity.setStatus(domain.getStatus());
        entity.setOrderedAt(domain.getOrderedAt());
        entity.setOrderedBy(domain.getOrderedBy());

        return entity;
    }

    public LabOrder toDomain(LabOrderEntity entity) {
        if (entity == null) {
            return null;
        }

        LabOrder domain = new LabOrder();
        domain.setId(entity.getId());
        domain.setConsultationId(entity.getConsultationId());
        domain.setPatientId(entity.getPatientId());
        domain.setDoctorId(entity.getDoctorId());
        domain.setOrderCode(entity.getOrderCode());
        domain.setTestNames(entity.getTestNames());
        domain.setStatus(entity.getStatus());
        domain.setOrderedAt(entity.getOrderedAt());
        domain.setOrderedBy(entity.getOrderedBy());

        return domain;
    }
}
