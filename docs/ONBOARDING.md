# Onboarding — MedFlow HIS

Guía de inicio para nuevos desarrolladores.

## Leer primero

Lee los documentos en este orden para entender el proyecto:

1. [01-ARQUITECTURA.md](./01-ARQUITECTURA.md) — Cómo está estructurado el sistema
2. [02-FLUJO-ATENCION.md](./02-FLUJO-ATENCION.md) — El flujo de negocio completo
3. [03-ROLES-Y-PERMISOS.md](./03-ROLES-Y-PERMISOS.md) — Quién puede hacer qué
4. [07-COMO-CORRER.md](./07-COMO-CORRER.md) — Cómo levantar el proyecto

Para referencia técnica:
- [04-API-ENDPOINTS.md](./04-API-ENDPOINTS.md) — Todos los endpoints
- [05-BASE-DE-DATOS.md](./05-BASE-DE-DATOS.md) — Esquemas de base de datos
- [06-FRONTEND.md](./06-FRONTEND.md) — Estructura del frontend

## Setup rápido

```bash
git clone https://github.com/4901oscar/HIS-Project.git
cd HIS-Project
git checkout develop
cp .env.example .env
docker-compose up -d
```

Abre http://localhost:3000 y loguéate con `admin` / `Admin1234`.

## Metodología de desarrollo

El proyecto usa **Spec-Driven Design + TDD**:

1. Crea el spec en `.kiro/specs/{feature}/` con `requirements.md`, `design.md` y `tasks.md`
2. Escribe el test primero (RED)
3. Implementa el mínimo código para que pase (GREEN)
4. Refactoriza (REFACTOR)

## Reglas importantes

- **Cero JOINs entre esquemas de PostgreSQL** — si necesitas datos de otro dominio, haz una llamada HTTP al servicio correspondiente
- **clinical-service usa arquitectura hexagonal** — el dominio no puede importar clases de infraestructura
- **Los demás servicios usan MVC** — controller → service → repository
- **ADMIN bypassa todas las verificaciones de rol** — tanto en frontend como en backend

## Convenciones de commits

```
feat: nueva funcionalidad
fix: corrección de bug
docs: cambios en documentación
refactor: refactorización sin cambio de comportamiento
test: agregar o modificar tests
chore: tareas de mantenimiento
```
