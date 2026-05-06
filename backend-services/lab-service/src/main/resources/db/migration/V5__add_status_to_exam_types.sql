ALTER TABLE lab_schema.exam_types
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

UPDATE lab_schema.exam_types SET status = CASE
    WHEN deleted = true THEN 'DELETED'
    WHEN active = false THEN 'INACTIVE'
    ELSE 'ACTIVE'
END;

ALTER TABLE lab_schema.exam_types DROP COLUMN IF EXISTS active;
ALTER TABLE lab_schema.exam_types DROP COLUMN IF EXISTS deleted;
