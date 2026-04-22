-- Migration: Add composite index on appointments table for workload queries
-- Version: V5
-- Description: Creates a composite index on appointments(doctor_id, appointment_date) to optimize workload counting queries
-- Author: MedFlow Team
-- Date: 2024-05-20

-- Create composite index for workload counting queries
-- This index is used by DoctorAssignmentService.calculateWorkload() to efficiently count
-- appointments per doctor per date when selecting the least-loaded doctor
CREATE INDEX IF NOT EXISTS idx_appointments_doctor_date ON appointments(doctor_id, appointment_date);

-- Comments for documentation
COMMENT ON INDEX idx_appointments_doctor_date IS 'Optimizes workload counting queries for doctor assignment algorithm (findByDoctorIdAndDate)';
