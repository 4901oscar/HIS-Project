package com.medframe.clinical.infrastructure.persistence.entity;

import com.medframe.clinical.domain.model.PriorityLevel;
import jakarta.persistence.*;

@Entity
@Table(name = "manchester_discriminators", schema = "clinical_schema")
public class ManchesterDiscriminatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", nullable = false, length = 20)
    private PriorityLevel priorityLevel;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motif_id")
    private ManchesterMotifEntity motif;

    // Constructors
    public ManchesterDiscriminatorEntity() {}

    // Getters and setters
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

    public ManchesterMotifEntity getMotif() { return motif; }
    public void setMotif(ManchesterMotifEntity motif) { this.motif = motif; }
}
