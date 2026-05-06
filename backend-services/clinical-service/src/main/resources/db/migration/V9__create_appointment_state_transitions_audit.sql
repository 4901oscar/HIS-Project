-- =====================================================
-- Clinical Service Database Migration
-- Version: 9
-- Description: Creates audit table for appointment state transitions
-- =====================================================

-- Create appointment_state_transitions audit table
CREATE TABLE clinical_schema.appointment_state_transitions (
    id VARCHAR(36) PRIMARY KEY,
    appointment_id VARCHAR(36) NOT NULL,
    from_state VARCHAR(50) NOT NULL,
    to_state VARCHAR(50) NOT NULL,
    transitioned_by VARCHAR(36) NOT NULL,
    transitioned_at TIMESTAMP NOT NULL,
    notes TEXT,
    CONSTRAINT fk_state_transition_appointment 
        FOREIGN KEY (appointment_id) 
        REFERENCES clinical_schema.appointments(id) 
        ON DELETE CASCADE
);

-- Index for appointment lookup (most common query)
CREATE INDEX idx_state_transitions_appointment 
    ON clinical_schema.appointment_state_transitions(appointment_id);

-- Index for time-based queries
CREATE INDEX idx_state_transitions_timestamp 
    ON clinical_schema.appointment_state_transitions(transitioned_at);

-- Index for user audit queries
CREATE INDEX idx_state_transitions_user 
    ON clinical_schema.appointment_state_transitions(transitioned_by);

-- Add comments for documentation
COMMENT ON TABLE clinical_schema.appointment_state_transitions IS 'Audit trail for all appointment state transitions';
COMMENT ON COLUMN clinical_schema.appointment_state_transitions.appointment_id IS 'Reference to the appointment';
COMMENT ON COLUMN clinical_schema.appointment_state_transitions.from_state IS 'Previous state before transition';
COMMENT ON COLUMN clinical_schema.appointment_state_transitions.to_state IS 'New state after transition';
COMMENT ON COLUMN clinical_schema.appointment_state_transitions.transitioned_by IS 'User ID who triggered the transition';
COMMENT ON COLUMN clinical_schema.appointment_state_transitions.transitioned_at IS 'Timestamp when transition occurred';
COMMENT ON COLUMN clinical_schema.appointment_state_transitions.notes IS 'Optional notes about the transition';
