# MedFlow HIS — Hospital Information System

Sistema de Información Hospitalaria construido con microservicios Spring Boot y frontend React. Cubre el flujo completo de atención médica: admisión → signos vitales → triaje → cita → consulta → receta → laboratorio → farmacia → caja.

---

## Documentación

Los documentos están en la carpeta `/docs/`:

| Documento | Qué explica |
|-----------|-------------|
| [01-ARQUITECTURA.md](./docs/01-ARQUITECTURA.md) | Estructura del sistema, microservicios, base de datos, infraestructura |
| [02-FLUJO-ATENCION.md](./docs/02-FLUJO-ATENCION.md) | El flujo completo de atención al paciente paso a paso |
| [03-ROLES-Y-PERMISOS.md](./docs/03-ROLES-Y-PERMISOS.md) | Los 8 roles del sistema y qué puede hacer cada uno |
| [04-API-ENDPOINTS.md](./docs/04-API-ENDPOINTS.md) | Todos los endpoints disponibles con ejemplos de request/response |
| [05-BASE-DE-DATOS.md](./docs/05-BASE-DE-DATOS.md) | Esquemas de base de datos, tablas y relaciones |
| [06-FRONTEND.md](./docs/06-FRONTEND.md) | Páginas del frontend, rutas, componentes y servicios |
| [07-COMO-CORRER.md](./docs/07-COMO-CORRER.md) | Cómo levantar el proyecto localmente |

---

## Stack rápido

- **Frontend**: React 19 + TypeScript + Vite + Tailwind CSS
- **Backend**: Java 17 + Spring Boot 3 + Spring Cloud
- **Base de datos**: PostgreSQL 15 (6 esquemas) + Redis 7
- **Infraestructura**: Docker Compose

## Puertos

| Servicio | Puerto |
|----------|--------|
| Frontend | 3000 |
| API Gateway | 8080 |
| Eureka Dashboard | 8761 |
| Auth Service | 8081 |
| Patient Service | 8082 |
| Clinical Service | 8083 |
| Lab Service | 8084 |
| Pharmacy Service | 8085 |
| Billing Service | 8086 |

## Inicio rápido

```bash
docker-compose up -d
```

Ver [07-COMO-CORRER.md](./docs/07-COMO-CORRER.md) para instrucciones completas.
