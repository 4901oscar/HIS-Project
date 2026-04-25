package com.medframe.clinical.infrastructure.rest.dto.response;

import com.medframe.clinical.infrastructure.persistence.entity.ManchesterMotifEntity;
import lombok.Data;

@Data
public class MotifCatalogResponse {
    private String id;
    private String code;
    private String description;
    private String category;
    private boolean active;

    public static MotifCatalogResponse from(ManchesterMotifEntity e) {
        MotifCatalogResponse r = new MotifCatalogResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setDescription(e.getDescription());
        r.setCategory(e.getCategory());
        r.setActive(e.isActive());
        return r;
    }
}
