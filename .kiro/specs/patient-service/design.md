# Design: Patient Service

## 1. Architecture Overview

### High-Level Architecture (MVC)

```
┌─────────────────────────────────────────────────────────┐
│                 API Gateway (8080)                       │
│              Validates JWT & Routes                      │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│            PATIENT SERVICE (Port 8082)                   │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Controller Layer                                 │  │
│  │  - PatientController                              │  │
│  │  - Endpoints: /api/patients/**                    │  │
│  └────────────────────┬──────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐  │
│  │  Service Layer                                    │  │
│  │  - PatientService (business logic)                │  │
│  │  - PasswordGeneratorService                       │  │
│  │  - ValidationService                              │  │
│  └────────────────────┬──────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐  │
│  │  Repository Layer                                 │  │
│  │  - PatientRepository (JPA)                        │  │
│  │  - AddressRepository (JPA)                        │  │
│  │  - EmergencyContactRepository (JPA)               │  │
│  └────────────────────┬──────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐  │
│  │  Database (PostgreSQL)                            │  │
│  │  - patient_schema.patients                        │  │
│  │  - patient_schema.addresses                       │  │
│  │  │  - patient_schema.emergency_contacts               │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Request Flow

```
1. Register Patient Request
   POST /api/patients
   Body: { "dpi": "1234567890123", "firstName": "Juan", ... }
   │
   ▼
2. PatientController receives request
   │
   ▼
3. ValidationService validates input
   ├─> Validate DPI format (13 digits)
   ├─> Validate email format
   ├─> Validate phone format (8 digits)
   └─> If valid, continue
   │
   ▼
4. PatientService checks uniqueness
   ├─> PatientRepository.existsByDpi()
   ├─> PatientRepository.existsByEmail()
   └─> If unique, continue
   │
   ▼
5. PasswordGeneratorService generates password
   ├─> Generate 8-char alphanumeric
   ├─> Include: 1 uppercase, 1 lowercase, 1 number
   └─> Hash with BCrypt (strength 10)
   │
   ▼
6. PatientService saves patient
   ├─> Save Patient entity
   ├─> Save Address entity
   ├─> Save EmergencyContact entities
   └─> Return PatientResponse with credentials
```

## 2. Components

### 2.1 PatientController (REST Layer)

```java
@RestController
@RequestMapping("/api/patients")
@Validated
public class PatientController {
    
    private final PatientService patientService;
    
