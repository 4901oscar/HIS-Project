package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Appointment domain entity and AppointmentEntity JPA entity.
 */
@Component
public class AppointmentMapper {

    public AppointmentEntity toEntity(Appointment domain) {
        if (domain == null) {
            return null;
        }

        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(domain.getId());
        entity.setPatientId(domain.getPatientId());
        entity.setDoctorId(domain.getDoctorId());
        entity.setAppointmentDate(domain.getAppointmentDate());
        entity.setAppointmentTime(domain.getAppointmentTime());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setInvoiceId(domain.getInvoiceId());

        return entity;
    }

    public Appointment toDomain(AppointmentEntity entity) {
        if (entity == null) {
            return null;
        }

        Appointment domain = new Appointment();
        domain.setId(entity.getId());
        domain.setPatientId(entity.getPatientId());
        domain.setDoctorId(entity.getDoctorId());
        domain.setAppointmentDate(entity.getAppointmentDate());
        domain.setAppointmentTime(entity.getAppointmentTime());
        domain.setStatus(entity.getStatus());
        domain.setNotes(entity.getNotes());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setInvoiceId(entity.getInvoiceId());

        return domain;
    }
}
