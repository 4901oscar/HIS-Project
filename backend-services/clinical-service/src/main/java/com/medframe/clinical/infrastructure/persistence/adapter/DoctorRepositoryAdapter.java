package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.infrastructure.persistence.entity.DoctorEntity;
import com.medframe.clinical.infrastructure.persistence.repository.JpaDoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of DoctorRepository port.
 * 
 * <p>This adapter bridges the domain layer (DoctorRepository interface) with the
 * infrastructure layer (JpaDoctorRepository). It follows hexagonal architecture
 * by implementing the port defined in the domain layer.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class DoctorRepositoryAdapter implements DoctorRepository {
    
    private final JpaDoctorRepository jpaRepository;
    private final DoctorMapper mapper;
    
    @Override
    public Doctor save(Doctor doctor) {
        DoctorEntity entity = mapper.toEntity(doctor);
        DoctorEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Doctor> findById(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Doctor> findByShiftAndStatus(LocalTime shiftStart, LocalTime shiftEnd, Doctor.DoctorStatus status) {
        DoctorEntity.DoctorStatus entityStatus = mapStatus(status);
        return jpaRepository.findByShiftAndStatus(shiftStart, shiftEnd, entityStatus)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Doctor> findAllActive() {
        return jpaRepository.findAllActive()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Doctor> findActiveByClinicId(String clinicId) {
        return jpaRepository.findActiveByClinicId(clinicId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
    
    /**
     * Maps domain status to entity status for queries.
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
}
