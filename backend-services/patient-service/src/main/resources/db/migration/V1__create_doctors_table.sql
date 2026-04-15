-- V1__create_doctors_table.sql
-- Create doctors table for patient appointment scheduling
-- Requirements: 2.2

CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    doctor_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL DEFAULT 'GENERAL',
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for active status and specialty
CREATE INDEX idx_doctors_active ON doctors(is_active);
CREATE INDEX idx_doctors_specialty ON doctors(specialty);

-- Insert 3 sample doctors for testing
INSERT INTO doctors (doctor_code, first_name, last_name, specialty, email, phone, is_active) VALUES
('DOC-001', 'María', 'González', 'GENERAL', 'maria.gonzalez@medflow.com', '12345678', TRUE),
('DOC-002', 'Carlos', 'Rodríguez', 'GENERAL', 'carlos.rodriguez@medflow.com', '23456789', TRUE),
('DOC-003', 'Ana', 'Martínez', 'GENERAL', 'ana.martinez@medflow.com', '34567890', TRUE);
