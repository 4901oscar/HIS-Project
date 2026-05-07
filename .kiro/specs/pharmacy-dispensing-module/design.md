# Design Document: Pharmacy Dispensing Module

## Overview

The Pharmacy Dispensing Module completes the medication dispensing workflow for internal pharmacy operations. This module provides pharmacists with a queue-based interface to view paid prescriptions awaiting dispensing, access detailed prescription information, and mark medications as dispensed when handed to patients.

### Design Goals

1. **Workflow Completion**: Close the gap in the pharmacy workflow by providing the final dispensing step
2. **Consistency**: Follow established patterns from Lab Sample Workflow and Cashier modules for familiar UX
3. **Simplicity**: Provide a straightforward queue → detail → dispense flow without unnecessary complexity
4. **Integration**: Seamlessly integrate with existing appointment status transitions and prescription management

### Key Design Decisions

- **Queue-Based Interface**: Similar to LabSampleWorkflow and CashierPage, use a list-based queue showing pending prescriptions
- **Two-View Pattern**: Main queue view + detail view (not a multi-step wizard like Lab, since dispensing is a single action)
- **Status-Driven**: Leverage existing PHARMACY appointment status to filter the queue
- **Prescription Retrieval**: Add new endpoint to fetch prescription by appointmentId (prescriptions are linked via consultation → appointment)
- **Domain Method**: Use existing `dispenseMedication()` method on Appointment entity for status transition
- **Role-Based Access**: Restrict access to users with PHARMACY role

## Architecture

### System Context

```
┌─────────────────────────────────────────────────────────────────┐
│                     Pharmacy Dispensing Module                   │
│                                                                   │
│  ┌──────────────────┐              ┌──────────────────────────┐ │
│  │  PharmacyPage    │              │  AppointmentController   │ │
│  │  (React)         │─────────────▶│  (Spring Boot)           │ │
│  │                  │   HTTP/REST  │                          │ │
│  │  - Queue View    │              │  GET /appointments       │ │
│  │  - Detail View   │              │    ?queue=pharmacy       │ │
│  │  - Dispense      │              │                          │ │
│  │    Action        │              │  GET /appointments/      │ │
│  └──────────────────┘              │    {id}/prescription     │ │
│                                     │                          │ │
│                                     │  PATCH /appointments/    │ │
│                                     │    {id}/dispense-        │ │
│                                     │    medication            │ │
│                                     └──────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                          │
                          │ Uses existing
                          ▼
        ┌─────────────────────────────────────┐
        │   Existing Domain Layer             │
        │                                     │
        │  - Appointment.dispenseMedication() │
        │  - AppointmentRepository            │
        │  - PrescriptionRepository           │
        │  - ConsultationRepository           │
        │  - PatientServiceClient             │
        │  - DoctorRepository                 │
        └─────────────────────────────────────┘
```

### Component Interaction Flow

```
User Action: View Queue
  │
  ├─▶ PharmacyPage.loadQueue()
  │     │
  │     └─▶ GET /api/clinical/appointments?queue=pharmacy
  │           │
  │           └─▶ AppointmentController.listAppointments()
  │                 │
  │                 └─▶ Filter appointments with status=PHARMACY
  │                       │
  │                       └─▶ Return AppointmentListItem[]

User Action: View Prescription Details
  │
  ├─▶ PharmacyPage.selectAppointment(id)
  │     │
  │     └─▶ GET /api/clinical/appointments/{id}/prescription
  │           │
  │           └─▶ AppointmentController.getPrescriptionByAppointment()
  │                 │
  │                 ├─▶ Find Consultation by appointmentId
  │                 │
  │                 ├─▶ Find Prescription by consultationId
  │                 │
  │                 ├─▶ Fetch Patient details
  │                 │
  │                 ├─▶ Fetch Doctor details
  │                 │
  │                 └─▶ Return PrescriptionDetailResponse

User Action: Dispense Medication
  │
  ├─▶ PharmacyPage.dispenseMedication(appointmentId)
  │     │
  │     └─▶ PATCH /api/clinical/appointments/{id}/dispense-medication
  │           │
  │           └─▶ AppointmentController.dispenseMedication()
  │                 │
  │                 ├─▶ Find Appointment by id
  │                 │
  │                 ├─▶ Validate status == PHARMACY
  │                 │
  │                 ├─▶ appointment.dispenseMedication()
  │                 │     │
  │                 │     └─▶ Transition: PHARMACY → COMPLETED
  │                 │
  │                 ├─▶ appointmentRepository.save(appointment)
  │                 │
  │                 └─▶ Return 200 OK
```

