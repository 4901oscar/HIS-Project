# Design: API Gateway

## 1. Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 Frontend (React)                         │
│              http://localhost:3000                       │
└────────────────────────┬────────────────────────────────┘
                         │
                         │ HTTP Requests
                         │ Authorization: Bearer <JWT>
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              API GATEWAY (Port 8080)                     │
│  ┌───────────────────────────────────────────────────┐  │
│  │  1. Rate Limit Check                              │  │
│  │  2. CORS Handling                                 │  │
│  │  3. JWT Validation (if required)                  │  │
│  │  4. Service Discovery (Eureka)                    │  │
│  │  5. Load Balancing                                │  │
│  │  6. Route to Service                              │  │
│  └───────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Eureka       │  │Auth Service  │  │Patient Svc   │
│ Server       │  │   (8081)     │  │   (8082)     │
│  (8761)      │  │              │  │              │
└──────────────┘  └──────────────┘  └──────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│Clinical Svc  │  │  Lab Service │  │Pharmacy Svc  │
│   (8083)     │  │   (8084)     │  │   (8085)     │
└──────────────┘  └──────────────┘  └──────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │Billing Svc   │
                  │   (8086)     │
                  └──────────────┘
```

### Request Flow

```
1. Frontend Request
   │
   ├─> GET /api/patients/123
   │   Header: Authorization: Bearer eyJhbGc...
   │
   ▼
2. API Gateway Receives
   │
   ├─> RateLimitFilter: Check rate limit
   │   └─> OK (< 100 req/min)
   │
   ├─> CorsFilter: Add CORS headers
   │   └─> OK
   │
   ├─> JwtAuthenticationFilter: Validate JWT
   │   ├─> Extract token from header
   │   ├─> Validate signature
   │   ├─> Check expiration
   │   └─> Extract claims (userId, roles)
   │
   ├─> ServiceDiscoveryFilter: Find service
   │   ├─> Query Eureka: "Where is patient-service?"
   │   └─> Eureka responds: "localhost:8082"
   │
   ├─> LoadBalancerFilter: Select instance
   │   └─> Round-robin if multiple instances
   │
   ▼
3. Forward to Service
   │
   ├─> Add headers:
   │   ├─> X-User-Id: 123
   │   ├─> X-User-Roles: DOCTOR,ADMIN
   │   └─> X-Request-Id: uuid-1234
   │
   └─> Forward to: http://localhost:8082/api/patients/123
   │
   ▼
4. Service Processes
   │
   └─> Patient Service handles request
   │
   ▼
5. Gateway Returns Response
   │
   └─> Return to frontend with CORS headers
```

## 2. Components

### 2.1 GatewayApplication (Main Class)

```java
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

**Responsibility**: Bootstrap the application

---

### 2.2 GatewayConfig (Route Configuration)

```java
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth Service
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .uri("lb://AUTH-SERVICE"))
            
            // Patient Service
            .route("patient-service", r -> r
                .path("/api/patients/**")
                .uri("lb://PATIENT-SERVICE"))
            
            // Clinical Service
            .route("clinical-service", r -> r
                .path("/api/clinical/**")
                .uri("lb://CLINICAL-SERVICE"))
            
            // Lab Service
            .route("lab-service", r -> r
                .path("/api/lab/**")
                .uri("lb://LAB-SERVICE"))
            
            // Pharmacy Service
            .route("pharmacy-service", r -> r
                .path("/api/pharmacy/**")
                .uri("lb://PHARMACY-SERVICE"))
            
            // Billing Service
            .route("billing-service", r -> r
                .path("/api/billing/**")
                .uri("lb://BILLING-SERVICE"))
            
            .build();
    }
}
```

**Responsibility**: 
- Define routing rules
- Use Eureka for service discovery (`lb://SERVICE-NAME`)
- Load balancing automático

---

### 2.3 JwtAuthenticationFilter (JWT Validation)

