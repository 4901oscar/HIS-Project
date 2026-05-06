-- V12: Add clinic_id to doctors table
-- Each doctor must be assigned to a clinic.
-- Nullable to preserve existing records; application enforces required on new records.

ALTER TABLE clinical_schema.doctors
    ADD COLUMN clinic_id VARCHAR(36);

ALTER TABLE clinical_schema.doctors
    ADD CONSTRAINT fk_doctors_clinic
    FOREIGN KEY (clinic_id) REFERENCES clinical_schema.clinics(id);

CREATE INDEX idx_doctors_clinic_id ON clinical_schema.doctors(clinic_id);
