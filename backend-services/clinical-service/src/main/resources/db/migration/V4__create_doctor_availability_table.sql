CREATE TABLE IF NOT EXISTS clinical_schema.doctor_availability (
    id VARCHAR(255) PRIMARY KEY,
    doctor_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT fk_doctor_availability_doctor FOREIGN KEY (doctor_id)
        REFERENCES clinical_schema.doctors(id) ON DELETE CASCADE,
    CONSTRAINT uq_doctor_availability_doctor_date UNIQUE (doctor_id, date)
);

CREATE INDEX IF NOT EXISTS idx_doctor_availability_doctor_date ON clinical_schema.doctor_availability(doctor_id, date);
CREATE INDEX IF NOT EXISTS idx_doctor_availability_date ON clinical_schema.doctor_availability(date);