package com.medframe.clinical.infrastructure.persistence.adapter;

import com.medframe.clinical.domain.model.ManchesterDiscriminator;
import com.medframe.clinical.infrastructure.persistence.entity.ManchesterDiscriminatorEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between ManchesterDiscriminator domain entity and ManchesterDiscriminatorEntity JPA entity.
 */
@Component
public class ManchesterDiscriminatorMapper {

    public ManchesterDiscriminatorEntity toEntity(ManchesterDiscriminator domain) {
        if (domain == null) {
            return null;
        }

        ManchesterDiscriminatorEntity entity = new ManchesterDiscriminatorEntity();
        entity.setId(domain.getId());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setPriorityLevel(domain.getPriorityLevel());
        entity.setActive(domain.isActive());

        return entity;
    }

    public ManchesterDiscriminator toDomain(ManchesterDiscriminatorEntity entity) {
        if (entity == null) {
            return null;
        }

        ManchesterDiscriminator domain = new ManchesterDiscriminator();
        domain.setId(entity.getId());
        domain.setCode(entity.getCode());
        domain.setDescription(entity.getDescription());
        domain.setPriorityLevel(entity.getPriorityLevel());
        domain.setActive(entity.isActive());

        return domain;
    }
}
