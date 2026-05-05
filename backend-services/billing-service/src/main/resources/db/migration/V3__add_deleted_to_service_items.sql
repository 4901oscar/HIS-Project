ALTER TABLE billing_schema.service_items
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
