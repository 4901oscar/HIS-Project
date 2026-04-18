-- Create lab_schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS lab_schema;

-- Create lab_orders table
CREATE TABLE IF NOT EXISTS lab_schema.lab_orders (
    id VARCHAR(36) PRIMARY KEY,
    order_code VARCHAR(8) UNIQUE NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    test_names TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ordered_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create samples table
CREATE TABLE IF NOT EXISTS lab_schema.samples (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    collected_at TIMESTAMP NOT NULL,
    collected_by VARCHAR(36) NOT NULL
);

-- Create lab_results table
CREATE TABLE IF NOT EXISTS lab_schema.lab_results (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    result_file_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    uploaded_by VARCHAR(36) NOT NULL
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_lab_orders_patient ON lab_schema.lab_orders(patient_id);
CREATE INDEX IF NOT EXISTS idx_lab_orders_status ON lab_schema.lab_orders(status);
CREATE INDEX IF NOT EXISTS idx_lab_orders_code ON lab_schema.lab_orders(order_code);
CREATE INDEX IF NOT EXISTS idx_samples_order ON lab_schema.samples(order_id);
CREATE INDEX IF NOT EXISTS idx_lab_results_patient ON lab_schema.lab_results(patient_id);
CREATE INDEX IF NOT EXISTS idx_lab_results_order ON lab_schema.lab_results(order_id);
