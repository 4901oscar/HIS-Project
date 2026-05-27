package com.medframe.clinical.domain.model;

public class ManchesterDiscriminator {

    private String id;
    private String code;
    private String description;
    private PriorityLevel priorityLevel;
    private boolean active;

    public ManchesterDiscriminator() {}

    public ManchesterDiscriminator(String id, String code, String description, PriorityLevel priorityLevel) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.priorityLevel = priorityLevel;
        this.active = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PriorityLevel getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(PriorityLevel priorityLevel) { this.priorityLevel = priorityLevel; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
