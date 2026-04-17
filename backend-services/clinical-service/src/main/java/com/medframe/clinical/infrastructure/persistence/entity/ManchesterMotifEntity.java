package com.medframe.clinical.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "manchester_motifs", schema = "clinical_schema")
public class ManchesterMotifEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "motif", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ManchesterDiscriminatorEntity> discriminators = new ArrayList<>();

    // Constructors
    public ManchesterMotifEntity() {}

    // Getters and setters
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

    public List<ManchesterDiscriminatorEntity> getDiscriminators() { return discriminators; }
    public void setDiscriminators(List<ManchesterDiscriminatorEntity> discriminators) { this.discriminators = discriminators; }
}