    @PostMapping
    public ResponseEntity<PatientRegistrationResponse> registerPatient(
            @Valid @RequestBody PatientRegistrationRequest request) {
        // Register new patient and generate credentials
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable String id) {
        // Get patient by ID
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<PatientSearchResult>> searchPatients(
            @RequestParam String query,
            @RequestParam(required = false) String searchType) {
        // Search by DPI, name, or email
        // searchType: "dpi", "name", "email" (default: auto-detect)
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable String id,
            @Valid @RequestBody PatientUpdateRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {
        // Update patient data (limited fields)
    }
    
    @GetMapping("/{id}/credentials")
    public ResponseEntity<PatientCredentialsResponse> getCredentials(
            @PathVariable String id) {
        // Get generated credentials (for admission staff)
    }
}
```


---

### 2.2 PatientService (Business Logic)

```java
@Service
@Transactional
public class PatientService {
    
    private final PatientRepository patientRepository;
    private final AddressRepository addressRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final PasswordGeneratorService passwordGeneratorService;
    private final ValidationService validationService;
    private final PasswordEncoder passwordEncoder;
    
    public PatientRegistrationResponse registerPatient(PatientRegistrationRequest request) {
        // 1. Validate input
        validationService.validatePatientRegistration(request);
        
        // 2. Check DPI uniqueness
        if (patientRepository.existsByDpi(request.getDpi())) {
            throw new DuplicateDpiException("DPI already registered");
        }
        
        // 3. Check email uniqueness
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }
        
        // 4. Generate temporary password
        String temporaryPassword = passwordGeneratorService.generateTemporaryPassword();
        String hashedPassword = passwordEncoder.encode(temporaryPassword);
        
        // 5. Create Patient entity
        Patient patient = new Patient();
        patient.setDpi(request.getDpi());
        patient.setFirstName(request.getFirstName());
        patient.setSecondName(request.getSecondName());
        patient.setFirstLastName(request.getFirstLastName());
        patient.setSecondLastName(request.getSecondLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setPassword(hashedPassword);
        patient.setStatus(PatientStatus.ACTIVE);
        patient.setCreatedAt(LocalDateTime.now());
        
        // 6. Create Address entity
        Address address = new Address();
        address.setDepartment(request.getAddress().getDepartment());
        address.setMunicipality(request.getAddress().getMunicipality());
        address.setZone(request.getAddress().getZone());
        address.setFullAddress(request.getAddress().getFullAddress());
        address.setPostalCode(request.getAddress().getPostalCode());
        address.setPatient(patient);
        patient.setAddress(address);
        
        // 7. Create EmergencyContact entities
        List<EmergencyContact> contacts = request.getEmergencyContacts().stream()
            .map(contactReq -> {
                EmergencyContact contact = new EmergencyContact();
                contact.setFullName(contactReq.getFullName());
                contact.setRelationship(contactReq.getRelationship());
                contact.setPhone(contactReq.getPhone());
                contact.setPatient(patient);
                return contact;
            })
            .collect(Collectors.toList());
        patient.setEmergencyContacts(contacts);
        
        // 8. Save patient (cascades to address and contacts)
        Patient savedPatient = patientRepository.save(patient);
        
        // 9. Return response with credentials
        return new PatientRegistrationResponse(
            savedPatient.getId(),
            savedPatient.getDpi(),
            savedPatient.getFullName(),
            savedPatient.getEmail(),
            temporaryPassword // Return plain password for admission staff
        );
    }
    
    @Transactional(readOnly = true)
    public PatientResponse getPatient(String id) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        
        return mapToPatientResponse(patient);
    }
    
    @Transactional(readOnly = true)
    public List<PatientSearchResult> searchPatients(String query, String searchType) {
        List<Patient> patients;
        
        if (searchType == null) {
            // Auto-detect search type
            if (query.matches("\\d{13}")) {
                searchType = "dpi";
            } else if (query.contains("@")) {
                searchType = "email";
            } else {
                searchType = "name";
            }
        }
        
        switch (searchType.toLowerCase()) {
            case "dpi":
                patients = patientRepository.findByDpi(query)
                    .map(List::of)
                    .orElse(Collections.emptyList());
                break;
            case "email":
                patients = patientRepository.findByEmailIgnoreCase(query)
                    .map(List::of)
                    .orElse(Collections.emptyList());
                break;
            case "name":
            default:
                patients = patientRepository.searchByName(query, PageRequest.of(0, 50))
                    .getContent();
                break;
        }
        
        return patients.stream()
            .map(this::mapToSearchResult)
            .collect(Collectors.toList());
    }
    
    public PatientResponse updatePatient(String id, PatientUpdateRequest request, 
                                         String userId, String roles) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        
        // Check permissions: PATIENT can only update their own record
        if (roles.contains("PATIENT") && !patient.getId().equals(userId)) {
            throw new UnauthorizedException("Cannot update other patient's data");
        }
        
        // Update allowed fields
        if (request.getPhone() != null) {
            validationService.validatePhone(request.getPhone());
            patient.setPhone(request.getPhone());
        }
        
        if (request.getEmail() != null) {
            validationService.validateEmail(request.getEmail());
            // Check email uniqueness (excluding current patient)
            patientRepository.findByEmailIgnoreCase(request.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateEmailException("Email already in use");
                    }
                });
            patient.setEmail(request.getEmail());
        }
        
        // Update address
        if (request.getAddress() != null) {
            Address address = patient.getAddress();
            if (request.getAddress().getDepartment() != null) {
                address.setDepartment(request.getAddress().getDepartment());
            }
            if (request.getAddress().getMunicipality() != null) {
                address.setMunicipality(request.getAddress().getMunicipality());
            }
            if (request.getAddress().getZone() != null) {
                address.setZone(request.getAddress().getZone());
            }
            if (request.getAddress().getFullAddress() != null) {
                address.setFullAddress(request.getAddress().getFullAddress());
            }
            if (request.getAddress().getPostalCode() != null) {
                address.setPostalCode(request.getAddress().getPostalCode());
            }
        }
        
        // Update emergency contacts (replace all)
        if (request.getEmergencyContacts() != null) {
            // Remove old contacts
            emergencyContactRepository.deleteAll(patient.getEmergencyContacts());
            patient.getEmergencyContacts().clear();
            
            // Add new contacts
            List<EmergencyContact> newContacts = request.getEmergencyContacts().stream()
                .map(contactReq -> {
                    EmergencyContact contact = new EmergencyContact();
                    contact.setFullName(contactReq.getFullName());
                    contact.setRelationship(contactReq.getRelationship());
                    contact.setPhone(contactReq.getPhone());
                    contact.setPatient(patient);
                    return contact;
                })
                .collect(Collectors.toList());
            patient.getEmergencyContacts().addAll(newContacts);
        }
        
        patient.setUpdatedAt(LocalDateTime.now());
        Patient updatedPatient = patientRepository.save(patient);
        
        return mapToPatientResponse(updatedPatient);
    }
    
    @Transactional(readOnly = true)
    public PatientCredentialsResponse getCredentials(String id) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        
        // Note: We cannot return the original password (it's hashed)
        // This endpoint is for showing username options
        return new PatientCredentialsResponse(
            patient.getEmail(),
            patient.getDpi(),
            "Password was generated at registration and cannot be retrieved"
        );
    }
    
    private PatientResponse mapToPatientResponse(Patient patient) {
        // Map entity to DTO
        // ... implementation
    }
    
    private PatientSearchResult mapToSearchResult(Patient patient) {
        // Map entity to search result DTO
        // ... implementation
    }
}
```

---

### 2.3 PasswordGeneratorService

```java
@Service
public class PasswordGeneratorService {
    
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS;
    private static final int PASSWORD_LENGTH = 8;
    
