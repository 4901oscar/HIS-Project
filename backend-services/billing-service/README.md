# Billing Service

Microservicio de facturación y procesamiento de pagos para MedFlow HIS.

## Descripción

El Billing Service gestiona la facturación interna del hospital, procesamiento de pagos y generación de recibos. Permite registrar cargos por servicios médicos, procesar pagos y consultar el historial de facturas de los pacientes.

## Tecnologías

- **Spring Boot**: 3.2.4
- **Java**: 17
- **Base de Datos**: PostgreSQL (billing_schema)
- **Service Discovery**: Eureka Client
- **Testing**: JUnit 5, jqwik (Property-Based Testing), Testcontainers

## Configuración

### Puerto
- **Local**: 8086
- **Docker**: 8086

### Registro en Eureka
- **Service Name**: BILLING-SERVICE

### Variables de Entorno

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=medflow_db
DB_USERNAME=medflow_user
DB_PASSWORD=medflow_pass
EUREKA_SERVER_URL=http://localhost:8761/eureka/
```

## Ejecución Local

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar el servicio
mvn spring-boot:run

# Ejecutar con perfil Docker
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

## Ejecución con Docker

```bash
# Construir imagen
docker build -t medflow/billing-service:latest .

# Ejecutar contenedor
docker run -p 8086:8086 \
  -e DB_HOST=postgres \
  -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
  medflow/billing-service:latest
```

## Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con cobertura
mvn clean test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

## Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | /api/billing/invoices | Crear factura con cargos |
| GET | /api/billing/invoices | Listar facturas |
| GET | /api/billing/invoices/{id} | Consultar factura |
| POST | /api/billing/invoices/{id}/pay | Procesar pago |
| PUT | /api/billing/invoices/{id}/discount | Aplicar descuento |
| DELETE | /api/billing/invoices/{id} | Cancelar factura |
| GET | /api/billing/invoices/patient/{patientId} | Facturas de un paciente |

## Health Check

```bash
curl http://localhost:8086/actuator/health
```

## Dependencias

- Patient Service (8082): Datos demográficos del paciente
- API Gateway (8080): Enrutamiento y validación JWT
- Eureka Server (8761): Service Discovery

## Estructura del Proyecto

```
billing-service/
├── src/main/java/com/medflow/billing/
│   ├── controller/       # REST Controllers
│   ├── service/          # Business Logic
│   ├── repository/       # Data Access Layer
│   ├── model/            # JPA Entities
│   ├── dto/              # Data Transfer Objects
│   ├── exception/        # Custom Exceptions
│   └── config/           # Configuration Classes
├── src/main/resources/
│   ├── application.yml
│   ├── application-docker.yml
│   └── schema.sql
└── src/test/             # Unit & Integration Tests
```

## Modelo de Datos

### Invoice (Factura)
- invoiceNumber: Número único (INV-YYYYMMDD-XXXX)
- patientId: ID del paciente
- charges: Lista de cargos
- subtotal: Suma de cargos
- discountAmount: Descuento aplicado
- total: Total a pagar
- status: PENDING | PAID | CANCELLED

### Charge (Cargo)
- type: CONSULTATION | LABORATORY | MEDICATION | OTHER
- description: Descripción del servicio
- quantity: Cantidad
- unitPrice: Precio unitario
- subtotal: quantity * unitPrice

### Payment (Pago)
- invoiceId: ID de la factura
- amount: Monto pagado
- change: Cambio devuelto
- method: CASH | CARD | TRANSFER
- paidAt: Fecha y hora del pago
- receivedBy: Usuario que recibió el pago

## Reglas de Negocio

1. Solo facturas PENDING pueden ser pagadas
2. Solo facturas PENDING pueden ser canceladas o recibir descuentos
3. El monto pagado debe ser >= total de la factura
4. Paciente solo puede ver sus propias facturas
5. Total nunca puede ser negativo
6. Números de factura únicos y secuenciales

## Autor

MedFlow Development Team

## Versión

1.0.0
