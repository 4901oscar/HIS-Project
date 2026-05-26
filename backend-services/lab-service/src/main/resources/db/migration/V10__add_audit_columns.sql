-- V10: Add standard audit columns to all lab_schema tables
-- Keeps existing semantic columns (ordered_at, collected_at, collected_by, uploaded_at, uploaded_by)
DO $$
BEGIN
    -- lab_orders: has ordered_at (semantic) — add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'lab_orders' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE lab_schema.lab_orders ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'lab_orders' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE lab_schema.lab_orders ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'lab_orders' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE lab_schema.lab_orders ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'lab_orders' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE lab_schema.lab_orders ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- samples: has collected_at and collected_by (semantic), add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'samples' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE lab_schema.samples ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'samples' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE lab_schema.samples ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'samples' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE lab_schema.samples ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'samples' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE lab_schema.samples ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- lab_results: has uploaded_at and uploaded_by (semantic), add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'lab_results' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE lab_schema.lab_results ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'lab_results' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE lab_schema.lab_results ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'lab_results' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE lab_schema.lab_results ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'lab_results' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE lab_schema.lab_results ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- exam_types: no audit fields, add all 4
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'exam_types' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE lab_schema.exam_types ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'exam_types' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE lab_schema.exam_types ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'exam_types' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE lab_schema.exam_types ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'lab_schema' AND table_name = 'exam_types' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE lab_schema.exam_types ADD COLUMN updated_by VARCHAR(36);
    END IF;
END $$;