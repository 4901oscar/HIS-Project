-- V7: Extend lab_results table for laboratory sample workflow
-- Feature: lab-sample-workflow
-- Requirements: 15.1, 15.2, 15.5
-- This migration adds columns to support the new laboratory sample workflow:
-- - test_name: identifies which test this result belongs to
-- - original_filename: preserves the original uploaded filename
-- - file_size: stores the file size in bytes for validation and display

-- Add new columns to lab_results table
ALTER TABLE lab_schema.lab_results
ADD COLUMN test_name VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN original_filename VARCHAR(500),
ADD COLUMN file_size BIGINT;

-- Add index on test_name for query performance
CREATE INDEX idx_lab_results_test_name ON lab_schema.lab_results(test_name);

-- Add comments to document the columns
COMMENT ON COLUMN lab_schema.lab_results.test_name IS 'Name of the test this result belongs to (e.g., "Hemograma Completo")';
COMMENT ON COLUMN lab_schema.lab_results.original_filename IS 'Original filename of the uploaded result file';
COMMENT ON COLUMN lab_schema.lab_results.file_size IS 'Size of the uploaded file in bytes';
