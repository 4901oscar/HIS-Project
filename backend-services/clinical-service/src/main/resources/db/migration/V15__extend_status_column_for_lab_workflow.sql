-- Extend status column to accommodate new lab workflow statuses
-- LAB_SAMPLE_COLLECTION has 21 characters, current limit is 20
ALTER TABLE clinical_schema.appointments 
ALTER COLUMN status TYPE VARCHAR(30);

-- Update existing LABORATORY status to LAB_SAMPLE_COLLECTION
UPDATE clinical_schema.appointments 
SET status = 'LAB_SAMPLE_COLLECTION' 
WHERE status = 'LABORATORY';
