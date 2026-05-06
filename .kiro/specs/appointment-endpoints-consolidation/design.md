# Design Document: Appointment Endpoints Consolidation

## Overview

Este documento describe el diseño técnico para la consolidación de endpoints de listado de citas en el Clinical Service. La refactorización reduce de 13 a 8 endpoints mediante la creación de un endpoint principal con filtros flexibles y un DTO unificado que sirve a todos los portales.

### Objetivos del Diseño

1. **Reducir duplicación**: Consolidar 5 endpoints redundantes que solo filtran por estado
2. **Unificar estructura de datos**: Crear AppointmentListItemResponse como DTO único para todos los listados
3. **Simplificar mantenimiento**: Centralizar lógica de mapeo y validación de pago en un solo lugar
4. **Mantener funcionalidad**: Preservar todos los casos de uso existentes durante la transición
5. **Facilitar frontend**: Proporcionar estructura consistente para reutilizar componentes de UI

### Alcance

**Incluye:**
- Consolidación de endpoints `/payment-queue`, `/lab-queue`, `/pharmacy-queue`, `/pending-triage`
- Creación de DTO unificado `AppointmentListItemResponse`
- Endpoint principal `/appointments` con query parameters flexibles
- Mapper unificado con validación de pago integrada
- Migración de endpoints mantenidos al DTO unificado
- Estrategia de deprecación gradual

**No incluye:**
- Cambios en la lógica de negocio de estados de citas
- Modificaciones al PaymentValidator
- Cambios en el modelo de dominio Appointment
- Refactorización de endpoints de creación/actualización

## Architecture

### Diagrama de Componentes


```mermaid
graph TB
    subgraph "Frontend Portals"
        A[Admission Portal]
        B[Cashier Portal]
        C[Triage Portal]
        D[Lab Portal]
        E[Pharmacy Portal]
        F[Doctor Portal]
        G[Patient Portal]
    end
    
    subgraph "AppointmentController"
        H[GET /appointments<br/>?status=X&date=Y&queue=Z]
        I[GET /appointments/my]
        J[GET /appointments/doctor]
        K[GET /appointments/today]
        L[GET /appointments/admission-queue]
        M[GET /appointments/{id}/triage]
        N[GET /appointments/{id}/qr-status]
        O[GET /appointments/available]
        P[GET /appointments/slots]
    end
    
    subgraph "Unified Mapper"
        Q[mapToUnifiedResponse]
        R[PaymentValidator]
        S[PatientServiceClient]
        T[DoctorRepository]
    end
    
    subgraph "Response"
        U[AppointmentListItemResponse]
    end
    
    A --> H
    B --> H
    C --> H
    D --> H
    E --> H
    A --> K
    A --> L
    F --> J
    G --> I
    
    H --> Q
    I --> Q
    J --> Q
    K --> Q
    L --> Q
    
    Q --> R
    Q --> S
    Q --> T
    Q --> U
    
    style H fill:#90EE90
    style Q fill:#FFD700
    style U fill:#87CEEB