    private final SecureRandom random = new SecureRandom();
    
    public String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        
        // Ensure at least one of each required character type
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        
        // Fill remaining characters randomly
        for (int i = 3; i < PASSWORD_LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        
        // Shuffle to avoid predictable pattern
        return shuffleString(password.toString());
    }
    
    private String shuffleString(String input) {
        List<Character> characters = input.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());
        Collections.shuffle(characters, random);
        return characters.stream()
            .map(String::valueOf)
            .collect(Collectors.joining());
    }
}
```

---

### 2.4 ValidationService

```java
@Service
public class ValidationService {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DPI_PATTERN = Pattern.compile("^\\d{13}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8}$");
    
    public void validatePatientRegistration(PatientRegistrationRequest request) {
        validateDpi(request.getDpi());
        validateEmail(request.getEmail());
        validatePhone(request.getPhone());
        validateDateOfBirth(request.getDateOfBirth());
        validateName(request.getFirstName(), "First name");
        validateName(request.getFirstLastName(), "First last name");
        
        if (request.getEmergencyContacts() == null || request.getEmergencyContacts().isEmpty()) {
            throw new ValidationException("At least one emergency contact is required");
        }
        
        if (request.getEmergencyContacts().size() > 3) {
            throw new ValidationException("Maximum 3 emergency contacts allowed");
        }
    }
    
    public void validateDpi(String dpi) {
        if (dpi == null || !DPI_PATTERN.matcher(dpi).matches()) {
            throw new ValidationException("DPI must be exactly 13 digits");
        }
    }
    
    public void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }
    
    public void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Phone must be exactly 8 digits");
        }
    }
    
    public void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new ValidationException("Date of birth is required");
        }
        
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future");
        }
        
        if (dateOfBirth.isAfter(LocalDate.now().minusDays(1))) {
            throw new ValidationException("Patient must be at least 1 day old");
        }
    }
    
    public void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        if (name.length() > 100) {
            throw new ValidationException(fieldName + " must not exceed 100 characters");
        }
    }
}
```


---

## 3. Data Model

### 3.1 Patient Entity

```java
@Entity
@Table(name = "patients", schema = "patient_schema",
       indexes = {
           @Index(name = "idx_patients_dpi", columnList = "dpi"),
           @Index(name = "idx_patients_email", columnList = "email")
       })
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false, length = 13)
    private String dpi;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "second_name", length = 100)
    private String secondName;
    
    @Column(name = "first_last_name", nullable = false, length = 100)
    private String firstLastName;
    
    @Column(name = "second_last_name", length = 100)
    private String secondLastName;
    
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 8)
    private String phone;
    
    @Column(nullable = false, length = 255)
    private String password; // BCrypt hashed
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PatientStatus status;
    
    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper method
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(firstName);
        if (secondName != null && !secondName.isEmpty()) {
            fullName.append(" ").append(secondName);
        }
        fullName.append(" ").append(firstLastName);
        if (secondLastName != null && !secondLastName.isEmpty()) {
            fullName.append(" ").append(secondLastName);
        }
        return fullName.toString();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PatientStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

