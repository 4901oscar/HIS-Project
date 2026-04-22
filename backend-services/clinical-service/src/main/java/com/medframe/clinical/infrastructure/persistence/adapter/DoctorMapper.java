package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.infrastructure.persistence.entity.DoctorEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Doctor domain model and DoctorEntity.
 * 
 * <p>This mapper follows hexagonal architecture by translating between the domain layer
 * (Doctor) and the infrastructure layer (DoctorEntity).
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Component
public class DoctorMapper {
    
    /**
     * Converts a Doctor domain model to a DoctorEntity.
     * 
     * @param domain the domain model
     * @return the JPA entity
     */
    public DoctorEntity toEntity(Doctor domain) {
        if (domain == null) {
            return null;
        }
        
        DoctorEntity entity = new DoctorEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setSpecialty(domain.getSpecialty());
        entity.setShiftStart(domain.getShiftStart());
        entity.setShiftEnd(domain.getShiftEnd());
        entity.setStatus(mapStatus(domain.getStatus()));
        entity.setCreatedAt(domain.getCreatedAt());
        
        return entity;
    }
    
    /**
     * Converts a DoctorEntity to a Doctor domain model.
     * 
     * @param entity the JPA entity
     * @return the domain model
     */
    public Doctor toDomain(DoctorEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Doctor domain = new Doctor();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        domain.setSpecialty(entity.getSpecialty());
        domain.setShiftStart(entity.getShiftStart());
        domain.setShiftEnd(entity.getShiftEnd());
        domain.setStatus(mapStatus(entity.getStatus()));
        domain.setCreatedAt(entity.getCreatedAt());
        
        return domain;
    }
    
    /**
     * Maps domain status to entity status.
     */
    private DoctorEntity.DoctorStatus mapStatus(Doctor.DoctorStatus domainStatus) {
        if (domainStatus == null) {
            return null;
        }
        
        return switch (domainStatus) {
            case ACTIVE -> DoctorEntity.DoctorStatus.ACTIVE;
            case INACTIVE -> DoctorEntity.DoctorStatus.INACTIVE;
        };
    }
    
    /**
     * Maps entity status to domain status.
     */
    private Doctor.DoctorStatus mapStatus(DoctorEntity.DoctorStatus entityStatus) {
        if (entityStatus == null) {
            return null;
        }
        
        return switch (entityStatus) {
            case ACTIVE -> Doctor.DoctorStatus.ACTIVE;
            case INACTIVE -> Doctor.DoctorStatus.INACTIVE;
        };
    }
}
