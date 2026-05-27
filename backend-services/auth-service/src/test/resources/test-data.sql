-- Insert default roles for testing
INSERT INTO auth_schema.roles (name, description) VALUES
('ADMIN', 'Súper Usuario'),
('ADMISSION', 'Personal de Admisión'),
('VITAL_SIGNS', 'Personal de Signos Vitales'),
('DOCTOR', 'Médico'),
('LABORATORY', 'Personal de Laboratorio'),
('PHARMACY', 'Farmacéutico'),
('CASHIER', 'Cajero'),
('PATIENT', 'Paciente');