## Components and Interfaces

### Frontend Components

#### 1. PharmacyPage (Main Container)

**Location**: `frontend-medflow/src/pages/pharmacy/PharmacyPage.tsx`

**Responsibilities**:
- Manage application state (queue data, selected appointment, loading states, errors)
- Fetch pharmacy queue on mount and after dispensing
- Handle navigation between queue view and detail view
- Coordinate child components

**State**:
```typescript
interface PharmacyPageState {
  appointments: AppointmentListItem[];
  selectedAppointment: AppointmentListItem | null;
  prescriptionDetails: PrescriptionDetailResponse | null;
  loading: boolean;
  loadingPrescription: boolean;
  dispensing: boolean;
  error: string | null;
  view: 'queue' | 'detail';
}
```

**Key Methods**:
- `loadQueue()`: Fetch appointments with status PHARMACY
- `selectAppointment(id)`: Load prescription details and switch to detail view
- `dispenseMedication(appointmentId)`: Call dispense endpoint and refresh queue
- `returnToQueue()`: Switch back to queue view

**Pattern**: Similar to LabSampleWorkflow container, but simpler (no wizard steps)

#### 2. PharmacyQueue (Queue View Component)

**Location**: `frontend-medflow/src/components/pharmacy/PharmacyQueue.tsx`

**Responsibilities**:
- Display table of pending prescriptions
- Handle row selection
- Provide refresh button
- Show empty state when no prescriptions

**Props**:
```typescript
interface PharmacyQueueProps {
  appointments: AppointmentListItem[];
  loading: boolean;
  onSelect: (appointment: AppointmentListItem) => void;
  onRefresh: () => void;
}
```

**Display Columns**:
- Patient Name
- DPI
- Appointment Date
- Appointment Time
- Prescription Code
- Action (View Details button)

**Pattern**: Similar to CashierPage appointment tables

#### 3. PrescriptionDetail (Detail View Component)

**Location**: `frontend-medflow/src/components/pharmacy/PrescriptionDetail.tsx`

**Responsibilities**:
- Display patient information
- Display prescription metadata (code, doctor, date)
- Display medication list with all details
- Provide "Dispensar Medicamentos" button
- Handle dispense action
- Show loading/error states

**Props**:
```typescript
interface PrescriptionDetailProps {
  appointment: AppointmentListItem;
  prescription: PrescriptionDetailResponse;
  onDispense: (appointmentId: string) => Promise<void>;
  onBack: () => void;
  dispensing: boolean;
}
```

**Sections**:
1. Patient Information Card
2. Prescription Metadata Card
3. Medications Table
4. Action Buttons (Dispense, Back)

**Pattern**: Similar to LabSampleWorkflow step components, but single view

### Backend Endpoints

#### 1. GET /api/clinical/appointments?queue=pharmacy

**Purpose**: Fetch all appointments in PHARMACY status

**Implementation**: Already exists in AppointmentController.listAppointments()

**Query Parameters**:
- `queue=pharmacy` (filters for PHARMACY status)

**Response**: `List<AppointmentListItemResponse>`

**Status Codes**:
- 200: Success

**Notes**: No new implementation needed, existing endpoint supports this filter

#### 2. GET /api/clinical/appointments/{appointmentId}/prescription

**Purpose**: Retrieve prescription details for a specific appointment

**Implementation**: New method in AppointmentController

**Path Parameters**:
- `appointmentId`: The appointment ID

**Response**: `PrescriptionDetailResponse`

