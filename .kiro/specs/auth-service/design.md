# Design: Auth Service

## 1. Architecture Overview

### High-Level Architecture (MVC)

```
┌─────────────────────────────────────────────────────────┐
│                 API Gateway (8080)                       │
│              Validates JWT from here                     │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              AUTH SERVICE (Port 8081)                    │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Controller Layer                                 │  │
│  │  - AuthController                                 │  │
│  │  - Endpoints: /login, /logout, /refresh, /me     │  │
│  └────────────────────┬──────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐  │
│  │  Service Layer                                    │  │
│  │  - AuthService (business logic)                   │  │
│  │  - JwtService (JWT generation/validation)        │  │
│  │  - TokenBlacklistService (logout)                │  │
│  └────────────────────┬──────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐  │
│  │  Repository Layer                                 │  │
│  │  - UserRepository (JPA)                           │  │
│  │  - RoleRepository (JPA)                           │  │
│  └────────────────────┬──────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐  │
│  │  Database (PostgreSQL)                            │  │
│  │  - auth_schema.users                              │  │
│  │  - auth_schema.roles                              │  │
│  │  - auth_schema.user_roles                         │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Request Flow

```
1. Login Request
   POST /api/auth/login
   Body: { "username": "doctor1", "password": "pass123" }
   │
   ▼
2. AuthController receives request
   │
   ▼
3. AuthService validates credentials
   ├─> UserRepository.findByUsername()
   ├─> BCrypt.matches(password, user.password)
   └─> If valid, continue
   │
   ▼
4. JwtService generates token
   ├─> Create claims: userId, username, roles
   ├─> Sign with secret key
   └─> Set expiration (24 hours)
   │
   ▼
5. Return JWT to client
   Response: { "token": "eyJhbGc...", "expiresIn": 86400 }
```

## 2. Components

### 2.1 AuthController (REST Layer)

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Validate credentials and generate JWT
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        // Add token to blacklist
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestHeader("Authorization") String token) {
        // Generate new JWT from valid token
    }
    
    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestParam String token) {
        // Validate JWT and return claims
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String token) {
        // Get current user info from JWT
    }
}
```

---

### 2.2 AuthService (Business Logic)

```java
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService blacklistService;
    
    public LoginResponse login(String username, String password) {
        // 1. Find user by username
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        // 2. Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        
        // 3. Check if user is active
        if (!user.isActive()) {
            throw new AccountDisabledException("Account is disabled");
        }
        
        // 4. Generate JWT
        String token = jwtService.generateToken(user);
        
        // 5. Return response
        return new LoginResponse(token, jwtService.getExpirationTime());
    }
    
    public void logout(String token) {
        // Add token to blacklist
        blacklistService.addToBlacklist(token);
    }
    
    public LoginResponse refresh(String token) {
        // Validate current token
        if (!jwtService.isValid(token)) {
            throw new InvalidTokenException("Invalid token");
        }
        
        // Extract user from token
        String userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Generate new token
        String newToken = jwtService.generateToken(user);
        
        return new LoginResponse(newToken, jwtService.getExpirationTime());
    }
    
    public ValidateResponse validate(String token) {
        // Check blacklist
        if (blacklistService.isBlacklisted(token)) {
            throw new InvalidTokenException("Token is blacklisted");
        }
        
        // Validate token
        if (!jwtService.isValid(token)) {
            throw new InvalidTokenException("Invalid token");
        }
        
        // Extract claims
        Claims claims = jwtService.extractClaims(token);
        
        return new ValidateResponse(
            claims.get("userId", String.class),
            claims.get("username", String.class),
            claims.get("roles", String.class)
        );
    }
}
```

---

### 2.3 JwtService (JWT Logic)

```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long expirationTime;
    
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRolesAsString()); // "DOCTOR,ADMIN"
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody();
    }
    
    public String extractUserId(String token) {
        return extractClaims(token).get("userId", String.class);
    }
    
    public long getExpirationTime() {
        return expirationTime / 1000; // Return in seconds
    }
}
```

---

### 2.4 TokenBlacklistService (Logout Logic)

```java
@Service
public class TokenBlacklistService {
    
    // Phase 1: In-memory (simple)
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    
    // Phase 2: Redis (distributed)
    // private final RedisTemplate<String, String> redisTemplate;
    
    public void addToBlacklist(String token) {
        blacklist.add(token);
        
        // Schedule cleanup after token expiration
        scheduleCleanup(token, 24 * 60 * 60 * 1000); // 24 hours
    }
    
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
    
    private void scheduleCleanup(String token, long delayMillis) {
        // Remove token from blacklist after expiration
        CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS)
            .execute(() -> blacklist.remove(token));
    }
}
```

