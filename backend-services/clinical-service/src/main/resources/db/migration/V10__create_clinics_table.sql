-- V10: Create clinics table for clinic management
-- Supports Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9

-- Create enum type for clinic status
CREATE TYPE clinical_schema.clinic_status AS ENUM ('ACTIVE', 'INACTIVE', 'DELETED');

-- Create clinics table
CREATE TABLE clinical_schema.clinics (
    id VARCHAR(36) PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    estado clinical_schema.clinic_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    
    -- Unique constraint on clinic code
    CONSTRAINT uq_clinics_codigo UNIQUE (codigo),
    
    -- Check constraint to ensure codigo is numeric only
    CONSTRAINT chk_clinics_codigo_numeric CHECK (codigo ~ '^[0-9]+$')
);

-- Create index on codigo for fast lookups
CREATE INDEX idx_clinics_codigo ON clinical_schema.clinics(codigo);

-- Create index on estado for filtering
CREATE INDEX idx_clinics_estado ON clinical_schema.clinics(estado);

-- Create index on created_at for sorting
CREATE INDEX idx_clinics_created_at ON clinical_schema.clinics(created_at DESC);

-- Add comment to table
COMMENT ON TABLE clinical_schema.clinics IS 'Stores clinic information for organizational management. Clinics will be associated with doctors.';

-- Add comments to columns
COMMENT ON COLUMN clinical_schema.clinics.id IS 'Unique identifier (UUID) for the clinic';
COMMENT ON COLUMN clinical_schema.clinics.codigo IS 'Unique numeric clinic code (e.g., 101, 201) representing level and office number';
COMMENT ON COLUMN clinical_schema.clinics.nombre IS 'Clinic name (alphanumeric)';
COMMENT ON COLUMN clinical_schema.clinics.descripcion IS 'Clinic description (alphanumeric)';
COMMENT ON COLUMN clinical_schema.clinics.estado IS 'Clinic operational status: ACTIVE, INACTIVE, or DELETED (soft delete)';
COMMENT ON COLUMN clinical_schema.clinics.created_at IS 'Timestamp when the clinic was created';
COMMENT ON COLUMN clinical_schema.clinics.created_by IS 'User ID who created the clinic';
COMMENT ON COLUMN clinical_schema.clinics.updated_at IS 'Timestamp when the clinic was last updated';
COMMENT ON COLUMN clinical_schema.clinics.updated_by IS 'User ID who last updated the clinic';
