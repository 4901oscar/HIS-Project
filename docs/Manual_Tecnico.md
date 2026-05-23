# Manual Técnico — MedFlow HIS

**Versión**: 1.0  
**Fecha de Actualización**: Abril 2026  
**Propósito**: Documentación técnica para desarrolladores y administradores del sistema

---

## 1. Herramientas Utilizadas

### 1.1 Backend — Java y Spring Boot

#### Framework Principal
- **Spring Boot**: 3.2.4
- **Java**: 17 (LTS)
- **Build Tool**: Maven 3.x

#### Spring Cloud (Microservicios)
- **Spring Cloud Gateway**: Enrutamiento y validación de JWT
- **Spring Cloud Netflix Eureka**: Service Discovery (registro de servicios)
- **Spring Cloud OpenFeign**: Comunicación HTTP entre servicios
- **Resilience4j**: Circuit breaker para llamadas entre servicios

#### Persistencia de Datos
- **Spring Data JPA**: ORM para acceso a base de datos
- **Hibernate**: Implementación de JPA
- **PostgreSQL**: Base de datos relacional (versión 15)

#### Caché y Sesiones
- **Spring Data Redis**: Caché distribuido
- **Redis**: Almacenamiento en memoria (versión 7)

#### Seguridad
- **Spring Security**: Autenticación y autorización
- **JJWT** (JSON Web Token): Generación y validación de JWT
  - jjwt-api: 0.11.5
  - jjwt-impl: 0.11.5
  - jjwt-jackson: 0.11.5
- **BCrypt**: Hashing de contraseñas

#### Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking de dependencias
- **Spring Boot Test**: Testing de aplicaciones Spring
- **WireMock**: Mocking de servicios HTTP
- **Reactor Test**: Testing de streams reactivos
- **JaCoCo**: Cobertura de código

#### Monitoreo y Observabilidad
- **Spring Boot Actuator**: Health checks y métricas
- **Micrometer**: Recolección de métricas

#### Otros
- **Lombok**: Reducción de boilerplate
- **Jackson**: Serialización/deserialización JSON
- **Validation API**: Validación de datos

### 1.2 Frontend — React y TypeScript

#### Framework Principal
- **React**: 19.2.0
- **TypeScript**: 5.9.3
- **Vite**: 7.3.1 (build tool)

#### Enrutamiento
- **React Router DOM**: 7.13.1

#### HTTP Client
- **Axios**: 1.13.6 (comunicación con API)

#### UI y Componentes
- **Tailwind CSS**: 3.4.1 (estilos)
- **Heroicons**: 2.2.0 (iconos)
- **SweetAlert2**: 11.26.24 (alertas y modales)

#### Lectura de Códigos QR
- **html5-qrcode**: 2.3.8

#### Testing
- **Vitest**: 4.1.5 (test runner)
- **@testing-library/react**: 16.3.2
- **@testing-library/jest-dom**: 6.9.1
- **@testing-library/user-event**: 14.6.1
- **fast-check**: 4.7.0 (property-based testing)
- **jsdom**: 29.0.2 (DOM simulation)

#### Linting y Formato
- **ESLint**: 9.39.1
- **TypeScript ESLint**: 8.48.0
- **Autoprefixer**: 10.4.16
- **PostCSS**: 8.4.33

#### Desarrollo
- **@vitejs/plugin-react**: 5.1.1
- **@vitest/ui**: 4.1.5

### 1.3 Infraestructura

#### Contenedorización
- **Docker**: Versión 20.x+
- **Docker Compose**: Versión 2.x+

#### Orquestación de Servicios
- **Eureka Server**: Service Discovery (Spring Cloud Netflix)

#### Base de Datos
- **PostgreSQL**: 15-alpine
- **Esquemas**: 6 esquemas aislados (auth, patient, clinical, lab, pharmacy, billing)

#### Caché
- **Redis**: 7-alpine

#### Almacenamiento
- **Volúmenes Docker**: Para persistencia de datos y resultados de laboratorio

---

## 2. Versionado en GitHub

### 2.1 Estrategia de Ramas

El proyecto utiliza un modelo de ramas basado en Git Flow:

```
main (producción)
  ↑
  └─ release/v1.0.0
       ↑
       └─ develop (desarrollo)
            ↑
            ├─ feature/auth-service
            ├─ feature/patient-service
            ├─ feature/clinical-service
            ├─ bugfix/login-validation
            └─ ...
```

#### Rama `main`
- Contiene código en producción
- Solo se actualiza mediante Pull Requests desde `release/*`
- Cada commit en `main` debe tener un tag de versión (v1.0.0, v1.0.1, etc.)