**Status Codes**:
- 200: Success
- 404: No prescription found for appointment
- 403: User lacks PHARMACY role

**Business Logic**:
1. Validate user has PHARMACY role
2. Find appointment by ID
3. Find consultation by appointmentId
4. Find prescription by consultationId
5. Fetch patient details from patient-service
6. Fetch doctor details from DoctorRepository
7. Map to PrescriptionDetailResponse

**Error Handling**:
- If appointment not found → 404 "Cita no encontrada"
- If consultation not found → 404 "No se encontró consulta para esta cita"
- If prescription not found → 404 "No se encontró receta para esta cita"
- If patient-service unavailable → 503 "Servicio de pacientes no disponible"

#### 3. PATCH /api/clinical/appointments/{id}/dispense-medication

**Purpose**: Mark medications as dispensed and transition appointment to COMPLETED

**Implementation**: New method in AppointmentController

**Path Parameters**:
- `id`: The appointment ID

**Request Body**: None (empty PATCH)

**Response**: 200 OK (no body)

**Status Codes**:
- 200: Success
- 400: Invalid appointment status
- 404: Appointment not found
- 403: User lacks PHARMACY role

**Business Logic**:
1. Validate user has PHARMACY role
2. Find appointment by ID
3. Validate appointment.status == PHARMACY
4. Call appointment.dispenseMedication()
5. Save appointment
6. Return success

**Error Handling**:
- If appointment not found → 404 "Cita no encontrada"
- If status != PHARMACY → 400 "Solo se pueden dispensar medicamentos para citas en estado PHARMACY"
- If domain method throws → 400 with domain exception message

### Service Layer

#### AppointmentService (Existing)

**No changes needed**: Existing methods handle appointment retrieval and persistence

#### PrescriptionService (New Methods)

**Location**: Create new service or add to existing ConsultationService

**Method**: `getPrescriptionByAppointmentId(String appointmentId)`

**Responsibilities**:
1. Find consultation by appointmentId
2. Find prescription by consultationId
3. Return prescription or throw exception

**Implementation**:
```java
public Prescription getPrescriptionByAppointmentId(String appointmentId) {
    // 1. Find consultation
    Consultation consultation = consultationRepository
        .findByAppointmentId(appointmentId)
        .orElseThrow(() -> new NotFoundException(
            "No se encontró consulta para esta cita"));
    
    // 2. Find prescription
    List<Prescription> prescriptions = prescriptionRepository
        .findByConsultationId(consultation.getId());
    
    if (prescriptions.isEmpty()) {
        throw new NotFoundException(
            "No se encontró receta para esta cita");
    }
    
    // Return most recent prescription
    return prescriptions.get(0);
}
```

**Note**: Requires adding new repository methods:
- `ConsultationRepository.findByAppointmentId(String appointmentId)`
- `PrescriptionRepository.findByConsultationId(String consultationId)`

## Data Models

### Frontend DTOs

#### PrescriptionDetailResponse

```typescript
export interface PrescriptionDetailResponse {
  // Prescription metadata
  id: string;
  prescriptionCode: string;
  status: 'PENDING' | 'DISPENSED' | 'CANCELLED';
  issuedAt: string; // ISO 8601 datetime
  
  // Patient information
  patient: {
    id: string;
    fullName: string;
    dpi?: string;
    phone?: string;
    email?: string;
  };
  
  // Doctor information
  doctor: {
    id: string;
    name: string;
    specialty?: string;
  };
  
  // Medications
  medications: MedicationItem[];
}

export interface MedicationItem {
  name: string;
  dosage: string;
  frequency: string;
  durationDays: number;
  route: string;
  specialInstructions?: string;
  
  // Optional calculated fields for internal pharmacy
  dosageAmount?: number;
  dosageUnit?: string;
  frequencyHours?: number;
  totalQuantity?: number;
}
```

### Backend DTOs

