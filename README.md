# 🏥 MedFlow HIS - Hospital Information System

Sistema de Información Hospitalaria (HIS) basado en microservicios con arquitectura Spring Cloud y frontend React.

**MVP para Graduación** - Sistema integral que cubre todo el flujo de atención médica hospitalaria.

## 🏗️ Arquitectura

```
medflow-his/
├── frontend-medflow/         # React 18 + Vite + Tailwind CSS
├── backend-cloud/            # Spring Cloud Infrastructure
│   ├── eureka-server/        # Service Discovery (8761) ✅
│   └── api-gateway/          # API Gateway (8080) ✅
├── backend-services/         # Microservicios de Negocio
│   ├── auth-service/         # Autenticación JWT + RBAC (8081)
│   ├── patient-service/      # Gestión de Pacientes (8082)
│   ├── clinical-service/     # Motor Médico - Triaje Manchester (8083)
│   ├── lab-service/          # Laboratorio Clínico (8084)
│   ├── pharmacy-service/     # Farmacia e Inventario (8085)
│   └── billing-service/      # Facturación Interna (8086)
├── docker-compose.yml        # Orquestación de servicios
└── .kiro/                    # Configuración de IA (Kiro)
```

## 🚀 Stack Tecnológico

### Frontend
- **React 18** (sin TypeScript)
- **Vite** como build tool
- **Tailwind CSS** para estilos
- **React Router** para navegación
- **Axios** para peticiones HTTP (con Service Abstraction + Mocks)

### Backend
- **Java 17**
- **Spring Boot 3.x**
- **Spring Cloud** (Eureka, Gateway)
- **PostgreSQL** (Monolito Lógico - Schema-per-Service)
- **Redis** (Cache para slots de citas médicas)
- **JWT** para autenticación stateless
- **Maven** como build tool

### Arquitectura de Código
- **Hexagonal (Puertos y Adaptadores)**: Para `clinical-service` (lógica pesada)
- **MVC (Capas)**: Para servicios CRUD simples

### DevOps
- **Docker Compose** (Monorepo)
- **Git Flow** para control de versiones
- **GitHub** como repositorio remoto

## 📋 Prerequisitos

- **Node.js** 18+ y npm
- **Java** 17+
- **Maven** 3.8+
- **Docker** y **Docker Compose**
- **PostgreSQL** (si ejecutas sin Docker)
- **Redis** (para Clinical Service - slots de citas)
- **Git**

## 🔧 Instalación y Ejecución

### Opción 1: Con Docker (Recomendado)

```bash
# Clonar el repositorio
git clone https://github.com/4901oscar/HIS-Project.git
cd HIS-Project

# Levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

### Opción 2: Desarrollo Local

#### Backend

```bash
# 1. Iniciar PostgreSQL
# Asegúrate de tener PostgreSQL corriendo en localhost:5432

# 2. Eureka Server
cd backend-cloud/eureka-server
mvn spring-boot:run

# 3. API Gateway (en otra terminal)
cd backend-cloud/api-gateway
mvn spring-boot:run

# 4. Auth Service (en otra terminal)
cd backend-services/auth-service
mvn spring-boot:run

# 5. Patient Service (en otra terminal)
cd backend-services/patient-service
mvn spring-boot:run

