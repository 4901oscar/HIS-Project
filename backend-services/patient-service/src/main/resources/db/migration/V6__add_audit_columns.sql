-- V6: Add standard audit columns (created_by, updated_by) to patients
-- keeps created_at and updated_at that already exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'patient_schema' AND table_name = 'patients' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE patient_schema.patients
            ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'patient_schema' AND table_name = 'patients' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE patient_schema.patients
            ADD COLUMN updated_by VARCHAR(36);
    END IF;
END $$;