public enum Gender {
    MALE,
    FEMALE,
    OTHER
}

public enum PatientStatus {
    ACTIVE,
    INACTIVE
}
```

### 3.2 Address Entity

```java
@Entity
@Table(name = "addresses", schema = "patient_schema")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, length = 100)
    private String department;
    
    @Column(nullable = false, length = 100)
    private String municipality;
    
    @Column(length = 50)
    private String zone;
    
    @Column(name = "full_address", nullable = false, length = 500)
    private String fullAddress;
    
    @Column(name = "postal_code", length = 10)
    private String postalCode;
    
    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
}
```

### 3.3 EmergencyContact Entity

```java
@Entity
@Table(name = "emergency_contacts", schema = "patient_schema")
public class EmergencyContact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Relationship relationship;
    
    @Column(nullable = false, length = 8)
    private String phone;
    
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
}

public enum Relationship {
    FATHER,
    MOTHER,
    SPOUSE,
    SON,
    DAUGHTER,
    BROTHER,
    SISTER,
    OTHER
}
```

---

## 4. Repository Layer

### 4.1 PatientRepository

```java
@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    
    Optional<Patient> findByDpi(String dpi);
    
    Optional<Patient> findByEmailIgnoreCase(String email);
    
    boolean existsByDpi(String dpi);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.secondName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.firstLastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.secondLastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Patient> searchByName(@Param("query") String query, Pageable pageable);
}
```

### 4.2 AddressRepository

```java
@Repository
public interface AddressRepository extends JpaRepository<Address, String> {
    // Basic CRUD operations provided by JpaRepository
}
```

### 4.3 EmergencyContactRepository

```java
@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, String> {
    
    List<EmergencyContact> findByPatientId(String patientId);
}
```

---

## 5. DTOs (Data Transfer Objects)

### 5.1 Request DTOs

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRegistrationRequest {
    
    @NotBlank(message = "DPI is required")
    @Pattern(regexp = "^\\d{13}$", message = "DPI must be exactly 13 digits")
    private String dpi;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Second name must not exceed 100 characters")
    private String secondName;
    
    @NotBlank(message = "First last name is required")
    @Size(max = 100, message = "First last name must not exceed 100 characters")
    private String firstLastName;
    
    @Size(max = 100, message = "Second last name must not exceed 100 characters")
    private String secondLastName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotNull(message = "Gender is required")
    private Gender gender;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{8}$", message = "Phone must be exactly 8 digits")
    private String phone;
    
    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;
    
    @NotEmpty(message = "At least one emergency contact is required")
    @Size(max = 3, message = "Maximum 3 emergency contacts allowed")
    @Valid
    private List<EmergencyContactRequest> emergencyContacts;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @NotBlank(message = "Municipality is required")
    private String municipality;
    
    private String zone;
    
    @NotBlank(message = "Full address is required")
    private String fullAddress;
    
    private String postalCode;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactRequest {
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotNull(message = "Relationship is required")
    private Relationship relationship;
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{8}$", message = "Phone must be exactly 8 digits")
    private String phone;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateRequest {
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Pattern(regexp = "^\\d{8}$", message = "Phone must be exactly 8 digits")
    private String phone;
    
    @Valid
    private AddressRequest address;
    
    @Size(max = 3, message = "Maximum 3 emergency contacts allowed")
    @Valid
    private List<EmergencyContactRequest> emergencyContacts;
}
```