```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    private final JwtValidator jwtValidator;
    private final List<String> publicPaths = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/actuator/health"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip JWT validation for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // Extract JWT token
        String token = extractToken(exchange.getRequest());
        
        if (token == null) {
            return onError(exchange, "Token not provided", HttpStatus.UNAUTHORIZED);
        }
        
        // Validate JWT
        try {
            Claims claims = jwtValidator.validateAndGetClaims(token);
            
            // Add user info to headers
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", claims.get("userId", String.class))
                .header("X-User-Roles", claims.get("roles", String.class))
                .header("X-Request-Id", UUID.randomUUID().toString())
                .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (JwtException e) {
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }
    
    @Override
    public int getOrder() {
        return -100; // Execute before routing
    }
}
```

**Responsibility**:
- Extract JWT from Authorization header
- Validate JWT signature and expiration
- Extract claims (userId, roles)
- Add user info to request headers
- Skip validation for public paths

---

### 2.4 JwtValidator (JWT Validation Logic)

```java
@Component
public class JwtValidator {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token expired");
        } catch (SignatureException e) {
            throw new JwtException("Invalid token signature");
        } catch (Exception e) {
            throw new JwtException("Invalid token");
        }
    }
    
    public boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
```

**Responsibility**:
- Parse and validate JWT
- Check signature
- Check expiration
- Extract claims

---

### 2.5 RateLimitFilter (Rate Limiting)

```java
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {
    
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = getClientIp(exchange.getRequest());
        RateLimitBucket bucket = buckets.computeIfAbsent(clientIp, 
            k -> new RateLimitBucket(MAX_REQUESTS, WINDOW));
        
        if (!bucket.tryConsume()) {
            return onError(exchange, "Too many requests", HttpStatus.TOO_MANY_REQUESTS);
        }
        
        // Add rate limit headers
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
        response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining()));
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -200; // Execute before JWT validation
    }
}
```

**Responsibility**:
- Track requests per IP
- Limit to 100 requests per minute
- Add rate limit headers
- Return 429 if exceeded

---

### 2.6 CorsConfiguration (CORS Handling)

```java
@Configuration
public class CorsConfiguration {
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
```

**Responsibility**:
- Allow requests from localhost:3000
- Allow all HTTP methods
- Allow all headers
- Handle preflight requests

---

### 2.7 GlobalExceptionHandler (Error Handling)

```java
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        ErrorResponse errorResponse;
        
        if (ex instanceof JwtException) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            errorResponse = new ErrorResponse("Unauthorized", ex.getMessage());
        } else if (ex instanceof ServiceUnavailableException) {
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            errorResponse = new ErrorResponse("Service Unavailable", ex.getMessage());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            errorResponse = new ErrorResponse("Internal Server Error", "An error occurred");
        }
        
        byte[] bytes = new ObjectMapper().writeValueAsBytes(errorResponse);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        
        return response.writeWith(Mono.just(buffer));
    }
}
```

**Responsibility**:
- Handle all exceptions globally
- Return consistent error format
- Map exceptions to HTTP status codes

---

## 3. Data Model

### 3.1 ErrorResponse (DTO)

```java
public class ErrorResponse {
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    
    // Constructor, getters, setters
}
```

### 3.2 RateLimitBucket (Internal)

```java
public class RateLimitBucket {
    private final int maxRequests;
    private final Duration window;
    private int remaining;
    private Instant windowStart;
    
    public synchronized boolean tryConsume() {
        resetIfNeeded();
        if (remaining > 0) {
            remaining--;
            return true;
        }
        return false;
    }
    
    private void resetIfNeeded() {
        if (Instant.now().isAfter(windowStart.plus(window))) {
            remaining = maxRequests;
            windowStart = Instant.now();
        }
    }
}
```

---

## 4. API Design

### 4.1 Gateway Routes

El gateway NO expone endpoints propios (excepto actuator). Solo enruta:

