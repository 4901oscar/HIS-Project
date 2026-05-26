-- V17: Add standard audit columns (created_at, created_by, updated_at, updated_by) to all clinical_schema tables
-- Existing semantic columns (recorded_at, performed_at, ordered_at, issued_at, transitioned_at, etc.) are preserved
DO $$
BEGIN

    -- ── appointments ──────────────────────────────────────────────────────────
    -- has: created_at, created_by — add: updated_at, updated_by
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'appointments' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.appointments ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'appointments' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.appointments ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── vital_signs ───────────────────────────────────────────────────────────
    -- has: recorded_at, recorded_by (semantic) — add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'vital_signs' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE clinical_schema.vital_signs ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'vital_signs' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.vital_signs ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'vital_signs' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.vital_signs ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'vital_signs' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.vital_signs ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── triages ───────────────────────────────────────────────────────────────
    -- has: performed_at, performed_by (semantic) — add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'triages' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE clinical_schema.triages ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'triages' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.triages ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'triages' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.triages ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'triages' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.triages ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── consultations ─────────────────────────────────────────────────────────
    -- has: consultation_date, performed_by (semantic) — add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'consultations' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE clinical_schema.consultations ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'consultations' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.consultations ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'consultations' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.consultations ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'consultations' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.consultations ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── prescriptions ─────────────────────────────────────────────────────────
    -- has: issued_at, issued_by (semantic) — add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'prescriptions' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE clinical_schema.prescriptions ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'prescriptions' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.prescriptions ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'prescriptions' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.prescriptions ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'prescriptions' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.prescriptions ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── lab_orders ────────────────────────────────────────────────────────────
    -- has: ordered_at, ordered_by (semantic) — add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'lab_orders' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE clinical_schema.lab_orders ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'lab_orders' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.lab_orders ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'lab_orders' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.lab_orders ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'lab_orders' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.lab_orders ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── manchester_motifs ─────────────────────────────────────────────────────
    -- no audit fields — add all 4
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'manchester_motifs' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE clinical_schema.manchester_motifs ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'manchester_motifs' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.manchester_motifs ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'manchester_motifs' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.manchester_motifs ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'manchester_motifs' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.manchester_motifs ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── manchester_discriminators ─────────────────────────────────────────────
    -- no audit fields — add all 4
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'manchester_discriminators' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE clinical_schema.manchester_discriminators ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'manchester_discriminators' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.manchester_discriminators ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'manchester_discriminators' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.manchester_discriminators ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'manchester_discriminators' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.manchester_discriminators ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── doctors ───────────────────────────────────────────────────────────────
    -- has: created_at — add: created_by, updated_at, updated_by
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'doctors' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.doctors ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'doctors' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.doctors ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'doctors' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.doctors ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── doctor_availability ───────────────────────────────────────────────────
    -- has: created_at, created_by — add: updated_at, updated_by
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'doctor_availability' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.doctor_availability ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'doctor_availability' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.doctor_availability ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── appointment_state_transitions ─────────────────────────────────────────
    -- has: transitioned_at, transitioned_by (semantic) — add all 4 standard
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'appointment_state_transitions' AND column_name = 'created_at'
    ) THEN
        ALTER TABLE clinical_schema.appointment_state_transitions ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'appointment_state_transitions' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE clinical_schema.appointment_state_transitions ADD COLUMN created_by VARCHAR(36) NOT NULL DEFAULT 'SYSTEM';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'appointment_state_transitions' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE clinical_schema.appointment_state_transitions ADD COLUMN updated_at TIMESTAMP;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'clinical_schema' AND table_name = 'appointment_state_transitions' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE clinical_schema.appointment_state_transitions ADD COLUMN updated_by VARCHAR(36);
    END IF;

    -- ── clinics ───────────────────────────────────────────────────────────────
    -- already has all 4 — nothing to add

END $$;