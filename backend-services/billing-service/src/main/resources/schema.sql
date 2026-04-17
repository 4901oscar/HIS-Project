-- Billing Service Schema
CREATE SCHEMA IF NOT EXISTS billing_schema;

-- Invoices Table
CREATE TABLE IF NOT EXISTS billing_schema.invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(20) UNIQUE NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT chk_total_positive CHECK (total >= 0)
);

-- Charges Table
CREATE TABLE IF NOT EXISTS billing_schema.charges (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL REFERENCES billing_schema.invoices(id),
    type VARCHAR(20) NOT NULL,
    description VARCHAR(300) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_subtotal_positive CHECK (subtotal >= 0),
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

-- Payments Table
CREATE TABLE IF NOT EXISTS billing_schema.payments (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    change_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    method VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP NOT NULL,
    received_by VARCHAR(36) NOT NULL,
    CONSTRAINT chk_amount_positive CHECK (amount >= 0)
);

-- Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_invoices_patient ON billing_schema.invoices(patient_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON billing_schema.invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_number ON billing_schema.invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_charges_invoice ON billing_schema.charges(invoice_id);
CREATE INDEX IF NOT EXISTS idx_payments_invoice ON billing_schema.payments(invoice_id);
