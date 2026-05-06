-- =====================================================
-- Clinical Service Database Schema Migration
-- Version: 6
-- Description: Adds appointment_id column to triages table
--              to link triage records with appointments.
--              Creates unique and regular indexes for
--              constraint enforcement and query optimization.
-- =====================================================

-- Add appointment_id column to triages table
-- Initially nullable to support existing triage records
ALTER TABLE clinical_schema.triages 
ADD COLUMN appointment_id VARCHAR(255);

-- Create unique index to enforce one-triage-per-appointment constraint
-- This prevents duplicate triage records for the same appointment
CREATE UNIQUE INDEX idx_triages_appointment_unique 
ON clinical_schema.triages(appointment_id);

-- Create regular BTREE index for query optimization
-- Optimizes findByAppointmentId queries to O(log n) performance
CREATE INDEX idx_triages_appointment 
ON clinical_schema.triages(appointment_id);

-- Note: appointment_id is initially nullable for backward compatibility
-- with existing triage records. A future migration can enforce NOT NULL
-- constraint after data migration if needed.
