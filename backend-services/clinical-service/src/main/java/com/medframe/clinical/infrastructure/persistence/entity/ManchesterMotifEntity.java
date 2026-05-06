package com.medframe.clinical.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "manchester_motifs", schema = "clinical_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
