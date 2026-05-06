-- V6: Add appointment_id column to lab_orders table
-- This establishes the relationship between lab orders and appointments

ALTER TABLE lab_schema.lab_orders
ADD COLUMN appointment_id VARCHAR(36);

-- Add index for better query performance
CREATE INDEX idx_lab_orders_appointment_id ON lab_schema.lab_orders(appointment_id);

-- Add comment to document the column
COMMENT ON COLUMN lab_schema.lab_orders.appointment_id IS 'Foreign key reference to the appointment that generated this lab order';
