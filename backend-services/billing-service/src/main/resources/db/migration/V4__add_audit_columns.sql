-- V4: Add standard audit columns to all billing_schema tables
-- invoices already has created_at, created_by, updated_at — only add updated_by
-- charges, payments, service_items need all 4 audit columns
DO $$
BEGIN
    -- invoices: add only updated_by (rest already present)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'invoices' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE billing_schema.invoices ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- charges: no audit fields, add all 4
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'charges' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE billing_schema.charges ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'charges' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE billing_schema.charges ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'charges' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE billing_schema.charges ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'charges' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE billing_schema.charges ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- payments: has paid_at and received_by (semantic), add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'payments' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE billing_schema.payments ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'payments' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE billing_schema.payments ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'payments' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE billing_schema.payments ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'payments' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE billing_schema.payments ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- service_items: no audit fields, add all 4
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'service_items' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE billing_schema.service_items ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'service_items' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE billing_schema.service_items ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'service_items' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE billing_schema.service_items ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing_schema' AND table_name = 'service_items' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE billing_schema.service_items ADD COLUMN updated_by VARCHAR(36);
    END IF;
END $$;