#### PrescriptionDetailResponse (Java)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDetailResponse {
    private String id;
    private String prescriptionCode;
    private String status;
    private LocalDateTime issuedAt;
    private PatientInfo patient;
    private DoctorInfo doctor;
    private List<MedicationItemResponse> medications;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientInfo {
        private String id;
        private String fullName;
        private String dpi;
        private String phone;
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorInfo {
        private String id;
        private String name;
        private String specialty;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationItemResponse {
        private String name;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String route;
        private String specialInstructions;
        private Double dosageAmount;
        private String dosageUnit;
        private Integer frequencyHours;
        private Double totalQuantity;
    }
}
```

### Database Schema

**No schema changes required**. All necessary tables and relationships already exist:

- `appointments` table (has `status` column)
- `consultations` table (has `appointment_id` foreign key)
- `prescriptions` table (has `consultation_id` foreign key)
- `prescription_medications` table (stores medication details)

### Repository Extensions

#### ConsultationRepository

**Add method**:
```java
Optional<Consultation> findByAppointmentId(String appointmentId);
```

**JPA Implementation**:
```java
// In JpaConsultationRepository
Optional<ConsultationEntity> findByAppointmentId(String appointmentId);
```

#### PrescriptionRepository

**Add method**:
```java
List<Prescription> findByConsultationId(String consultationId);
```

**JPA Implementation**:
```java
// In JpaPrescriptionRepository
List<PrescriptionEntity> findByConsultationId(String consultationId);
```

**Note**: JpaPrescriptionRepository already has this method based on grep search results

## Error Handling

### Frontend Error Handling

#### Queue Loading Errors

**Scenario**: GET /appointments?queue=pharmacy fails

**Handling**:
- Display error message: "Error al cargar la cola de farmacia"
- Provide "Reintentar" button
- Log error to console

**UI State**:
```typescript
{
  loading: false,
  error: "Error al cargar la cola de farmacia",
  appointments: []
}
```

#### Prescription Loading Errors

**Scenario**: GET /appointments/{id}/prescription fails

**Handling**:
- Display error message: "Error al cargar los detalles de la receta"
- Provide "Reintentar" and "Volver a la cola" buttons
- Log error to console

**Status-Specific Messages**:
- 404: "No se encontró receta para esta cita"
- 403: "No tiene permisos para ver esta receta"
- 500: "Error al cargar los detalles de la receta"

#### Dispensing Errors

**Scenario**: PATCH /appointments/{id}/dispense-medication fails

**Handling**:
- Display error message from backend or generic message
- Keep user on detail view
- Re-enable "Dispensar Medicamentos" button

**Status-Specific Messages**:
- 400: Display backend error message (e.g., "Solo se pueden dispensar medicamentos para citas en estado PHARMACY")
- 404: "Cita no encontrada"
- 403: "No tiene permisos para dispensar medicamentos"
- 500: "Error al dispensar medicamentos. Por favor, intente nuevamente."

### Backend Error Handling

#### Validation Errors

**Scenario**: Invalid appointment status for dispensing

**Response**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Solo se pueden dispensar medicamentos para citas en estado PHARMACY",
  "timestamp": "2026-04-27T10:30:00Z"
}
```

#### Not Found Errors

**Scenario**: Appointment, consultation, or prescription not found

**Response**:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No se encontró receta para esta cita",
  "timestamp": "2026-04-27T10:30:00Z"
}
```

#### Service Unavailable Errors

**Scenario**: Patient-service is down

**Response**:
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Servicio de pacientes no disponible",
  "timestamp": "2026-04-27T10:30:00Z"
}
```

**Handling**: Use @ControllerAdvice for consistent error responses

## Testing Strategy

### Unit Tests

#### Frontend Unit Tests

**PharmacyPage Component**:
- Test queue loading on mount
- Test appointment selection
- Test dispense success flow
- Test dispense error handling
- Test navigation between views
- Test refresh functionality

**PharmacyQueue Component**:
- Test rendering with appointments
- Test empty state
- Test row selection
- Test refresh button

**PrescriptionDetail Component**:
- Test rendering prescription details
- Test medication list display
- Test dispense button enabled/disabled states
- Test back navigation

**Test Framework**: React Testing Library + Jest

