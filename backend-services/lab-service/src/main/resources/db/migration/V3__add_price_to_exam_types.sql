ALTER TABLE lab_schema.exam_types
    ADD COLUMN IF NOT EXISTS price NUMERIC(10, 2) NOT NULL DEFAULT 50.00;

-- Precios iniciales para los 5 exámenes del seed
UPDATE lab_schema.exam_types SET price = 75.00 WHERE code = 'HEM';
UPDATE lab_schema.exam_types SET price = 50.00 WHERE code = 'GLU';
UPDATE lab_schema.exam_types SET price = 120.00 WHERE code = 'COL';
UPDATE lab_schema.exam_types SET price = 45.00 WHERE code = 'ORI';
UPDATE lab_schema.exam_types SET price = 150.00 WHERE code = 'HEP';
