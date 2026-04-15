-- V2__create_doctor_schedules_table.sql
-- Create doctor_schedules table for patient appointment scheduling
-- Requirements: 2.2, 2.3

CREATE TABLE doctor_schedules (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_time_range CHECK (start_time < end_time),
    CONSTRAINT idx_doctor_schedule UNIQUE (doctor_id, day_of_week, start_time)
);

-- Create indexes for performance
CREATE INDEX idx_doctor_schedules_doctor ON doctor_schedules(doctor_id);
CREATE INDEX idx_doctor_schedules_active ON doctor_schedules(is_active);

-- Insert sample schedules (Monday-Friday, 8:00-17:00) for all doctors
-- Doctor 1: María González - Monday to Friday, 8:00-17:00
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_active) VALUES
(1, 1, '08:00:00', '17:00:00', TRUE),  -- Monday
(1, 2, '08:00:00', '17:00:00', TRUE),  -- Tuesday
(1, 3, '08:00:00', '17:00:00', TRUE),  -- Wednesday
(1, 4, '08:00:00', '17:00:00', TRUE),  -- Thursday
(1, 5, '08:00:00', '17:00:00', TRUE);  -- Friday

-- Doctor 2: Carlos Rodríguez - Monday to Friday, 8:00-17:00
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_active) VALUES
(2, 1, '08:00:00', '17:00:00', TRUE),  -- Monday
(2, 2, '08:00:00', '17:00:00', TRUE),  -- Tuesday
(2, 3, '08:00:00', '17:00:00', TRUE),  -- Wednesday
(2, 4, '08:00:00', '17:00:00', TRUE),  -- Thursday
(2, 5, '08:00:00', '17:00:00', TRUE);  -- Friday

-- Doctor 3: Ana Martínez - Monday to Friday, 8:00-17:00
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, is_active) VALUES
(3, 1, '08:00:00', '17:00:00', TRUE),  -- Monday
(3, 2, '08:00:00', '17:00:00', TRUE),  -- Tuesday
(3, 3, '08:00:00', '17:00:00', TRUE),  -- Wednesday
(3, 4, '08:00:00', '17:00:00', TRUE),  -- Thursday
(3, 5, '08:00:00', '17:00:00', TRUE);  -- Friday
