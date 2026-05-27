-- Script para borrar todos los datos de la base de datos MedFlow
-- ADVERTENCIA: Este script eliminará TODOS los datos

-- Clinical Schema
TRUNCATE TABLE clinical_schema.triages CASCADE;
TRUNCATE TABLE clinical_schema.vital_signs CASCADE;
TRUNCATE TABLE clinical_schema.appointments CASCADE;
TRUNCATE TABLE clinical_schema.doctors CASCADE;
TRUNCATE TABLE clinical_schema.manchester_discriminators CASCADE;
TRUNCATE TABLE clinical_schema.manchester_motifs CASCADE;

-- Patient Schema
TRUNCATE TABLE patient_schema.patients CASCADE;

-- Auth Schema
TRUNCATE TABLE auth_schema.users CASCADE;
TRUNCATE TABLE auth_schema.roles CASCADE;
TRUNCATE TABLE auth_schema.user_roles CASCADE;

-- Lab Schema
TRUNCATE TABLE lab_schema.lab_tests CASCADE;
TRUNCATE TABLE lab_schema.lab_results CASCADE;
TRUNCATE TABLE lab_schema.lab_samples CASCADE;

-- Pharmacy Schema
TRUNCATE TABLE pharmacy_schema.medications CASCADE;
TRUNCATE TABLE pharmacy_schema.prescriptions CASCADE;
TRUNCATE TABLE pharmacy_schema.prescription_items CASCADE;
TRUNCATE TABLE pharmacy_schema.dispensations CASCADE;

-- Billing Schema
TRUNCATE TABLE billing_schema.invoices CASCADE;
TRUNCATE TABLE billing_schema.invoice_items CASCADE;
TRUNCATE TABLE billing_schema.payments CASCADE;

-- Mensaje de confirmación
SELECT 'Todos los datos han sido eliminados exitosamente' AS resultado;