# 6. Clinical Service (en otra terminal)
cd backend-services/clinical-service
mvn spring-boot:run
```

#### Frontend

```bash
cd frontend-medflow
npm install
npm run dev
```

## 🌐 URLs de Acceso

| Servicio | URL | Puerto | Estado |
|----------|-----|--------|--------|
| Frontend | http://localhost:3000 | 3000 | ⏳ |
| API Gateway | http://localhost:8080 | 8080 | ✅ |
| Eureka Dashboard | http://localhost:8761 | 8761 | ✅ |
| Auth Service | http://localhost:8081 | 8081 | ⏳ |
| Patient Service | http://localhost:8082 | 8082 | ⏳ |
| Clinical Service | http://localhost:8083 | 8083 | ⏳ |
| Lab Service | http://localhost:8084 | 8084 | ⏳ |
| Pharmacy Service | http://localhost:8085 | 8085 | ⏳ |
| Billing Service | http://localhost:8086 | 8086 | ⏳ |
| PostgreSQL | localhost:5432 | 5432 | ⏳ |
| Redis | localhost:6379 | 6379 | ⏳ |

## 📚 Documentación

### Documentos Principales
- **[MVP_CORE_SPECS.md](./MVP_CORE_SPECS.md)** - ⭐ Especificaciones Core del MVP (LEER PRIMERO)
- [Git Flow Workflow](./GITFLOW.md)
- [Arquitectura DDD](./ARCHITECTURE_DDD.md)
- [Estado Actual](./CURRENT_STATUS.md)
- [Contexto del Proyecto](./PROJECT_CONTEXT.md)

### Documentación por Servicio
- [Backend Cloud](./backend-cloud/README.md)
- [Backend Services](./backend-services/README.md)
- [Eureka Server](./backend-cloud/eureka-server/README.md)
- [API Gateway](./backend-cloud/api-gateway/README.md)

## 🔐 Roles y Permisos (RBAC)

El sistema maneja los siguientes roles con control de acceso basado en JWT:

| Rol | Código | Descripción |
|-----|--------|-------------|
| **Administrador** | `ADMIN` | Súper Usuario - Crea cuentas y asigna roles |
| **Admisión** | `ADMISSION` | Registra pacientes, genera QR, gestiona citas |
| **Signos Vitales** | `VITAL_SIGNS` | Captura signos vitales en sala de espera |
| **Doctor** | `DOCTOR` | Triaje Manchester, consultas, recetas |
| **Laboratorio** | `LABORATORY` | Gestiona muestras y sube resultados |
| **Farmacia** | `PHARMACY` | Gestiona inventario y despacha medicamentos |
| **Caja** | `CASHIER` | Procesa facturación interna |
| **Paciente** | `PATIENT` | Ve su historial, recetas, laboratorios |

### Autenticación
- **Staff**: Login con credenciales asignadas por ADMIN
- **Pacientes**: Login con Email/DPI + contraseña temporal (generada en Admisión)
- **Mecanismo**: JWT Stateless (almacenado en localStorage)

## 🗄️ Base de Datos

### Estrategia: Monolito Lógico (Schema-per-Service)
- **Motor**: PostgreSQL (Una única instancia)
- **Estructura**: Esquemas aislados por dominio
- **Regla Estricta**: ❌ CERO JOINs entre esquemas

```sql
CREATE SCHEMA auth_schema;
CREATE SCHEMA patient_schema;
CREATE SCHEMA clinical_schema;
CREATE SCHEMA lab_schema;
CREATE SCHEMA pharmacy_schema;
CREATE SCHEMA billing_schema;
```

### Comunicación entre Servicios
- ✅ Composición de APIs (llamadas HTTP)
- ❌ NO JOINs directos entre esquemas

## 🏗️ Arquitectura de Código

### Hexagonal (Puertos y Adaptadores)
**Uso**: Microservicios con lógica de negocio pesada

**Ejemplo**: `clinical-service` (Triaje Manchester)

### MVC (Capas)
**Uso**: Microservicios CRUD simples

**Ejemplo**: `patient-service`, `pharmacy-service`, `auth-service`

## ❌ Características Descartadas (MVP)

Para mantener el alcance del MVP manejable:

- ❌ **Autenticación Biométrica** (Huella dactilar eliminada)
- ❌ **Integración con SAT** (Facturación Electrónica FEL eliminada)
- ❌ **Notificaciones Push** en tiempo real
- ❌ **Dashboard de Analytics** avanzado

La facturación será **solo interna** del hospital.

## 🌿 Git Flow

Este proyecto usa Git Flow para gestión de branches:

- `main`: Código en producción
- `develop`: Integración de features
- `feature/*`: Nuevas funcionalidades
- `release/*`: Preparación de releases
- `hotfix/*`: Fixes urgentes en producción

Ver [GITFLOW.md](./GITFLOW.md) para más detalles.

## 📦 Build para Producción

### Backend
```bash
# Cada servicio
mvn clean package
```

### Frontend
```bash
cd frontend-medflow
npm run build
```

### Docker Images
```bash
docker-compose build
```

## 🧪 Testing

```bash
# Backend (cada servicio)
mvn test

# Frontend
cd frontend-medflow
npm run test
```

## 🤖 Flujo de Trabajo con IA (Kiro)

### Estructura .kiro/
```
.kiro/
├── specs/              # Especificaciones de features (paso a paso)
├── steering/           # Reglas y estándares del proyecto
└── hooks/              # Validaciones automáticas
```

### Service Abstraction (Frontend)
Los servicios de Axios apuntan al Gateway pero devuelven **Mocks** hasta que el backend esté conectado:

```javascript
const USE_MOCK = true; // Cambiar a false cuando backend esté listo

export const getPatient = async (id) => {
  if (USE_MOCK) return mockPatient;
  const response = await axios.get(`/api/patients/${id}`);
  return response.data;
};
```

## 🤝 Contribución

1. Crea un branch desde `develop`: `git checkout -b feature/mi-feature`
2. Haz tus cambios y commits: `git commit -m "feat: descripción"`
3. Push al branch: `git push origin feature/mi-feature`
4. Crea un Pull Request a `develop`

## 📝 Convenciones de Commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

- `feat`: Nueva funcionalidad
- `fix`: Corrección de bug
- `docs`: Cambios en documentación
- `style`: Formato de código
- `refactor`: Refactorización
- `test`: Tests
- `chore`: Tareas de mantenimiento

## 📄 Licencia

Este proyecto es privado y confidencial.

## 👥 Equipo

- **Desarrollador Principal**: Oscar
- **Repositorio**: https://github.com/4901oscar/HIS-Project

## 🆘 Soporte

Para reportar issues o solicitar features, usa el sistema de Issues de GitHub.

---

**Versión**: 1.0.0  
**Última actualización**: Abril 2026
