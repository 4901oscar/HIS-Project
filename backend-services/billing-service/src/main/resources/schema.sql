-- Billing Service Schema
CREATE SCHEMA IF NOT EXISTS billing_schema;

-- Invoices Table
CREATE TABLE IF NOT EXISTS billing_schema.invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(20) UNIQUE NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    appointment_id VARCHAR(36),  -- Logical FK to clinical_schema.appointments.id (not enforced)
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    customer_nit VARCHAR(20),
    customer_name VARCHAR(200),
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

-- Service Items Catalog Table
CREATE TABLE IF NOT EXISTS billing_schema.service_items (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);
ALTER TABLE billing_schema.service_items ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE billing_schema.service_items DROP COLUMN IF EXISTS active;
ALTER TABLE billing_schema.service_items DROP COLUMN IF EXISTS deleted;

-- Audit columns for tables that existed before audit tracking was added
ALTER TABLE billing_schema.invoices ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36);

ALTER TABLE billing_schema.charges ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE billing_schema.charges ADD COLUMN IF NOT EXISTS created_by VARCHAR(36) NOT NULL DEFAULT 'internal';
ALTER TABLE billing_schema.charges ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE billing_schema.charges ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36);

ALTER TABLE billing_schema.payments ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE billing_schema.payments ADD COLUMN IF NOT EXISTS created_by VARCHAR(36) NOT NULL DEFAULT 'internal';
ALTER TABLE billing_schema.payments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE billing_schema.payments ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36);

ALTER TABLE billing_schema.service_items ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE billing_schema.service_items ADD COLUMN IF NOT EXISTS created_by VARCHAR(36) NOT NULL DEFAULT 'internal';
ALTER TABLE billing_schema.service_items ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE billing_schema.service_items ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36);

CREATE INDEX IF NOT EXISTS idx_service_items_code ON billing_schema.service_items(code);
CREATE INDEX IF NOT EXISTS idx_service_items_category ON billing_schema.service_items(category);

INSERT INTO billing_schema.service_items (id, code, name, description, category, price, status) VALUES
  (gen_random_uuid(), 'CONS-GEN', 'Consulta General', 'Consulta médica general', 'CONSULTATION', 150.00, 'ACTIVE'),
  (gen_random_uuid(), 'CONS-ESP', 'Consulta Especialista', 'Consulta con médico especialista', 'CONSULTATION', 250.00, 'ACTIVE'),
  (gen_random_uuid(), 'LAB-HEM', 'Hemograma Completo', 'Examen de sangre completo', 'LABORATORY', 75.00, 'ACTIVE'),
  (gen_random_uuid(), 'LAB-GLU', 'Glucosa en Ayunas', 'Examen de glucosa', 'LABORATORY', 40.00, 'ACTIVE'),
  (gen_random_uuid(), 'MED-GEN', 'Medicamento Genérico', 'Dispensación de medicamento genérico', 'MEDICATION', 25.00, 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

-- Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_invoices_patient ON billing_schema.invoices(patient_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON billing_schema.invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_number ON billing_schema.invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_invoices_appointment ON billing_schema.invoices(appointment_id);  -- Index for appointment-billing integration
CREATE INDEX IF NOT EXISTS idx_charges_invoice ON billing_schema.charges(invoice_id);
CREATE INDEX IF NOT EXISTS idx_payments_invoice ON billing_schema.payments(invoice_id);
