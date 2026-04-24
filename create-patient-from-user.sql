-- Script para crear el registro de paciente basado en el usuario de auth
-- Usuario: guadami259@gmail.com

INSERT INTO patient_schema.patients (
    id,
    dpi,
    first_name,
    second_name,
    first_last_name,
    second_last_name,
    birth_date,
    gender,
    email,
    phone,
    auth_user_id,
    active
)
SELECT 
    u.id,                           -- Usar el mismo ID del usuario
    '1234567890123',                -- DPI temporal (debes cambiarlo por el real)
    u.first_name,
    u.second_name,
    u.first_last_name,
    u.second_last_name,
    '1990-01-01',                   -- Fecha de nacimiento temporal
    'MALE',                         -- Género temporal (MALE, FEMALE, OTHER)
    u.email,
    COALESCE(u.phone, '12345678'),  -- Teléfono del usuario o temporal
    u.id,                           -- Vincular con el usuario de auth
    true
FROM auth_schema.users u
WHERE u.email = 'guadami259@gmail.com'
ON CONFLICT (id) DO NOTHING;

-- Verificar que se creó correctamente
SELECT id, first_name, first_last_name, dpi, email 
FROM patient_schema.patients 
WHERE email = 'guadami259@gmail.com';