---

## 3. Data Model

### 3.1 User Entity

```java
@Entity
@Table(name = "users", schema = "auth_schema")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password; // BCrypt hashed
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        schema = "auth_schema",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper method
    public String getRolesAsString() {
        return roles.stream()
            .map(Role::getName)
            .collect(Collectors.joining(","));
    }
}
```

### 3.2 Role Entity

```java
@Entity
@Table(name = "roles", schema = "auth_schema")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleName name;
    
    private String description;
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
}

public enum RoleName {
    ADMIN,
    ADMISSION,
    VITAL_SIGNS,
    DOCTOR,
    LABORATORY,
    PHARMACY,
    CASHIER,
    PATIENT
}
```

---

## 4. API Design

### 4.1 Login Endpoint

**Request:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "doctor1",
  "password": "password123"
}
```

**Response (Success):**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": "123",
    "username": "doctor1",
    "email": "doctor1@medflow.com",
    "fullName": "Dr. Juan Pérez",
    "roles": ["DOCTOR", "ADMIN"]
  }
}
```

**Response (Error):**
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "error": "Unauthorized",
  "message": "Invalid credentials"
}
```

### 4.2 Logout Endpoint

**Request:**
```http
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "Logged out successfully"
}
```

### 4.3 Refresh Token Endpoint

**Request:**
```http
POST /api/auth/refresh
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

### 4.4 Validate Token Endpoint

**Request:**
```http
GET /api/auth/validate?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "valid": true,
  "userId": "123",
  "username": "doctor1",
  "roles": "DOCTOR,ADMIN"
}
```

### 4.5 Get Current User Endpoint

**Request:**
```http
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "123",
  "username": "doctor1",
  "email": "doctor1@medflow.com",
  "fullName": "Dr. Juan Pérez",
  "roles": ["DOCTOR", "ADMIN"],
  "active": true
}
```

---

## 5. Database Schema

```sql
-- Schema
CREATE SCHEMA IF NOT EXISTS auth_schema;

-- Users table
CREATE TABLE auth_schema.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(200),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE auth_schema.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- User-Roles junction table
CREATE TABLE auth_schema.user_roles (
    user_id UUID REFERENCES auth_schema.users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES auth_schema.roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Indexes
CREATE INDEX idx_users_username ON auth_schema.users(username);
CREATE INDEX idx_users_email ON auth_schema.users(email);
CREATE INDEX idx_users_active ON auth_schema.users(active);

-- Insert default roles
INSERT INTO auth_schema.roles (name, description) VALUES
('ADMIN', 'Súper Usuario'),
('ADMISSION', 'Personal de Admisión'),
('VITAL_SIGNS', 'Personal de Signos Vitales'),
('DOCTOR', 'Médico'),
('LABORATORY', 'Personal de Laboratorio'),
('PHARMACY', 'Farmacéutico'),
('CASHIER', 'Cajero'),
('PATIENT', 'Paciente');
```

---

## 6. Security Design

### 6.1 Password Hashing

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Strength 10
    }
}
```

### 6.2 JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-key-change-in-production-256-bits-minimum}
  expiration: 86400000 # 24 hours in milliseconds
```

### 6.3 Rate Limiting (Login)

```java
@Component
public class LoginRateLimiter {
    
    private final Map<String, AttemptTracker> attempts = new ConcurrentHashMap<>();
    
    public void checkRateLimit(String ipAddress) {
        AttemptTracker tracker = attempts.computeIfAbsent(ipAddress, 
            k -> new AttemptTracker());
        
        if (tracker.getAttempts() >= 5) {
            throw new TooManyAttemptsException("Too many login attempts");
        }
        
        tracker.increment();
    }
    
    public void resetAttempts(String ipAddress) {
        attempts.remove(ipAddress);
    }
}
```

---

## 7. Technology Stack

### Dependencies

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- Eureka Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

---

## 8. Configuration

### application.yml

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:postgresql://localhost:5432/medflow_db?currentSchema=auth_schema
    username: ${DB_USERNAME:medflow_user}
    password: ${DB_PASSWORD:medflow_pass}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: auth_schema

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}

jwt:
  secret: ${JWT_SECRET:default-secret-key-change-in-production}
  expiration: 86400000 # 24 hours

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

---

**Created**: April 13, 2026  
**Author**: MedFlow Team  
**Status**: ✅ Approved  
**Version**: 1.0.0
