package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.exception.DuplicateClinicCodeException;
import com.medframe.clinical.domain.model.Clinic;
import com.medframe.clinical.domain.model.ClinicStatus;
import com.medframe.clinical.domain.port.out.ClinicRepository;
import com.medframe.clinical.infrastructure.persistence.entity.ClinicEntity;
import com.medframe.clinical.infrastructure.persistence.repository.JpaClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation of ClinicRepository port.
 * 
 * <p>This adapter bridges the domain layer (ClinicRepository interface) with the
 * infrastructure layer (JpaClinicRepository). It follows hexagonal architecture
 * by implementing the port defined in the domain layer.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ClinicRepositoryAdapter implements ClinicRepository {
    
    private final JpaClinicRepository jpaRepository;
    private final ClinicMapper mapper;
    
    @Override
    public Clinic save(Clinic clinic) {
        try {
            ClinicEntity entity = mapper.toEntity(clinic);
            ClinicEntity saved = jpaRepository.save(entity);
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException e) {
            // Handle unique constraint violation on codigo
            if (e.getMessage() != null && e.getMessage().contains("uk_clinics_codigo")) {
                throw new DuplicateClinicCodeException(
                    "Ya existe una clínica con el código: " + clinic.getCodigo(), e
                );
            }
            throw e;
        }
    }
    
    @Override
    public Optional<Clinic> findById(UUID id) {
        return jpaRepository.findById(id.toString())
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Clinic> findAll() {
        return jpaRepository.findAllOrderByCreatedAtDesc()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Clinic> findByEstado(ClinicStatus estado) {
        ClinicEntity.ClinicStatus entityStatus = mapStatus(estado);
        return jpaRepository.findByEstadoOrderByCreatedAtDesc(entityStatus)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByCodigo(String codigo) {
        return jpaRepository.existsByCodigo(codigo);
    }
    
    @Override
    public Optional<Clinic> findByCodigo(String codigo) {
        return jpaRepository.findByCodigo(codigo)
            .map(mapper::toDomain);
    }
    
    /**
     * Maps domain status to entity status for queries.
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
}
