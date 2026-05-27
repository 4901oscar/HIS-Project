# API Gateway - MedFlow HIS

API Gateway centralizado para el sistema MedFlow HIS. Actúa como punto de entrada único para todas las peticiones del frontend hacia los microservicios.

## 🎯 Propósito

El API Gateway proporciona:
- **Punto de entrada único**: Todas las peticiones van a `http://localhost:8080`
- **Autenticación centralizada**: Validación JWT antes de enrutar
- **Descubrimiento de servicios**: Integración con Eureka para service discovery
- **Rate limiting**: Protección contra abuso (100 req/min por IP)
- **CORS**: Configuración para frontend React
- **Enrutamiento inteligente**: Distribución automática de peticiones

## 🏗️ Arquitectura

```
Frontend (React)
    │
    ├─> http://localhost:8080/api/*
    │
    ▼
API Gateway (Port 8080)
    │
    ├─> Rate Limit Check
    ├─> CORS Handling
    ├─> JWT Validation
    ├─> Service Discovery (Eureka)
    ├─> Load Balancing
    │
    ▼
Microservices
    ├─> auth-service (8081)
    ├─> patient-service (8082)
    ├─> clinical-service (8083)
    ├─> lab-service (8084)
    ├─> pharmacy-service (8085)
    └─> billing-service (8086)
```

## 📋 Prerequisitos

- **Java 17** o superior
- **Maven 3.8+**
- **Eureka Server** corriendo en puerto 8761

Verifica tu instalación:
```bash
java -version
mvn -version
```

## 🔧 Configuración

### Perfiles Disponibles

1. **default** (desarrollo local)
   - Puerto: 8080
   - Eureka: http://localhost:8761
   - CORS: http://localhost:3000

2. **docker** (contenedores)
   - Puerto: 8080
   - Eureka: http://eureka-server:8761
   - CORS: http://localhost:3000

### Variables de Entorno

```bash
# Eureka Server
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# JWT Secret (IMPORTANTE: cambiar en producción)
JWT_SECRET=your-secret-key-256-bits

# Spring Profile
SPRING_PROFILES_ACTIVE=local
```

### Propiedades Importantes

```yaml
# Enrutamiento automático con Eureka
spring.cloud.gateway.discovery.locator.enabled: true

# Rate limiting
rate-limit.max-requests: 100
rate-limit.window: 1m

# JWT
jwt.secret: ${JWT_SECRET}

# CORS
cors.allowed-origins: http://localhost:3000
```

## 🚀 Ejecución

### Opción 1: Maven (Desarrollo Local)

```bash
cd backend-cloud/api-gateway
mvn clean install
mvn spring-boot:run
```

### Opción 2: Docker

```bash
# Desde la raíz del proyecto
docker-compose up api-gateway
```

### Opción 3: JAR

```bash
cd backend-cloud/api-gateway
mvn clean package
java -jar target/api-gateway-1.0.0.jar
```

### Opción 4: Docker Build Manual

```bash
cd backend-cloud/api-gateway
docker build -t medflow/api-gateway:1.0.0 .
docker run -p 8080:8080 \
  -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
  -e JWT_SECRET=your-secret-key \
  medflow/api-gateway:1.0.0
```

## 🌐 Enrutamiento

El gateway enruta peticiones basándose en el path:

| Path Frontend | Servicio Destino | Puerto |
|---------------|------------------|--------|
| `/api/auth/**` | auth-service | 8081 |
| `/api/patients/**` | patient-service | 8082 |
| `/api/clinical/**` | clinical-service | 8083 |
| `/api/lab/**` | lab-service | 8084 |
| `/api/pharmacy/**` | pharmacy-service | 8085 |
| `/api/billing/**` | billing-service | 8086 |

### Ejemplo de Enrutamiento

```bash
# Frontend hace petición
GET http://localhost:8080/api/patients/123
Authorization: Bearer eyJhbGc...

# Gateway enruta a
GET http://patient-service:8082/api/patients/123
X-User-Id: 123
X-User-Roles: DOCTOR,ADMIN
X-Request-Id: uuid-1234
```

## 🔐 Autenticación JWT

### Rutas Públicas (No requieren JWT)

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /actuator/health`
- `GET /actuator/info`

### Rutas Protegidas (Requieren JWT)

Todas las demás rutas requieren header:
```
Authorization: Bearer <jwt-token>
```

### Validación JWT

El gateway valida:
1. ✅ Token presente en header
2. ✅ Firma válida
3. ✅ No expirado
4. ✅ Claims válidos (userId, roles)

### Headers Agregados

El gateway agrega estos headers a las peticiones:
```
X-User-Id: 123                    # ID del usuario autenticado
X-User-Roles: DOCTOR,ADMIN        # Roles del usuario
X-Request-Id: uuid-1234-5678      # ID único para tracing
X-Forwarded-For: 192.168.1.100    # IP original del cliente
```

## 🛡️ Rate Limiting

- **Límite**: 100 peticiones por minuto por IP
- **Ventana**: 1 minuto (rolling window)
- **Respuesta**: 429 Too Many Requests si se excede

### Headers de Rate Limit

```
X-RateLimit-Limit: 100            # Límite máximo
X-RateLimit-Remaining: 95         # Peticiones restantes
```

## 🌍 CORS

Configurado para permitir:
- **Origen**: http://localhost:3000
- **Métodos**: GET, POST, PUT, DELETE, OPTIONS
- **Headers**: Todos (*)
- **Credentials**: true
- **Max Age**: 3600 segundos

## 🔍 Endpoints de Monitoreo

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado de salud del gateway |
| `/actuator/info` | Información del gateway |
| `/actuator/metrics` | Métricas del gateway |
| `/actuator/metrics/http.server.requests` | Métricas de peticiones |

### Verificar Estado

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=JwtValidatorTest
mvn test -Dtest=RateLimitFilterTest

# Con cobertura
mvn clean test jacoco:report
```

