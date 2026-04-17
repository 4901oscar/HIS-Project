package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.ManchesterMotif;
import com.medframe.clinical.infrastructure.persistence.entity.ManchesterMotifEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between ManchesterMotif domain entity and ManchesterMotifEntity JPA entity.
 */
@Component
public class ManchesterMotifMapper {

    private final ManchesterDiscriminatorMapper discriminatorMapper;

    public ManchesterMotifMapper(ManchesterDiscriminatorMapper discriminatorMapper) {
        this.discriminatorMapper = discriminatorMapper;
    }

    public ManchesterMotifEntity toEntity(ManchesterMotif domain) {
        if (domain == null) {
            return null;
        }

        ManchesterMotifEntity entity = new ManchesterMotifEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setCategory(domain.getCategory());
        entity.setActive(domain.isActive());

        if (domain.getDiscriminators() != null) {
            entity.setDiscriminators(
                domain.getDiscriminators().stream()
                    .map(discriminatorMapper::toEntity)
                    .collect(Collectors.toList())
            );
        }

        return entity;
    }

    public ManchesterMotif toDomain(ManchesterMotifEntity entity) {
        if (entity == null) {
            return null;
        }

        ManchesterMotif domain = new ManchesterMotif();
        domain.setId(entity.getId());
        domain.setCode(entity.getCode());
        domain.setDescription(entity.getDescription());
        domain.setCategory(entity.getCategory());
        domain.setActive(entity.isActive());

        if (entity.getDiscriminators() != null) {
            domain.setDiscriminators(
                entity.getDiscriminators().stream()
                    .map(discriminatorMapper::toDomain)
                    .collect(Collectors.toList())
            );
        }

        return domain;
    }
}