| Frontend Path | Gateway Path | Target Service | Target Port | Arquitectura | Base de Datos |
|---------------|--------------|----------------|-------------|--------------|---------------|
| /api/auth/login | /api/auth/login | auth-service | 8081 | MVC | auth_schema |
| /api/patients/123 | /api/patients/123 | patient-service | 8082 | MVC | patient_schema |
| /api/clinical/appointments | /api/clinical/appointments | clinical-service | 8083 | **Hexagonal** | clinical_schema |
| /api/lab/orders | /api/lab/orders | lab-service | 8084 | MVC | lab_schema |
| /api/pharmacy/medications | /api/pharmacy/medications | pharmacy-service | 8085 | MVC | pharmacy_schema |
| /api/billing/invoices | /api/billing/invoices | billing-service | 8086 | MVC | billing_schema |

**Nota**: 
- Clinical Service usa Arquitectura Hexagonal debido a su lógica de negocio pesada (Triaje Manchester, cálculo de slots con Redis)
- Todos los demás servicios usan arquitectura MVC (Capas) para CRUD simple
- Base de datos: PostgreSQL con Schema-per-Service (Monolito Lógico)
- **Regla estricta**: CERO JOINs entre esquemas - comunicación vía APIs HTTP

### 4.2 Request Headers (Added by Gateway)

```
X-User-Id: 123                    # User ID from JWT
X-User-Roles: DOCTOR,ADMIN        # User roles from JWT
X-Request-Id: uuid-1234-5678      # Unique request ID for tracing
X-Forwarded-For: 192.168.1.100    # Original client IP
```

### 4.3 Response Headers (Added by Gateway)

```
X-RateLimit-Limit: 100            # Max requests per window
X-RateLimit-Remaining: 95         # Remaining requests
Access-Control-Allow-Origin: *    # CORS
Access-Control-Allow-Methods: *   # CORS
```

### 4.4 Error Responses

