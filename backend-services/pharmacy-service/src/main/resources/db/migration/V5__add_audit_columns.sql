-- V5: Add standard audit columns to all pharmacy_schema tables
-- Keeps existing semantic columns (issued_at, dispensed_at, dispensed_by)
DO $$
BEGIN
    -- prescriptions: has issued_at (semantic) — add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'prescriptions' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE pharmacy_schema.prescriptions ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'prescriptions' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE pharmacy_schema.prescriptions ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'prescriptions' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE pharmacy_schema.prescriptions ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'prescriptions' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE pharmacy_schema.prescriptions ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- dispensations: has dispensed_at and dispensed_by (semantic), add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'dispensations' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE pharmacy_schema.dispensations ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'dispensations' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE pharmacy_schema.dispensations ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'dispensations' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE pharmacy_schema.dispensations ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'dispensations' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE pharmacy_schema.dispensations ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- medications: no audit fields, add all 4
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'medications' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE pharmacy_schema.medications ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'medications' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE pharmacy_schema.medications ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'medications' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE pharmacy_schema.medications ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'pharmacy_schema' AND table_name = 'medications' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE pharmacy_schema.medications ADD COLUMN updated_by VARCHAR(36);
    END IF;
END $$;