ALTER TABLE lab_schema.exam_types
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT false;
