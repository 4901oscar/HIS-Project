-- Migration: Add appointment_id to vital_signs table
-- Purpose: Link vital signs to specific appointments instead of just patients
-- This fixes the bug where vital signs from old appointments appear in new appointments for the same patient

-- Add appointment_id column (nullable initially to allow existing data)
ALTER TABLE clinical_schema.vital_signs
ADD COLUMN appointment_id VARCHAR(36);

-- Add index for appointment_id lookups
CREATE INDEX idx_vital_signs_appointment ON clinical_schema.vital_signs(appointment_id);

-- Note: appointment_id is nullable to support existing data
-- New records will require appointment_id (enforced at application level)
-- Existing records without appointment_id will be considered legacy data
