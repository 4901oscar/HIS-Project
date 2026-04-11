# Requirements: API Gateway

## 1. Overview

El API Gateway es el **punto de entrada único** para todas las peticiones del frontend hacia los microservicios del sistema MedFlow HIS. Actúa como un "recepcionista inteligente" que:
- Recibe todas las peticiones del frontend React
- Valida la autenticación (JWT)
- Enruta las peticiones al microservicio correcto
- Descubre servicios dinámicamente usando Eureka
- Implementa seguridad y rate limiting

## 2. User Stories

### US-1: Enrutamiento Centralizado
**Como** desarrollador frontend,  
**Quiero** enviar todas las peticiones a una sola URL (http://localhost:8080),  
**Para** no tener que conocer las IPs y puertos de cada microservicio.

**Acceptance Criteria:**
- [ ] Todas las peticiones van a http://localhost:8080/api/*
- [ ] El gateway enruta automáticamente a los servicios correctos
- [ ] No necesito configurar múltiples URLs en el frontend

### US-2: Validación de Autenticación
**Como** sistema de seguridad,  
**Quiero** validar tokens JWT antes de permitir acceso a los servicios,  
**Para** asegurar que solo usuarios autenticados accedan al sistema.

**Acceptance Criteria:**
- [ ] Peticiones sin token JWT retornan 401 Unauthorized
- [ ] Peticiones con token inválido retornan 401 Unauthorized
- [ ] Peticiones con token expirado retornan 401 Unauthorized
- [ ] Peticiones con token válido pasan al servicio destino

### US-3: Descubrimiento Dinámico de Servicios
**Como** API Gateway,  
**Quiero** descubrir servicios automáticamente usando Eureka,  
**Para** no tener que configurar IPs manualmente y soportar escalabilidad.

**Acceptance Criteria:**
- [ ] Gateway se registra en Eureka al iniciar
- [ ] Gateway descubre servicios registrados en Eureka
- [ ] Si un servicio cambia de IP, el gateway lo detecta automáticamente
- [ ] Si hay múltiples instancias, el gateway balancea la carga

### US-4: Manejo de CORS
**Como** frontend en localhost:3000,  
**Quiero** que el gateway permita peticiones cross-origin,  
**Para** poder comunicarme con el backend sin errores de CORS.

**Acceptance Criteria:**
- [ ] Peticiones desde localhost:3000 son permitidas
- [ ] Headers CORS correctos en las respuestas
- [ ] Métodos HTTP permitidos: GET, POST, PUT, DELETE
- [ ] Preflight requests (OPTIONS) funcionan correctamente

### US-5: Rate Limiting
**Como** sistema de protección,  
**Quiero** limitar el número de peticiones por IP,  
**Para** prevenir abuso y ataques DDoS.

**Acceptance Criteria:**
- [ ] Máximo 100 peticiones por minuto por IP
- [ ] Petición 101 retorna 429 Too Many Requests
- [ ] Contador se resetea cada minuto
- [ ] Header X-RateLimit-Remaining indica peticiones restantes

## 3. Functional Requirements

### FR1: Enrutamiento de Peticiones
El gateway DEBE enrutar peticiones basándose en el path:

| Path | Servicio Destino | Puerto |
|------|------------------|--------|
| /api/auth/** | auth-service | 8081 |
| /api/patients/** | patient-service | 8082 |
| /api/clinical/** | clinical-service | 8083 |
| /api/lab/** | lab-service | 8084 |
| /api/pharmacy/** | pharmacy-service | 8085 |
| /api/billing/** | billing-service | 8086 |

### FR2: Validación JWT
El gateway DEBE:
- Extraer el token del header `Authorization: Bearer <token>`
- Validar la firma del token usando la secret key
- Verificar que el token no esté expirado
- Extraer claims del token (userId, roles)
- Pasar el userId en un header `X-User-Id` al servicio destino

### FR3: Excepciones de Autenticación
El gateway NO DEBE validar JWT para:
- `POST /api/auth/login` (endpoint público de login)
- `POST /api/auth/register` (si existe registro público)
- `GET /actuator/health` (health checks)

### FR4: Integración con Eureka
El gateway DEBE:
- Registrarse en Eureka Server al iniciar
- Descubrir servicios usando Eureka Client
- Actualizar la lista de servicios cada 30 segundos
- Usar load balancing round-robin si hay múltiples instancias

### FR5: Manejo de Errores
El gateway DEBE retornar errores claros:

| Escenario | Status Code | Response |
|-----------|-------------|----------|
| Token ausente | 401 | `{"error": "Token not provided"}` |
| Token inválido | 401 | `{"error": "Invalid token"}` |
| Token expirado | 401 | `{"error": "Token expired"}` |
| Servicio no disponible | 503 | `{"error": "Service unavailable"}` |
| Rate limit excedido | 429 | `{"error": "Too many requests"}` |
| Ruta no encontrada | 404 | `{"error": "Route not found"}` |

### FR6: Logging
El gateway DEBE loggear:
- Cada petición recibida (método, path, IP)
- Resultado de validación JWT (éxito/fallo)
- Servicio destino y tiempo de respuesta
- Errores y excepciones

## 4. Non-Functional Requirements

### NFR1: Performance
- **Latencia de enrutamiento**: < 50ms (sin contar tiempo del servicio destino)
- **Throughput**: Soportar 1000 peticiones/segundo
- **Timeout**: 30 segundos para respuesta de servicios

### NFR2: Availability
- **Uptime**: 99.9% (máximo 8.76 horas de downtime al año)
- **Startup time**: < 30 segundos
- **Graceful shutdown**: Completar peticiones en curso antes de apagar

### NFR3: Scalability
- Soportar escalamiento horizontal (múltiples instancias del gateway)
- Stateless (no guardar estado en memoria)
- Usar Redis para rate limiting compartido entre instancias

### NFR4: Security
- Validar JWT en TODAS las peticiones (excepto rutas públicas)
- No loggear tokens completos (solo primeros 10 caracteres)
- Usar HTTPS en producción
- Headers de seguridad: X-Frame-Options, X-Content-Type-Options

### NFR5: Monitoring
- Exponer métricas en `/actuator/metrics`
- Health check en `/actuator/health`
- Integración con Prometheus (futuro)

## 5. Business Rules

### BR1: Orden de Validación
1. Verificar rate limit
2. Verificar si la ruta requiere autenticación
3. Si requiere, validar JWT
4. Enrutar al servicio

### BR2: Prioridad de Rutas
- Rutas específicas primero (ej: `/api/auth/login`)
- Rutas con wildcard después (ej: `/api/auth/**`)

### BR3: Propagación de Headers
El gateway DEBE propagar estos headers al servicio destino:
- `X-User-Id`: ID del usuario autenticado
- `X-User-Roles`: Roles del usuario (comma-separated)
- `X-Request-Id`: ID único de la petición (para tracing)
- `X-Forwarded-For`: IP original del cliente

## 6. Constraints

### C1: Tecnología
- DEBE usar Spring Cloud Gateway (no Zuul)
- DEBE usar Spring Boot 3.2.4
- DEBE usar Java 17

### C2: Dependencias
- Eureka Client para service discovery
- Spring Security para JWT
- Redis para rate limiting (opcional en fase 1)

### C3: Compatibilidad
- DEBE ser compatible con frontend React en localhost:3000
- DEBE funcionar en Docker con hostname `api-gateway`

## 7. Assumptions

### A1: Eureka Server
- Eureka Server está corriendo en localhost:8761 (local) o eureka-server:8761 (Docker)
- Eureka Server está disponible antes de iniciar el gateway

### A2: JWT
- Todos los servicios usan el mismo JWT secret
- JWT contiene claims: `userId`, `roles`, `exp`

### A3: Servicios
- Todos los servicios se registran en Eureka con nombres correctos
- Todos los servicios exponen `/actuator/health`

## 8. Dependencies

### D1: Eureka Server
- **Tipo**: Infraestructura
- **Estado**: ✅ Implementado
- **Ubicación**: `backend-cloud/eureka-server`

### D2: Auth Service (para testing)
- **Tipo**: Microservicio
- **Estado**: ⏳ Pendiente
- **Necesario para**: Probar validación JWT end-to-end

### D3: Redis (opcional fase 1)
- **Tipo**: Cache
- **Estado**: ⏳ Pendiente
- **Necesario para**: Rate limiting distribuido

## 9. Acceptance Criteria (Overall)

### Funcionalidad
- [ ] Gateway enruta correctamente a todos los servicios
- [ ] JWT se valida correctamente (válido, inválido, expirado)
- [ ] Rutas públicas funcionan sin JWT
- [ ] CORS configurado correctamente
- [ ] Rate limiting funciona (100 req/min)

### Integración
- [ ] Gateway se registra en Eureka
- [ ] Gateway descubre servicios vía Eureka
- [ ] Load balancing funciona con múltiples instancias

### Testing
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Cobertura de código >= 80%

### Documentación
- [ ] README.md completo
- [ ] API endpoints documentados
- [ ] Configuración documentada

### Deployment
- [ ] Dockerfile funciona
- [ ] docker-compose.yml actualizado
- [ ] Health checks funcionan

## 10. Out of Scope

### No incluido en esta versión:
- ❌ Circuit Breaker (Resilience4j) - Versión futura
- ❌ Request/Response transformation - No necesario
- ❌ API versioning - No necesario aún
- ❌ GraphQL support - Solo REST
- ❌ WebSocket support - Solo HTTP
- ❌ OAuth2 integration - Solo JWT
- ❌ API documentation UI (Swagger) - Cada servicio lo tiene

### Razones:
- Mantener el gateway simple y enfocado
- Agregar complejidad solo cuando sea necesario
- Circuit Breaker se agregará cuando tengamos problemas de resiliencia

## 11. Success Metrics

### Métricas de Éxito:
- **Latencia p95**: < 100ms (incluyendo servicio destino)
- **Error rate**: < 1%
- **Availability**: > 99.9%
- **Peticiones/segundo**: > 1000

### Cómo medir:
- Usar `/actuator/metrics` de Spring Boot
- Logs de acceso con tiempos de respuesta
- Monitoreo con Prometheus (futuro)

## 12. Risks and Mitigations

### R1: Eureka Server no disponible
**Riesgo**: Gateway no puede descubrir servicios  
**Mitigación**: 
- Implementar retry con backoff
- Cachear última lista de servicios conocidos
- Loggear error claramente

### R2: JWT secret comprometido
**Riesgo**: Tokens falsos podrían ser aceptados  
**Mitigación**:
- Usar secret fuerte (256 bits)
- Rotar secret periódicamente
- No commitear secret en git

### R3: Rate limiting en memoria
**Riesgo**: No funciona con múltiples instancias del gateway  
**Mitigación**:
- Fase 1: Aceptar limitación (una instancia)
- Fase 2: Implementar Redis para rate limiting distribuido

---

**Created**: April 10, 2026  
**Author**: MedFlow Team  
**Status**: ✅ Approved  
**Version**: 1.0.0
