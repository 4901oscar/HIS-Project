-- =====================================================
-- Clinical Service Database Schema
-- Version: 1.0
-- Description: Creates clinical_schema with all tables
--              for triages, vital signs, appointments,
--              consultations, prescriptions, lab orders,
--              and Manchester triage catalog
-- =====================================================

-- Create schema
CREATE SCHEMA IF NOT EXISTS clinical_schema;

-- =====================================================
-- Table: vital_signs
-- Description: Stores patient vital signs measurements
-- =====================================================
CREATE TABLE clinical_schema.vital_signs (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL,
    systolic_pressure INTEGER NOT NULL,
    diastolic_pressure INTEGER NOT NULL,
    heart_rate INTEGER NOT NULL,
    respiratory_rate INTEGER,
    temperature DOUBLE PRECISION NOT NULL,
    oxygen_saturation INTEGER NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    bmi DOUBLE PRECISION,
    recorded_at TIMESTAMP NOT NULL,
    recorded_by VARCHAR(36) NOT NULL
);

-- Index for patient lookup
CREATE INDEX idx_vital_signs_patient ON clinical_schema.vital_signs(patient_id);

-- =====================================================
-- Table: triages
-- Description: Stores Manchester triage assessments
-- =====================================================
CREATE TABLE clinical_schema.triages (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    motif_id VARCHAR(36) NOT NULL,
    discriminator_ids TEXT NOT NULL,
    priority_level VARCHAR(20) NOT NULL,
    max_wait_time_minutes INTEGER NOT NULL,
    performed_at TIMESTAMP NOT NULL,
    performed_by VARCHAR(36) NOT NULL
);

-- Index for patient lookup
CREATE INDEX idx_triages_patient ON clinical_schema.triages(patient_id);

-- =====================================================
-- Table: appointments
-- Description: Stores medical appointments
-- =====================================================
CREATE TABLE clinical_schema.appointments (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36) NOT NULL
);

-- Composite index for doctor and date queries
CREATE INDEX idx_appointments_doctor_date ON clinical_schema.appointments(doctor_id, appointment_date);

-- Index for patient lookup
CREATE INDEX idx_appointments_patient ON clinical_schema.appointments(patient_id);

-- =====================================================
-- Table: consultations
-- Description: Stores medical consultations
-- =====================================================
CREATE TABLE clinical_schema.consultations (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    appointment_id VARCHAR(36),
    chief_complaint VARCHAR(500) NOT NULL,
    symptoms VARCHAR(1000),
    primary_diagnosis VARCHAR(10) NOT NULL,
    secondary_diagnoses TEXT,
    medical_notes VARCHAR(2000),
    treatment_plan VARCHAR(1000),
    consultation_date TIMESTAMP NOT NULL,
    performed_by VARCHAR(36) NOT NULL
);

-- Index for patient lookup
CREATE INDEX idx_consultations_patient ON clinical_schema.consultations(patient_id);

-- =====================================================
-- Table: prescriptions
-- Description: Stores medical prescriptions
-- =====================================================
CREATE TABLE clinical_schema.prescriptions (
    id VARCHAR(36) PRIMARY KEY,
    consultation_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    prescription_code VARCHAR(8) NOT NULL UNIQUE,
    medications TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    issued_by VARCHAR(36) NOT NULL
);

-- Unique index for prescription code
CREATE UNIQUE INDEX idx_prescriptions_code ON clinical_schema.prescriptions(prescription_code);

-- Index for patient lookup
CREATE INDEX idx_prescriptions_patient ON clinical_schema.prescriptions(patient_id);

-- =====================================================
-- Table: lab_orders
-- Description: Stores laboratory test orders
-- =====================================================
CREATE TABLE clinical_schema.lab_orders (
    id VARCHAR(36) PRIMARY KEY,
    consultation_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    order_code VARCHAR(8) NOT NULL UNIQUE,
    test_names TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    ordered_at TIMESTAMP NOT NULL,
    ordered_by VARCHAR(36) NOT NULL
);

-- Unique index for order code
CREATE UNIQUE INDEX idx_lab_orders_code ON clinical_schema.lab_orders(order_code);

-- Index for patient lookup
CREATE INDEX idx_lab_orders_patient ON clinical_schema.lab_orders(patient_id);

-- =====================================================
-- Table: manchester_motifs
-- Description: Stores Manchester triage motifs/reasons
-- =====================================================
CREATE TABLE clinical_schema.manchester_motifs (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true
);

-- =====================================================
-- Table: manchester_discriminators
-- Description: Stores Manchester triage discriminators
-- =====================================================
CREATE TABLE clinical_schema.manchester_discriminators (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL,
    priority_level VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    motif_id VARCHAR(36),
    CONSTRAINT fk_discriminator_motif FOREIGN KEY (motif_id) 
        REFERENCES clinical_schema.manchester_motifs(id) ON DELETE SET NULL
);

-- Index for motif lookup
CREATE INDEX idx_discriminators_motif ON clinical_schema.manchester_discriminators(motif_id);
