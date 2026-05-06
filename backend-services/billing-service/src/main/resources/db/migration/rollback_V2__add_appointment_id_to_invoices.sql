-- Rollback for Migration V2: Remove appointment_id column from invoices table
-- Feature: appointment-billing-integration
-- WARNING: This will delete all appointment_id references. Use only if integration needs to be completely removed.

-- Remove index first
DROP INDEX IF EXISTS billing_schema.idx_invoices_appointment_id;

-- Remove column
ALTER TABLE billing_schema.invoices 
DROP COLUMN IF EXISTS appointment_id;

-- Verification query (commented out, for manual verification)
-- SELECT column_name 
-- FROM information_schema.columns
-- WHERE table_schema = 'billing_schema' 
-- AND table_name = 'invoices' 
-- AND column_name = 'appointment_id';
-- Expected: No rows returned
