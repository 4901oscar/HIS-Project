# Design: Billing Service

## 1. Architecture Overview

**Arquitectura**: MVC | **Puerto**: 8086 | **Schema**: billing_schema

```
┌──────────────────────────────────────┐
│       BILLING SERVICE (8086)         │
│  ┌──────────────────────────────┐   │
│  │      Controller Layer        │   │
│  │  - InvoiceController         │   │
│  └───────────────┬──────────────┘   │
│  ┌───────────────▼──────────────┐   │
│  │       Service Layer          │   │
│  │  - InvoiceService            │   │
│  │  - PaymentService            │   │
│  └───────────────┬──────────────┘   │
│  ┌───────────────▼──────────────┐   │
│  │     Repository Layer         │   │
│  │  - InvoiceRepository         │   │
│  │  - ChargeRepository          │   │
│  │  - PaymentRepository         │   │
│  └───────────────┬──────────────┘   │
│  ┌───────────────▼──────────────┐   │
│  │ PostgreSQL (billing_schema)  │   │
│  └──────────────────────────────┘   │
└──────────────────────────────────────┘
```

## 2. Directory Structure

```
billing-service/
├── controller/
│   └── InvoiceController.java
├── service/
│   ├── InvoiceService.java
│   └── PaymentService.java
├── repository/
│   ├── InvoiceRepository.java
│   ├── ChargeRepository.java
│   └── PaymentRepository.java
├── model/
│   ├── Invoice.java
│   ├── Charge.java
│   ├── Payment.java
│   ├── InvoiceStatus.java
│   ├── ChargeType.java
│   └── PaymentMethod.java
├── dto/
│   ├── request/
│   │   ├── CreateInvoiceRequest.java
│   │   ├── ProcessPaymentRequest.java
│   │   └── ApplyDiscountRequest.java
│   └── response/
│       ├── InvoiceResponse.java
│       └── PaymentResponse.java
├── exception/
│   ├── InvoiceNotFoundException.java
│   ├── InvalidInvoiceStatusException.java
│   ├── InsufficientPaymentException.java
│   └── GlobalExceptionHandler.java
└── config/
    └── SecurityConfig.java
```

## 3. Data Model

### Entities

```java
@Entity @Table(name = "invoices", schema = "billing_schema")
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String invoiceNumber;     // INV-YYYYMMDD-XXXX (auto-generated)
    private String patientId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "invoice")
    private List<Charge> charges;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;     // PENDING, PAID, CANCELLED
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
}

@Entity @Table(name = "charges", schema = "billing_schema")
public class Charge {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne @JoinColumn(name = "invoice_id")
    private Invoice invoice;
    @Enumerated(EnumType.STRING)
    private ChargeType type;          // CONSULTATION, LABORATORY, MEDICATION, OTHER
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;      // quantity * unitPrice
}

@Entity @Table(name = "payments", schema = "billing_schema")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String invoiceId;
    private BigDecimal amount;
    private BigDecimal change;        // amount - invoice.total
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;     // CASH, CARD, TRANSFER
    private LocalDateTime paidAt;
    private String receivedBy;
}
```

### Database Schema

```sql
CREATE SCHEMA IF NOT EXISTS billing_schema;

CREATE TABLE billing_schema.invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(20) UNIQUE NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE billing_schema.charges (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL REFERENCES billing_schema.invoices(id),
    type VARCHAR(20) NOT NULL,
    description VARCHAR(300) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_subtotal_positive CHECK (subtotal >= 0)
);

CREATE TABLE billing_schema.payments (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    change_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    method VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP NOT NULL,
    received_by VARCHAR(36) NOT NULL
);

CREATE INDEX idx_invoices_patient ON billing_schema.invoices(patient_id);
CREATE INDEX idx_invoices_status ON billing_schema.invoices(status);
CREATE INDEX idx_invoices_number ON billing_schema.invoices(invoice_number);
```

## 4. Key Service Logic

### InvoiceService.createInvoice()
```java
@Transactional
public InvoiceResponse createInvoice(CreateInvoiceRequest request, String createdBy) {
    // 1. Generate invoice number: INV-YYYYMMDD-XXXX
    String invoiceNumber = generateInvoiceNumber();
    
    // 2. Calculate subtotals for each charge
    List<Charge> charges = request.getCharges().stream()
        .map(c -> {
            Charge charge = new Charge();
            charge.setType(c.getType());
            charge.setDescription(c.getDescription());
            charge.setQuantity(c.getQuantity());
            charge.setUnitPrice(c.getUnitPrice());
            charge.setSubtotal(c.getUnitPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
            return charge;
        }).collect(Collectors.toList());
    
    // 3. Calculate total
    BigDecimal subtotal = charges.stream()
        .map(Charge::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // 4. Create invoice
    Invoice invoice = new Invoice();
    invoice.setInvoiceNumber(invoiceNumber);
    invoice.setPatientId(request.getPatientId());
    invoice.setCharges(charges);
    invoice.setSubtotal(subtotal);
    invoice.setDiscountAmount(BigDecimal.ZERO);
    invoice.setTotal(subtotal);
    invoice.setStatus(InvoiceStatus.PENDING);
    invoice.setCreatedAt(LocalDateTime.now());
    invoice.setCreatedBy(createdBy);
    
    return mapToResponse(invoiceRepository.save(invoice));
}
```

### PaymentService.processPayment()
```java
@Transactional
public PaymentResponse processPayment(String invoiceId, ProcessPaymentRequest request, String receivedBy) {
    Invoice invoice = invoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new InvoiceNotFoundException("Factura no encontrada"));
    
    if (invoice.getStatus() != InvoiceStatus.PENDING) {
        throw new InvalidInvoiceStatusException("Solo se pueden pagar facturas PENDIENTES");
    }
    
    if (request.getAmount().compareTo(invoice.getTotal()) < 0) {
        throw new InsufficientPaymentException(
            "El monto pagado es insuficiente. Total: " + invoice.getTotal());
    }
    
    BigDecimal change = request.getAmount().subtract(invoice.getTotal());
    
    Payment payment = new Payment();
    payment.setInvoiceId(invoiceId);
    payment.setAmount(request.getAmount());
    payment.setChange(change);
    payment.setMethod(request.getMethod());
    payment.setPaidAt(LocalDateTime.now());
    payment.setReceivedBy(receivedBy);
    paymentRepository.save(payment);
    
    invoice.setStatus(InvoiceStatus.PAID);
    invoice.setUpdatedAt(LocalDateTime.now());
    invoiceRepository.save(invoice);
    
    return mapToResponse(payment, change);
}
```

## 5. Correctness Properties

### Property 1: Invoice Total Equals Sum of Charges Minus Discount
Para cualquier factura, total = sum(charges.subtotal) - discountAmount. Total nunca negativo.

### Property 2: Invoice Status Transitions are Valid
Solo PENDING → PAID | CANCELLED. Transiciones inválidas lanzan excepción.

### Property 3: Payment Amount Must Cover Invoice Total
Para cualquier pago, amount >= invoice.total. Si no, InsufficientPaymentException.

### Property 4: Patient Can Only Access Own Invoices
Usuario con rol PATIENT solo puede ver facturas donde patientId == userId.

## 6. Configuration

```yaml
server:
  port: 8086
spring:
  application:
    name: billing-service
  jpa:
    properties:
      hibernate:
        default_schema: billing_schema
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
```

---
**Created**: April 16, 2026 | **Status**: ✅ Ready | **Version**: 1.0.0
