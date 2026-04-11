# API Gateway - Documentación de la API

## 📋 Tabla de Contenidos

1. [Descripción General](#descripción-general)
2. [Enrutamiento](#enrutamiento)
3. [Autenticación JWT](#autenticación-jwt)
4. [Rate Limiting](#rate-limiting)
5. [CORS](#cors)
6. [Headers](#headers)
7. [Respuestas de Error](#respuestas-de-error)
8. [Endpoints de Monitoreo](#endpoints-de-monitoreo)
9. [Ejemplos de Uso](#ejemplos-de-uso)

---

## Descripción General

El API Gateway es el punto de entrada único para todas las peticiones del frontend hacia los microservicios. Proporciona:

- **Enrutamiento centralizado**: Todas las peticiones van a `http://localhost:8080`
- **Autenticación JWT**: Validación de tokens antes de enrutar
- **Service Discovery**: Descubrimiento automático de servicios con Eureka
- **Rate Limiting**: Protección contra abuso (100 req/min por IP)
- **CORS**: Configuración para frontend React
- **Load Balancing**: Distribución automática entre instancias

**Base URL**: `http://localhost:8080`

---

## Enrutamiento

El gateway enruta peticiones basándose en el path de la URL.

### Tabla de Enrutamiento

| Path Frontend | Servicio Destino | Puerto | Descripción |
|---------------|------------------|--------|-------------|
| `/api/auth/**` | auth-service | 8081 | Autenticación y autorización |
| `/api/patients/**` | patient-service | 8082 | Gestión de pacientes |
| `/api/clinical/**` | clinical-service | 8083 | Datos clínicos y citas |
| `/api/lab/**` | lab-service | 8084 | Órdenes de laboratorio |
| `/api/pharmacy/**` | pharmacy-service | 8085 | Medicamentos y recetas |
| `/api/billing/**` | billing-service | 8086 | Facturación y pagos |

### Ejemplo de Enrutamiento

```
Frontend Request:
GET http://localhost:8080/api/patients/123

Gateway Routes To:
GET http://patient-service:8082/api/patients/123
```

### Service Discovery

El gateway usa Eureka para descubrir servicios dinámicamente:

```yaml
# Configuración
spring.cloud.gateway.discovery.locator.enabled: true

# El gateway consulta a Eureka:
# "¿Dónde está patient-service?"
# Eureka responde: "localhost:8082"
```

### Load Balancing

Si hay múltiples instancias de un servicio, el gateway distribuye las peticiones usando **round-robin**:

```
patient-service:
  - Instance 1: localhost:8082
  - Instance 2: localhost:8092

Request 1 → Instance 1
Request 2 → Instance 2
Request 3 → Instance 1
...
```

---

## Autenticación JWT

### Rutas Públicas (No requieren JWT)

Estas rutas NO requieren autenticación:

- `POST /api/auth/login` - Login de usuario
- `POST /api/auth/register` - Registro de usuario (si existe)
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Información del servicio

### Rutas Protegidas (Requieren JWT)

Todas las demás rutas requieren un token JWT válido en el header `Authorization`.

### Formato del Token

```
Authorization: Bearer <jwt-token>
```

### Ejemplo

```bash
curl http://localhost:8080/api/patients/123 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Validación JWT

El gateway valida:

1. ✅ **Token presente**: Header `Authorization` existe
2. ✅ **Formato correcto**: `Bearer <token>`
3. ✅ **Firma válida**: Token firmado con el secret correcto
4. ✅ **No expirado**: `exp` claim no ha pasado
5. ✅ **Claims válidos**: Contiene `userId` y `roles`

### Claims Esperados

```json
{
  "userId": "123",
  "username": "doctor1",
  "roles": "DOCTOR,ADMIN",
  "exp": 1712779200
}
```

### Flujo de Validación

```
1. Frontend envía petición con JWT
   │
   ▼
2. Gateway extrae token del header
   │
   ▼
3. Gateway valida firma y expiración
   │
   ├─> Si inválido → 401 Unauthorized
   │
   ▼
4. Gateway extrae claims (userId, roles)
   │
   ▼
5. Gateway agrega headers a la petición
   │
   ├─> X-User-Id: 123
   ├─> X-User-Roles: DOCTOR,ADMIN
   └─> X-Request-Id: uuid-1234
   │
   ▼
6. Gateway enruta al servicio destino
```

### Errores de Autenticación

| Escenario | Status Code | Response |
|-----------|-------------|----------|
| Token ausente | 401 | `{"error": "Unauthorized", "message": "Token not provided"}` |
| Token inválido | 401 | `{"error": "Unauthorized", "message": "Invalid token"}` |
| Token expirado | 401 | `{"error": "Unauthorized", "message": "Token expired"}` |
| Firma inválida | 401 | `{"error": "Unauthorized", "message": "Invalid token signature"}` |

---

## Rate Limiting

El gateway implementa rate limiting para proteger contra abuso y ataques DDoS.

### Configuración

- **Límite**: 100 peticiones por minuto
- **Ventana**: 1 minuto (rolling window)
- **Identificador**: IP del cliente
- **Respuesta**: 429 Too Many Requests si se excede

### Headers de Rate Limit

El gateway agrega estos headers a todas las respuestas:

```
X-RateLimit-Limit: 100            # Límite máximo
X-RateLimit-Remaining: 95         # Peticiones restantes en la ventana
```

### Ejemplo

```bash
# Petición 1
curl -i http://localhost:8080/actuator/health
# Response headers:
# X-RateLimit-Limit: 100
# X-RateLimit-Remaining: 99

# Petición 100
curl -i http://localhost:8080/actuator/health
# X-RateLimit-Remaining: 0

# Petición 101
curl -i http://localhost:8080/actuator/health
# HTTP/1.1 429 Too Many Requests
# {"error": "Too Many Requests", "message": "Rate limit exceeded"}
```

### Reseteo de Contador

El contador se resetea cada minuto (rolling window):

```
20:00:00 - Request 1 (99 remaining)
20:00:30 - Request 50 (50 remaining)
20:01:00 - Request 1 resetea (99 remaining)
```

### Limitaciones

- **Fase 1**: Rate limiting en memoria (solo funciona con una instancia del gateway)
- **Fase 2**: Rate limiting distribuido con Redis (múltiples instancias)

---

## CORS

El gateway está configurado para permitir peticiones cross-origin desde el frontend React.

### Configuración

```yaml
cors:
  allowed-origins: http://localhost:3000
  allowed-methods: GET, POST, PUT, DELETE, OPTIONS
  allowed-headers: *
  allow-credentials: true
  max-age: 3600
```

### Headers CORS

El gateway agrega estos headers a las respuestas:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

### Preflight Requests

El gateway maneja automáticamente las peticiones OPTIONS (preflight):

```bash
# Preflight request
curl -X OPTIONS http://localhost:8080/api/patients/123 \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET"

# Response:
# HTTP/1.1 200 OK
# Access-Control-Allow-Origin: http://localhost:3000
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
```

### Ejemplo desde Frontend

```javascript
// Fetch desde React (http://localhost:3000)
fetch('http://localhost:8080/api/patients/123', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  credentials: 'include'
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## Headers

### Headers de Request (Agregados por el Gateway)

El gateway agrega estos headers a las peticiones antes de enrutarlas:

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| `X-User-Id` | ID del usuario autenticado (del JWT) | `123` |
| `X-User-Roles` | Roles del usuario (del JWT) | `DOCTOR,ADMIN` |
| `X-Request-Id` | ID único para tracing | `uuid-1234-5678` |
| `X-Forwarded-For` | IP original del cliente | `192.168.1.100` |

### Headers de Response (Agregados por el Gateway)

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| `X-RateLimit-Limit` | Límite máximo de peticiones | `100` |
| `X-RateLimit-Remaining` | Peticiones restantes | `95` |
| `Access-Control-Allow-Origin` | CORS origin permitido | `http://localhost:3000` |
| `Access-Control-Allow-Methods` | CORS métodos permitidos | `GET, POST, PUT, DELETE, OPTIONS` |

### Ejemplo Completo

```bash
# Request del frontend
GET http://localhost:8080/api/patients/123
Authorization: Bearer eyJhbGc...

# Request al servicio (con headers agregados)
GET http://patient-service:8082/api/patients/123
Authorization: Bearer eyJhbGc...
X-User-Id: 123
X-User-Roles: DOCTOR,ADMIN
X-Request-Id: uuid-1234-5678
X-Forwarded-For: 192.168.1.100

# Response del servicio
HTTP/1.1 200 OK
Content-Type: application/json
{"id": 123, "name": "John Doe"}

# Response al frontend (con headers agregados)
HTTP/1.1 200 OK
Content-Type: application/json
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
Access-Control-Allow-Origin: http://localhost:3000
{"id": 123, "name": "John Doe"}
```

---

## Respuestas de Error

El gateway retorna respuestas de error consistentes en formato JSON.

### Formato de Error

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/resource"
}
```

### Códigos de Estado HTTP

| Status Code | Descripción | Cuándo ocurre |
|-------------|-------------|---------------|
| 200 OK | Petición exitosa | Servicio retorna 200 |
| 401 Unauthorized | No autorizado | JWT ausente, inválido o expirado |
| 404 Not Found | No encontrado | Ruta no existe |
| 429 Too Many Requests | Demasiadas peticiones | Rate limit excedido |
| 500 Internal Server Error | Error del servidor | Error interno del gateway |
| 503 Service Unavailable | Servicio no disponible | Servicio destino no responde |

### Ejemplos de Errores

#### 401 Unauthorized - Token Ausente

```json
{
  "error": "Unauthorized",
  "message": "Token not provided",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

```bash
curl http://localhost:8080/api/patients/123
# HTTP/1.1 401 Unauthorized
```

#### 401 Unauthorized - Token Inválido

```json
{
  "error": "Unauthorized",
  "message": "Invalid token",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

```bash
curl http://localhost:8080/api/patients/123 \
  -H "Authorization: Bearer invalid-token"
# HTTP/1.1 401 Unauthorized
```

#### 401 Unauthorized - Token Expirado

```json
{
  "error": "Unauthorized",
  "message": "Token expired",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

#### 429 Too Many Requests

```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

```bash
# Después de 100 peticiones en 1 minuto
curl http://localhost:8080/api/patients/123
# HTTP/1.1 429 Too Many Requests
```

#### 503 Service Unavailable

```json
{
  "error": "Service Unavailable",
  "message": "patient-service is not available",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

```bash
# Cuando el servicio destino no está corriendo
curl http://localhost:8080/api/patients/123 \
  -H "Authorization: Bearer valid-token"
# HTTP/1.1 503 Service Unavailable
```

#### 404 Not Found

```json
{
  "error": "Not Found",
  "message": "Route not found",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/unknown/123"
}
```

```bash
curl http://localhost:8080/api/unknown/123
# HTTP/1.1 404 Not Found
```

---

## Endpoints de Monitoreo

El gateway expone endpoints de Spring Boot Actuator para monitoreo.

### Health Check

**Endpoint**: `GET /actuator/health`

**Descripción**: Verifica el estado de salud del gateway.

**Respuesta**:
```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "status": "UP",
      "components": {
        "eureka": {
          "status": "UP",
          "details": {
            "applications": {
              "API-GATEWAY": 1,
              "PATIENT-SERVICE": 1
            }
          }
        }
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Ejemplo**:
```bash
curl http://localhost:8080/actuator/health
```

### Info

**Endpoint**: `GET /actuator/info`

**Descripción**: Información del gateway.

**Respuesta**:
```json
{
  "app": {
    "name": "api-gateway",
    "version": "1.0.0"
  }
}
```

### Metrics

**Endpoint**: `GET /actuator/metrics`

**Descripción**: Lista de métricas disponibles.

**Respuesta**:
```json
{
  "names": [
    "http.server.requests",
    "jvm.memory.used",
    "jvm.threads.live",
    "system.cpu.usage"
  ]
}
```

### HTTP Metrics

**Endpoint**: `GET /actuator/metrics/http.server.requests`

**Descripción**: Métricas de peticiones HTTP.

**Respuesta**:
```json
{
  "name": "http.server.requests",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 1234
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 45.678
    }
  ],
  "availableTags": [
    {
      "tag": "uri",
      "values": ["/api/patients/**", "/api/auth/**"]
    },
    {
      "tag": "status",
      "values": ["200", "401", "429"]
    }
  ]
}
```

**Filtrar por URI**:
```bash
curl "http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/patients/**"
```

---

## Ejemplos de Uso

### Ejemplo 1: Login y Petición Protegida

```bash
# 1. Login (ruta pública)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctor1",
    "password": "password123"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "userId": "123",
#   "username": "doctor1",
#   "roles": ["DOCTOR", "ADMIN"]
# }

# 2. Guardar token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 3. Hacer petición protegida
curl http://localhost:8080/api/patients/123 \
  -H "Authorization: Bearer $TOKEN"

# Response:
# {
#   "id": 123,
#   "name": "John Doe",
#   "age": 45
# }
```

### Ejemplo 2: CORS desde Frontend

```javascript
// React component
const fetchPatient = async (patientId) => {
  const token = localStorage.getItem('token');
  
  try {
    const response = await fetch(
      `http://localhost:8080/api/patients/${patientId}`,
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      }
    );
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching patient:', error);
    throw error;
  }
};
```

### Ejemplo 3: Rate Limiting

```bash
# Script para probar rate limiting
for i in {1..105}; do
  echo "Request $i"
  curl -i http://localhost:8080/actuator/health 2>&1 | grep -E "HTTP|X-RateLimit"
  sleep 0.1
done

# Output:
# Request 1
# HTTP/1.1 200 OK
# X-RateLimit-Limit: 100
# X-RateLimit-Remaining: 99
# ...
# Request 100
# HTTP/1.1 200 OK
# X-RateLimit-Remaining: 0
# Request 101
# HTTP/1.1 429 Too Many Requests
```

### Ejemplo 4: Verificar Enrutamiento

```bash
# 1. Verificar que el gateway está registrado en Eureka
curl http://localhost:8761/eureka/apps/API-GATEWAY

# 2. Verificar que patient-service está registrado
curl http://localhost:8761/eureka/apps/PATIENT-SERVICE

# 3. Hacer petición a través del gateway
curl http://localhost:8080/api/patients/123 \
  -H "Authorization: Bearer $TOKEN" \
  -v

# Los logs mostrarán:
# > GET /api/patients/123 HTTP/1.1
# > Host: localhost:8080
# < HTTP/1.1 200 OK
# < X-Forwarded-Host: localhost:8080
```

### Ejemplo 5: Manejo de Errores

```bash
# 1. Sin token (401)
curl -i http://localhost:8080/api/patients/123

# 2. Token inválido (401)
curl -i http://localhost:8080/api/patients/123 \
  -H "Authorization: Bearer invalid-token"

# 3. Servicio no disponible (503)
# (Detener patient-service primero)
curl -i http://localhost:8080/api/patients/123 \
  -H "Authorization: Bearer $TOKEN"

# 4. Ruta no encontrada (404)
curl -i http://localhost:8080/api/unknown/123 \
  -H "Authorization: Bearer $TOKEN"

# 5. Rate limit excedido (429)
# (Hacer 101 peticiones en 1 minuto)
```

---

## Configuración Avanzada

### Variables de Entorno

```bash
# Eureka Server
export EUREKA_SERVER_URL=http://localhost:8761/eureka/

# JWT Secret
export JWT_SECRET=your-secret-key-256-bits

# Server Port
export SERVER_PORT=8080

# Spring Profile
export SPRING_PROFILES_ACTIVE=local

# Rate Limiting
export RATE_LIMIT_MAX_REQUESTS=100
export RATE_LIMIT_WINDOW=1m
```

### Timeouts

```yaml
# application.yml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000      # 5 segundos
        response-timeout: 30000    # 30 segundos
```

### Retry

```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: patient-service
          uri: lb://PATIENT-SERVICE
          predicates:
            - Path=/api/patients/**
          filters:
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY
```

---

## Seguridad

### Headers de Seguridad

El gateway agrega estos headers de seguridad:

```
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000
```

### JWT Secret

**IMPORTANTE**: Cambiar el secret en producción.

```yaml
# Desarrollo
jwt:
  secret: default-secret-key-change-in-production

# Producción
jwt:
  secret: ${JWT_SECRET}  # Variable de entorno
```

### HTTPS

En producción, usar HTTPS:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

---

## Performance

### Métricas de Performance

- **Latencia de enrutamiento**: < 50ms
- **Throughput**: 1000+ req/s
- **Timeout**: 30 segundos
- **Connection pool**: 100 conexiones

### Optimizaciones

- **Connection pooling**: Reutilización de conexiones
- **Async processing**: Programación reactiva (WebFlux)
- **Caching**: Cache de lista de servicios de Eureka (30s TTL)

---

**Version**: 1.0.0  
**Created**: April 2026  
**Team**: MedFlow Development Team
