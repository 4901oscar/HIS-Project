package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.DoctorAvailability;
import com.medframe.clinical.infrastructure.persistence.entity.DoctorAvailabilityEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between DoctorAvailability domain model and DoctorAvailabilityEntity.
 * 
 * <p>This mapper follows hexagonal architecture by translating between the domain layer
 * (DoctorAvailability) and the infrastructure layer (DoctorAvailabilityEntity).
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Component
public class DoctorAvailabilityMapper {
    
    /**
     * Converts a DoctorAvailability domain model to a DoctorAvailabilityEntity.
     * 
     * @param domain the domain model
     * @return the JPA entity
     */
    public DoctorAvailabilityEntity toEntity(DoctorAvailability domain) {
        if (domain == null) {
            return null;
        }
        
        DoctorAvailabilityEntity entity = new DoctorAvailabilityEntity();
        entity.setId(domain.getId());
        entity.setDoctorId(domain.getDoctorId());
        entity.setDate(domain.getDate());
        entity.setAvailable(domain.isAvailable());
        entity.setReason(domain.getReason());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setCreatedBy(domain.getCreatedBy());
        
        return entity;
    }
    
    /**
     * Converts a DoctorAvailabilityEntity to a DoctorAvailability domain model.
     * 
     * @param entity the JPA entity
     * @return the domain model
     */
    public DoctorAvailability toDomain(DoctorAvailabilityEntity entity) {
        if (entity == null) {
            return null;
        }
        
        DoctorAvailability domain = new DoctorAvailability();
        domain.setId(entity.getId());
        domain.setDoctorId(entity.getDoctorId());
        domain.setDate(entity.getDate());
        domain.setAvailable(entity.isAvailable());
        domain.setReason(entity.getReason());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setCreatedBy(entity.getCreatedBy());
        
        return domain;
    }
}