#### Rama `develop`
- Rama de integración para desarrollo
- Contiene las últimas características completadas
- Base para crear ramas de feature y bugfix

#### Ramas `feature/*`
- Patrón: `feature/nombre-descriptivo`
- Ejemplos: `feature/auth-service`, `feature/patient-registration`
- Se crean desde `develop`
- Se fusionan a `develop` mediante Pull Request

#### Ramas `bugfix/*`
- Patrón: `bugfix/descripcion-del-bug`
- Ejemplos: `bugfix/login-validation`, `bugfix/dpi-format`
- Se crean desde `develop`
- Se fusionan a `develop` mediante Pull Request

#### Ramas `release/*`
- Patrón: `release/vX.Y.Z`
- Ejemplos: `release/v1.0.0`, `release/v1.0.1`
- Se crean desde `develop` cuando se prepara una versión
- Se fusionan a `main` y se etiquetan
- Se fusionan de vuelta a `develop`

### 2.2 Convenciones de Commits

El proyecto sigue la especificación de Conventional Commits:

```
<tipo>(<alcance>): <descripción>

<cuerpo>

<pie>
```

#### Tipos de Commit
- `feat`: Nueva característica
- `fix`: Corrección de bug
- `docs`: Cambios en documentación
- `style`: Cambios de formato (sin cambios de lógica)
- `refactor`: Refactorización de código
- `perf`: Mejoras de rendimiento
- `test`: Adición o modificación de tests
- `chore`: Cambios en configuración o dependencias
- `ci`: Cambios en CI/CD

#### Alcances Comunes
- `auth`: Servicio de autenticación
- `patient`: Servicio de pacientes
- `clinical`: Servicio clínico
- `lab`: Servicio de laboratorio
- `pharmacy`: Servicio de farmacia
- `billing`: Servicio de facturación
- `gateway`: API Gateway
- `frontend`: Frontend React
- `docker`: Configuración Docker

#### Ejemplos de Commits

```
feat(auth): agregar validación de contraseña fuerte

- Implementar validación de mínimo 8 caracteres
- Requerir mayúsculas, minúsculas y números
- Mostrar mensaje de error específico

Closes #123
```

```
fix(patient): corregir validación de DPI duplicado

El sistema no validaba correctamente si el DPI ya existía.
Ahora realiza una búsqueda en la base de datos antes de crear.

Fixes #456
```

```
docs(manual): actualizar guía de usuario

Agregar sección de FAQ y validaciones de datos.
```

### 2.3 Pull Requests

#### Proceso de PR
1. Crear rama desde `develop`
2. Hacer commits siguiendo convenciones
3. Hacer push a la rama
4. Crear Pull Request en GitHub
5. Esperar revisión de al menos 1 revisor
6. Resolver comentarios
7. Merge a `develop` (squash o rebase según política)

#### Plantilla de PR
```markdown
## Descripción
Breve descripción de los cambios

## Tipo de Cambio
- [ ] Bug fix
- [ ] Nueva característica
- [ ] Cambio que rompe compatibilidad

## Cómo se probó
Describir cómo se probaron los cambios

## Checklist
- [ ] Mi código sigue las convenciones del proyecto
- [ ] He actualizado la documentación
- [ ] He agregado tests
- [ ] Los tests pasan localmente
```

---

## 3. Arquitectura del Sistema

### 3.1 Diagrama de Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (React)                         │
│                    Puerto 3000                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ • Dashboard por rol                                  │  │
│  │ • Gestión de pacientes, citas, consultas            │  │
│  │ • Portal del paciente                               │  │
│  │ • Autenticación con JWT                             │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP + JWT
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              API GATEWAY (Spring Cloud)                     │
│              Puerto 8080                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ • Validación de JWT                                 │  │
│  │ • Rate limiting (100 req/min por IP)               │  │
│  │ • CORS (localhost:3000)                            │  │
│  │ • Enrutamiento a microservicios                    │  │
│  │ • Service Discovery (Eureka)                       │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┬──────────────┐
        ▼                ▼                ▼              ▼
   ┌─────────┐    ┌──────────┐    ┌──────────┐    ┌─────────┐
   │  Auth   │    │ Patient  │    │Clinical  │    │   Lab   │
   │Service  │    │ Service  │    │ Service  │    │ Service │
   │ :8081   │    │  :8082   │    │  :8083   │    │  :8084  │
   │ MVC     │    │   MVC    │    │Hexagonal │    │   MVC   │
   └─────────┘    └──────────┘    └──────────┘    └─────────┘
        │              │                │              │
        └──────────────┼────────────────┼──────────────┘
                       ▼
            ┌──────────────────────┐
            │   PostgreSQL         │
            │   Puerto 5432        │
            │   6 Esquemas:        │
            │   • auth_schema      │
            │   • patient_schema   │
            │   • clinical_schema  │
            │   • lab_schema       │
            │   • pharmacy_schema  │
            │   • billing_schema   │
            └──────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   ┌─────────┐  ┌──────────┐  ┌──────────┐
   │Pharmacy │  │ Billing  │  │  Redis   │
   │Service  │  │ Service  │  │ (Cache)  │
   │ :8085   │  │  :8086   │  │ :6379    │
   │   MVC   │  │   MVC    │  │          │
   └─────────┘  └──────────┘  └──────────┘
