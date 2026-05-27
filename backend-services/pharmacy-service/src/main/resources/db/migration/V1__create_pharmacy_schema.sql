-- Pharmacy Service Database Schema
-- Schema: pharmacy_schema
-- Description: Gestión de inventario de medicamentos y despacho de recetas

-- Create schema
CREATE SCHEMA IF NOT EXISTS pharmacy_schema;

-- Table: prescriptions
-- Almacena las prescripciones médicas recibidas desde Clinical Service
CREATE TABLE IF NOT EXISTS pharmacy_schema.prescriptions (
    id VARCHAR(36) PRIMARY KEY,
    prescription_code VARCHAR(8) UNIQUE NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    medications_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issued_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT chk_prescription_status CHECK (status IN ('PENDING', 'DISPENSED', 'CANCELLED'))
);

-- Table: medications
-- Catálogo de medicamentos con control de inventario
CREATE TABLE IF NOT EXISTS pharmacy_schema.medications (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    unit VARCHAR(50) NOT NULL,
    current_stock INTEGER NOT NULL DEFAULT 0,
    min_stock INTEGER NOT NULL DEFAULT 10,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_current_stock_positive CHECK (current_stock >= 0),
    CONSTRAINT chk_min_stock_positive CHECK (min_stock >= 0)
);

-- Table: dispensations
-- Registro de despachos de medicamentos
CREATE TABLE IF NOT EXISTS pharmacy_schema.dispensations (
    id VARCHAR(36) PRIMARY KEY,
    prescription_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    dispensed_at TIMESTAMP NOT NULL,
    dispensed_by VARCHAR(36) NOT NULL,
    dispensed_medications_json TEXT NOT NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON pharmacy_schema.prescriptions(patient_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_status ON pharmacy_schema.prescriptions(status);
CREATE INDEX IF NOT EXISTS idx_prescriptions_code ON pharmacy_schema.prescriptions(prescription_code);
CREATE INDEX IF NOT EXISTS idx_medications_stock ON pharmacy_schema.medications(current_stock);
CREATE INDEX IF NOT EXISTS idx_dispensations_patient ON pharmacy_schema.dispensations(patient_id);
CREATE INDEX IF NOT EXISTS idx_dispensations_prescription ON pharmacy_schema.dispensations(prescription_id);

-- Seed data: Common medications
INSERT INTO pharmacy_schema.medications (id, name, description, unit, current_stock, min_stock, active) VALUES
('med-001', 'Paracetamol 500mg', 'Analgésico y antipirético', 'tablets', 500, 100, true),
('med-002', 'Ibuprofeno 400mg', 'Antiinflamatorio no esteroideo', 'tablets', 300, 50, true),
('med-003', 'Amoxicilina 500mg', 'Antibiótico de amplio espectro', 'capsules', 200, 50, true),
('med-004', 'Omeprazol 20mg', 'Inhibidor de la bomba de protones', 'capsules', 250, 50, true),
('med-005', 'Losartán 50mg', 'Antihipertensivo', 'tablets', 400, 100, true),
('med-006', 'Metformina 850mg', 'Antidiabético oral', 'tablets', 350, 80, true),
('med-007', 'Atorvastatina 20mg', 'Hipolipemiante', 'tablets', 180, 50, true),
('med-008', 'Salbutamol 100mcg', 'Broncodilatador', 'inhaler', 80, 20, true),
('med-009', 'Diclofenaco gel 1%', 'Antiinflamatorio tópico', 'tube', 120, 30, true),
('med-010', 'Loratadina 10mg', 'Antihistamínico', 'tablets', 220, 50, true)
ON CONFLICT (id) DO NOTHING;

-- Comments
COMMENT ON TABLE pharmacy_schema.prescriptions IS 'Prescripciones médicas recibidas desde Clinical Service';
COMMENT ON TABLE pharmacy_schema.medications IS 'Catálogo de medicamentos con control de inventario';
COMMENT ON TABLE pharmacy_schema.dispensations IS 'Registro de despachos de medicamentos a pacientes';

COMMENT ON COLUMN pharmacy_schema.prescriptions.prescription_code IS 'Código único de 8 caracteres generado por Clinical Service';
COMMENT ON COLUMN pharmacy_schema.prescriptions.medications_json IS 'Array JSON con los medicamentos prescritos';
COMMENT ON COLUMN pharmacy_schema.medications.current_stock IS 'Stock actual disponible (nunca puede ser negativo)';
COMMENT ON COLUMN pharmacy_schema.medications.min_stock IS 'Stock mínimo para alertas';