**Example Test**:
```typescript
describe('PharmacyPage', () => {
  it('should load pharmacy queue on mount', async () => {
    const mockAppointments = [/* mock data */];
    jest.spyOn(appointmentService, 'listAppointments')
      .mockResolvedValue(mockAppointments);
    
    render(<PharmacyPage />);
    
    await waitFor(() => {
      expect(screen.getByText(mockAppointments[0].patient.fullName))
        .toBeInTheDocument();
    });
  });
});
```

#### Backend Unit Tests

**AppointmentController**:
- Test getPrescriptionByAppointment success
- Test getPrescriptionByAppointment with missing prescription
- Test dispenseMedication success
- Test dispenseMedication with invalid status
- Test role-based access control

**PrescriptionService**:
- Test getPrescriptionByAppointmentId success
- Test getPrescriptionByAppointmentId with missing consultation
- Test getPrescriptionByAppointmentId with missing prescription

**Test Framework**: JUnit 5 + Mockito

**Example Test**:
```java
@Test
void shouldDispenseMedicationWhenStatusIsPharmacy() {
    // Given
    String appointmentId = "appt-123";
    Appointment appointment = new Appointment();
    appointment.setId(appointmentId);
    appointment.setStatus(AppointmentStatus.PHARMACY);
    
    when(appointmentRepository.findById(appointmentId))
        .thenReturn(Optional.of(appointment));
    
    // When
    ResponseEntity<Void> response = controller.dispenseMedication(appointmentId);
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
    verify(appointmentRepository).save(appointment);
}
```

### Integration Tests

**Pharmacy Queue Flow**:
1. Create appointment with PHARMACY status
2. Call GET /appointments?queue=pharmacy
3. Verify appointment appears in response

**Prescription Retrieval Flow**:
1. Create appointment → consultation → prescription
2. Call GET /appointments/{id}/prescription
3. Verify prescription details returned correctly

**Dispensing Flow**:
1. Create appointment with PHARMACY status
2. Call PATCH /appointments/{id}/dispense-medication
3. Verify appointment status changed to COMPLETED
4. Verify appointment no longer appears in pharmacy queue

**Test Framework**: Spring Boot Test + TestRestTemplate

### End-to-End Tests

**Complete Dispensing Workflow**:
1. Pharmacist logs in
2. Navigates to /pharmacy
3. Sees list of pending prescriptions
4. Clicks on a prescription
5. Views prescription details
6. Clicks "Dispensar Medicamentos"
7. Sees success message
8. Returns to queue
9. Verifies prescription no longer in queue

**Test Framework**: Cypress or Playwright

## Integration Points

### Existing Appointment Flow Integration

**Status Transition Chain**:
```
CONSULTATION
  ↓ (doctor prescribes internal pharmacy meds)
PENDING_PHARMACY_PAYMENT
  ↓ (cashier confirms payment)
PHARMACY ← [Pharmacy Queue shows appointments here]
  ↓ (pharmacist dispenses medications)
COMPLETED
```

**Integration Method**: Use existing `confirmPharmacyPayment()` to transition to PHARMACY, then new `dispenseMedication()` to transition to COMPLETED

### Patient Service Integration

**Purpose**: Fetch patient details (name, DPI, phone, email) for prescription detail view

**Method**: `PatientServiceClient.getPatientById(String patientId)`

**Error Handling**: If patient-service unavailable, show partial data with placeholder for patient details

### Doctor Repository Integration

**Purpose**: Fetch doctor name and specialty for prescription detail view

**Method**: `DoctorRepository.findById(String doctorId)`

**Error Handling**: If doctor not found, show "Dr. Asignado" as fallback

### Billing Service Integration

**No direct integration needed**: Payment is already confirmed before appointment reaches PHARMACY status

### Lab Service Integration

**No integration needed**: Pharmacy and Lab are independent workflows

## Security Considerations

### Role-Based Access Control

**Required Role**: PHARMACY

**Enforcement Points**:
- Frontend: Route guard on /pharmacy path
- Backend: @PreAuthorize("hasRole('PHARMACY')") on controller methods

