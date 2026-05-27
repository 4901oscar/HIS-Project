# Manual Testing Guide
## Triage-Appointment Integration

This guide provides step-by-step instructions for manually testing the triage-appointment integration endpoints.

---

## Prerequisites

### 1. Start PostgreSQL Database
```bash
# Ensure PostgreSQL is running with correct credentials
# Default: localhost:5432, user: medflow_user, password: medflow_pass
```

### 2. Start Clinical Service
```bash
cd backend-services/clinical-service
mvn spring-boot:run
```

The service will start on `http://localhost:8083`

### 3. Prepare Test Data

You'll need:
- A patient ID (e.g., `patient-123`)
- A doctor ID (e.g., `doctor-456`)
- Vital signs recorded for the patient
- Manchester triage catalog data (motifs and discriminators)

---

## Test Scenarios

### Scenario 1: Create Appointment and Perform Triage

#### Step 1.1: Create an Appointment
```bash
POST http://localhost:8083/api/clinical/appointments
Content-Type: application/json
X-User-Id: doctor-456

{
  "patientId": "patient-123",
  "doctorId": "doctor-456",
  "appointmentDate": "2026-04-25",
  "appointmentTime": "10:00",
  "notes": "Dolor de cabeza"
}
```

**Expected Response:** HTTP 200
```json
{
  "id": "appt-001",
  "patientId": "patient-123",
  "doctorId": "doctor-456",
  "appointmentDate": "2026-04-25",
  "appointmentTime": "10:00",
  "status": "SCHEDULED",
  "notes": "Dolor de cabeza",
  "createdAt": "2026-04-23T15:00:00"
}
```

**Save the appointment ID** for next steps.

#### Step 1.2: Activate the Appointment
```bash
PUT http://localhost:8083/api/clinical/appointments/appt-001/activate
X-User-Id: doctor-456
```

**Expected Response:** HTTP 200
```json
{
  "id": "appt-001",
  "status": "ACTIVE",
  ...
}
```

#### Step 1.3: Record Vital Signs
```bash
POST http://localhost:8083/api/clinical/vital-signs
Content-Type: application/json
X-User-Id: doctor-456

{
  "patientId": "patient-123",
  "temperature": 38.5,
  "heartRate": 95,
  "bloodPressureSystolic": 130,
  "bloodPressureDiastolic": 85,
  "respiratoryRate": 18,
  "oxygenSaturation": 97,
  "weight": 70.5,
  "height": 1.75
}
```

**Expected Response:** HTTP 200

#### Step 1.4: Perform Triage with Appointment Link
```bash
POST http://localhost:8083/api/clinical/triage
Content-Type: application/json
X-User-Id: doctor-456

{
  "appointmentId": "appt-001",
  "patientId": "patient-123",
  "motifId": "motif-headache",
  "discriminatorIds": ["disc-severe-pain", "disc-recent-onset"]
}
```

**Expected Response:** HTTP 200
```json
{
  "id": "triage-001",
  "patientId": "patient-123",
  "priorityLevel": "YELLOW",
  "priorityDescription": "Urgente - Espera máxima 60 minutos",
  "maxWaitTimeMinutes": 60,
  "performedAt": "2026-04-23T15:05:00"
}
```

✅ **Verification:** Triage created successfully with appointmentId link.

---

### Scenario 2: Query Triage by Appointment

#### Step 2.1: Get Triage for Appointment
```bash
GET http://localhost:8083/api/clinical/appointments/appt-001/triage
X-User-Id: doctor-456
```

**Expected Response:** HTTP 200
```json
{
  "id": "triage-001",
  "patientId": "patient-123",
  "priorityLevel": "YELLOW",
  "priorityDescription": "Urgente - Espera máxima 60 minutos",
  "maxWaitTimeMinutes": 60,
  "performedAt": "2026-04-23T15:05:00"
}
```

✅ **Verification:** Triage retrieved successfully by appointment ID.

#### Step 2.2: Query Non-existent Triage
```bash
GET http://localhost:8083/api/clinical/appointments/appt-999/triage
X-User-Id: doctor-456
```

