# 🏥 MedFlow HIS - Contexto Completo del Proyecto

**Versión**: 1.0.0  
**Última actualización**: Abril 10, 2026  
**Propósito**: Documento maestro para retomar el proyecto desde cero

---

## 📋 Índice

1. [Visión General](#visión-general)
2. [Arquitectura](#arquitectura)
3. [Tecnologías](#tecnologías)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Metodologías](#metodologías)
6. [Estado Actual](#estado-actual)
7. [Próximos Pasos](#próximos-pasos)
8. [Referencias Rápidas](#referencias-rápidas)

---

## 🎯 Visión General

### ¿Qué es MedFlow HIS?

**MedFlow HIS** (Hospital Information System) es un sistema integral de gestión hospitalaria basado en microservicios que cubre todo el flujo de atención médica:

1. **Admisión**: Registro de pacientes con biometría y QR
2. **Triaje**: Clasificación Manchester de urgencias
3. **Consulta Médica**: Historial clínico, diagnóstico, recetas
4. **Laboratorio**: Trazabilidad de muestras y resultados
5. **Farmacia**: Control de inventario y dispensación
6. **Facturación**: Integración con SAT y métodos de pago

### Objetivos del Proyecto

- ✅ Sistema escalable y mantenible
- ✅ Arquitectura de microservicios con DDD
- ✅ Separación clara de responsabilidades
- ✅ Testing completo (TDD)
- ✅ Documentación exhaustiva
- ✅ Despliegue con Docker

---

## 🏗️ Arquitectura

### Principios de Diseño

1. **Domain-Driven Design (DDD)**
   - Cada microservicio = un dominio del hospital
   - Bounded Contexts bien definidos
   - Ubiquitous Language

2. **Microservicios**
   - Database per Service
   - Loose Coupling, High Cohesion
   - Service Discovery (Eureka)
   - API Gateway como punto de entrada único

3. **Clean Architecture**
   - Separación de capas
   - Dependencias hacia adentro
   - Independencia de frameworks

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                 FRONTEND (React + Vite)                  │
│                      Port: 3000                          │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              API GATEWAY (Spring Cloud Gateway)          │
│                      Port: 8080                          │
│  - JWT Validation                                        │
│  - Routing                                               │
│  - Rate Limiting                                         │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Discovery   │  │Auth Service  │  │Patient Svc   │
│  Server      │  │   (8081)     │  │   (8082)     │
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
                         │
                         ▼
                  ┌──────────────┐
                  │ PostgreSQL   │
                  │  (6 schemas) │
                  └──────────────┘
```

### Microservicios Definidos

#### Infraestructura (2)
| Servicio | Puerto | Estado | Responsabilidad |
|----------|--------|--------|-----------------|
| Discovery Server (Eureka) | 8761 | ✅ IMPLEMENTADO | Service Registry |
| API Gateway | 8080 | ⏳ Pendiente | Punto de entrada único |

#### Negocio (6)
| Servicio | Puerto | DB | Dominio | Estado |
|----------|--------|-------|---------|--------|
| Auth Service | 8081 | medflow_auth_db | Autenticación | ⏳ Pendiente |
| Patient Service | 8082 | medflow_patient_db | Pacientes | ⏳ Pendiente |
| Clinical Service | 8083 | medflow_clinical_db | Clínica | ⏳ Pendiente |
| Lab Service | 8084 | medflow_lab_db | Laboratorio | ⏳ Pendiente |
| Pharmacy Service | 8085 | medflow_pharmacy_db | Farmacia | ⏳ Pendiente |
| Billing Service | 8086 | medflow_billing_db | Facturación | ⏳ Pendiente |

---

## 💻 Tecnologías

### Backend
- **Framework**: Spring Boot 3.2.4
- **Cloud**: Spring Cloud 2023.0.1
- **Java**: 17
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **Cache**: Redis (para Clinical Service)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Testing**: JUnit 5, Mockito, TestContainers

### Frontend
- **Framework**: React 18
- **Language**: TypeScript
- **Build Tool**: Vite
- **Styling**: TailwindCSS
- **Routing**: React Router
- **HTTP Client**: Axios
- **State Management**: Context API

### DevOps
- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **Version Control**: Git + Git Flow
- **CI/CD**: (Pendiente)
- **Monitoring**: (Pendiente)

---

## 📁 Estructura del Proyecto

```
medflow-his/
├── .kiro/                          # Configuración de Kiro AI
│   ├── specs/                      # Specs de features
│   └── steering/                   # Reglas y guías para Kiro
│
├── docs/                           # Documentación del proyecto
│   ├── architecture/               # Diagramas y decisiones
│   ├── development/                # Guías de desarrollo
│   ├── specs/                      # Especificaciones técnicas
│   └── testing/                    # Estrategias de testing
│
├── frontend-medflow/               # Aplicación React
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── context/
│   │   └── types/
│   ├── package.json
│   └── Dockerfile
│
├── backend-cloud/                  # Infraestructura Spring Cloud
│   ├── eureka-server/             # ✅ Service Discovery
│   └── api-gateway/               # ⏳ API Gateway
│
├── backend-services/               # Microservicios de negocio
│   ├── auth-service/
│   ├── patient-service/
│   ├── clinical-service/
│   ├── lab-service/
│   ├── pharmacy-service/
│   └── billing-service/
│
├── Documentacion/                  # Casos de uso y diagramas
│   ├── casos-de-uso/
│   └── diagramas/
│
├── docker-compose.yml              # Orquestación de servicios
├── .gitignore
├── README.md
├── ARCHITECTURE_DDD.md
├── CURRENT_STATUS.md
├── PROJECT_CONTEXT.md              # Este archivo
└── GITFLOW.md
```

---

## 🎯 Metodologías

### 1. Spec-Driven Design (SDD)

**¿Qué es?**
Escribir especificaciones detalladas ANTES de implementar código.

**Proceso:**
1. **Requirements**: Definir qué necesita el usuario
2. **Design**: Diseñar la solución técnica
3. **Tasks**: Dividir en tareas implementables
4. **Implementation**: Implementar siguiendo el spec
5. **Validation**: Verificar que cumple el spec

**Ubicación de Specs:**
- `.kiro/specs/{feature-name}/`
  - `requirements.md`
  - `design.md`
  - `tasks.md`

### 2. Test-Driven Development (TDD)

**¿Qué es?**
Escribir tests ANTES de escribir el código de producción.

**Ciclo Red-Green-Refactor:**
```
1. RED: Escribir test que falla
   ↓
2. GREEN: Escribir código mínimo para pasar el test
   ↓
3. REFACTOR: Mejorar el código manteniendo tests verdes
   ↓
Repetir
```

**Tipos de Tests:**
- **Unit Tests**: Probar clases/métodos individuales
- **Integration Tests**: Probar integración entre componentes
- **E2E Tests**: Probar flujos completos

**Herramientas:**
- JUnit 5
- Mockito (mocking)
- TestContainers (bases de datos en tests)
- Spring Boot Test

### 3. Domain-Driven Design (DDD)

**Conceptos Clave:**
- **Bounded Context**: Límites claros de cada dominio
- **Ubiquitous Language**: Lenguaje común entre devs y negocio
- **Entities**: Objetos con identidad
- **Value Objects**: Objetos sin identidad
- **Aggregates**: Grupos de entidades relacionadas
- **Repositories**: Acceso a datos
- **Services**: Lógica de negocio

### 4. Git Flow

**Branches:**
- `main`: Producción
- `develop`: Integración
- `feature/*`: Nuevas funcionalidades
- `release/*`: Preparación de releases
- `hotfix/*`: Fixes urgentes

**Conventional Commits:**
```
feat: Nueva funcionalidad
fix: Corrección de bug
docs: Cambios en documentación
style: Formato de código
refactor: Refactorización
test: Tests
chore: Tareas de mantenimiento
```

---

## 📊 Estado Actual

### Completado ✅

1. **Reestructuración del Proyecto**
   - Monorepo configurado
   - Estructura de directorios
   - Docker Compose

2. **Discovery Server (Eureka)**
   - Implementación completa
   - Configuración para local y Docker
   - Tests básicos
   - Documentación exhaustiva

3. **Documentación Base**
   - README principal
   - Arquitectura DDD
   - Git Flow
   - Estado actual

### En Progreso ⏳

- API Gateway (siguiente paso)

### Pendiente 📋

- 6 microservicios de negocio
- Frontend actualizado
- Tests completos
- CI/CD
- Monitoreo

### Progreso: 12.5% (1/8 componentes)

---

## 🚀 Próximos Pasos

### Inmediato (Esta Semana)

1. **Crear Spec para API Gateway**
   - Requirements
   - Design
   - Tasks

2. **Implementar API Gateway (TDD)**
   - Tests primero
   - Implementación
   - Integración con Eureka

3. **Crear Spec para Auth Service**
   - JWT implementation
   - RBAC design
   - Database schema

### Corto Plazo (Este Mes)

4. Implementar Auth Service (TDD)
5. Implementar Patient Service (TDD)
6. Implementar Clinical Service (TDD)

### Mediano Plazo (Próximos 2 Meses)

7. Implementar Lab Service
8. Implementar Pharmacy Service
9. Implementar Billing Service
10. Actualizar Frontend
11. Tests E2E

---

## 📚 Referencias Rápidas

### Documentos Clave

| Documento | Ubicación | Propósito |
|-----------|-----------|-----------|
| README Principal | `/README.md` | Visión general |
| Arquitectura DDD | `/ARCHITECTURE_DDD.md` | Arquitectura completa |
| Estado Actual | `/CURRENT_STATUS.md` | Progreso y roadmap |
| Git Flow | `/GITFLOW.md` | Workflow de Git |
| Contexto Proyecto | `/PROJECT_CONTEXT.md` | Este documento |

### Comandos Útiles

```bash
# Levantar Eureka Server
cd backend-cloud/eureka-server
mvn spring-boot:run

# Ver dashboard de Eureka
http://localhost:8761

# Levantar todo con Docker
docker-compose up -d

# Ver logs
docker-compose logs -f [service-name]

# Ejecutar tests
mvn test

# Build
mvn clean package
```

### URLs Importantes

| Servicio | URL | Estado |
|----------|-----|--------|
| Frontend | http://localhost:3000 | ⏳ |
| API Gateway | http://localhost:8080 | ⏳ |
| Eureka Dashboard | http://localhost:8761 | ✅ |
| Auth Service | http://localhost:8081 | ⏳ |
| Patient Service | http://localhost:8082 | ⏳ |
| Clinical Service | http://localhost:8083 | ⏳ |
| Lab Service | http://localhost:8084 | ⏳ |
| Pharmacy Service | http://localhost:8085 | ⏳ |
| Billing Service | http://localhost:8086 | ⏳ |

---

## 🎓 Conceptos Importantes

### Service Discovery (Eureka)

**¿Qué hace?**
- Registro de servicios
- Descubrimiento dinámico
- Health checks
- Load balancing

**¿Por qué lo usamos?**
- IPs dinámicas en Docker
- Escalabilidad horizontal
- Failover automático

### API Gateway

**¿Qué hace?**
- Punto de entrada único
- Validación JWT
- Enrutamiento dinámico
- Rate limiting
- CORS

**¿Por qué lo usamos?**
- Simplifica el frontend
- Seguridad centralizada
- Monitoreo centralizado

### Database per Service

**¿Qué significa?**
Cada microservicio tiene su propia base de datos (esquema).

**¿Por qué?**
- Aislamiento de datos
- Escalabilidad independiente
- Cambios de esquema independientes
- Fallas aisladas

---

## 🔧 Configuración del Entorno

### Prerequisitos

```bash
# Java 17
java -version

# Maven 3.8+
mvn -version

# Node.js 18+
node -version

# Docker
docker -version

# Git
git --version
```

### Variables de Entorno

Ver `.env.example` para configuración completa.

```bash
# Database
POSTGRES_DB=medflow_db
POSTGRES_USER=medflow_user
POSTGRES_PASSWORD=medflow_pass

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Eureka
EUREKA_SERVER_URL=http://localhost:8761/eureka/
```

---

## 📞 Contacto y Recursos

- **Repositorio**: https://github.com/4901oscar/HIS-Project
- **Branch Principal**: develop
- **Desarrollador**: Oscar
- **Equipo**: MedFlow Team

---

## 🎯 Checklist para Retomar el Proyecto

Cuando retomes el proyecto desde cero, sigue este checklist:

- [ ] Leer este documento completo
- [ ] Revisar `CURRENT_STATUS.md` para ver el progreso
- [ ] Revisar `ARCHITECTURE_DDD.md` para entender la arquitectura
- [ ] Clonar el repositorio
- [ ] Checkout a branch `develop`
- [ ] Revisar últimos commits
- [ ] Levantar Eureka Server para verificar que funciona
- [ ] Revisar specs en `.kiro/specs/` si existen
- [ ] Continuar con el siguiente paso del roadmap

---

**Última actualización**: Abril 10, 2026  
**Versión del Documento**: 1.0.0  
**Mantenido por**: MedFlow Team