**Implementation**:
```java
@GetMapping("/{appointmentId}/prescription")
@PreAuthorize("hasRole('PHARMACY')")
public ResponseEntity<PrescriptionDetailResponse> getPrescriptionByAppointment(
    @PathVariable String appointmentId) {
    // ...
}

@PatchMapping("/{id}/dispense-medication")
@PreAuthorize("hasRole('PHARMACY')")
public ResponseEntity<Void> dispenseMedication(@PathVariable String id) {
    // ...
}
```

### Data Privacy

**Sensitive Data**: Patient information, prescription details

**Protection**:
- HTTPS for all API calls
- Role-based access (only PHARMACY role can access)
- No logging of sensitive patient data
- Audit trail for dispensing actions (via appointment status transitions)

### Input Validation

**Appointment ID**: Validate format (UUID or alphanumeric)

**Status Validation**: Verify appointment status == PHARMACY before dispensing

**Authorization**: Verify user has PHARMACY role

## Performance Considerations

### Frontend Performance

**Queue Loading**:
- Expected queue size: 10-50 appointments
- Load time target: < 500ms
- Optimization: Use pagination if queue exceeds 100 items

**Prescription Detail Loading**:
- Load time target: < 300ms
- Optimization: Cache prescription details for recently viewed appointments

**UI Responsiveness**:
- Debounce refresh button (prevent spam clicking)
- Show loading spinners for all async operations
- Disable action buttons during operations

### Backend Performance

**Query Optimization**:
- Index on `appointments.status` column (already exists)
- Index on `consultations.appointment_id` column (add if not exists)
- Index on `prescriptions.consultation_id` column (add if not exists)

**N+1 Query Prevention**:
- Fetch patient and doctor details in parallel
- Use batch queries if multiple prescriptions loaded

**Caching**:
- Cache doctor details (rarely change)
- No caching for appointments or prescriptions (real-time data)

## Deployment Considerations

### Frontend Deployment

**Build**: Standard React build process (npm run build)

**Assets**: Bundle includes new PharmacyPage and components

**Routing**: Add /pharmacy route to App.tsx

**Environment Variables**: None required (uses existing API base URL)

### Backend Deployment

**Database Migration**: Add new repository methods (no schema changes)

**API Versioning**: New endpoints under existing /api/clinical/appointments path

**Backward Compatibility**: No breaking changes to existing endpoints

**Rollback Plan**: New endpoints can be disabled without affecting existing functionality

### Configuration

**Frontend**:
- No new configuration required

**Backend**:
- No new configuration required
- Uses existing Spring Security role configuration

## Monitoring and Observability

### Metrics to Track

**Frontend**:
- Pharmacy queue load time
- Prescription detail load time
- Dispense operation success rate
- Error rate by error type

**Backend**:
- GET /appointments?queue=pharmacy response time
- GET /appointments/{id}/prescription response time
- PATCH /appointments/{id}/dispense-medication response time
- Error rate by endpoint
- Appointments in PHARMACY status (queue depth)

### Logging

**Frontend**:
- Log all API errors to console
- Log dispense operations (success/failure)

**Backend**:
- Log all prescription retrievals: `log.info("Fetching prescription for appointment {}", appointmentId)`
- Log all dispense operations: `log.info("Dispensing medication for appointment {}", appointmentId)`
- Log errors with full stack trace: `log.error("Error dispensing medication", exception)`

**Log Format**: Use existing SLF4J logging configuration

### Alerts

**Critical Alerts**:
- Prescription retrieval error rate > 5%
- Dispense operation error rate > 5%
- Patient-service unavailable

**Warning Alerts**:
- Pharmacy queue depth > 50 appointments
- Average dispense time > 2 minutes

## Future Enhancements

### Phase 2 Enhancements (Not in Current Scope)