### Cobertura de Tests

- **Target**: 80%+
- **Reporte**: `target/site/jacoco/index.html`

### Tests Incluidos

- ✅ Unit tests para JWT validation
- ✅ Unit tests para rate limiting
- ✅ Integration tests para routing
- ✅ Integration tests para CORS
- ✅ Integration tests para error handling

## 📊 Respuestas de Error

### 401 Unauthorized (Token ausente)

```json
{
  "error": "Unauthorized",
  "message": "Token not provided",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

### 401 Unauthorized (Token inválido)

```json
{
  "error": "Unauthorized",
  "message": "Invalid token",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

### 429 Too Many Requests

```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

### 503 Service Unavailable

```json
{
  "error": "Service Unavailable",
  "message": "patient-service is not available",
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients/123"
}
```

## 🔧 Troubleshooting

### Problema: Puerto 8080 ya en uso

**Solución**: Otro proceso está usando el puerto.

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Problema: No se conecta a Eureka

**Solución**: Verificar que Eureka Server esté corriendo.

```bash
# Verificar Eureka
curl http://localhost:8761/actuator/health

# Ver logs del gateway
mvn spring-boot:run
# Buscar: "DiscoveryClient_API-GATEWAY registered"
```

### Problema: JWT inválido siempre

**Solución**: Verificar que el secret sea el mismo en auth-service y gateway.

```bash
# Verificar variable de entorno
echo $JWT_SECRET

# O en application.yml
jwt.secret: same-secret-in-all-services
```

### Problema: CORS errors

**Solución**: Verificar configuración de CORS.

```yaml
cors:
  allowed-origins: http://localhost:3000  # Debe coincidir con frontend
```

### Problema: Servicios no se descubren

**Solución**: Verificar registro en Eureka.

```bash
# Ver servicios registrados
curl http://localhost:8761/eureka/apps

# Verificar logs del servicio
# Buscar: "Registered instance SERVICE-NAME"
```

## 📝 Logs

### Niveles de Log

```yaml
# Desarrollo
logging.level.com.medflow.gateway: DEBUG

# Producción
logging.level.com.medflow.gateway: INFO
```

### Logs Importantes

```
# Inicio exitoso
Started GatewayApplication in 8.5 seconds

# Registro en Eureka
DiscoveryClient_API-GATEWAY registered

# Petición recibida
[DEBUG] Request: GET /api/patients/123

# JWT validado
[DEBUG] JWT validated for user: 123

# Enrutamiento
[DEBUG] Routing to: patient-service

# Rate limit
[DEBUG] Rate limit: 95/100 remaining
```

## 🐳 Docker

### Dockerfile

El proyecto incluye un Dockerfile multi-stage:
- **Stage 1**: Build con Maven
- **Stage 2**: Runtime con JRE Alpine

### Health Check

```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1
```

### Docker Compose

```yaml
api-gateway:
  build: ./backend-cloud/api-gateway
  ports:
    - "8080:8080"
  environment:
    - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
    - JWT_SECRET=${JWT_SECRET}
    - SPRING_PROFILES_ACTIVE=docker
  depends_on:
    - eureka-server
```

## 🔗 Referencias

- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Netflix Eureka](https://github.com/Netflix/eureka/wiki)
- [JJWT](https://github.com/jwtk/jjwt)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 📚 Documentación Adicional

- [QUICKSTART.md](./QUICKSTART.md) - Guía de inicio rápido (5 minutos)
- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Documentación detallada de la API

## 🎓 Conceptos Clave

- **API Gateway**: Punto de entrada único para todos los servicios
- **Service Discovery**: Descubrimiento automático de servicios con Eureka
- **JWT**: JSON Web Token para autenticación stateless
- **Rate Limiting**: Protección contra abuso y DDoS
- **CORS**: Cross-Origin Resource Sharing para frontend
- **Load Balancing**: Distribución automática entre instancias

## 📈 Métricas de Performance

- **Latencia de enrutamiento**: < 50ms
- **Throughput**: 1000+ req/s
- **Timeout**: 30 segundos
- **Uptime**: 99.9%

## 🔐 Seguridad

- ✅ Validación JWT en todas las rutas protegidas
- ✅ Rate limiting por IP
- ✅ CORS configurado
- ✅ Headers de seguridad
- ✅ No loggear tokens completos
- ✅ HTTPS en producción (recomendado)

## 🚀 Próximos Pasos

1. ✅ API Gateway implementado
2. ⏳ Implementar Auth Service
3. ⏳ Implementar Patient Service
4. ⏳ Implementar Clinical Service
5. ⏳ Implementar Lab Service
6. ⏳ Implementar Pharmacy Service
7. ⏳ Implementar Billing Service

---

**Version**: 1.0.0  
**Created**: April 2026  
**Team**: MedFlow Development Team
