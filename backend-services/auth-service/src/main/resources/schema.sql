-- Auth Service Database Schema
-- MedFlow HIS - Authentication and Authorization
-- Version: 1.0.0

-- Create schema
CREATE SCHEMA IF NOT EXISTS auth_schema;

-- Users table
CREATE TABLE IF NOT EXISTS auth_schema.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(200),
    active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Roles table
CREATE TABLE IF NOT EXISTS auth_schema.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- User-Roles junction table
CREATE TABLE IF NOT EXISTS auth_schema.user_roles (
    user_id UUID NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES auth_schema.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES auth_schema.roles(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON auth_schema.users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON auth_schema.users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON auth_schema.users(active);

-- Insert default roles (8 roles as per requirements)
INSERT INTO auth_schema.roles (name, description) VALUES
('ADMIN', 'Súper Usuario'),
('ADMISSION', 'Personal de Admisión'),
('VITAL_SIGNS', 'Personal de Signos Vitales'),
('DOCTOR', 'Médico'),
('LABORATORY', 'Personal de Laboratorio'),
('PHARMACY', 'Farmacéutico'),
('CASHIER', 'Cajero'),
('PATIENT', 'Paciente')
ON CONFLICT (name) DO NOTHING;