1. **Prescription Status Tracking**: Update prescription.status to DISPENSED when medications are dispensed
2. **Partial Dispensing**: Allow dispensing subset of medications if some are out of stock
3. **Inventory Integration**: Check medication availability before dispensing
4. **Barcode Scanning**: Scan medication barcodes to verify correct items
5. **Patient Signature**: Capture digital signature on tablet when patient receives medications
6. **Dispensing Notes**: Allow pharmacist to add notes (e.g., "Patient counseled on side effects")
7. **Print Labels**: Generate medication labels with patient name, dosage, instructions
8. **Medication Substitution**: Handle generic substitutions with doctor approval
9. **Refill Management**: Track refills for chronic medications
10. **Analytics Dashboard**: Show dispensing metrics, average wait time, most prescribed medications

### Technical Debt to Address

1. **Add findByAppointmentId to ConsultationRepository**: Currently missing, needed for prescription retrieval
2. **Add findByConsultationId to PrescriptionRepository**: May already exist (verify), needed for prescription retrieval
3. **Standardize Error Responses**: Use consistent error DTO across all endpoints
4. **Add API Documentation**: Document new endpoints with OpenAPI/Swagger annotations
5. **Add Integration Tests**: Comprehensive tests for prescription retrieval and dispensing flows

## Appendix

### Appointment Status Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Appointment Lifecycle                         │
└─────────────────────────────────────────────────────────────────┘

PENDING_PAYMENT
    │
    │ confirmPayment()
    ▼
SCHEDULED
    │
    │ activate()
    ▼
VITAL_SIGNS
    │
    │ completeVitalSigns()
    ▼
CONSULTATION
    │
    │ registerConsultation(hasLabOrders=false, hasPrescription=true)
    ▼
PENDING_PHARMACY_PAYMENT
    │
    │ confirmPharmacyPayment()
    ▼
PHARMACY ◄─────────────────── [Pharmacy Module operates here]
    │
    │ dispenseMedication()
    ▼
COMPLETED
```

### API Endpoint Summary

| Method | Endpoint | Purpose | New/Existing |
|--------|----------|---------|--------------|
| GET | /api/clinical/appointments?queue=pharmacy | Fetch pharmacy queue | Existing |
| GET | /api/clinical/appointments/{id}/prescription | Fetch prescription details | **New** |
| PATCH | /api/clinical/appointments/{id}/dispense-medication | Mark as dispensed | **New** |

### Component File Structure

```
frontend-medflow/
├── src/
│   ├── pages/
│   │   └── pharmacy/
│   │       └── PharmacyPage.tsx          [New]
│   ├── components/
│   │   └── pharmacy/
│   │       ├── PharmacyQueue.tsx         [New]
│   │       └── PrescriptionDetail.tsx    [New]
│   ├── services/
│   │   └── clinicalService.ts            [Extend]
│   └── App.tsx                            [Modify - add route]

backend-services/clinical-service/
├── src/main/java/com/medframe/clinical/
│   ├── infrastructure/rest/
│   │   ├── controller/
│   │   │   └── AppointmentController.java    [Extend]
│   │   └── dto/
│   │       └── response/
│   │           └── PrescriptionDetailResponse.java [New]
│   ├── domain/
│   │   └── port/out/
│   │       ├── ConsultationRepository.java   [Extend]
│   │       └── PrescriptionRepository.java   [Verify/Extend]
│   └── infrastructure/persistence/
│       ├── repository/
│       │   ├── JpaConsultationRepository.java [Extend]
│       │   └── JpaPrescriptionRepository.java [Verify]
│       └── adapter/
│           └── ConsultationRepositoryAdapter.java [Extend]
```

### References

- **Lab Sample Workflow**: `frontend-medflow/src/pages/lab/LabSampleWorkflow.tsx`
- **Cashier Page**: `frontend-medflow/src/pages/cashier/CashierPage.tsx`
- **Appointment Controller**: `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/controller/AppointmentController.java`
- **Appointment Entity**: `backend-services/clinical-service/src/main/java/com/medframe/clinical/domain/model/Appointment.java`
- **Prescription Entity**: `backend-services/clinical-service/src/main/java/com/medframe/clinical/domain/model/Prescription.java`
- **Requirements Document**: `.kiro/specs/pharmacy-dispensing-module/requirements.md`
