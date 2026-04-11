# MedFlow HIS - Hospital Information System

Sistema de Información Hospitalaria (HIS) basado en microservicios con arquitectura Spring Cloud y frontend React.

## 🏗️ Arquitectura

```
medflow-his/
├── frontend-medflow/         # React + Vite + TypeScript
├── backend-cloud/            # Spring Cloud Infrastructure
│   ├── eureka-server/        # Service Discovery (8761)
│   └── api-gateway/          # API Gateway (8080)
├── backend-services/         # Microservicios de Negocio
│   ├── auth-service/         # Autenticación (8081)
│   ├── patient-service/      # Gestión de Pacientes (8082)
│   └── clinical-service/     # Gestión Clínica (8083)
├── Documentacion/            # Casos de uso y diagramas
├── docker-compose.yml        # Orquestación de servicios
└── .kiro/                    # Configuración de IA
```

## 🚀 Tecnologías

### Frontend
- **React 18** con TypeScript
- **Vite** como build tool
- **TailwindCSS** para estilos
- **React Router** para navegación
- **Axios** para peticiones HTTP

### Backend
- **Spring Boot 3.x**
- **Spring Cloud** (Eureka, Gateway)
- **Spring Data JPA** + Hibernate
- **PostgreSQL 15**
- **JWT** para autenticación
- **Maven** como build tool

### DevOps
- **Docker** y **Docker Compose**
- **Git Flow** para control de versiones
- **GitHub** como repositorio remoto

## 📋 Prerequisitos

- **Node.js** 18+ y npm
- **Java** 17+
- **Maven** 3.8+
- **Docker** y **Docker Compose**
- **PostgreSQL** 15+ (si ejecutas sin Docker)
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

| Servicio | URL | Puerto |
|----------|-----|--------|
| Frontend | http://localhost:3000 | 3000 |
| API Gateway | http://localhost:8080 | 8080 |
| Eureka Dashboard | http://localhost:8761 | 8761 |
| Auth Service | http://localhost:8081 | 8081 |
| Patient Service | http://localhost:8082 | 8082 |
| Clinical Service | http://localhost:8083 | 8083 |
| PostgreSQL | localhost:5432 | 5432 |

## 📚 Documentación

- [Git Flow Workflow](./GITFLOW.md)
- [Backend Cloud](./backend-cloud/README.md)
- [Backend Services](./backend-services/README.md)
- [Casos de Uso](./Documentacion/casos-de-uso/)

## 🔐 Roles y Permisos

El sistema maneja los siguientes roles:

1. **Administrador**: Gestión completa del sistema
2. **Admisión**: Registro de pacientes y activación de citas
3. **Enfermería**: Captura de signos vitales
4. **Doctor**: Consultas médicas y recetas
5. **Laboratorio**: Gestión de muestras
6. **Farmacia**: Dispensación de medicamentos
7. **Caja**: Facturación

## 🌿 Git Flow

Este proyecto usa Git Flow para gestión de branches:

- `main`: Código en producción
- `develop`: Integración de features
- `feature/*`: Nuevas funcionalidades
- `release/*`: Preparación de releases
- `hotfix/*`: Fixes urgentes en producción

Ver [GITFLOW.md](./GITFLOW.md) para más detalles.

## 🧪 Testing

```bash
# Backend (cada servicio)
mvn test

# Frontend
cd frontend-medflow
npm run test
```

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
