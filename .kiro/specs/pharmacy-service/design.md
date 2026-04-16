# Design: Pharmacy Service

## 1. Architecture Overview

**Arquitectura**: MVC | **Puerto**: 8085 | **Schema**: pharmacy_schema

```
┌──────────────────────────────────────┐
│       PHARMACY SERVICE (8085)        │
│  ┌──────────────────────────────┐   │
│  │      Controller Layer        │   │
│  │  - PrescriptionController    │   │
│  │  - MedicationController      │   │
│  └───────────────┬──────────────┘   │
│  ┌───────────────▼──────────────┐   │
│  │       Service Layer          │   │
│  │  - PrescriptionService       │   │
│  │  - InventoryService          │   │
│  └───────────────┬──────────────┘   │
│  ┌───────────────▼──────────────┐   │
│  │     Repository Layer         │   │
│  │  - PrescriptionRepository    │   │
│  │  - MedicationRepository      │   │
│  │  - DispensationRepository    │   │
│  └───────────────┬──────────────┘   │
│  ┌───────────────▼──────────────┐   │
│  │ PostgreSQL (pharmacy_schema) │   │
│  └──────────────────────────────┘   │
└──────────────────────────────────────┘
```

## 2. Directory Structure

```
pharmacy-service/
├── controller/
│   ├── PrescriptionController.java
│   └── MedicationController.java
├── service/
│   ├── PrescriptionService.java
│   └── InventoryService.java
├── repository/
│   ├── PrescriptionRepository.java
│   ├── MedicationRepository.java
│   └── DispensationRepository.java
├── model/
│   ├── Prescription.java
│   ├── Medication.java
│   ├── Dispensation.java
│   └── PrescriptionStatus.java
├── dto/
│   ├── request/
│   │   ├── PrescriptionNotificationRequest.java
│   │   ├── MedicationRequest.java
│   │   └── StockUpdateRequest.java
│   └── response/
│       ├── PrescriptionResponse.java
│       └── MedicationResponse.java
├── exception/
│   ├── PrescriptionNotFoundException.java
│   ├── InsufficientStockException.java
│   ├── InvalidPrescriptionStatusException.java
│   └── GlobalExceptionHandler.java
└── config/
    └── SecurityConfig.java
```

## 3. Data Model

### Entities

```java
@Entity @Table(name = "prescriptions", schema = "pharmacy_schema")
public class Prescription {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String prescriptionCode;  // From Clinical Service (8 chars)
    private String patientId;
    private String doctorId;
    @Column(columnDefinition = "TEXT")
    private String medicationsJson;   // JSON array of medications
    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status; // PENDING, DISPENSED, CANCELLED
    private LocalDateTime issuedAt;
    private LocalDateTime updatedAt;
}

@Entity @Table(name = "medications", schema = "pharmacy_schema")
public class Medication {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String description;
    private String unit;              // tablets, ml, capsules, etc.
    private Integer currentStock;
    private Integer minStock;         // Alert threshold
    private boolean active;
}

@Entity @Table(name = "dispensations", schema = "pharmacy_schema")
public class Dispensation {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String prescriptionId;
    private String patientId;
    private LocalDateTime dispensedAt;
    private String dispensedBy;
    @Column(columnDefinition = "TEXT")
    private String dispensedMedicationsJson;
}
```

### Database Schema

```sql
CREATE SCHEMA IF NOT EXISTS pharmacy_schema;

CREATE TABLE pharmacy_schema.prescriptions (
    id VARCHAR(36) PRIMARY KEY,
    prescription_code VARCHAR(8) UNIQUE NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    medications_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issued_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE pharmacy_schema.medications (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    unit VARCHAR(50) NOT NULL,
    current_stock INTEGER NOT NULL DEFAULT 0,
    min_stock INTEGER NOT NULL DEFAULT 10,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE pharmacy_schema.dispensations (
    id VARCHAR(36) PRIMARY KEY,
    prescription_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    dispensed_at TIMESTAMP NOT NULL,
    dispensed_by VARCHAR(36) NOT NULL,
    dispensed_medications_json TEXT NOT NULL
);

CREATE INDEX idx_prescriptions_patient ON pharmacy_schema.prescriptions(patient_id);
CREATE INDEX idx_prescriptions_status ON pharmacy_schema.prescriptions(status);
CREATE INDEX idx_prescriptions_code ON pharmacy_schema.prescriptions(prescription_code);
CREATE INDEX idx_medications_stock ON pharmacy_schema.medications(current_stock);
```

## 4. Key Service Logic

### PrescriptionService.dispense()
```java
@Transactional
public PrescriptionResponse dispense(String prescriptionId, String dispensedBy) {
    Prescription prescription = findById(prescriptionId);
    
    // 1. Validate status
    if (prescription.getStatus() != PrescriptionStatus.PENDING) {
        throw new InvalidPrescriptionStatusException(
            "Solo se pueden despachar recetas con estado PENDIENTE");
    }
    
    // 2. Parse medications from JSON
    List<MedicationItem> items = parseMedications(prescription.getMedicationsJson());
    
    // 3. Validate stock for each medication (best-effort match by name)
    for (MedicationItem item : items) {
        medicationRepository.findByNameIgnoreCase(item.getName())
            .ifPresent(med -> {
                if (med.getCurrentStock() < 1) {
                    throw new InsufficientStockException(
                        "Stock insuficiente para: " + item.getName());
                }
            });
    }
    
    // 4. Deduct stock and create dispensation
    items.forEach(item -> 
        medicationRepository.findByNameIgnoreCase(item.getName())
            .ifPresent(med -> {
                med.setCurrentStock(med.getCurrentStock() - 1);
                medicationRepository.save(med);
            })
    );
    
    // 5. Update prescription status
    prescription.setStatus(PrescriptionStatus.DISPENSED);
    prescription.setUpdatedAt(LocalDateTime.now());
    prescriptionRepository.save(prescription);
    
    // 6. Create dispensation record
    Dispensation dispensation = new Dispensation();
    dispensation.setPrescriptionId(prescriptionId);
    dispensation.setPatientId(prescription.getPatientId());
    dispensation.setDispensedAt(LocalDateTime.now());
    dispensation.setDispensedBy(dispensedBy);
    dispensationRepository.save(dispensation);
    
    return mapToResponse(prescription);
}
```

## 5. Correctness Properties

### Property 1: Stock Never Goes Negative
Para cualquier operación de despacho, currentStock nunca puede ser < 0.

### Property 2: Prescription Status Transitions are Valid
Solo PENDING → DISPENSED | CANCELLED. Transiciones inválidas lanzan excepción.

### Property 3: Patient Can Only Access Own Prescriptions
Usuario con rol PATIENT solo puede ver prescripciones donde patientId == userId.

## 6. Configuration

```yaml
server:
  port: 8085
spring:
  application:
    name: pharmacy-service
  jpa:
    properties:
      hibernate:
        default_schema: pharmacy_schema
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
```

---
**Created**: April 16, 2026 | **Status**: ✅ Ready | **Version**: 1.0.0
