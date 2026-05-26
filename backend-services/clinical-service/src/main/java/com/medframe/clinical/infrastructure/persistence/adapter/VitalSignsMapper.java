package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.VitalSigns;
import com.medframe.clinical.infrastructure.persistence.entity.VitalSignsEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between VitalSigns domain entity and VitalSignsEntity JPA entity.
 */
@Component
public class VitalSignsMapper {

    public VitalSignsEntity toEntity(VitalSigns domain) {
        if (domain == null) {
            return null;
        }

        VitalSignsEntity entity = new VitalSignsEntity();
        entity.setId(domain.getId());
        entity.setAppointmentId(domain.getAppointmentId());
        entity.setPatientId(domain.getPatientId());
        entity.setSystolicPressure(domain.getSystolicPressure());
        entity.setDiastolicPressure(domain.getDiastolicPressure());
        entity.setHeartRate(domain.getHeartRate());
        entity.setRespiratoryRate(domain.getRespiratoryRate());
        entity.setTemperature(domain.getTemperature());
        entity.setOxygenSaturation(domain.getOxygenSaturation());
        entity.setWeight(domain.getWeight());
        entity.setHeight(domain.getHeight());
        entity.setBmi(domain.getBmi());
        entity.setRecordedAt(domain.getRecordedAt());
        entity.setRecordedBy(domain.getRecordedBy());
        entity.setCreatedBy(domain.getRecordedBy());

        return entity;
    }

    public VitalSigns toDomain(VitalSignsEntity entity) {
        if (entity == null) {
            return null;
        }

        VitalSigns domain = new VitalSigns();
        domain.setId(entity.getId());
        domain.setAppointmentId(entity.getAppointmentId());
        domain.setPatientId(entity.getPatientId());
        domain.setSystolicPressure(entity.getSystolicPressure());
        domain.setDiastolicPressure(entity.getDiastolicPressure());
        domain.setHeartRate(entity.getHeartRate());
        domain.setRespiratoryRate(entity.getRespiratoryRate());
        domain.setTemperature(entity.getTemperature());
        domain.setOxygenSaturation(entity.getOxygenSaturation());
        domain.setWeight(entity.getWeight());
        domain.setHeight(entity.getHeight());
        domain.setBmi(entity.getBmi());
        domain.setRecordedAt(entity.getRecordedAt());
        domain.setRecordedBy(entity.getRecordedBy());

        return domain;
    }
}
