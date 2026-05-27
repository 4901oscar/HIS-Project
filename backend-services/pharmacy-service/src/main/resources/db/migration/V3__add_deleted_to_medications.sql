ALTER TABLE pharmacy_schema.medications
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
