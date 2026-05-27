-- Seed: Catálogo de exámenes de laboratorio con resultados el mismo día
-- Todos los exámenes tienen tiempo de resultado de 30 min a 2 horas

INSERT INTO lab_schema.exam_types (id, code, name, description, price, status, test_type, sample_type) VALUES

-- Hematología
(gen_random_uuid(), 'HEM-001', 'Hemograma Completo (BHC)',
 'Conteo completo de células sanguíneas: eritrocitos, leucocitos, plaquetas, hemoglobina y hematocrito.',
 75.00, 'ACTIVE', 'Hematología', 'Sangre'),

(gen_random_uuid(), 'HEM-002', 'Grupo Sanguíneo y Factor Rh',
 'Determinación del grupo ABO y factor Rh del paciente.',
 50.00, 'ACTIVE', 'Hematología', 'Sangre'),

(gen_random_uuid(), 'HEM-003', 'Tiempo de Coagulación y Sangría',
 'Evaluación del tiempo de coagulación (TC) y tiempo de sangría (TS) para valorar hemostasia.',
 60.00, 'ACTIVE', 'Hematología', 'Sangre'),

(gen_random_uuid(), 'HEM-004', 'Velocidad de Sedimentación Globular (VSG)',
 'Marcador inespecífico de inflamación. Útil en seguimiento de procesos infecciosos y autoinmunes.',
 55.00, 'ACTIVE', 'Hematología', 'Sangre'),

-- Química Clínica
(gen_random_uuid(), 'QUI-001', 'Glucosa en Ayunas',
 'Medición de glucosa en sangre tras ayuno mínimo de 8 horas. Cribado de diabetes mellitus.',
 50.00, 'ACTIVE', 'Química Clínica', 'Sangre'),

(gen_random_uuid(), 'QUI-002', 'Glucosa Postprandial',
 'Medición de glucosa 2 horas después de una comida estándar.',
 50.00, 'ACTIVE', 'Química Clínica', 'Sangre'),

(gen_random_uuid(), 'QUI-003', 'Colesterol Total',
 'Determinación del colesterol total en sangre. Factor de riesgo cardiovascular.',
 60.00, 'ACTIVE', 'Química Clínica', 'Sangre'),

(gen_random_uuid(), 'QUI-004', 'Triglicéridos',
 'Medición de triglicéridos séricos. Requiere ayuno de 12 horas.',
 60.00, 'ACTIVE', 'Química Clínica', 'Sangre'),

(gen_random_uuid(), 'QUI-005', 'Creatinina Sérica',
 'Indicador de función renal. Evalúa la capacidad de filtración glomerular.',
 65.00, 'ACTIVE', 'Química Clínica', 'Sangre'),

(gen_random_uuid(), 'QUI-006', 'Ácido Úrico',
 'Medición de ácido úrico en sangre. Diagnóstico y seguimiento de gota e hiperuricemia.',
 60.00, 'ACTIVE', 'Química Clínica', 'Sangre'),

(gen_random_uuid(), 'QUI-007', 'Proteína C Reactiva (PCR)',
 'Marcador de inflamación aguda. Útil en diagnóstico de infecciones bacterianas.',
 70.00, 'ACTIVE', 'Química Clínica', 'Sangre'),

-- Uroanálisis
(gen_random_uuid(), 'URO-001', 'Examen General de Orina (EGO)',
 'Análisis físico, químico y microscópico de orina. Detecta infecciones urinarias, diabetes y daño renal.',
 45.00, 'ACTIVE', 'Uroanálisis', 'Orina'),

(gen_random_uuid(), 'URO-002', 'Urocultivo Rápido',
 'Detección rápida de bacteriuria significativa mediante tira reactiva y microscopía.',
 80.00, 'ACTIVE', 'Uroanálisis', 'Orina'),

-- Parasitología
(gen_random_uuid(), 'PAR-001', 'Examen General de Heces',
 'Análisis macroscópico y microscópico de heces. Detecta parásitos, sangre oculta y bacterias.',
 45.00, 'ACTIVE', 'Parasitología', 'Heces'),

(gen_random_uuid(), 'PAR-002', 'Test Rápido de H. pylori',
 'Detección de antígeno de Helicobacter pylori en heces mediante inmunocromatografía.',
 90.00, 'ACTIVE', 'Parasitología', 'Heces'),

-- Pruebas Rápidas
(gen_random_uuid(), 'RAP-001', 'Test Rápido de Dengue (NS1/IgM/IgG)',
 'Detección simultánea de antígeno NS1 e inmunoglobulinas IgM/IgG para diagnóstico de dengue.',
 120.00, 'ACTIVE', 'Pruebas Rápidas', 'Sangre'),

(gen_random_uuid(), 'RAP-002', 'Test Rápido de COVID-19 (Antígeno)',
 'Detección rápida de antígeno del SARS-CoV-2 mediante inmunocromatografía. Resultado en 15 min.',
 100.00, 'ACTIVE', 'Pruebas Rápidas', 'Hisopo nasal'),

(gen_random_uuid(), 'RAP-003', 'Test Rápido de Influenza A/B',
 'Diferenciación rápida entre influenza tipo A y tipo B. Resultado en 15 minutos.',
 100.00, 'ACTIVE', 'Pruebas Rápidas', 'Hisopo nasal'),

(gen_random_uuid(), 'RAP-004', 'Test Rápido de Embarazo (β-hCG)',
 'Detección de gonadotropina coriónica humana en orina. Resultado en 5 minutos.',
 40.00, 'ACTIVE', 'Pruebas Rápidas', 'Orina'),

(gen_random_uuid(), 'RAP-005', 'Test Rápido VIH (1 y 2)',
 'Detección de anticuerpos contra VIH-1 y VIH-2 mediante inmunocromatografía. Resultado en 20 min.',
 150.00, 'ACTIVE', 'Pruebas Rápidas', 'Sangre')

ON CONFLICT (code) DO NOTHING;