# Design Document: Frontend-Backend Integration

## Overview

This design document outlines the technical approach for integrating the React frontend with the Spring Boot microservices backend of MedFlow HIS. The integration will enable complete communication between the frontend application and backend services through the API Gateway, implementing JWT authentication, aligning TypeScript types with backend DTOs, and supporting both staff workflows and patient self-service features.

**Key Integration Points:**
- **API Gateway Communication**: Single entry point at `http://localhost:8080/api`
- **JWT Authentication Flow**: Token-based authentication with automatic header injection
- **Type Alignment**: TypeScript types matching backend DTOs exactly
- **Service Integration**: Auth, Patient, and Clinical services fully connected
- **Patient Portal**: Self-registration, appointment booking, and medical history access
- **Payment Integration**: Online payment system for appointment booking
- **Error Handling**: Consistent error responses across all operations

**Scope:**
- Configure Axios to communicate with API Gateway
- Implement JWT interceptor for automatic authentication
- Align all TypeScript types with backend DTOs
- Integrate authService, patientService, and appointmentService
- Implement patient self-registration and login
- Create patient portal with appointment and medical history views
- Implement online appointment booking with payment
- Standardize error handling across the application

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Frontend                            │
│                  (localhost:3000)                            │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Auth Context │  │ Patient      │  │ Appointment  │     │
│  │              │  │ Context      │  │ Context      │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                  │                  │              │
│  ┌──────▼──────────────────▼──────────────────▼───────┐    │
│  │           Axios Instance (with JWT Interceptor)     │    │
│  │           baseURL: http://localhost:8080/api        │    │
│  └──────────────────────────┬──────────────────────────┘    │
└─────────────────────────────┼───────────────────────────────┘
                              │
                              │ HTTPS (Production)
                              │ HTTP (Development)
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                      API Gateway                             │
│                   (localhost:8080)                           │
│                                                              │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐ │
│  │ JWT Validation │  │ Rate Limiting  │  │ CORS Config  │ │
│  └────────────────┘  └────────────────┘  └──────────────┘ │
│                                                              │
│  Routes:                                                     │
│  /api/auth/**       → Auth Service (8081)                   │
│  /api/patients/**   → Patient Service (8082)                │
│  /api/clinical/**   → Clinical Service (8083)               │
│  /api/billing/**    → Billing Service (8086)                │
└──────────────────────────────────────────────────────────────┘
```

### Component Architecture

```
frontend-medflow/
├── src/
│   ├── services/
│   │   ├── api/
│   │   │   ├── axiosInstance.ts      # Configured Axios with interceptors
│   │   │   └── errorHandler.ts       # Centralized error handling
│   │   ├── authService.ts            # Authentication operations
│   │   ├── patientService.ts         # Patient CRUD operations
│   │   ├── appointmentService.ts     # Appointment management
│   │   ├── medicalRecordService.ts   # Medical history (NEW)
│   │   └── paymentService.ts         # Payment processing (NEW)
│   ├── types/
│   │   ├── auth.ts                   # Auth DTOs aligned with backend
│   │   ├── patient.ts                # Patient DTOs aligned with backend
│   │   ├── appointment.ts            # Appointment DTOs aligned with backend
│   │   ├── medicalRecord.ts          # Medical record types (NEW)
│   │   ├── payment.ts                # Payment types (NEW)
│   │   └── common.ts                 # Shared types (ErrorResponse, etc.)
│   ├── context/
│   │   ├── AuthContext.tsx           # Authentication state management
│   │   └── PatientContext.tsx        # Patient portal state (NEW)
│   ├── hooks/
│   │   ├── useAuth.ts                # Authentication hook
│   │   ├── usePatient.ts             # Patient operations hook
│   │   └── useAppointment.ts         # Appointment operations hook
│   ├── pages/
│   │   ├── public/
│   │   │   ├── Home.tsx              # Landing page
│   │   │   ├── Register.tsx          # Patient self-registration (NEW)
│   │   │   ├── Login.tsx             # Login for patients and staff
│   │   │   └── BookAppointment.tsx   # Public appointment booking (NEW)
│   │   ├── patient/
│   │   │   ├── Dashboard.tsx         # Patient portal dashboard (NEW)
│   │   │   ├── MyAppointments.tsx    # Patient's appointments (NEW)
│   │   │   ├── MedicalHistory.tsx    # Patient's medical records (NEW)
│   │   │   └── Profile.tsx           # Patient profile management (NEW)
│   │   └── staff/
│   │       ├── Dashboard.tsx         # Staff dashboard
│   │       └── admission/
│   │           ├── PatientRegistration.tsx
│   │           └── AppointmentManagement.tsx
│   ├── components/
│   │   ├── common/
│   │   │   ├── ErrorMessage.tsx      # Standardized error display
│   │   │   ├── LoadingSpinner.tsx    # Loading indicator
│   │   │   └── ProtectedRoute.tsx    # Route protection
│   │   └── payment/
│   │       └── PaymentForm.tsx       # Payment form component (NEW)
│   └── utils/
│       ├── validation.ts             # Form validation utilities
│       └── formatters.ts             # Data formatting utilities
└── .env                              # Environment configuration
```

## Components and Interfaces

### 1. Axios Configuration

#### axiosInstance.ts

**Purpose**: Create a configured Axios instance with base URL and interceptors

**Configuration:**
```typescript
const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});
```

**Request Interceptor:**
- Retrieves JWT token from localStorage
- Adds `Authorization: Bearer <token>` header to all requests
- Skips header if token doesn't exist (for public endpoints)

**Response Interceptor:**
- Handles 401 Unauthorized → redirect to login
- Handles 403 Forbidden → show access denied message
- Handles network errors → show connection error message
- Passes successful responses through unchanged

### 2. Authentication Service

#### authService.ts

**Operations:**
- `login(credentials: LoginRequest): Promise<LoginResponse>`
- `logout(): Promise<void>`
- `register(data: RegisterRequest): Promise<RegisterResponse>`
- `refreshToken(): Promise<RefreshResponse>`
- `getCurrentUser(): UserResponse | null`
- `isAuthenticated(): boolean`

**Token Management:**
- Store JWT in localStorage with key `auth_token`
- Store user data in localStorage with key `user_data`
- Clear both on logout or 401 error
- Validate token expiration on app load

### 3. Patient Service

#### patientService.ts

**Operations:**
- `registerPatient(data: CreatePatientRequest): Promise<PatientResponse>`
- `getPatientById(id: string): Promise<PatientResponse>`
- `searchPatients(query: string): Promise<PatientResponse[]>`
- `updatePatient(id: string, data: UpdatePatientRequest): Promise<PatientResponse>`
- `getPatientByDPI(dpi: string): Promise<PatientResponse>`

**Validation:**
- DPI format: 13 digits
- Phone format: 8 digits
- Email format: standard email regex
- Birth date: not in future, at least 1 day old

### 4. Appointment Service

#### appointmentService.ts

**Operations:**
- `getAppointments(): Promise<AppointmentResponse[]>`
- `createAppointment(data: CreateAppointmentRequest): Promise<AppointmentResponse>`
- `activateAppointment(id: string): Promise<void>`
- `cancelAppointment(id: string): Promise<void>`
- `getMyAppointments(): Promise<AppointmentResponse[]>` (for patients)
- `getAvailableSlots(doctorId: string, date: string): Promise<AvailableSlotsResponse>`

**Business Rules:**
- Appointment date must be in the future
- Appointment time must be within available slots
- Patient must be authenticated to create appointment
- Payment required for online bookings

### 5. Medical Record Service (NEW)

#### medicalRecordService.ts

**Operations:**
- `getMyMedicalHistory(): Promise<MedicalHistoryResponse>`
- `getConsultationDetails(id: string): Promise<ConsultationResponse>`
- `getLabResults(id: string): Promise<LabResultResponse>`
- `downloadLabResult(id: string): Promise<Blob>`

**Access Control:**
- Only authenticated patients can access their own records
- Staff can access any patient's records with proper role

### 6. Payment Service (NEW)

#### paymentService.ts

**Operations:**
- `processPayment(data: PaymentRequest): Promise<PaymentResponse>`
- `getPaymentMethods(): Promise<PaymentMethod[]>`
- `downloadReceipt(paymentId: string): Promise<Blob>`

**Integration:**
- Payment gateway integration (Stripe/PayPal/local)
- Secure card data handling (PCI compliance)
- Payment confirmation before appointment creation

### 7. Error Handler

#### errorHandler.ts

**Purpose**: Centralized error handling for consistent user experience

**Error Mapping:**
```typescript
400 Bad Request → Show field-specific validation errors
401 Unauthorized → Redirect to login, show "Session expired"
403 Forbidden → Show "Access denied"
404 Not Found → Show "Resource not found"
409 Conflict → Show specific conflict message (e.g., "DPI already registered")
429 Too Many Requests → Show "Too many requests, try again later"
500 Internal Server Error → Show "Server error, contact support"
Network Error → Show "Connection error, check your network"
```

**Implementation:**
- Extract error message from response body
- Format validation errors for form display
- Log errors to console for debugging
- Show user-friendly messages in UI

## Data Models

### TypeScript Types Aligned with Backend DTOs

#### auth.ts

```typescript
// Aligned with com.medflow.auth.dto.LoginRequest
export interface LoginRequest {
  username: string;
  password: string;
}

// Aligned with com.medflow.auth.dto.LoginResponse
export interface LoginResponse {
  token: string;
  expiresIn: number;
  user: UserResponse;
}

// Aligned with com.medflow.auth.dto.UserResponse
export interface UserResponse {
  id: string;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
  active: boolean;
}

// Aligned with com.medflow.auth.dto.RegisterRequest
export interface RegisterRequest {
  dpi: string;
  nit: string;
  firstName: string;
  secondName?: string;
  firstLastName: string;
  secondLastName?: string;
  email: string;
  phone: string;
  address?: string;
  password: string;
}

// Aligned with com.medflow.auth.dto.RegisterResponse
export interface RegisterResponse {
  message: string;
  email: string;
}
```

#### patient.ts

```typescript
// Aligned with com.medflow.patient.dto.CreatePatientRequest
export interface CreatePatientRequest {
  dpi: string;
  nit: string;
  firstName: string;
  secondName?: string;
  firstLastName: string;
  secondLastName?: string;
  birthDate: string; // ISO 8601: YYYY-MM-DD
  gender: Gender;
  email: string;
  phone: string;
  department?: string;
  municipality?: string;
  zone?: string;
  address?: string;
}

// Aligned with com.medflow.patient.dto.PatientResponse
export interface PatientResponse {
  id: string;
  dpi: string;
  nit: string;
  fullName: string;
  firstName: string;
  firstLastName: string;
  birthDate: string;
  gender: string;
  email: string;
  phone: string;
  department?: string;
  municipality?: string;
  zone?: string;
  address?: string;
  authUserId?: string;
  active: boolean;
}

// Aligned with backend Gender enum
export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER'
}
```

#### appointment.ts

```typescript
// Aligned with CreateAppointmentRequest from Clinical Service
export interface CreateAppointmentRequest {
  patientId: string;
  doctorId: string;
  appointmentDate: string; // ISO 8601: YYYY-MM-DD
  appointmentTime: string; // HH:mm format
}

// Aligned with AppointmentResponse from Clinical Service
export interface AppointmentResponse {
  id: string;
  patientId: string;
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  status: AppointmentStatus;
  createdAt: string;
}

// Aligned with backend AppointmentStatus enum
export enum AppointmentStatus {
  PENDING = 'PENDING',
  ACTIVATED = 'ACTIVATED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// Aligned with AvailableSlotsResponse from Clinical Service
export interface AvailableSlotsResponse {
  doctorId: string;
  date: string;
  availableSlots: string[]; // Array of time strings (HH:mm)
}
```

#### medicalRecord.ts (NEW)

```typescript
export interface MedicalHistoryResponse {
  patient: PatientResponse;
  consultations: ConsultationSummary[];
  labOrders: LabOrderSummary[];
}

export interface ConsultationSummary {
  id: string;
  date: string;
  doctorName: string;
  diagnosis: string;
  treatment: string;
}

export interface LabOrderSummary {
  id: string;
  orderDate: string;
  testType: string;
  status: string;
  resultAvailable: boolean;
}

export interface ConsultationResponse {
  id: string;
  patientId: string;
  doctorId: string;
  consultationDate: string;
  chiefComplaint: string;
  diagnosis: string;
  treatment: string;
  prescriptions: PrescriptionSummary[];
}

export interface PrescriptionSummary {
  id: string;
  prescriptionCode: string;
  medications: MedicationDetail[];
}

export interface MedicationDetail {
  name: string;
  dosage: string;
  frequency: string;
  duration: string;
}
```

#### payment.ts (NEW)

```typescript
export interface PaymentRequest {
  appointmentId: string;
  amount: number;
  currency: string;
  paymentMethod: string;
  cardNumber: string;
  cardExpiry: string;
  cardCVV: string;
  cardholderName: string;
}

export interface PaymentResponse {
  id: string;
  appointmentId: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
  transactionId: string;
  receiptUrl: string;
  createdAt: string;
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

export interface PaymentMethod {
  id: string;
  name: string;
  type: string;
  enabled: boolean;
}
```

#### common.ts

```typescript
// Aligned with backend ErrorResponse
export interface ErrorResponse {
  error: string;
  message: string;
  timestamp?: string;
  path?: string;
}

// For validation errors (400 Bad Request)
export interface ValidationErrorResponse {
  error: string;
  message: string;
  errors: Record<string, string[]>;
}

// Generic API response wrapper
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: ErrorResponse;
}
```

## Error Handling

### Error Handling Strategy

**Centralized Error Handling:**
- All API errors caught by Axios response interceptor
- Errors transformed to user-friendly messages
- Consistent error display across application

**Error Types:**

1. **Validation Errors (400)**
   - Display field-specific errors below form inputs
   - Highlight invalid fields in red
   - Show summary message at top of form

2. **Authentication Errors (401)**
   - Clear localStorage (token and user data)
   - Redirect to login page
   - Show "Session expired, please login again"
   - Preserve intended destination URL

3. **Authorization Errors (403)**
   - Show "Access denied" message
   - Redirect to appropriate dashboard based on user role
   - Log attempt for security monitoring

4. **Not Found Errors (404)**
   - Show "Resource not found" message
   - Provide navigation back to previous page
   - Suggest alternative actions

5. **Conflict Errors (409)**
   - Show specific conflict message (e.g., "DPI already registered")
   - Highlight conflicting field
   - Suggest resolution (e.g., "Use different DPI or login if you have an account")

6. **Rate Limit Errors (429)**
   - Show "Too many requests" message
   - Display countdown timer for retry
   - Disable submit button temporarily

7. **Server Errors (500)**
   - Show "Server error, please try again later"
   - Provide contact support option
   - Log error details for debugging

8. **Network Errors**
   - Show "Connection error, check your network"
   - Provide retry button
   - Check if API Gateway is reachable

### Error Display Components

**ErrorMessage Component:**
```typescript
interface ErrorMessageProps {
  error: ErrorResponse | null;
  onDismiss?: () => void;
}
```

**FieldError Component:**
```typescript
interface FieldErrorProps {
  errors: string[];
  fieldName: string;
}
```

**ErrorBoundary Component:**
- Catch React rendering errors
- Display fallback UI
- Log error to monitoring service
- Provide "Reload page" button

## Testing Strategy

### Unit Testing

**Services:**
- Test each service method independently
- Mock Axios responses
- Test error handling paths
- Test token management logic

**Components:**
- Test form validation
- Test error message display
- Test loading states
- Test user interactions

**Utilities:**
- Test validation functions
- Test formatting functions
- Test error transformation logic

**Example Tests:**
```typescript
describe('authService', () => {
  it('should store token on successful login', async () => {
    // Mock successful login response
    // Call login
    // Assert token stored in localStorage
  });

  it('should clear token on logout', async () => {
    // Set token in localStorage
    // Call logout
    // Assert token removed from localStorage
  });

  it('should handle 401 error', async () => {
    // Mock 401 response
    // Call protected endpoint
    // Assert redirect to login
  });
});
```

### Integration Testing

**API Integration:**
- Test actual API calls to backend (with test environment)
- Test JWT flow end-to-end
- Test error responses from backend
- Test timeout handling

**User Flows:**
- Test complete registration flow
- Test complete login flow
- Test complete appointment booking flow
- Test complete payment flow

**Example Integration Tests:**
```typescript
describe('Patient Registration Flow', () => {
  it('should register patient and redirect to login', async () => {
    // Fill registration form
    // Submit form
    // Assert success message
    // Assert redirect to login
  });

  it('should show error for duplicate DPI', async () => {
    // Register patient with DPI
    // Try to register again with same DPI
    // Assert error message shown
  });
});
```

### End-to-End Testing

**Critical Paths:**
- Staff login → Patient registration → Appointment creation
- Patient registration → Patient login → Appointment booking → Payment
- Patient login → View appointments → View medical history

**Tools:**
- Cypress or Playwright for E2E tests
- Test against local backend (docker-compose)
- Mock payment gateway for testing

### Manual Testing Checklist

**Authentication:**
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Logout clears session
- [ ] Token refresh works
- [ ] Session expires after 24 hours

**Patient Registration (Staff):**
- [ ] Register patient with all required fields
- [ ] Validate DPI format
- [ ] Validate phone format
- [ ] Validate email format
- [ ] Handle duplicate DPI error
- [ ] Handle duplicate email error

**Patient Self-Registration:**
- [ ] Register with all required fields
- [ ] Password validation works
- [ ] Confirmation password matches
- [ ] Redirect to login after registration
- [ ] Can login after registration

**Appointment Management (Staff):**
- [ ] View all appointments
- [ ] Create new appointment
- [ ] Activate appointment
- [ ] Cancel appointment
- [ ] Filter appointments by status

**Appointment Booking (Patient):**
- [ ] View available slots
- [ ] Select date and time
- [ ] Prompted to login if not authenticated
- [ ] Payment form appears after login
- [ ] Appointment created after successful payment
- [ ] Confirmation shown with appointment number

**Patient Portal:**
- [ ] View my appointments
- [ ] Filter appointments by status
- [ ] Cancel pending appointment
- [ ] View medical history
- [ ] View consultation details
- [ ] Download lab results

**Error Handling:**
- [ ] 401 redirects to login
- [ ] 403 shows access denied
- [ ] 404 shows not found
- [ ] 409 shows conflict message
- [ ] 429 shows rate limit message
- [ ] 500 shows server error
- [ ] Network error shows connection error

## Implementation Plan

### Phase 1: Core Infrastructure (Week 1)

**Tasks:**
1. Configure Axios instance with base URL and timeout
2. Implement JWT request interceptor
3. Implement response interceptor for error handling
4. Create centralized error handler
5. Set up environment variables (.env, .env.example)
6. Create common types (ErrorResponse, ApiResponse)

**Deliverables:**
- axiosInstance.ts configured and tested
- errorHandler.ts with all error types handled
- .env.example documented
- common.ts with shared types

### Phase 2: Authentication Integration (Week 1-2)

**Tasks:**
1. Align auth types with backend DTOs
2. Update authService to use configured Axios
3. Implement login flow with token storage
4. Implement logout flow with token cleanup
5. Implement token validation on app load
6. Update AuthContext to use new authService
7. Test authentication flow end-to-end

**Deliverables:**
- auth.ts types aligned with backend
- authService.ts fully integrated
- AuthContext updated
- Login/Logout working with backend

### Phase 3: Patient Service Integration (Week 2)

**Tasks:**
1. Align patient types with backend DTOs
2. Update patientService to use configured Axios
3. Implement patient registration (staff)
4. Implement patient search
5. Implement patient update
6. Add form validation for DPI, phone, email
7. Test patient operations end-to-end

**Deliverables:**
- patient.ts types aligned with backend
- patientService.ts fully integrated
- Patient registration form working
- Patient search working

### Phase 4: Patient Self-Registration (Week 2-3)

**Tasks:**
1. Create public registration page
2. Implement registration form with validation
3. Add password strength validation
4. Implement registration API call
5. Add success message and redirect to login
6. Handle duplicate DPI/email errors
7. Test registration flow end-to-end

**Deliverables:**
- Register.tsx page created
- Registration form with validation
- Registration working with backend
- Error handling for duplicates

### Phase 5: Appointment Service Integration (Week 3)

**Tasks:**
1. Align appointment types with backend DTOs
2. Update appointmentService to use configured Axios
3. Implement get appointments
4. Implement create appointment
5. Implement activate appointment
6. Implement cancel appointment
7. Test appointment operations end-to-end

**Deliverables:**
- appointment.ts types aligned with backend
- appointmentService.ts fully integrated
- Appointment management working
- Appointment activation working

### Phase 6: Patient Portal - Appointments (Week 3-4)

**Tasks:**
1. Create patient dashboard page
2. Create my appointments page
3. Implement get my appointments API call
4. Display appointments with status badges
5. Implement cancel appointment functionality
6. Add appointment filtering by status
7. Test patient appointment view end-to-end

**Deliverables:**
- Dashboard.tsx for patients
- MyAppointments.tsx page
- Appointment list with filtering
- Cancel appointment working

### Phase 7: Online Appointment Booking (Week 4)

**Tasks:**
1. Create public appointment booking page
2. Implement available slots API call
3. Create date/time picker
4. Implement authentication check
5. Show login/register options if not authenticated
6. Implement booking flow with authentication
7. Test booking flow end-to-end

**Deliverables:**
- BookAppointment.tsx page
- Available slots display
- Authentication flow integrated
- Booking working for authenticated users

### Phase 8: Payment Integration (Week 4-5)

**Tasks:**
1. Create payment types
2. Implement paymentService
3. Create payment form component
4. Integrate payment gateway (Stripe/PayPal)
5. Implement payment processing
6. Link payment to appointment creation
7. Show confirmation with receipt
8. Test payment flow end-to-end

**Deliverables:**
- payment.ts types created
- paymentService.ts implemented
- PaymentForm.tsx component
- Payment working with appointment booking
- Receipt download working

### Phase 9: Medical History (Week 5)

**Tasks:**
1. Create medical record types
2. Implement medicalRecordService
3. Create medical history page
4. Implement get my medical history API call
5. Display consultations and lab results
6. Implement download lab results
7. Test medical history view end-to-end

**Deliverables:**
- medicalRecord.ts types created
- medicalRecordService.ts implemented
- MedicalHistory.tsx page
- Medical history display working
- Lab result download working

### Phase 10: Route Protection (Week 5-6)

**Tasks:**
1. Create ProtectedRoute component
2. Define public routes
3. Define patient-protected routes
4. Define staff-protected routes
5. Implement role-based access control
6. Add 403 error page
7. Test route protection end-to-end

**Deliverables:**
- ProtectedRoute.tsx component
- Route configuration updated
- Role-based access working
- 403 page created

### Phase 11: Testing and Polish (Week 6)

**Tasks:**
1. Write unit tests for all services
2. Write integration tests for critical flows
3. Perform manual testing with checklist
4. Fix bugs found during testing
5. Improve error messages
6. Add loading states where missing
7. Optimize performance

**Deliverables:**
- Unit tests for services
- Integration tests for flows
- All manual tests passing
- Bugs fixed
- Performance optimized

### Phase 12: Documentation (Week 6)

**Tasks:**
1. Document API integration
2. Document authentication flow
3. Document error handling
4. Document environment variables
5. Create developer guide
6. Create user guide
7. Update README

**Deliverables:**
- API integration documentation
- Developer guide
- User guide
- README updated

## Deployment Considerations

### Environment Configuration

**Development:**
```
VITE_API_URL=http://localhost:8080/api
VITE_PAYMENT_GATEWAY_URL=https://sandbox.stripe.com
VITE_PAYMENT_PUBLIC_KEY=pk_test_...
```

**Staging:**
```
VITE_API_URL=https://staging-api.medflow.com/api
VITE_PAYMENT_GATEWAY_URL=https://sandbox.stripe.com
VITE_PAYMENT_PUBLIC_KEY=pk_test_...
```

**Production:**
```
VITE_API_URL=https://api.medflow.com/api
VITE_PAYMENT_GATEWAY_URL=https://api.stripe.com
VITE_PAYMENT_PUBLIC_KEY=pk_live_...
```

### Security Considerations

**Token Storage:**
- Store JWT in localStorage (acceptable for MVP)
- Consider httpOnly cookies for production
- Implement token refresh before expiration

**HTTPS:**
- Use HTTPS in production
- Redirect HTTP to HTTPS
- Set secure flag on cookies

**CORS:**
- API Gateway configured for frontend origin
- Update CORS for production domain

**Payment Security:**
- Never store card details in frontend
- Use payment gateway tokenization
- Implement PCI compliance measures

**Input Validation:**
- Validate all inputs on frontend
- Backend validation is primary defense
- Sanitize user inputs

### Performance Optimization

**Code Splitting:**
- Lazy load routes
- Lazy load heavy components
- Split vendor bundles

**Caching:**
- Cache API responses where appropriate
- Use React Query or SWR for data fetching
- Implement stale-while-revalidate strategy

**Bundle Size:**
- Minimize dependencies
- Tree-shake unused code
- Compress assets

**Network:**
- Implement request debouncing for search
- Use pagination for large lists
- Compress API responses (gzip)

### Monitoring and Logging

**Error Tracking:**
- Integrate Sentry or similar
- Log all API errors
- Track user actions leading to errors

**Analytics:**
- Track user flows
- Monitor conversion rates
- Identify bottlenecks

**Performance Monitoring:**
- Track page load times
- Monitor API response times
- Identify slow components

---

**Created**: 2026-04-16  
**Author**: MedFlow Team  
**Status**: Draft  
**Version**: 1.0.0