### 5.2 Response DTOs

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRegistrationResponse {
    
    private String id;
    private String dpi;
    private String fullName;
    private String email;
    private String temporaryPassword; // Plain text for admission staff
    private String message;
    
    public PatientRegistrationResponse(String id, String dpi, String fullName, 
                                       String email, String temporaryPassword) {
        this.id = id;
        this.dpi = dpi;
        this.fullName = fullName;
        this.email = email;
        this.temporaryPassword = temporaryPassword;
        this.message = "Patient registered successfully. Credentials: Email or DPI + password";
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    
    private String id;
    private String dpi;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String email;
    private String phone;
    private PatientStatus status;
    private AddressResponse address;
    private List<EmergencyContactResponse> emergencyContacts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    
    private String id;
    private String department;
    private String municipality;
    private String zone;
    private String fullAddress;
    private String postalCode;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactResponse {
    
    private String id;
    private String fullName;
    private Relationship relationship;
    private String phone;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientSearchResult {
    
    private String id;
    private String dpi;
    private String fullName;
    private LocalDate dateOfBirth;
    private String phone;
    private String email;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientCredentialsResponse {
    
    private String email;
    private String dpi;
    private String note;
}
```


---

## 6. Exception Handling

### 6.1 Custom Exceptions

```java
public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}

public class DuplicateDpiException extends RuntimeException {
    public DuplicateDpiException(String message) {
        super(message);
    }
}

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

### 6.2 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler({DuplicateDpiException.class, DuplicateEmailException.class})
    public ResponseEntity<ErrorResponse> handleDuplicateResource(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
            "CONFLICT",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            message,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        ErrorResponse error = new ErrorResponse(
            "UNAUTHORIZED",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private LocalDateTime timestamp;
}
```

---

## 7. Database Schema

```sql
-- Create schema
CREATE SCHEMA IF NOT EXISTS patient_schema;

-- Patients table
CREATE TABLE patient_schema.patients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dpi VARCHAR(13) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100),
    first_last_name VARCHAR(100) NOT NULL,
    second_last_name VARCHAR(100),
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(8) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Addresses table
CREATE TABLE patient_schema.addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    municipality VARCHAR(100) NOT NULL,
    zone VARCHAR(50),
    full_address VARCHAR(500) NOT NULL,
    postal_code VARCHAR(10),
    FOREIGN KEY (patient_id) REFERENCES patient_schema.patients(id) ON DELETE CASCADE
);

-- Emergency contacts table
CREATE TABLE patient_schema.emergency_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    relationship VARCHAR(50) NOT NULL CHECK (relationship IN 
        ('FATHER', 'MOTHER', 'SPOUSE', 'SON', 'DAUGHTER', 'BROTHER', 'SISTER', 'OTHER')),
    phone VARCHAR(8) NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patient_schema.patients(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_patients_dpi ON patient_schema.patients(dpi);
CREATE INDEX idx_patients_email ON patient_schema.patients(email);
CREATE INDEX idx_patients_status ON patient_schema.patients(status);
CREATE INDEX idx_patients_first_name ON patient_schema.patients(first_name);
CREATE INDEX idx_patients_first_last_name ON patient_schema.patients(first_last_name);
CREATE INDEX idx_emergency_contacts_patient_id ON patient_schema.emergency_contacts(patient_id);

-- Comments
COMMENT ON TABLE patient_schema.patients IS 'Stores patient demographic information';
COMMENT ON TABLE patient_schema.addresses IS 'Stores patient addresses (one-to-one with patients)';
COMMENT ON TABLE patient_schema.emergency_contacts IS 'Stores emergency contacts (one-to-many with patients)';
```

---

## 8. API Endpoints

### 8.1 Register Patient

**Request:**
```http
POST /api/patients
Content-Type: application/json

{
  "dpi": "1234567890123",
  "firstName": "Juan",
  "secondName": "Carlos",
  "firstLastName": "Pérez",
  "secondLastName": "García",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "email": "juan.perez@example.com",
  "phone": "12345678",
  "address": {
    "department": "Guatemala",
    "municipality": "Guatemala",
    "zone": "10",
    "fullAddress": "5ta Avenida 12-34, Zona 10",
    "postalCode": "01010"
  },
  "emergencyContacts": [
    {
      "fullName": "María García",
      "relationship": "SPOUSE",
      "phone": "87654321"
    }
  ]
}
```

**Response (Success):**
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "dpi": "1234567890123",
  "fullName": "Juan Carlos Pérez García",
  "email": "juan.perez@example.com",
  "temporaryPassword": "Abc123xy",
  "message": "Patient registered successfully. Credentials: Email or DPI + password"
}
```

**Response (Error - Duplicate DPI):**
```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "error": "CONFLICT",
  "message": "DPI already registered",
  "timestamp": "2026-04-13T10:30:00"
}
```

### 8.2 Get Patient by ID

**Request:**
```http
GET /api/patients/550e8400-e29b-41d4-a716-446655440000
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "dpi": "1234567890123",
  "firstName": "Juan",
  "secondName": "Carlos",
  "firstLastName": "Pérez",
  "secondLastName": "García",
  "fullName": "Juan Carlos Pérez García",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "email": "juan.perez@example.com",
  "phone": "12345678",
  "status": "ACTIVE",
  "address": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "department": "Guatemala",
    "municipality": "Guatemala",
    "zone": "10",
    "fullAddress": "5ta Avenida 12-34, Zona 10",
    "postalCode": "01010"
  },
  "emergencyContacts": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "fullName": "María García",
      "relationship": "SPOUSE",
      "phone": "87654321"
    }
  ],
  "createdAt": "2026-04-13T10:30:00",
  "updatedAt": null
}
```

### 8.3 Search Patients

**Request (by name):**
```http
GET /api/patients/search?query=Juan&searchType=name
```

**Request (by DPI):**
```http
GET /api/patients/search?query=1234567890123&searchType=dpi
```

**Request (by email):**
```http
GET /api/patients/search?query=juan.perez@example.com&searchType=email
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "dpi": "1234567890123",
    "fullName": "Juan Carlos Pérez García",
    "dateOfBirth": "1990-05-15",
    "phone": "12345678",
    "email": "juan.perez@example.com"
  }
]
```

### 8.4 Update Patient

**Request:**
```http
PUT /api/patients/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
X-User-Roles: PATIENT

{
  "phone": "99887766",
  "email": "juan.new@example.com",
  "address": {
    "department": "Guatemala",
    "municipality": "Mixco",
    "zone": "1",
    "fullAddress": "Calle Principal 1-23, Zona 1",
    "postalCode": "01057"
  },
  "emergencyContacts": [
    {
      "fullName": "María García",
      "relationship": "SPOUSE",
      "phone": "87654321"
    },
    {
      "fullName": "Pedro Pérez",
      "relationship": "FATHER",
      "phone": "11223344"
    }
  ]
}
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "dpi": "1234567890123",
  "firstName": "Juan",
  "secondName": "Carlos",
  "firstLastName": "Pérez",
  "secondLastName": "García",
  "fullName": "Juan Carlos Pérez García",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "email": "juan.new@example.com",
  "phone": "99887766",
  "status": "ACTIVE",
  "address": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "department": "Guatemala",
    "municipality": "Mixco",
    "zone": "1",
    "fullAddress": "Calle Principal 1-23, Zona 1",
    "postalCode": "01057"
  },
  "emergencyContacts": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440003",
      "fullName": "María García",
      "relationship": "SPOUSE",
      "phone": "87654321"
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440004",
      "fullName": "Pedro Pérez",
      "relationship": "FATHER",
      "phone": "11223344"
    }
  ],
  "createdAt": "2026-04-13T10:30:00",
  "updatedAt": "2026-04-13T11:45:00"
}
```

### 8.5 Get Patient Credentials

**Request:**
```http
GET /api/patients/550e8400-e29b-41d4-a716-446655440000/credentials
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "email": "juan.perez@example.com",
  "dpi": "1234567890123",
  "note": "Password was generated at registration and cannot be retrieved"
}
```


---

## 9. Configuration

### 9.1 application.yml

```yaml
server:
  port: 8082

spring:
  application:
    name: patient-service
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:medflow_db}?currentSchema=patient_schema
    username: ${DB_USERNAME:medflow_user}
    password: ${DB_PASSWORD:medflow_pass}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: patient_schema
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false
  
  jackson:
    serialization:
      write-dates-as-timestamps: false
    time-zone: America/Guatemala

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.medflow.patient: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/patient-service.log
```

### 9.2 application-docker.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/medflow_db?currentSchema=patient_schema
    username: medflow_user
    password: medflow_pass

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

### 9.3 SecurityConfig

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Strength 10
    }
}
```

---

## 10. Testing Strategy

### 10.1 Unit Tests

**PatientServiceTest:**
- Test patient registration with valid data
- Test duplicate DPI detection
- Test duplicate email detection
- Test password generation
- Test patient search by DPI, name, email
- Test patient update with valid data
- Test patient update authorization (PATIENT can only update own record)

**PasswordGeneratorServiceTest:**
- Test password length (8 characters)
- Test password contains at least 1 uppercase
- Test password contains at least 1 lowercase
- Test password contains at least 1 digit
- Test password uniqueness (generate multiple, ensure different)

**ValidationServiceTest:**
- Test DPI validation (13 digits)
- Test email validation (valid format)
- Test phone validation (8 digits)
- Test date of birth validation (not future, at least 1 day old)
- Test name validation (not empty, max 100 chars)

### 10.2 Integration Tests

**PatientControllerIntegrationTest:**
- Test POST /api/patients (full registration flow)
- Test GET /api/patients/{id}
- Test GET /api/patients/search
- Test PUT /api/patients/{id}
- Test GET /api/patients/{id}/credentials
- Test error responses (404, 409, 400)

**RepositoryTest:**
- Test PatientRepository.findByDpi()
- Test PatientRepository.findByEmailIgnoreCase()
- Test PatientRepository.searchByName()
- Test cascade operations (save patient with address and contacts)

### 10.3 Test Coverage Goals

- **Unit Tests**: >= 80% code coverage
- **Integration Tests**: All endpoints covered
- **Edge Cases**: Empty inputs, null values, boundary conditions

### 10.4 Test Data

```java
@TestConfiguration
public class TestDataConfig {
    
    public static PatientRegistrationRequest createValidPatientRequest() {
        PatientRegistrationRequest request = new PatientRegistrationRequest();
        request.setDpi("1234567890123");
        request.setFirstName("Juan");
        request.setSecondName("Carlos");
        request.setFirstLastName("Pérez");
        request.setSecondLastName("García");
        request.setDateOfBirth(LocalDate.of(1990, 5, 15));
        request.setGender(Gender.MALE);
        request.setEmail("juan.perez@example.com");
        request.setPhone("12345678");
        
        AddressRequest address = new AddressRequest();
        address.setDepartment("Guatemala");
        address.setMunicipality("Guatemala");
        address.setZone("10");
        address.setFullAddress("5ta Avenida 12-34, Zona 10");
        address.setPostalCode("01010");
        request.setAddress(address);
        
        EmergencyContactRequest contact = new EmergencyContactRequest();
        contact.setFullName("María García");
        contact.setRelationship(Relationship.SPOUSE);
        contact.setPhone("87654321");
        request.setEmergencyContacts(List.of(contact));
        
        return request;
    }
}
```

---

## 11. Error Handling Strategy

### 11.1 Error Response Format

All errors follow a consistent format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2026-04-13T10:30:00"
}
```

### 11.2 HTTP Status Codes

| Status Code | Error Type | Example |
|-------------|------------|---------|
| 400 | Bad Request | Invalid input, validation errors |
| 404 | Not Found | Patient not found |
| 409 | Conflict | Duplicate DPI or email |
| 403 | Forbidden | Unauthorized to update patient |
| 500 | Internal Server Error | Unexpected errors |

### 11.3 Validation Error Details

For validation errors, include field-level details:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "dpi: DPI must be exactly 13 digits, email: Invalid email format",
  "timestamp": "2026-04-13T10:30:00"
}
```

---

## 12. Security Considerations

### 12.1 Password Security

- **Generation**: Use `SecureRandom` for cryptographically secure randomness
- **Hashing**: BCrypt with strength 10
- **Storage**: Never store plain text passwords
- **Transmission**: Return plain password only once at registration (for admission staff)

### 12.2 Input Validation

- **DPI**: Exactly 13 digits, no special characters
- **Email**: Valid format, unique in system
- **Phone**: Exactly 8 digits (Guatemala format)
- **SQL Injection**: Use JPA parameterized queries
- **XSS**: Sanitize all string inputs

### 12.3 Authorization

- **PATIENT role**: Can only update their own record
- **ADMIN, ADMISSION roles**: Can update any patient record
- **Validation**: Check `X-User-Id` header matches patient ID for PATIENT role

### 12.4 Data Privacy

- **Password**: Never return hashed password in responses
- **Credentials endpoint**: Only accessible by ADMIN and ADMISSION roles
- **Logging**: Never log passwords (plain or hashed)

---

## 13. Performance Optimization

### 13.1 Database Indexes

```sql
-- Primary indexes for uniqueness
CREATE UNIQUE INDEX idx_patients_dpi ON patient_schema.patients(dpi);
CREATE UNIQUE INDEX idx_patients_email ON patient_schema.patients(email);

