-- U7: Rollback lab_results table extensions
-- This script removes the columns added in V7

-- Drop index
DROP INDEX IF EXISTS lab_schema.idx_lab_results_test_name;

-- Remove columns
ALTER TABLE lab_schema.lab_results
DROP COLUMN IF EXISTS test_name,
DROP COLUMN IF EXISTS original_filename,
DROP COLUMN IF EXISTS file_size;
