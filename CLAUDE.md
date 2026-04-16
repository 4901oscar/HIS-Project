\# HIS-Project (MedFlow HIS)



Sistema de Información Hospitalaria — MVP de graduación.

Microservicios con Spring Cloud + frontend React.



\## Stack



\### Backend

\- Java 17, Spring Boot 3.x, Spring Cloud (Eureka + Gateway)

\- PostgreSQL (schema-per-service, una sola instancia)

\- Redis (cache para slots de citas — clinical-service)

\- JWT stateless para auth

\- Maven



\### Frontend

\- React 18 (sin TypeScript), Vite, Tailwind CSS

\- React Router, Axios con capa de mocks (USE\_MOCK toggle)



\### DevOps

\- Docker Compose (monorepo)

\- Git Flow (main, develop, feature/\*, release/\*, hotfix/\*)



\## Estructura

\- `backend-cloud/`: eureka-server (8761), api-gateway (8080)

\- `backend-services/`: auth (8081), patient (8082), clinical (8083), lab (8084), pharmacy (8085), billing (8086)

\- `frontend-medflow/`: React app

\- `docker-compose.yml`: orquestación



\## Arquitectura de código

\- \*\*Hexagonal (puertos y adaptadores)\*\*: clinical-service (lógica pesada, triaje Manchester)

\- \*\*MVC por capas\*\*: servicios CRUD simples (patient, pharmacy, auth, lab, billing)



\## Reglas estrictas

\- ❌ CERO JOINs entre schemas de PostgreSQL

\- ✅ Comunicación entre servicios solo vía HTTP (composición de APIs)

\- Cada servicio es dueño de su schema



\## Roles RBAC

ADMIN, ADMISSION, VITAL\_SIGNS, DOCTOR, LABORATORY, PHARMACY, CASHIER, PATIENT



\## Convenciones de commits

Conventional Commits: feat, fix, docs, style, refactor, test, chore



\## Code review

\- Comentarios en español, técnicos y concisos

\- Categorías: crítico, mayor, menor

\- Verificar contratos de API y schemas antes de sugerir renombrados

\- Respetar el estilo arquitectónico del servicio (hexagonal vs MVC)



\## Fuera de alcance (MVP)

Biometría, integración SAT/FEL, push notifications, analytics avanzado.

Facturación es solo interna.

