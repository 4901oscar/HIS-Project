-- V3__create_appointments_table.sql
-- Create appointments table for patient appointment scheduling
-- Requirements: 2.1, 2.2, 2.3, 3.1, 3.3

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    appointment_id VARCHAR(50) UNIQUE NOT NULL,
    
    -- Patient Information (embedded PatientInfo value object)
    dpi VARCHAR(13) NOT NULL,
    nit VARCHAR(20) NOT NULL,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(255) NOT NULL,
    
    -- Appointment Details
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    symptoms TEXT NOT NULL,
    doctor_id BIGINT REFERENCES doctors(id),
    
    -- Status
    status VARCHAR(50) NOT NULL,
    
    -- Payment Information (embedded PaymentRecord value object)
    payment_status VARCHAR(50) NOT NULL,
    payment_amount DECIMAL(10, 2) NOT NULL,
    transaction_id VARCHAR(100),
    payment_method VARCHAR(50),
    payment_timestamp TIMESTAMP,
    
    -- QR Code Information (embedded AppointmentQR value object)
    qr_code_data TEXT,
    qr_valid_from TIMESTAMP,
    qr_valid_until TIMESTAMP,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    
    -- Constraints
    CONSTRAINT chk_dpi_length CHECK (LENGTH(dpi) = 13 AND dpi ~ '^[0-9]{13}$'),
    CONSTRAINT chk_appointment_time CHECK (appointment_time >= '06:00' AND appointment_time <= '20:00')
);

-- Create indexes for performance
CREATE INDEX idx_appointments_date_time ON appointments(appointment_date, appointment_time);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_payment_status ON appointments(payment_status);
CREATE INDEX idx_appointments_dpi ON appointments(dpi);
CREATE INDEX idx_appointments_correo ON appointments(correo);

-- Create unique index to prevent double-booking (same doctor, date, and time)
CREATE UNIQUE INDEX idx_appointments_doctor_slot ON appointments(doctor_id, appointment_date, appointment_time)
    WHERE status NOT IN ('CANCELLED');
