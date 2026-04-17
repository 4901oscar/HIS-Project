package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.domain.port.out.ConsultationRepository;
import com.medframe.clinical.infrastructure.persistence.repository.JpaConsultationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the ConsultationRepository output port using JPA.
 */
@Component
public class ConsultationRepositoryAdapter implements ConsultationRepository {

    private final JpaConsultationRepository jpaRepository;
    private final ConsultationMapper mapper;

    public ConsultationRepositoryAdapter(JpaConsultationRepository jpaRepository, ConsultationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Consultation save(Consultation consultation) {
        var entity = mapper.toEntity(consultation);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Consultation> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Consultation> findByPatientIdOrderByConsultationDateDesc(String patientId) {
        return jpaRepository.findByPatientIdOrderByConsultationDateDesc(patientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
