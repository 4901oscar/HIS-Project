-- V11: Convert clinics.estado from custom PostgreSQL enum to VARCHAR
-- Hibernate @Enumerated(EnumType.STRING) uses VARCHAR binding which is
-- incompatible with PostgreSQL custom enum types, causing 500 errors on INSERT.
-- Must drop the DEFAULT first since it depends on the enum type.

ALTER TABLE clinical_schema.clinics
    ALTER COLUMN estado DROP DEFAULT;

ALTER TABLE clinical_schema.clinics
    ALTER COLUMN estado TYPE VARCHAR(20) USING estado::VARCHAR;

ALTER TABLE clinical_schema.clinics
    ALTER COLUMN estado SET DEFAULT 'ACTIVE';

ALTER TABLE clinical_schema.clinics
    ADD CONSTRAINT chk_clinics_estado
    CHECK (estado IN ('ACTIVE', 'INACTIVE', 'DELETED'));

DROP TYPE IF EXISTS clinical_schema.clinic_status;
