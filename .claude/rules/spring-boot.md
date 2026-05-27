# Reglas Spring Boot

## Estructura de servicios
- Servicios CRUD: patrón MVC (Controller → Service → Repository)
- clinical-service: arquitectura hexagonal (puertos y adaptadores)
- No mezclar patrones entre servicios

## Convenciones
- DTOs separados de entidades JPA
- Validaciones con @Valid y Jakarta Validation
- Excepciones manejadas con @ControllerAdvice
- Inyección por constructor, nunca por campo (@Autowired en campo prohibido)

## Base de datos
- CERO JOINs entre schemas de PostgreSQL
- Comunicación entre servicios solo vía HTTP (RestTemplate/WebClient)
- Cada servicio es dueño exclusivo de su schema

## Seguridad
- JWT stateless, no guardar tokens en backend
- Roles RBAC: ADMIN, ADMISSION, VITAL_SIGNS, DOCTOR, LABORATORY, PHARMACY, CASHIER, PATIENT
- Endpoints protegidos por rol con @PreAuthorize