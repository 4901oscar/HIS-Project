package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.domain.port.out.LabOrderRepository;
import com.medframe.clinical.infrastructure.persistence.repository.JpaLabOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements the LabOrderRepository output port using JPA.
 */
@Component
public class LabOrderRepositoryAdapter implements LabOrderRepository {

    private final JpaLabOrderRepository jpaRepository;
    private final LabOrderMapper mapper;

    public LabOrderRepositoryAdapter(JpaLabOrderRepository jpaRepository, LabOrderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public LabOrder save(LabOrder labOrder) {
        var entity = mapper.toEntity(labOrder);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<LabOrder> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByOrderCode(code);
    }

    @Override
    public List<LabOrder> findByPatientIdOrderByOrderedAtDesc(String patientId) {
        return jpaRepository.findByPatientIdOrderByOrderedAtDesc(patientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
