package com.medframe.clinical.infrastructure.rest.dto.response;

import com.medframe.clinical.infrastructure.persistence.entity.ManchesterDiscriminatorEntity;
import lombok.Data;

@Data
public class DiscriminatorCatalogResponse {
    private String id;
    private String code;
    private String description;
    private String priorityLevel;
    private String motifId;
    private boolean active;

    public static DiscriminatorCatalogResponse from(ManchesterDiscriminatorEntity e) {
        DiscriminatorCatalogResponse r = new DiscriminatorCatalogResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setDescription(e.getDescription());
        r.setPriorityLevel(e.getPriorityLevel() != null ? e.getPriorityLevel().name() : null);
        r.setMotifId(e.getMotif() != null ? e.getMotif().getId() : null);
        r.setActive(e.isActive());
        return r;
    }
}