**Expected Response:** HTTP 404
```json
{
  "timestamp": "2026-04-23T15:10:00",
  "status": 404,
  "error": "Not Found",
  "message": "No triage found for this appointment",
  "path": "/api/clinical/appointments/appt-999/triage"
}
```

✅ **Verification:** Proper 404 error when triage doesn't exist.

---

### Scenario 3: List Pending Triage Appointments

#### Step 3.1: Create Multiple Appointments
Create 3 appointments and activate them (repeat Step 1.1 and 1.2 three times with different IDs).

#### Step 3.2: Perform Triage on One Appointment
Perform triage on only one of the appointments (Step 1.4).

#### Step 3.3: List Pending Triage Appointments
```bash
GET http://localhost:8083/api/clinical/appointments/pending-triage
X-User-Id: doctor-456
```

**Expected Response:** HTTP 200
```json
[
  {
    "id": "appt-002",
    "patientId": "patient-124",
    "doctorId": "doctor-456",
    "appointmentDate": "2026-04-25",
    "appointmentTime": "10:30",
    "status": "ACTIVE",
    "notes": "Fiebre",
    "createdAt": "2026-04-23T15:15:00"
  },
  {
    "id": "appt-003",
    "patientId": "patient-125",
    "doctorId": "doctor-456",
    "appointmentDate": "2026-04-25",
    "appointmentTime": "11:00",
    "status": "ACTIVE",
    "notes": "Tos",
    "createdAt": "2026-04-23T15:20:00"
  }
]
```

✅ **Verification:** Only ACTIVE appointments without triage are returned.

#### Step 3.4: Verify Empty List
Perform triage on all remaining appointments, then query again:

```bash
GET http://localhost:8083/api/clinical/appointments/pending-triage
X-User-Id: doctor-456
```

**Expected Response:** HTTP 200
```json
[]
```

✅ **Verification:** Empty array when no pending appointments.

---

### Scenario 4: Validation Error Cases

#### Step 4.1: Missing appointmentId
```bash
POST http://localhost:8083/api/clinical/triage
Content-Type: application/json
X-User-Id: doctor-456

{
  "patientId": "patient-123",
  "motifId": "motif-headache",
  "discriminatorIds": ["disc-severe-pain"]
}
```

**Expected Response:** HTTP 400
```json
{
  "timestamp": "2026-04-23T15:25:00",
  "status": 400,
  "error": "Bad Request",
  "message": "appointmentId es requerido",
  "path": "/api/clinical/triage"
}
```

✅ **Verification:** Validation error when appointmentId is missing.

#### Step 4.2: Non-existent Appointment
```bash
POST http://localhost:8083/api/clinical/triage
Content-Type: application/json
X-User-Id: doctor-456

{
  "appointmentId": "appt-999",
  "patientId": "patient-123",
  "motifId": "motif-headache",
  "discriminatorIds": ["disc-severe-pain"]
}
```

**Expected Response:** HTTP 404
```json
{
  "timestamp": "2026-04-23T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Appointment not found",
  "path": "/api/clinical/triage"
}
```

✅ **Verification:** Error when appointment doesn't exist.