```

### 3.2 Diagramas Referenciados

El proyecto incluye los siguientes diagramas en la carpeta `/Documentación/`:

#### Diagrama de Base de Datos (ER)
- **Archivo**: `Documentación/DIAGRAMA ER.png`
- **Descripción**: Modelo entidad-relación de las 6 bases de datos
- **Esquemas**: auth_schema, patient_schema, clinical_schema, lab_schema, pharmacy_schema, billing_schema

#### Diagrama de Secuencia
- **Archivo**: `Documentación/DIAGRAMA DE SECUENCIA.png`
- **Descripción**: Flujo de interacción entre servicios para casos de uso principales
- **Casos Cubiertos**: Registro de paciente, consulta médica, laboratorio, facturación

#### Diagrama de Clases
- **Archivo Principal**: `Documentación/Diagrama Clases/Diagrama de clases completas.png`
- **Archivos Específicos por Servicio**:
  - `auth-service.png`: Clases de autenticación
  - `patient-services.png`: Clases de gestión de pacientes
  - `clinical-services.png`: Clases de consulta médica y triaje
  - `Lab-services.png`: Clases de laboratorio
  - `pharmacy-service.png`: Clases de farmacia
  - `billing-service.png`: Clases de facturación

#### Estructura de Módulos
- **Archivo**: `Documentación/DIAGRAMA DE MODULOS.png`
- **Descripción**: Organización de los 6 microservicios y sus dependencias

#### Diagrama de Despliegue
- **Archivo**: `Documentación/DIAGRAMA DE DESPLIEGUE.png`
- **Descripción**: Infraestructura de contenedores Docker y orquestación

### 3.3 Arquitectura de Microservicios

#### Auth Service (Puerto 8081)
- **Arquitectura**: MVC
- **Esquema**: `auth_schema`
- **Responsabilidades**:
  - Autenticación de usuarios
  - Emisión de JWT
  - Gestión de roles (RBAC)
  - Revocación de tokens (logout)
- **Dependencias**: PostgreSQL

#### Patient Service (Puerto 8082)
- **Arquitectura**: MVC
- **Esquema**: `patient_schema`
- **Responsabilidades**:
  - Registro de pacientes
  - Búsqueda de pacientes
  - Actualización de datos demográficos
- **Dependencias**: PostgreSQL, Auth Service

#### Clinical Service (Puerto 8083)
- **Arquitectura**: Hexagonal (Puertos y Adaptadores)
- **Esquema**: `clinical_schema` + Redis
- **Responsabilidades**:
  - Gestión de citas (con reserva atómica en Redis)
  - Triaje Manchester
  - Consultas médicas
  - Recetas y órdenes de laboratorio
  - Historial clínico
- **Dependencias**: PostgreSQL, Redis, Patient Service, Auth Service, Billing Service

#### Lab Service (Puerto 8084)
- **Arquitectura**: MVC
- **Esquema**: `lab_schema`
- **Responsabilidades**:
  - Recepción de órdenes de laboratorio
  - Trazabilidad de muestras
  - Carga de resultados
- **Dependencias**: PostgreSQL

#### Pharmacy Service (Puerto 8085)
- **Arquitectura**: MVC
- **Esquema**: `pharmacy_schema`
- **Responsabilidades**:
  - Catálogo de medicamentos
  - Despacho de recetas
  - Control de inventario
- **Dependencias**: PostgreSQL

#### Billing Service (Puerto 8086)
- **Arquitectura**: MVC
- **Esquema**: `billing_schema`
- **Responsabilidades**:
  - Catálogo de servicios y precios
  - Creación de facturas
  - Procesamiento de pagos
- **Dependencias**: PostgreSQL

### 3.4 Comunicación entre Servicios

#### Síncrona (HTTP / Feign Clients)
Se usa cuando el servicio necesita la respuesta para continuar:
- `clinical-service` → `patient-service`: Obtener datos del paciente
- `clinical-service` → `billing-service`: Validar pago
- `patient-service` → `auth-service`: Crear cuenta de usuario

#### Fire-and-Forget (HTTP Asíncrono)
Se usa cuando el servicio solo notifica:
- `clinical-service` → `pharmacy-service`: Notificar receta
- `clinical-service` → `lab-service`: Notificar orden de laboratorio

#### Circuit Breaker
- **Librería**: Resilience4j
- **Propósito**: Evitar cascadas de fallos
- **Configuración**: Umbral de fallos, timeout, reintentos

---

## 4. Base de Datos

### 4.1 Estrategia: Schema-per-Service

Cada microservicio tiene su propio esquema en PostgreSQL. No se permiten JOINs entre esquemas.

```
medflow_db (Base de datos única)
├── auth_schema
│   ├── users
│   ├── roles
│   └── permissions
├── patient_schema
│   ├── patients
│   └── patient_contacts
├── clinical_schema
│   ├── appointments
│   ├── vital_signs
│   ├── triage_records
│   ├── consultations
│   ├── prescriptions
│   ├── lab_orders
│   ├── doctors
│   └── clinics
├── lab_schema
│   ├── lab_orders
│   ├── samples
│   └── results
├── pharmacy_schema
│   ├── medications
│   ├── prescriptions
│   └── dispensations
└── billing_schema
    ├── service_items
    ├── invoices
    └── payments
