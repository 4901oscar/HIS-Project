package com.medframe.clinical.domain.model;

import java.util.List;

public class ManchesterMotif {

    private String id;
    private String code;
    private String description;
    private String category;
    private boolean active;
    private List<ManchesterDiscriminator> discriminators;

    public ManchesterMotif() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<ManchesterDiscriminator> getDiscriminators() { return discriminators; }
    public void setDiscriminators(List<ManchesterDiscriminator> discriminators) { this.discriminators = discriminators; }
}
