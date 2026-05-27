package com.medflow.patient.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "patients",
    schema = "patient_schema",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "dpi"),
        @UniqueConstraint(columnNames = "email")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false, length = 13, unique = true)
    private String dpi;

    @Column(length = 20)
    private String nit;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "second_name", length = 100)
    private String secondName;

    @Column(name = "first_last_name", nullable = false, length = 100)
    private String firstLastName;

    @Column(name = "second_last_name", length = 100)
    private String secondLastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 8)
    private String phone;

    // Dirección
    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String municipality;

    @Column(length = 10)
    private String zone;

    @Column(length = 300)
    private String address;

    @Column(name = "auth_user_id", length = 36)
    private String authUserId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (createdBy == null) createdBy = "internal";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (secondName != null && !secondName.isBlank()) sb.append(" ").append(secondName);
        sb.append(" ").append(firstLastName);
        if (secondLastName != null && !secondLastName.isBlank()) sb.append(" ").append(secondLastName);
        return sb.toString();
    }
}
