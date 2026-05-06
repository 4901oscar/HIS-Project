CREATE TABLE IF NOT EXISTS lab_schema.exam_types (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_exam_types_code ON lab_schema.exam_types(code);

INSERT INTO lab_schema.exam_types (id, code, name, description, active) VALUES
  (gen_random_uuid(), 'HEM', 'Hemograma Completo', 'Conteo completo de células sanguíneas', true),
  (gen_random_uuid(), 'GLU', 'Glucosa en Ayunas', 'Medición de glucosa en sangre en ayunas', true),
  (gen_random_uuid(), 'COL', 'Perfil Lipídico', 'Colesterol total, HDL, LDL y triglicéridos', true),
  (gen_random_uuid(), 'ORI', 'Uroanálisis', 'Análisis completo de orina', true),
  (gen_random_uuid(), 'HEP', 'Función Hepática', 'AST, ALT, bilirrubinas', true)
ON CONFLICT (code) DO NOTHING;
