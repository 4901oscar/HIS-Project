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
    first_name VARCHAR(50) NOT NULL,
    second_name VARCHAR(50),
    first_last_name VARCHAR(50) NOT NULL,
    second_last_name VARCHAR(50),
    phone VARCHAR(15),
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

-- Add standard audit columns (idempotent - ADD COLUMN IF NOT EXISTS supported since PostgreSQL 9.6)
ALTER TABLE auth_schema.users ADD COLUMN IF NOT EXISTS created_by VARCHAR(36) NOT NULL DEFAULT 'internal';
ALTER TABLE auth_schema.users ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36);

-- ============================================================
-- USUARIOS SEED (solo para desarrollo/demo)
-- Cambiar contraseñas antes de producción
-- ============================================================

-- Admin: usuario=admin  contraseña=Admin1234
-- Doctor: usuario=doctor  contraseña=Doctor1234
-- Admisión: usuario=admision  contraseña=Admision1234
INSERT INTO auth_schema.users (id, username, email, password, first_name, first_last_name, active)
VALUES
  (gen_random_uuid(), 'admin',    'admin@medflow.com',    '$2b$10$si.B4lmdLQ9.YuMQfd1/PuFRfBSgx9r0YjZyozn7F3WfL79b6WIRG', 'Administrador', 'Sistema', true),
  (gen_random_uuid(), 'doctor',   'doctor@medflow.com',   '$2b$10$pUPU.KlQtbc2UNC.ZU/fjulwLst.v.UImczXnPs4JFl0/iF2qI2fa', 'Juan',          'Pérez',   true),
  (gen_random_uuid(), 'admision', 'admision@medflow.com', '$2b$10$WOodPSQVE/.356GTOi1kG.pK2eW2J6syh0vXsHmSCgB20hhKqWQ4e', 'María',         'López',   true)
ON CONFLICT (username) DO NOTHING;

-- Asignar roles a los usuarios seed
INSERT INTO auth_schema.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth_schema.users u, auth_schema.roles r
WHERE u.username = 'admin'    AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO auth_schema.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth_schema.users u, auth_schema.roles r
WHERE u.username = 'doctor'   AND r.name = 'DOCTOR'
ON CONFLICT DO NOTHING;

INSERT INTO auth_schema.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM auth_schema.users u, auth_schema.roles r
WHERE u.username = 'admision' AND r.name = 'ADMISSION'
ON CONFLICT DO NOTHING;
