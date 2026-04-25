-- Migration V2: Add appointment_id column to invoices table for billing integration
-- Feature: appointment-billing-integration
-- Requirements: 5.2, 5.4, 10.1, 10.2, 10.3, 10.4, 10.5, 10.8
-- Note: This migration is for future Flyway adoption. Currently using schema.sql

-- Add appointment_id column to invoices table (nullable to support manual invoice creation)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'billing_schema' 
        AND table_name = 'invoices' 
        AND column_name = 'appointment_id'
    ) THEN
        ALTER TABLE billing_schema.invoices 
        ADD COLUMN appointment_id VARCHAR(36) NULL;
        
        RAISE NOTICE 'Column appointment_id added to invoices table';
    ELSE
        RAISE NOTICE 'Column appointment_id already exists in invoices table';
    END IF;
END $$;

-- Add index for faster lookups by appointment_id
CREATE INDEX IF NOT EXISTS idx_invoices_appointment_id 
ON billing_schema.invoices(appointment_id);

-- Add comment for documentation
COMMENT ON COLUMN billing_schema.invoices.appointment_id 
IS 'Logical foreign key reference to clinical_schema.appointments.id (not enforced by database constraint). Used for appointment-billing integration. NULL when invoice was created manually without an appointment.';

-- Verification query (commented out, for manual verification)
-- SELECT column_name, data_type, is_nullable, column_default
-- FROM information_schema.columns
-- WHERE table_schema = 'billing_schema' 
-- AND table_name = 'invoices' 
-- AND column_name = 'appointment_id';
