ALTER TABLE pharmacy_schema.medications
    ADD COLUMN IF NOT EXISTS price NUMERIC(10, 2) NOT NULL DEFAULT 0.00;

-- Precios iniciales para los 10 medicamentos del seed
UPDATE pharmacy_schema.medications SET price = 15.00 WHERE id = 'med-001';
UPDATE pharmacy_schema.medications SET price = 18.00 WHERE id = 'med-002';
UPDATE pharmacy_schema.medications SET price = 45.00 WHERE id = 'med-003';
UPDATE pharmacy_schema.medications SET price = 25.00 WHERE id = 'med-004';
UPDATE pharmacy_schema.medications SET price = 35.00 WHERE id = 'med-005';
UPDATE pharmacy_schema.medications SET price = 30.00 WHERE id = 'med-006';
UPDATE pharmacy_schema.medications SET price = 40.00 WHERE id = 'med-007';
UPDATE pharmacy_schema.medications SET price = 85.00 WHERE id = 'med-008';
UPDATE pharmacy_schema.medications SET price = 22.00 WHERE id = 'med-009';
UPDATE pharmacy_schema.medications SET price = 12.00 WHERE id = 'med-010';
