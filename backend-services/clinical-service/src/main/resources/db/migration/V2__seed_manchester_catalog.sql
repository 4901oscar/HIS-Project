-- =====================================================
-- Clinical Service - Manchester Catalog Seed Data
-- Version: 2.0
-- Description: Seeds Manchester triage catalog with
--              sample motifs and discriminators for
--              testing and initial deployment
-- =====================================================

-- =====================================================
-- Manchester Motifs (Reasons for consultation)
-- =====================================================

-- Motif 1: Chest Pain
INSERT INTO clinical_schema.manchester_motifs (id, code, description, category, active)
VALUES ('M001', 'CP', 'Dolor torácico', 'Cardiovascular', true);

-- Motif 2: Dyspnea (Difficulty breathing)
INSERT INTO clinical_schema.manchester_motifs (id, code, description, category, active)
VALUES ('M002', 'DYSP', 'Disnea', 'Respiratorio', true);

-- Motif 3: Trauma
INSERT INTO clinical_schema.manchester_motifs (id, code, description, category, active)
VALUES ('M003', 'TRAUMA', 'Traumatismo', 'Trauma', true);

-- Motif 4: Abdominal Pain
INSERT INTO clinical_schema.manchester_motifs (id, code, description, category, active)
VALUES ('M004', 'ABD', 'Dolor abdominal', 'Gastrointestinal', true);

-- Motif 5: Headache
INSERT INTO clinical_schema.manchester_motifs (id, code, description, category, active)
VALUES ('M005', 'HEAD', 'Cefalea', 'Neurológico', true);

-- Motif 6: Fever
INSERT INTO clinical_schema.manchester_motifs (id, code, description, category, active)
VALUES ('M006', 'FEVER', 'Fiebre', 'Infeccioso', true);

-- Motif 7: Altered Mental Status
INSERT INTO clinical_schema.manchester_motifs (id, code, description, category, active)
VALUES ('M007', 'AMS', 'Alteración del estado mental', 'Neurológico', true);

-- Motif 8: Allergic Reaction
INSERT INTO clinical_schema.manchester_motifs (id, code, description, category, active)
VALUES ('M008', 'ALLERGY', 'Reacción alérgica', 'Inmunológico', true);

-- =====================================================
-- Manchester Discriminators for Chest Pain (M001)
-- =====================================================

-- RED (Immediate - 0 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D001', 'CP-R1', 'Paro cardiorrespiratorio', 'RED', true, 'M001');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D002', 'CP-R2', 'Dolor torácico con shock', 'RED', true, 'M001');

-- ORANGE (Very urgent - 10 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D003', 'CP-O1', 'Dolor torácico agudo con diaforesis', 'ORANGE', true, 'M001');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D004', 'CP-O2', 'Dolor torácico con disnea severa', 'ORANGE', true, 'M001');

-- YELLOW (Urgent - 60 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D005', 'CP-Y1', 'Dolor torácico moderado reciente', 'YELLOW', true, 'M001');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D006', 'CP-Y2', 'Historia de enfermedad cardíaca', 'YELLOW', true, 'M001');

