-- Patient Service Schema
-- CU-01: Registro de pacientes y admisión

CREATE SCHEMA IF NOT EXISTS patient_schema;

CREATE TABLE IF NOT EXISTS patient_schema.patients (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dpi         VARCHAR(13)  NOT NULL UNIQUE,
    nit         VARCHAR(20),
    first_name  VARCHAR(100) NOT NULL,
    second_name VARCHAR(100),
    first_last_name  VARCHAR(100) NOT NULL,
    second_last_name VARCHAR(100),
    birth_date  DATE         NOT NULL,
    gender      VARCHAR(20)  NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    phone       VARCHAR(8)   NOT NULL,
    department  VARCHAR(100),
    municipality VARCHAR(100),
    zone        VARCHAR(10),
    address     VARCHAR(300),
    auth_user_id VARCHAR(36),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_patients_dpi   ON patient_schema.patients(dpi);
CREATE INDEX IF NOT EXISTS idx_patients_email ON patient_schema.patients(email);
CREATE INDEX IF NOT EXISTS idx_patients_name  ON patient_schema.patients(first_last_name, first_name);