**401 Unauthorized (Token missing)**
```json
{
  "error": "Unauthorized",
  "message": "Token not provided",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

**401 Unauthorized (Token invalid)**
```json
{
  "error": "Unauthorized",
  "message": "Invalid token",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

**429 Too Many Requests**
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

**503 Service Unavailable**
```json
{
  "error": "Service Unavailable",
  "message": "patient-service is not available",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

---

## 5. Technology Stack

### Core Dependencies

```xml
<!-- Spring Cloud Gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- Eureka Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
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

<!-- Spring Boot Actuator -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-contract-wiremock</artifactId>
    <scope>test</scope>
</dependency>
```

### Versions

- **Spring Boot**: 3.2.4
- **Spring Cloud**: 2023.0.1
- **Java**: 17
- **JJWT**: 0.11.5

---

## 6. Security Design

### 6.1 JWT Validation Flow

```
1. Extract token from header
   Authorization: Bearer eyJhbGc...
   │
   ▼
2. Parse JWT
   │
   ├─> Verify signature with secret key
   │   └─> If invalid → 401
   │
   ├─> Check expiration
   │   └─> If expired → 401
   │
   └─> Extract claims
       ├─> userId
       ├─> roles
       └─> exp
   │
   ▼
3. Add to request headers
   X-User-Id: 123
   X-User-Roles: DOCTOR,ADMIN
```

### 6.2 Public Paths (No JWT Required)

```java
/api/auth/login          # Login endpoint
/api/auth/register       # Registration (if exists)
/actuator/health         # Health check
/actuator/info           # Info endpoint
```

### 6.3 Security Headers

```yaml
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000
```

### 6.4 Secret Management

```yaml
# application.yml (local)
jwt:
  secret: ${JWT_SECRET:default-secret-key-change-in-production}

# application-docker.yml
jwt:
  secret: ${JWT_SECRET}
```

**NEVER commit real secrets to git!**

---

## 7. Error Handling

### 7.1 Exception Hierarchy

```
RuntimeException
├── JwtException
│   ├── TokenExpiredException
│   ├── InvalidTokenException
│   └── TokenNotFoundException
├── RateLimitExceededException
└── ServiceUnavailableException
```

### 7.2 Error Response Format

```json
{
  "error": "Error Type",
  "message": "Detailed message",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/resource"
}
```

### 7.3 HTTP Status Codes

| Status | When |
|--------|------|
| 200 OK | Successful request |
| 401 Unauthorized | JWT missing/invalid/expired |
| 404 Not Found | Route not found |
| 429 Too Many Requests | Rate limit exceeded |
| 500 Internal Server Error | Gateway error |
| 503 Service Unavailable | Target service down |

---

## 8. Testing Strategy

### 8.1 Unit Tests

**What to test:**
- `JwtValidator`: Token validation logic
- `RateLimitBucket`: Rate limiting logic
- `JwtAuthenticationFilter`: JWT extraction and validation
- `RateLimitFilter`: Rate limit checking

**Coverage target**: 90%

**Example:**
```java
@Test
void shouldValidateValidJwt() {
    String validToken = generateValidToken();
    Claims claims = jwtValidator.validateAndGetClaims(validToken);
    assertNotNull(claims);
    assertEquals("123", claims.get("userId"));
}

@Test
void shouldRejectExpiredJwt() {
    String expiredToken = generateExpiredToken();
    assertThrows(JwtException.class, () -> {
        jwtValidator.validateAndGetClaims(expiredToken);
    });
}
```

### 8.2 Integration Tests

**What to test:**
- Routing to services (using WireMock)
- JWT validation end-to-end
- CORS headers
- Rate limiting
- Error responses

**Coverage target**: 80%

**Example:**
```java
@SpringBootTest
@AutoConfigureWebTestClient
class GatewayIntegrationTest {
    
    @Autowired
    private WebTestClient webClient;
    
    @Test
    void shouldRouteToAuthService() {
        webClient.get()
            .uri("/api/auth/test")
            .header("Authorization", "Bearer " + validToken)
            .exchange()
            .expectStatus().isOk();
    }
    
    @Test
    void shouldReturn401WithoutToken() {
        webClient.get()
            .uri("/api/patients/123")
            .exchange()
            .expectStatus().isUnauthorized();
    }
}
```

### 8.3 E2E Tests

**Scenarios:**
1. **Happy Path**: Frontend → Gateway → Service → Response
2. **Authentication**: Invalid JWT → 401
3. **Rate Limiting**: 101 requests → 429
4. **Service Down**: Service unavailable → 503

---

## 9. Performance Considerations

### 9.1 Optimization Strategies

- **Connection Pooling**: Reuse connections to services
- **Async Processing**: Use reactive programming (WebFlux)
- **Caching**: Cache Eureka service list (30s TTL)
- **Timeout**: 30s timeout for service responses

### 9.2 Monitoring

**Metrics to track:**
- Request count per route
- Response time (p50, p95, p99)
- Error rate
- Rate limit hits
- JWT validation failures

**Actuator endpoints:**
```
/actuator/health          # Health status
/actuator/metrics         # All metrics
/actuator/metrics/http.server.requests  # Request metrics
```

---

## 10. Deployment

### 10.1 Environment Variables

```bash
# Eureka
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# JWT
JWT_SECRET=your-secret-key-256-bits

# Server
SERVER_PORT=8080

# Spring Profile
SPRING_PROFILES_ACTIVE=local
```

### 10.2 application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
  instance:
    prefer-ip-address: true

jwt:
  secret: ${JWT_SECRET:default-secret-change-me}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

### 10.3 Docker Configuration

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

**Created**: April 10, 2026  
**Author**: MedFlow Team  
**Reviewed by**: Tech Lead  
**Status**: ✅ Approved  
**Version**: 1.0.0
