# 📋 Spec-Driven Design (SDD) - Guía Completa

## 🎯 ¿Qué es Spec-Driven Design?

**Spec-Driven Design** es una metodología donde escribimos especificaciones detalladas ANTES de escribir código. Es como tener un plano arquitectónico antes de construir una casa.

### Beneficios

✅ **Claridad**: Todos saben qué se va a construir  
✅ **Menos retrabajo**: Detectamos problemas antes de codificar  
✅ **Mejor comunicación**: Specs sirven como documentación viva  
✅ **Facilita TDD**: Los specs guían los tests  
✅ **Onboarding rápido**: Nuevos devs entienden el sistema rápidamente  

---

## 📐 Estructura de un Spec

Cada feature tiene su propio directorio en `.kiro/specs/{feature-name}/`:

```
.kiro/specs/api-gateway/
├── requirements.md      # ¿QUÉ necesitamos?
├── design.md           # ¿CÓMO lo vamos a construir?
└── tasks.md            # ¿QUÉ pasos seguir?
```

---

## 📝 1. Requirements (requirements.md)

### Propósito
Definir **QUÉ** necesita el usuario/sistema sin entrar en detalles técnicos.

### Estructura

```markdown
# Requirements: [Feature Name]

## 1. Overview
Descripción breve de la feature

## 2. User Stories
Como [rol], quiero [acción], para [beneficio]

## 3. Functional Requirements
- FR1: El sistema debe...
- FR2: El sistema debe...

## 4. Non-Functional Requirements
- NFR1: Performance
- NFR2: Security
- NFR3: Scalability

## 5. Acceptance Criteria
- [ ] Criterio 1
- [ ] Criterio 2

## 6. Out of Scope
Qué NO incluye esta feature
```

### Ejemplo: API Gateway

```markdown
# Requirements: API Gateway

## 1. Overview
Punto de entrada único para todos los microservicios del sistema MedFlow HIS.

## 2. User Stories

### US-1: Enrutamiento de Peticiones
Como desarrollador frontend,
Quiero enviar todas las peticiones a una sola URL,
Para no tener que conocer las IPs de cada microservicio.

### US-2: Validación de JWT
Como sistema,
Quiero validar tokens JWT antes de enrutar peticiones,
Para asegurar que solo usuarios autenticados accedan a los servicios.

## 3. Functional Requirements

- FR1: El gateway debe enrutar peticiones a microservicios basándose en el path
- FR2: El gateway debe validar tokens JWT en cada petición
- FR3: El gateway debe descubrir servicios usando Eureka
- FR4: El gateway debe manejar CORS
- FR5: El gateway debe implementar rate limiting

## 4. Non-Functional Requirements

- NFR1: Latencia < 50ms para enrutamiento
- NFR2: Disponibilidad 99.9%
- NFR3: Soportar 1000 req/s

## 5. Acceptance Criteria

- [ ] Peticiones a /api/auth/* se enrutan a auth-service
- [ ] Peticiones sin JWT válido retornan 401
- [ ] Servicios se descubren dinámicamente vía Eureka
- [ ] CORS configurado correctamente
- [ ] Rate limiting funciona (100 req/min por IP)

## 6. Out of Scope

- Autenticación (responsabilidad de auth-service)
- Lógica de negocio
- Almacenamiento de datos
```

---

## 🏗️ 2. Design (design.md)

### Propósito
Definir **CÓMO** vamos a implementar los requirements.

### Estructura

```markdown
# Design: [Feature Name]

## 1. Architecture Overview
Diagrama y descripción de alto nivel

## 2. Components
Descripción de cada componente

## 3. Data Model
Entidades, DTOs, schemas

## 4. API Design
Endpoints, request/response

## 5. Technology Stack
Frameworks, librerías, herramientas

## 6. Security Design
Autenticación, autorización, encriptación

## 7. Error Handling
Cómo manejar errores

## 8. Testing Strategy
Qué tests escribir

## 9. Deployment
Cómo desplegar
```

### Ejemplo: API Gateway

```markdown
# Design: API Gateway

## 1. Architecture Overview

```
Frontend → API Gateway → Eureka → Microservices
```

## 2. Components

### GatewayConfig
- Configura rutas dinámicas
- Integra con Eureka

### JwtAuthenticationFilter
- Valida tokens JWT
- Extrae claims del token

### RateLimitFilter
- Implementa rate limiting
- Usa Redis para contadores

## 3. Data Model

No aplica (gateway no almacena datos)

## 4. API Design

### Rutas

| Path | Target Service | Port |
|------|---------------|------|
| /api/auth/** | auth-service | 8081 |
| /api/patients/** | patient-service | 8082 |
| /api/clinical/** | clinical-service | 8083 |

### Headers Requeridos

```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

## 5. Technology Stack

- Spring Cloud Gateway
- Spring Security
- Eureka Client
- Redis (rate limiting)

## 6. Security Design

### JWT Validation
1. Extraer token del header Authorization
2. Validar firma con secret key
3. Verificar expiración
4. Extraer roles del token

### CORS
```yaml
allowed-origins: http://localhost:3000
allowed-methods: GET, POST, PUT, DELETE
allowed-headers: *
```

## 7. Error Handling

| Error | Status Code | Response |
|-------|-------------|----------|
| Token inválido | 401 | {"error": "Unauthorized"} |
| Servicio no disponible | 503 | {"error": "Service Unavailable"} |
| Rate limit excedido | 429 | {"error": "Too Many Requests"} |

## 8. Testing Strategy

### Unit Tests
- JwtAuthenticationFilter
- RateLimitFilter
- GatewayConfig

### Integration Tests
- Enrutamiento a servicios
- Validación JWT end-to-end
- Rate limiting

