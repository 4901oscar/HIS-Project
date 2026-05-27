-- Migration V7: Add invoice_id column to appointments table for billing integration
-- Feature: appointment-billing-integration
-- Requirements: 5.1, 5.2, 5.3, 5.4, 10.1, 10.2, 10.3, 10.4, 10.5, 10.8

-- Add invoice_id column to appointments table (nullable to support compensation scenarios)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'clinical_schema' 
        AND table_name = 'appointments' 
        AND column_name = 'invoice_id'
    ) THEN
        ALTER TABLE clinical_schema.appointments 
        ADD COLUMN invoice_id VARCHAR(36) NULL;
        
        RAISE NOTICE 'Column invoice_id added to appointments table';
    ELSE
        RAISE NOTICE 'Column invoice_id already exists in appointments table';
    END IF;
END $$;

-- Add index for faster lookups by invoice_id
CREATE INDEX IF NOT EXISTS idx_appointments_invoice_id 
ON clinical_schema.appointments(invoice_id);

-- Add comment for documentation
COMMENT ON COLUMN clinical_schema.appointments.invoice_id 
IS 'Logical foreign key reference to billing_schema.invoices.id (not enforced by database constraint). Used for appointment-billing integration. NULL when billing service was unavailable during appointment creation.';

-- Verification query (commented out, for manual verification)
-- SELECT column_name, data_type, is_nullable, column_default
-- FROM information_schema.columns
-- WHERE table_schema = 'clinical_schema' 
-- AND table_name = 'appointments' 
-- AND column_name = 'invoice_id';
