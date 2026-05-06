package com.medflow.lab.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_types", schema = "lab_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "test_type", length = 100)
    private String testType;

    @Column(name = "sample_type", length = 100)
    private String sampleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExamTypeStatus status = ExamTypeStatus.ACTIVE;
}
