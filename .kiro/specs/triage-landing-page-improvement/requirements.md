# Requirements Document

## Introduction

Este documento define los requisitos para mejorar la experiencia de usuario del rol Triaje en el sistema MedFlow. Actualmente, después del login con rol VITAL_SIGNS (Triaje), el usuario es redirigido a una página de búsqueda de DPI para captura de signos vitales (`/vitals`), lo cual no es intuitivo para el flujo de trabajo de triaje y viola el principio arquitectónico fundamental del sistema: **"Todo debe estar amarrado a la cita"**.

El sistema ya cuenta con un dashboard de citas pendientes de triaje (`/vitals/triage`) que es más apropiado como landing page para este rol. Este dashboard permite al personal de triaje ver las citas activas y capturar signos vitales en el contexto correcto de una cita específica.

El objetivo es:
1. Modificar la lógica de redirección post-login para que los usuarios con rol VITAL_SIGNS sean dirigidos directamente al dashboard de citas pendientes de triaje
2. Eliminar completamente la ruta `/vitals` y su componente asociado (`VitalSignsCapture.tsx`), ya que permite registrar signos vitales sin cita asociada, violando el principio arquitectónico del sistema

## Glossary

- **Login_System**: Sistema de autenticación que valida credenciales y redirige usuarios según su rol
- **Triage_Dashboard**: Dashboard que muestra la lista de citas activas pendientes de triaje (TriagePendingPage)
- **VITAL_SIGNS_Role**: Rol de usuario asignado al personal de triaje
- **Role_Routes_Map**: Mapeo de roles a rutas de redirección post-login
- **Appointment_Context**: Contexto de cita que debe estar presente para registrar signos vitales, asegurando trazabilidad

## Requirements

### Requirement 1: Redirección Post-Login para Rol Triaje

**User Story:** Como personal de triaje, quiero ser redirigido al dashboard de citas pendientes después del login, para poder comenzar inmediatamente con mi flujo de trabajo sin pasos adicionales.

#### Acceptance Criteria

1. WHEN a user with VITAL_SIGNS_Role successfully logs in, THE Login_System SHALL redirect to the Triage_Dashboard route (`/vitals/triage`)
2. WHEN a user with VITAL_SIGNS_Role logs in AND no previous location is stored, THE Login_System SHALL redirect to `/vitals/triage`
3. WHEN a user with VITAL_SIGNS_Role logs in AND a previous location is stored, THE Login_System SHALL redirect to the stored location
4. THE Role_Routes_Map SHALL map VITAL_SIGNS_Role to `/vitals/triage` route
5. WHEN the redirect occurs, THE Login_System SHALL preserve the authentication state

### Requirement 2: Acceso Directo al Dashboard de Triaje

**User Story:** Como personal de triaje, quiero acceder directamente al dashboard de citas pendientes, para visualizar inmediatamente las citas que requieren atención.

#### Acceptance Criteria

1. THE Triage_Dashboard SHALL remain accessible at route `/vitals/triage`
2. WHEN a user with VITAL_SIGNS_Role accesses `/vitals/triage`, THE system SHALL display the pending appointments list
3. THE Triage_Dashboard SHALL display all active appointments pending triage
4. THE Triage_Dashboard SHALL auto-refresh the appointments list every 30 seconds
5. THE Triage_Dashboard SHALL provide a manual refresh button with debounce protection

### Requirement 3: Eliminación de Ruta de Búsqueda de DPI

**User Story:** Como arquitecto del sistema, quiero eliminar la ruta `/vitals` y su componente asociado, para asegurar que todos los signos vitales estén amarrados a una cita y mantener la integridad arquitectónica del sistema.

#### Acceptance Criteria

1. THE system SHALL remove the route `/vitals` from the routing configuration
2. THE system SHALL remove the VitalSignsCapture component file (`VitalSignsCapture.tsx`)
3. WHEN a user attempts to navigate to `/vitals`, THE system SHALL return a 404 or redirect to an appropriate page
4. THE system SHALL ensure all vital signs capture flows require an associated appointment
5. THE routing configuration SHALL not contain any references to the removed `/vitals` route

### Requirement 4: Navegación desde Dashboard de Triaje

**User Story:** Como personal de triaje, quiero poder acceder a la captura de signos vitales desde el dashboard de citas pendientes, para completar el proceso de triaje de manera fluida.

#### Acceptance Criteria

1. WHEN a user clicks on an appointment in Triage_Dashboard, THE system SHALL navigate to the vital signs capture page with appointment context
2. THE vital signs capture page SHALL receive appointmentId and patientId as navigation state
3. WHEN vital signs are saved from the capture page, THE system SHALL redirect back to Triage_Dashboard
4. WHEN the user cancels vital signs capture, THE system SHALL redirect back to Triage_Dashboard
5. THE navigation flow SHALL maintain data consistency between pages

### Requirement 5: Compatibilidad con Otros Roles

**User Story:** Como administrador del sistema, quiero asegurar que otros roles no sean afectados por este cambio, para mantener la estabilidad del sistema.

#### Acceptance Criteria

1. WHEN a user with ADMIN role logs in, THE Login_System SHALL redirect to `/administrator`
2. WHEN a user with ADMISSION role logs in, THE Login_System SHALL redirect to `/admission`
3. WHEN a user with DOCTOR role logs in, THE Login_System SHALL redirect to `/doctor`
4. WHEN a user with LABORATORY role logs in, THE Login_System SHALL redirect to `/lab`
5. WHEN a user with PHARMACY role logs in, THE Login_System SHALL redirect to `/pharmacy`
6. WHEN a user with CASHIER role logs in, THE Login_System SHALL redirect to `/cashier`
7. WHEN a user with PATIENT role logs in, THE Login_System SHALL redirect to `/`
8. FOR ALL non-VITAL_SIGNS roles, THE Login_System SHALL maintain existing redirect behavior