```

### 4.2 Administración Externa

La base de datos es administrada externamente. El proyecto:
- No incluye scripts de creación de esquemas
- Usa Hibernate con `spring.jpa.hibernate.ddl-auto=update` en desarrollo
- En producción, los esquemas deben crearse manualmente

### 4.3 Conexión a PostgreSQL

**Configuración en docker-compose.yml**:
```yaml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_DB: medflow_db
    POSTGRES_USER: medflow_user
    POSTGRES_PASSWORD: medflow_pass
  ports:
    - "5432:5432"
```

**Conexión desde servicios**:
```
jdbc:postgresql://postgres:5432/medflow_db?currentSchema={schema_name}
```

---

## 5. Caché con Redis

### 5.1 Uso de Redis

Redis se utiliza principalmente en Clinical Service para:
- **Reserva atómica de slots de citas**: Evitar doble reserva
- **Caché de datos frecuentes**: Motivos y discriminadores de triaje

### 5.2 Configuración

**docker-compose.yml**:
```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
```

**Conexión desde Clinical Service**:
```
spring.data.redis.host=redis
spring.data.redis.port=6379
```

---

## 6. Seguridad

### 6.1 Autenticación con JWT

- **Algoritmo**: HS256 (HMAC con SHA-256)
- **Emisor**: Auth Service
- **Validador**: API Gateway
- **Expiración**: 24 horas
- **Almacenamiento en Frontend**: localStorage

### 6.2 Estructura del JWT

```json
{
  "sub": "user-uuid",
  "username": "doctor",
  "roles": ["DOCTOR"],
  "iat": 1713024000,
  "exp": 1713110400
}
```

### 6.3 Headers Requeridos

```
Authorization: Bearer {jwt_token}     ← Obligatorio en todos los endpoints protegidos
Content-Type: application/json        ← En POST/PUT
X-User-Id: {user_uuid}               ← Requerido por Clinical Service para auditoría
```

### 6.4 Rate Limiting

- **Límite**: 100 solicitudes por minuto por IP
- **Implementación**: API Gateway
- **Respuesta**: HTTP 429 si se excede

### 6.5 Contraseñas

- **Hashing**: BCrypt
- **Requisitos**:
  - Mínimo 8 caracteres
  - Mayúsculas, minúsculas y números
- **Rate Limiting en Login**: 5 intentos fallidos = bloqueo de 1 minuto

---

## 7. Despliegue

### 7.1 Despliegue Local con Docker Compose

```bash
# Clonar el repositorio
git clone https://github.com/medflow/his-project.git
cd his-project

# Configurar variables de entorno
cp .env.example .env
# Editar .env con valores reales

# Iniciar todos los servicios
docker-compose up -d

# Verificar estado
docker-compose ps

