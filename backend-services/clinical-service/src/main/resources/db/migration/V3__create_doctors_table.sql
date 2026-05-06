CREATE TABLE IF NOT EXISTS clinical_schema.doctors (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    shift_start TIME NOT NULL,
    shift_end TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_doctor_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX IF NOT EXISTS idx_doctors_shift_status ON clinical_schema.doctors(shift_start, shift_end, status);
CREATE INDEX IF NOT EXISTS idx_doctors_status ON clinical_schema.doctors(status);