-- Search indexes
CREATE INDEX idx_patients_first_name ON patient_schema.patients(first_name);
CREATE INDEX idx_patients_first_last_name ON patient_schema.patients(first_last_name);
CREATE INDEX idx_patients_status ON patient_schema.patients(status);

-- Foreign key indexes
CREATE INDEX idx_emergency_contacts_patient_id ON patient_schema.emergency_contacts(patient_id);
```

### 13.2 Query Optimization

- **Eager Loading**: Load address and emergency contacts with patient (avoid N+1)
- **Pagination**: Limit search results to 50 records
- **Caching**: Consider caching frequently accessed patients (future enhancement)

### 13.3 Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 14. Deployment

### 14.1 Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/patient-service-*.jar app.jar

EXPOSE 8082

ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 14.2 Docker Compose Integration

```yaml
services:
  patient-service:
    build: ./backend-services/patient-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=medflow_db
      - DB_USERNAME=medflow_user
      - DB_PASSWORD=medflow_pass
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
    depends_on:
      - postgres
      - eureka-server
    networks:
      - medflow-network
```

### 14.3 Health Checks

```yaml
management:
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
    diskspace:
      enabled: true
```

**Health Check Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    }
  }
}
```

---

## 15. Monitoring and Logging

### 15.1 Logging Strategy

