ALTER TABLE pharmacy_schema.medications
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

UPDATE pharmacy_schema.medications SET status = CASE
    WHEN deleted = true THEN 'DELETED'
    WHEN active = false THEN 'INACTIVE'
    ELSE 'ACTIVE'
END;

ALTER TABLE pharmacy_schema.medications DROP COLUMN IF EXISTS active;
ALTER TABLE pharmacy_schema.medications DROP COLUMN IF EXISTS deleted;
