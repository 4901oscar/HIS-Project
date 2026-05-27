package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.port.out.PrescriptionRepository;
import com.medframe.clinical.infrastructure.persistence.repository.JpaPrescriptionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the PrescriptionRepository output port using JPA.
 */
@Component
public class PrescriptionRepositoryAdapter implements PrescriptionRepository {

    private final JpaPrescriptionRepository jpaRepository;
    private final PrescriptionMapper mapper;

    public PrescriptionRepositoryAdapter(JpaPrescriptionRepository jpaRepository, PrescriptionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Prescription save(Prescription prescription) {
        var entity = mapper.toEntity(prescription);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Prescription> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByPrescriptionCode(code);
    }

    @Override
    public List<Prescription> findByPatientIdOrderByIssuedAtDesc(String patientId) {
        return jpaRepository.findByPatientIdOrderByIssuedAtDesc(patientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Prescription> findByConsultationId(String consultationId) {
        return jpaRepository.findByConsultationId(consultationId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
