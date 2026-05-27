-- =====================================================
-- Clinical Service Database Migration
-- Version: 8
-- Description: Expands appointment state machine from 5 to 13 states
--              and adds invoice tracking fields for lab and pharmacy
-- =====================================================

-- Add new invoice tracking columns
ALTER TABLE clinical_schema.appointments 
ADD COLUMN lab_invoice_id VARCHAR(36),
ADD COLUMN pharmacy_invoice_id VARCHAR(36);

-- Note: PostgreSQL doesn't support ALTER TYPE for enums directly
-- The status column is VARCHAR(20), so we don't need to modify the column type
-- The application layer (JPA) will handle the enum validation

-- Add comments for documentation
COMMENT ON COLUMN clinical_schema.appointments.invoice_id IS 'Consultation invoice ID from billing service';
COMMENT ON COLUMN clinical_schema.appointments.lab_invoice_id IS 'Laboratory tests invoice ID from billing service';
COMMENT ON COLUMN clinical_schema.appointments.pharmacy_invoice_id IS 'Medications invoice ID from billing service';

-- The status column now supports these values:
-- PENDING_PAYMENT: Appointment created but consultation fee not paid
-- SCHEDULED: Consultation fee paid, waiting for patient arrival
-- ACTIVE: Patient arrived, waiting for triage
-- VITAL_SIGNS: Triage in progress (vital signs being captured)
-- CONSULTATION: Waiting for doctor consultation
-- PENDING_LAB_PAYMENT: Lab tests ordered, waiting for lab fee payment
-- LABORATORY: Lab fee paid, tests in progress
-- RE_EVALUATION: Lab results ready, waiting for doctor review
-- PENDING_PHARMACY_PAYMENT: Prescription issued, waiting for medication fee payment
-- PHARMACY: Medication fee paid, waiting for dispensing
-- COMPLETED: All services completed, patient discharged
-- CANCELLED: Appointment cancelled at any stage
-- MISSED: Patient did not arrive within scheduled time window

-- Existing appointments remain in their current states (backward compatible)
-- No data migration needed
