-- Add test_type and sample_type columns to exam_types table
ALTER TABLE lab_schema.exam_types 
ADD COLUMN test_type VARCHAR(100),
ADD COLUMN sample_type VARCHAR(100);

-- Update existing records with appropriate values
UPDATE lab_schema.exam_types SET test_type = 'Hematología', sample_type = 'Sangre' WHERE code = 'HEM';
UPDATE lab_schema.exam_types SET test_type = 'Química Clínica', sample_type = 'Sangre' WHERE code = 'GLU';
UPDATE lab_schema.exam_types SET test_type = 'Química Clínica', sample_type = 'Sangre' WHERE code = 'COL';
UPDATE lab_schema.exam_types SET test_type = 'Uroanálisis', sample_type = 'Orina' WHERE code = 'ORI';
UPDATE lab_schema.exam_types SET test_type = 'Química Clínica', sample_type = 'Sangre' WHERE code = 'HEP';
