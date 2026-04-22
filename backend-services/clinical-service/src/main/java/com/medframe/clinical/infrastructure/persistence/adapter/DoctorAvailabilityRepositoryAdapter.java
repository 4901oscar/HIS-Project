package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.DoctorAvailability;
import com.medframe.clinical.domain.port.out.DoctorAvailabilityRepository;
import com.medframe.clinical.infrastructure.persistence.entity.DoctorAvailabilityEntity;
import com.medframe.clinical.infrastructure.persistence.repository.JpaDoctorAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of DoctorAvailabilityRepository port.
 * 
 * <p>This adapter bridges the domain layer (DoctorAvailabilityRepository interface) with the
 * infrastructure layer (JpaDoctorAvailabilityRepository). It follows hexagonal architecture
 * by implementing the port defined in the domain layer.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class DoctorAvailabilityRepositoryAdapter implements DoctorAvailabilityRepository {
    
    private final JpaDoctorAvailabilityRepository jpaRepository;
    private final DoctorAvailabilityMapper mapper;
    
    @Override
    public DoctorAvailability save(DoctorAvailability availability) {
        DoctorAvailabilityEntity entity = mapper.toEntity(availability);
        DoctorAvailabilityEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<DoctorAvailability> findById(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<DoctorAvailability> findByDoctorIdAndDate(String doctorId, LocalDate date) {
        return jpaRepository.findByDoctorIdAndDate(doctorId, date)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DoctorAvailability> findByDoctorIdAndDateBetween(String doctorId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByDoctorIdAndDateBetween(doctorId, startDate, endDate)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteByDoctorIdAndDate(String doctorId, LocalDate date) {
        jpaRepository.deleteByDoctorIdAndDate(doctorId, date);
    }
    
    @Override
    @Transactional
    public void deleteByDoctorId(String doctorId) {
        jpaRepository.deleteByDoctorId(doctorId);
    }
    
    @Override
    public List<DoctorAvailability> findByDoctorId(String doctorId) {
        return jpaRepository.findByDoctorId(doctorId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