**Log Levels:**
- **INFO**: Patient registration, updates, searches
- **WARN**: Duplicate DPI/email attempts, validation failures
- **ERROR**: Database errors, unexpected exceptions

**Log Format:**
```
2026-04-13 10:30:00 [http-nio-8082-exec-1] INFO  PatientService - Patient registered: id=550e8400, dpi=1234567890123
2026-04-13 10:31:00 [http-nio-8082-exec-2] WARN  PatientService - Duplicate DPI attempt: dpi=1234567890123
2026-04-13 10:32:00 [http-nio-8082-exec-3] ERROR PatientService - Database error: Connection timeout
```

### 15.2 Metrics

**Expose via Actuator:**
- Request count per endpoint
- Response times (avg, p95, p99)
- Error rates
- Database connection pool metrics

**Prometheus Integration:**
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 16. Future Enhancements

### 16.1 Phase 2 Features

- **QR Code Generation**: Generate QR code for patient identification
- **Photo Upload**: Store patient photo
- **Document Attachments**: Store PDF documents (ID, insurance cards)
- **Audit Trail**: Track all changes to patient records
- **Email/SMS Notifications**: Send credentials via email/SMS

### 16.2 Performance Improvements

- **Redis Caching**: Cache frequently accessed patients
- **Read Replicas**: Separate read/write database connections
- **Elasticsearch**: Full-text search for patient names