## 9. Deployment

```yaml
# application-docker.yml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
```
```

---

## ✅ 3. Tasks (tasks.md)

### Propósito
Dividir el trabajo en tareas implementables con TDD.

### Estructura

```markdown
# Tasks: [Feature Name]

## Task 1: [Nombre]
- [ ] Subtask 1.1
- [ ] Subtask 1.2

## Task 2: [Nombre]
- [ ] Subtask 2.1
- [ ] Subtask 2.2
```

### Ejemplo: API Gateway

```markdown
# Tasks: API Gateway

## Task 1: Setup Project
- [ ] 1.1 Create Maven project
- [ ] 1.2 Add Spring Cloud Gateway dependency
- [ ] 1.3 Add Eureka Client dependency
- [ ] 1.4 Configure application.yml

## Task 2: Implement JWT Validation (TDD)
- [ ] 2.1 Write test: valid JWT should pass
- [ ] 2.2 Write test: invalid JWT should return 401
- [ ] 2.3 Write test: expired JWT should return 401
- [ ] 2.4 Implement JwtAuthenticationFilter
- [ ] 2.5 Refactor

## Task 3: Configure Routes
- [ ] 3.1 Write test: /api/auth/** routes to auth-service
- [ ] 3.2 Write test: /api/patients/** routes to patient-service
- [ ] 3.3 Implement GatewayConfig
- [ ] 3.4 Refactor

## Task 4: Implement Rate Limiting (TDD)
- [ ] 4.1 Write test: 100 requests should pass
- [ ] 4.2 Write test: 101st request should return 429
- [ ] 4.3 Implement RateLimitFilter
- [ ] 4.4 Refactor

## Task 5: Configure CORS
- [ ] 5.1 Write test: OPTIONS request should return CORS headers
- [ ] 5.2 Implement CORS configuration
- [ ] 5.3 Refactor

## Task 6: Integration Tests
- [ ] 6.1 Test complete flow: Frontend → Gateway → Service
- [ ] 6.2 Test error scenarios
- [ ] 6.3 Test rate limiting end-to-end
```

---

## 🔄 Workflow Completo

### Paso 1: Crear Spec
```bash
# Crear directorio
mkdir -p .kiro/specs/api-gateway

# Crear archivos
touch .kiro/specs/api-gateway/requirements.md
touch .kiro/specs/api-gateway/design.md
touch .kiro/specs/api-gateway/tasks.md
```

### Paso 2: Escribir Requirements
- Definir user stories
- Listar functional requirements
- Definir acceptance criteria

### Paso 3: Escribir Design
- Diseñar arquitectura
- Definir componentes
- Diseñar APIs
- Planear tests

### Paso 4: Crear Tasks
- Dividir en tareas pequeñas
- Cada tarea debe ser testeable
- Ordenar por dependencias

### Paso 5: Implementar con TDD
Para cada task:
1. Escribir test (RED)
2. Implementar código mínimo (GREEN)
3. Refactorizar (REFACTOR)
4. Marcar task como completa

### Paso 6: Validar contra Spec
- Verificar que cumple todos los requirements
- Verificar que pasa todos los acceptance criteria
- Actualizar documentación si es necesario

---

## 📊 Template de Spec Completo

### requirements.md
```markdown
# Requirements: [Feature Name]

## 1. Overview
[Descripción breve]

## 2. User Stories
### US-1: [Título]
Como [rol],
Quiero [acción],
Para [beneficio].

## 3. Functional Requirements
- FR1: [Requirement]
- FR2: [Requirement]

## 4. Non-Functional Requirements
- NFR1: [Performance]
- NFR2: [Security]
- NFR3: [Scalability]

## 5. Acceptance Criteria
- [ ] [Criterio 1]
- [ ] [Criterio 2]

## 6. Out of Scope
- [Qué NO incluye]
```

### design.md
```markdown
# Design: [Feature Name]

## 1. Architecture Overview
[Diagrama y descripción]

## 2. Components
### [Component Name]
[Descripción]

## 3. Data Model
[Entidades, DTOs]

## 4. API Design
[Endpoints, request/response]

## 5. Technology Stack
[Frameworks, librerías]

## 6. Security Design
[Autenticación, autorización]

## 7. Error Handling
[Manejo de errores]

## 8. Testing Strategy
[Qué tests escribir]

## 9. Deployment
[Cómo desplegar]
```

### tasks.md
```markdown
# Tasks: [Feature Name]

## Task 1: [Nombre]
- [ ] 1.1 [Subtask]
- [ ] 1.2 [Subtask]

## Task 2: [Nombre] (TDD)
- [ ] 2.1 Write test: [descripción]
- [ ] 2.2 Implement [componente]
- [ ] 2.3 Refactor
```

---

## 🎯 Best Practices

### Requirements
✅ Usar lenguaje de negocio, no técnico  
✅ Ser específico y medible  
✅ Incluir acceptance criteria claros  
✅ Definir qué NO está en scope  

### Design
✅ Incluir diagramas visuales  
✅ Ser específico en tecnologías  
✅ Documentar decisiones de diseño  
✅ Planear testing desde el inicio  

### Tasks
✅ Tareas pequeñas (< 4 horas)  
✅ Cada tarea debe ser testeable  
✅ Ordenar por dependencias  
✅ Incluir TDD en cada task  

---

## 🔗 Referencias

- [ARCHITECTURE_DDD.md](../../ARCHITECTURE_DDD.md) - Arquitectura del proyecto
- [TDD_GUIDE.md](./TDD_GUIDE.md) - Guía de TDD
- [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md) - Contexto completo

---

**Versión**: 1.0.0  
**Última actualización**: Abril 10, 2026
