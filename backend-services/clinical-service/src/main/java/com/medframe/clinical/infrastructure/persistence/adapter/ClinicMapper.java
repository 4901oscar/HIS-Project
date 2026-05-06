package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Clinic;
import com.medframe.clinical.domain.model.ClinicStatus;
import com.medframe.clinical.infrastructure.persistence.entity.ClinicEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Clinic domain model and ClinicEntity.
 * 
 * <p>This mapper follows hexagonal architecture by translating between the domain layer
 * (Clinic) and the infrastructure layer (ClinicEntity).
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Component
public class ClinicMapper {
    
    /**
     * Converts a Clinic domain model to a ClinicEntity.
     * 
     * @param domain the domain model
     * @return the JPA entity
     */
    public ClinicEntity toEntity(Clinic domain) {
        if (domain == null) {
            return null;
        }
        
        ClinicEntity entity = new ClinicEntity();
        entity.setId(domain.getId());
        entity.setCodigo(domain.getCodigo());
        entity.setNombre(domain.getNombre());
        entity.setDescripcion(domain.getDescripcion());
        entity.setEstado(mapStatus(domain.getEstado()));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setUpdatedBy(domain.getUpdatedBy());
        
        return entity;
    }
    
    /**
     * Converts a ClinicEntity to a Clinic domain model.
     * 
     * @param entity the JPA entity
     * @return the domain model
     */
    public Clinic toDomain(ClinicEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Clinic domain = new Clinic();
        domain.setId(entity.getId());
        domain.setCodigo(entity.getCodigo());
        domain.setNombre(entity.getNombre());
        domain.setDescripcion(entity.getDescripcion());
        domain.setEstado(mapStatus(entity.getEstado()));
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setCreatedBy(entity.getCreatedBy());
        domain.setUpdatedAt(entity.getUpdatedAt());
        domain.setUpdatedBy(entity.getUpdatedBy());
        
        return domain;
    }
    
    /**
     * Maps domain status to entity status.
     */
    private ClinicEntity.ClinicStatus mapStatus(ClinicStatus domainStatus) {
        if (domainStatus == null) {
            return null;
        }
        
        return switch (domainStatus) {
            case ACTIVE -> ClinicEntity.ClinicStatus.ACTIVE;
            case INACTIVE -> ClinicEntity.ClinicStatus.INACTIVE;
            case DELETED -> ClinicEntity.ClinicStatus.DELETED;
        };
    }
    
    /**
     * Maps entity status to domain status.
     */
    private ClinicStatus mapStatus(ClinicEntity.ClinicStatus entityStatus) {
        if (entityStatus == null) {
            return null;
        }
        
        return switch (entityStatus) {
            case ACTIVE -> ClinicStatus.ACTIVE;
            case INACTIVE -> ClinicStatus.INACTIVE;
            case DELETED -> ClinicStatus.DELETED;
        };
    }
}
