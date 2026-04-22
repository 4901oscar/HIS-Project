package com.medframe.clinical.infrastructure.persistence.entity;

import com.medframe.clinical.domain.model.PriorityLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "manchester_discriminators", schema = "clinical_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
