-- Prevents duplicate consultations for the same appointment.
-- One appointment can only have one consultation.
ALTER TABLE clinical_schema.consultations
    ADD CONSTRAINT uq_consultations_appointment_id UNIQUE (appointment_id);
