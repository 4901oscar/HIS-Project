# MedFlow HIS - Project Restructure Plan

## Current Structure
```
HIS-Project/
├── frontend-react/
├── Documentación/
└── (root files)
```

## Target Structure
```
medflow-his/                  <-- Carpeta principal (git init aquí)
├── .kiro/                    <-- Reglas de IA (Steering, Specs, Hooks)
├── .git/                     <-- Control de versiones global
├── .gitignore                <-- Ignora node_modules y targets de TODO el proyecto
├── docker-compose.yml        <-- Orquestador global
│
├── frontend-medflow/         <-- Proyecto React + Vite
│   ├── src/
│   └── package.json
│
├── backend-cloud/            <-- Infraestructura Spring Cloud
│   ├── api-gateway/
│   └── eureka-server/
│
├── backend-services/         <-- Microservicios de Negocio
│   ├── auth-service/
│   ├── patient-service/
│   └── clinical-service/
│
└── Documentacion/            <-- Documentación
    ├── casos-de-uso/
    └── diagramas/
```

## Migration Steps

### Step 1: Rename frontend-react to frontend-medflow
```bash
git mv frontend-react frontend-medflow
```

### Step 2: Create backend structure
```bash
mkdir -p backend-cloud/api-gateway
mkdir -p backend-cloud/eureka-server
mkdir -p backend-services/auth-service
mkdir -p backend-services/patient-service
mkdir -p backend-services/clinical-service
```

### Step 3: Update .gitignore for monorepo
Add patterns for:
- Java/Spring Boot (target/, *.class, *.jar)
- Node.js (node_modules/)
- Docker (.env files)
- IDE files

### Step 4: Create docker-compose.yml
Define services for:
- Frontend (React + Vite)
- API Gateway
- Eureka Server
- Auth Service
- Patient Service
- Clinical Service
- Database (PostgreSQL/MySQL)

### Step 5: Update documentation structure
```bash
git mv Documentación Documentacion
```

## Benefits of This Structure

1. **Monorepo**: Todo el código en un solo repositorio
2. **Organización clara**: Frontend, backend cloud, y servicios separados
3. **Docker**: Orquestación completa con docker-compose
4. **Git Flow**: Branches por feature que afectan múltiples servicios
5. **Kiro AI**: Reglas centralizadas para todo el proyecto

## Next Steps

1. ¿Quieres que ejecute la reestructuración ahora?
2. ¿Necesitas que cree los proyectos Spring Boot base?
3. ¿Configuramos docker-compose.yml primero?