### 16.3 Security Enhancements

- **Password Expiration**: Force password change after X days
- **Account Lockout**: Lock account after failed login attempts
- **Two-Factor Authentication**: Optional 2FA for patient portal

---

**Created**: April 13, 2026  
**Author**: MedFlow Team  
**Status**: ✅ Ready for Implementation  
**Version**: 1.0.0


## 17. Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

**Note on PBT Applicability**: The Patient Service is primarily a CRUD service with simple database operations. Property-based testing is most valuable for the pure functions (password generation, validation logic) rather than the CRUD operations themselves. The main testing strategy will be unit tests and integration tests, with selective property-based tests for algorithmic components.

### Property 1: Password Generation Strength

*For any* generated temporary password, it SHALL contain exactly 8 characters, including at least one uppercase letter, one lowercase letter, and one digit.

**Validates: Requirements FR2 (Password Generation)**

### Property 2: Password Generation Uniqueness

*For any* two consecutive password generation calls, the generated passwords SHALL be different (with overwhelming probability given secure randomness).

**Validates: Requirements FR2 (Password Generation)**

### Property 3: DPI Validation Consistency

*For any* string input, the DPI validation SHALL accept if and only if the string contains exactly 13 numeric digits.

**Validates: Requirements FR3 (DPI Validation)**

### Property 4: Email Validation Consistency

*For any* string input, the email validation SHALL accept if and only if the string matches the standard email format pattern.

**Validates: Requirements FR3 (Email Validation)**

### Property 5: Phone Validation Consistency

*For any* string input, the phone validation SHALL accept if and only if the string contains exactly 8 numeric digits.

**Validates: Requirements FR3 (Phone Validation)**

### Property 6: Full Name Concatenation

*For any* patient with firstName, secondName (optional), firstLastName, and secondLastName (optional), the getFullName() method SHALL produce a space-separated concatenation in the correct order, omitting null or empty optional fields.

**Validates: Requirements FR1 (Patient Data)**

### Property 7: BCrypt Hash Non-Reversibility

*For any* plain text password, after hashing with BCrypt, it SHALL be impossible to retrieve the original password from the hash alone.

**Validates: Requirements FR2, NFR4 (Security)**

### Property 8: Search Result Limit

*For any* search query, the results SHALL never exceed 50 records, regardless of how many matching patients exist in the database.

**Validates: Requirements FR4 (Search Limits)**

---