-- GREEN (Less urgent - 120 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D007', 'CP-G1', 'Dolor torácico leve sin otros síntomas', 'GREEN', true, 'M001');

-- BLUE (Non-urgent - 240 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D008', 'CP-B1', 'Dolor torácico crónico estable', 'BLUE', true, 'M001');

-- =====================================================
-- Manchester Discriminators for Dyspnea (M002)
-- =====================================================

-- RED (Immediate - 0 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D009', 'DYSP-R1', 'Obstrucción completa de vía aérea', 'RED', true, 'M002');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D010', 'DYSP-R2', 'Cianosis central', 'RED', true, 'M002');

-- ORANGE (Very urgent - 10 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D011', 'DYSP-O1', 'Disnea severa con estridor', 'ORANGE', true, 'M002');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D012', 'DYSP-O2', 'Saturación de oxígeno < 90%', 'ORANGE', true, 'M002');

-- YELLOW (Urgent - 60 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D013', 'DYSP-Y1', 'Disnea moderada de inicio reciente', 'YELLOW', true, 'M002');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D014', 'DYSP-Y2', 'Historia de asma o EPOC', 'YELLOW', true, 'M002');

-- GREEN (Less urgent - 120 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D015', 'DYSP-G1', 'Disnea leve sin otros síntomas', 'GREEN', true, 'M002');

-- BLUE (Non-urgent - 240 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D016', 'DYSP-B1', 'Disnea crónica estable', 'BLUE', true, 'M002');

-- =====================================================
-- Manchester Discriminators for Trauma (M003)
-- =====================================================

-- RED (Immediate - 0 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D017', 'TRAUMA-R1', 'Trauma con compromiso de vía aérea', 'RED', true, 'M003');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D018', 'TRAUMA-R2', 'Hemorragia masiva incontrolable', 'RED', true, 'M003');

-- ORANGE (Very urgent - 10 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D019', 'TRAUMA-O1', 'Trauma craneoencefálico con pérdida de conciencia', 'ORANGE', true, 'M003');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D020', 'TRAUMA-O2', 'Fractura abierta con hemorragia activa', 'ORANGE', true, 'M003');

-- YELLOW (Urgent - 60 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D021', 'TRAUMA-Y1', 'Trauma con dolor severo', 'YELLOW', true, 'M003');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D022', 'TRAUMA-Y2', 'Sospecha de fractura cerrada', 'YELLOW', true, 'M003');

-- GREEN (Less urgent - 120 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D023', 'TRAUMA-G1', 'Trauma menor sin complicaciones', 'GREEN', true, 'M003');

-- BLUE (Non-urgent - 240 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D024', 'TRAUMA-B1', 'Contusión leve sin dolor significativo', 'BLUE', true, 'M003');

-- =====================================================
-- Manchester Discriminators for Abdominal Pain (M004)
-- =====================================================

-- RED (Immediate - 0 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D025', 'ABD-R1', 'Dolor abdominal con shock', 'RED', true, 'M004');

-- ORANGE (Very urgent - 10 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D026', 'ABD-O1', 'Dolor abdominal severo con vómito persistente', 'ORANGE', true, 'M004');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D027', 'ABD-O2', 'Abdomen agudo con defensa muscular', 'ORANGE', true, 'M004');

-- YELLOW (Urgent - 60 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D028', 'ABD-Y1', 'Dolor abdominal moderado reciente', 'YELLOW', true, 'M004');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D029', 'ABD-Y2', 'Dolor abdominal con fiebre', 'YELLOW', true, 'M004');

-- GREEN (Less urgent - 120 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D030', 'ABD-G1', 'Dolor abdominal leve sin otros síntomas', 'GREEN', true, 'M004');

-- BLUE (Non-urgent - 240 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D031', 'ABD-B1', 'Dolor abdominal crónico estable', 'BLUE', true, 'M004');

-- =====================================================
-- Manchester Discriminators for Headache (M005)
-- =====================================================

-- RED (Immediate - 0 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D032', 'HEAD-R1', 'Cefalea súbita severa (cefalea en trueno)', 'RED', true, 'M005');

-- ORANGE (Very urgent - 10 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D033', 'HEAD-O1', 'Cefalea con alteración neurológica focal', 'ORANGE', true, 'M005');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D034', 'HEAD-O2', 'Cefalea con fiebre y rigidez de nuca', 'ORANGE', true, 'M005');

-- YELLOW (Urgent - 60 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D035', 'HEAD-Y1', 'Cefalea severa de inicio reciente', 'YELLOW', true, 'M005');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D036', 'HEAD-Y2', 'Cefalea con vómito persistente', 'YELLOW', true, 'M005');

-- GREEN (Less urgent - 120 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D037', 'HEAD-G1', 'Cefalea moderada sin otros síntomas', 'GREEN', true, 'M005');

-- BLUE (Non-urgent - 240 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D038', 'HEAD-B1', 'Cefalea crónica tipo tensional', 'BLUE', true, 'M005');

-- =====================================================
-- Manchester Discriminators for Fever (M006)
-- =====================================================

-- RED (Immediate - 0 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D039', 'FEVER-R1', 'Fiebre con shock séptico', 'RED', true, 'M006');

-- ORANGE (Very urgent - 10 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D040', 'FEVER-O1', 'Fiebre alta (>39.5°C) con alteración del estado mental', 'ORANGE', true, 'M006');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D041', 'FEVER-O2', 'Fiebre en paciente inmunocomprometido', 'ORANGE', true, 'M006');

-- YELLOW (Urgent - 60 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D042', 'FEVER-Y1', 'Fiebre alta (>39°C) persistente', 'YELLOW', true, 'M006');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D043', 'FEVER-Y2', 'Fiebre con dolor abdominal o torácico', 'YELLOW', true, 'M006');

-- GREEN (Less urgent - 120 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D044', 'FEVER-G1', 'Fiebre moderada sin otros síntomas', 'GREEN', true, 'M006');

-- BLUE (Non-urgent - 240 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D045', 'FEVER-B1', 'Fiebre leve (<38.5°C) sin complicaciones', 'BLUE', true, 'M006');

-- =====================================================
-- Manchester Discriminators for Altered Mental Status (M007)
-- =====================================================

-- RED (Immediate - 0 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D046', 'AMS-R1', 'Coma o no responde a estímulos', 'RED', true, 'M007');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D047', 'AMS-R2', 'Convulsiones activas', 'RED', true, 'M007');

-- ORANGE (Very urgent - 10 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D048', 'AMS-O1', 'Confusión aguda con signos neurológicos focales', 'ORANGE', true, 'M007');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D049', 'AMS-O2', 'Alteración del estado mental post-convulsión', 'ORANGE', true, 'M007');

-- YELLOW (Urgent - 60 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D050', 'AMS-Y1', 'Confusión de inicio reciente', 'YELLOW', true, 'M007');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D051', 'AMS-Y2', 'Desorientación con historia de trauma', 'YELLOW', true, 'M007');

-- GREEN (Less urgent - 120 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D052', 'AMS-G1', 'Alteración leve del estado mental', 'GREEN', true, 'M007');

-- BLUE (Non-urgent - 240 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D053', 'AMS-B1', 'Cambios cognitivos crónicos estables', 'BLUE', true, 'M007');

-- =====================================================
-- Manchester Discriminators for Allergic Reaction (M008)
-- =====================================================

-- RED (Immediate - 0 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D054', 'ALLERGY-R1', 'Anafilaxia con compromiso respiratorio', 'RED', true, 'M008');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D055', 'ALLERGY-R2', 'Angioedema con obstrucción de vía aérea', 'RED', true, 'M008');

-- ORANGE (Very urgent - 10 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D056', 'ALLERGY-O1', 'Reacción alérgica con disnea o estridor', 'ORANGE', true, 'M008');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D057', 'ALLERGY-O2', 'Urticaria generalizada con hipotensión', 'ORANGE', true, 'M008');

-- YELLOW (Urgent - 60 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D058', 'ALLERGY-Y1', 'Reacción alérgica con urticaria extensa', 'YELLOW', true, 'M008');

INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D059', 'ALLERGY-Y2', 'Angioedema facial sin compromiso respiratorio', 'YELLOW', true, 'M008');

-- GREEN (Less urgent - 120 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D060', 'ALLERGY-G1', 'Reacción alérgica leve localizada', 'GREEN', true, 'M008');

-- BLUE (Non-urgent - 240 minutes)
INSERT INTO clinical_schema.manchester_discriminators (id, code, description, priority_level, active, motif_id)
VALUES ('D061', 'ALLERGY-B1', 'Prurito leve sin otros síntomas', 'BLUE', true, 'M008');

-- =====================================================
-- End of seed data
-- Total: 8 motifs, 61 discriminators
-- =====================================================
