-- V5: Limpieza de tablas que no pertenecen a patient-service.
-- Doctors, schedules y appointments pertenecen a clinical-service.

DROP TABLE IF EXISTS patient_schema.appointments      CASCADE;
DROP TABLE IF EXISTS patient_schema.doctor_schedules  CASCADE;
DROP TABLE IF EXISTS patient_schema.doctors           CASCADE;