# Ver logs
docker-compose logs -f api-gateway
```

### 7.2 Servicios Disponibles

| Servicio | Puerto | URL |
|----------|--------|-----|
| Frontend | 3000 | http://localhost:3000 |
| API Gateway | 8080 | http://localhost:8080 |
| Auth Service | 8081 | http://localhost:8081 |
| Patient Service | 8082 | http://localhost:8082 |
| Clinical Service | 8083 | http://localhost:8083 |
| Lab Service | 8084 | http://localhost:8084 |
| Pharmacy Service | 8085 | http://localhost:8085 |
| Billing Service | 8086 | http://localhost:8086 |
| Eureka Server | 8761 | http://localhost:8761 |
| PostgreSQL | 5432 | localhost:5432 |
| Redis | 6379 | localhost:6379 |

### 7.3 Variables de Entorno

```bash
# JWT
JWT_SECRET=your-secret-key-256-bits-minimum

# Email (opcional)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Frontend
VITE_API_GATEWAY_URL=http://localhost:8080
APP_FRONTEND_URL=http://localhost:3000
```

### 7.4 Health Checks

Todos los servicios incluyen health checks:

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health

# Etc.
```

---

## 8. Desarrollo Local

### 8.1 Requisitos

- Java 17 (JDK)
- Node.js 18+
- Docker y Docker Compose
- Git
- Maven 3.x (incluido en Spring Boot)

### 8.2 Configuración del Backend

```bash
# Navegar a un servicio
cd backend-services/auth-service

# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run

# Tests
mvn test

# Cobertura
mvn jacoco:report
```

### 8.3 Configuración del Frontend

```bash
# Navegar al frontend
cd frontend-medflow

# Instalar dependencias
npm install

# Desarrollo
npm run dev

# Build
npm run build

# Tests
npm run test

# Linting
npm run lint
```

### 8.4 Estructura de Directorios

```
his-project/
├── backend-cloud/
│   ├── api-gateway/
│   ├── eureka-server/
│   └── README.md
├── backend-services/
│   ├── auth-service/
│   ├── patient-service/
│   ├── clinical-service/
│   ├── lab-service/
│   ├── pharmacy-service/
│   └── billing-service/
├── frontend-medflow/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.ts
├── docs/
│   ├── 01-ARQUITECTURA.md
│   ├── 02-FLUJO-ATENCION.md
│   ├── 03-ROLES-Y-PERMISOS.md
│   ├── 04-API-ENDPOINTS.md
│   ├── Manual_de_Usuario.md
│   ├── Manual_Tecnico.md
│   └── Plan_de_Pruebas.md
├── Documentación/
│   ├── casos-de-uso/
│   ├── Diagrama Clases/
│   └── *.png (diagramas)
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 9. Troubleshooting

### 9.1 Problemas Comunes

**Error: "Connection refused" en PostgreSQL**
- Verificar que PostgreSQL está corriendo: `docker-compose ps`
- Verificar credenciales en `.env`
- Esperar a que PostgreSQL inicie (health check)

**Error: "Eureka Server not available"**
- Verificar que Eureka está corriendo: `docker-compose ps`
- Esperar a que Eureka inicie completamente (30-60 segundos)

**Error: "JWT validation failed"**
- Verificar que JWT_SECRET es igual en todos los servicios
- Verificar que el token no ha expirado
- Verificar que el header Authorization está presente

**Error: "Rate limit exceeded"**
- Esperar 1 minuto
- Verificar que no hay scripts haciendo muchas solicitudes

### 9.2 Logs

```bash
# Ver logs de un servicio específico
docker-compose logs -f auth-service

# Ver logs de todos los servicios
docker-compose logs -f

# Ver últimas 100 líneas
docker-compose logs --tail=100 api-gateway
```

### 9.3 Reiniciar Servicios

```bash
# Reiniciar un servicio
docker-compose restart auth-service

# Reiniciar todos
docker-compose restart

# Detener y eliminar contenedores
docker-compose down

# Iniciar nuevamente
docker-compose up -d
```

---

## 10. Recursos Adicionales

- **Documentación de Arquitectura**: `docs/01-ARQUITECTURA.md`
- **Flujo de Atención**: `docs/02-FLUJO-ATENCION.md`
- **Roles y Permisos**: `docs/03-ROLES-Y-PERMISOS.md`
- **API Endpoints**: `docs/04-API-ENDPOINTS.md`
- **Manual de Usuario**: `docs/Manual_de_Usuario.md`
- **Casos de Uso**: `Documentación/USE_CASES_REFERENCE.md`

---

**Versión**: 1.0  
**Última Actualización**: Abril 2026  
**Mantenido por**: MedFlow Team
