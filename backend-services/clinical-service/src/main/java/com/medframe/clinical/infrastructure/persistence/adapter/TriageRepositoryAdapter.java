package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.port.out.TriageRepository;
import com.medframe.clinical.infrastructure.persistence.entity.TriageEntity;
import com.medframe.clinical.infrastructure.persistence.repository.JpaTriageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the TriageRepository output port using JPA.
 */
@Component
public class TriageRepositoryAdapter implements TriageRepository {

    private final JpaTriageRepository jpaRepository;
    private final TriageMapper mapper;

    public TriageRepositoryAdapter(JpaTriageRepository jpaRepository, TriageMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Triage save(Triage triage) {
        TriageEntity entity = mapper.toEntity(triage);
        TriageEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Triage> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Triage> findByPatientId(String patientId) {
        return jpaRepository.findByPatientId(patientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
