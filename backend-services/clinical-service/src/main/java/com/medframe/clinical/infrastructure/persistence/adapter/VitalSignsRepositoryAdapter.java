package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.VitalSigns;
import com.medframe.clinical.domain.port.out.VitalSignsRepository;
import com.medframe.clinical.infrastructure.persistence.repository.JpaVitalSignsRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the VitalSignsRepository output port using JPA.
 */
@Component
public class VitalSignsRepositoryAdapter implements VitalSignsRepository {

    private final JpaVitalSignsRepository jpaRepository;
    private final VitalSignsMapper mapper;

    public VitalSignsRepositoryAdapter(JpaVitalSignsRepository jpaRepository, VitalSignsMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public VitalSigns save(VitalSigns vitalSigns) {
        var entity = mapper.toEntity(vitalSigns);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VitalSigns> findByAppointmentId(String appointmentId) {
        return jpaRepository.findFirstByAppointmentIdOrderByRecordedAtDesc(appointmentId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<VitalSigns> findLatestByPatientId(String patientId) {
        return jpaRepository.findFirstByPatientIdOrderByRecordedAtDesc(patientId)
                .map(mapper::toDomain);
    }

    @Override
    public List<VitalSigns> findByPatientIdOrderByRecordedAtDesc(String patientId) {
        return jpaRepository.findByPatientIdOrderByRecordedAtDesc(patientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