#### Step 4.3: Appointment Not ACTIVE
Create a SCHEDULED appointment (don't activate it), then try to perform triage:

```bash
POST http://localhost:8083/api/clinical/triage
Content-Type: application/json
X-User-Id: doctor-456

{
  "appointmentId": "appt-scheduled",
  "patientId": "patient-123",
  "motifId": "motif-headache",
  "discriminatorIds": ["disc-severe-pain"]
}
```

**Expected Response:** HTTP 400
```json
{
  "timestamp": "2026-04-23T15:35:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Appointment must be in ACTIVE status for triage",
  "path": "/api/clinical/triage"
}
```

✅ **Verification:** Error when appointment is not ACTIVE.

#### Step 4.4: Duplicate Triage
Try to perform triage on an appointment that already has triage:

```bash
POST http://localhost:8083/api/clinical/triage
Content-Type: application/json
X-User-Id: doctor-456

{
  "appointmentId": "appt-001",
  "patientId": "patient-123",
  "motifId": "motif-headache",
  "discriminatorIds": ["disc-severe-pain"]
}
```

**Expected Response:** HTTP 409
```json
{
  "timestamp": "2026-04-23T15:40:00",
  "status": 409,
  "error": "Conflict",
  "message": "Triage already exists for this appointment",
  "path": "/api/clinical/triage"
}
```

✅ **Verification:** Duplicate triage prevented with 409 Conflict.

---

## Database Verification

### Check Migration Status
```sql
-- Connect to PostgreSQL
psql -U medflow_user -d medflow_db

-- Check Flyway migration history
SELECT * FROM clinical_schema.flyway_schema_history 
WHERE version = '6';

-- Verify appointment_id column exists
\d clinical_schema.triages

-- Verify indexes exist
\di clinical_schema.idx_triages_appointment_unique
\di clinical_schema.idx_triages_appointment

-- Query pending triage appointments
SELECT a.* 
FROM clinical_schema.appointments a
WHERE a.status = 'ACTIVE'
AND NOT EXISTS (
    SELECT 1 FROM clinical_schema.triages t
    WHERE t.appointment_id = a.id
);

-- Query triage by appointment
SELECT * FROM clinical_schema.triages 
WHERE appointment_id = 'appt-001';
```

---

## Rollback Testing

### Test Rollback Procedure
```sql
-- Rollback migration V6
DROP INDEX IF EXISTS clinical_schema.idx_triages_appointment;
DROP INDEX IF EXISTS clinical_schema.idx_triages_appointment_unique;
ALTER TABLE clinical_schema.triages DROP COLUMN IF EXISTS appointment_id;

-- Verify rollback
\d clinical_schema.triages

-- Re-apply migration
-- Restart clinical service to trigger Flyway migration
```

✅ **Verification:** Rollback removes column and indexes cleanly.

---

## Performance Testing

### Test Query Performance
```sql
-- Create test data (10,000 appointments, 5,000 triages)
-- Use a script to generate test data

-- Test findByAppointmentId performance
EXPLAIN ANALYZE
SELECT * FROM clinical_schema.triages 
WHERE appointment_id = 'appt-001';

-- Expected: Index Scan using idx_triages_appointment_unique
-- Expected: Execution time < 50ms

-- Test findPendingTriage performance
EXPLAIN ANALYZE
SELECT a.* 
FROM clinical_schema.appointments a
WHERE a.status = 'ACTIVE'
AND NOT EXISTS (
    SELECT 1 FROM clinical_schema.triages t
    WHERE t.appointment_id = a.id
);

-- Expected: Uses NOT EXISTS optimization
-- Expected: Execution time < 500ms for 10,000 appointments
```

---

## Troubleshooting

### Issue: Database Connection Failed
**Solution:** Verify PostgreSQL is running and credentials are correct in `application.yml`

### Issue: Vital Signs Not Found
**Solution:** Record vital signs for the patient before performing triage

### Issue: Motif/Discriminator Not Found
**Solution:** Ensure Manchester triage catalog data is loaded in the database

### Issue: 403 Forbidden
**Solution:** Verify X-User-Id header is set and user has DOCTOR role

### Issue: Migration Not Applied
**Solution:** Check Flyway configuration and ensure database schema exists

---

## Success Criteria

All scenarios should pass with expected responses:

- ✅ Triage creation with appointmentId succeeds
- ✅ Triage query by appointmentId returns correct triage
- ✅ Pending triage query returns only ACTIVE appointments without triage
- ✅ Validation errors return appropriate HTTP status codes
- ✅ Duplicate triage prevention works (HTTP 409)
- ✅ Database migration creates column and indexes
- ✅ Query performance meets requirements (<50ms, <500ms)
- ✅ Rollback procedure works cleanly

---

*Generated by: Kiro AI Assistant*  
*Date: 2026-04-23*
