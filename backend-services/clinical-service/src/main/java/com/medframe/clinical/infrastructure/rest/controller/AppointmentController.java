package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.application.usecase.ManageAppointmentUseCaseImpl;
import com.medframe.clinical.config.ConsultationPriceConfig;
import com.medframe.clinical.domain.exception.InvalidAppointmentStatusException;
import com.medframe.clinical.domain.exception.PaymentValidationException;
import com.medframe.clinical.domain.exception.ServiceUnavailableException;
import com.medframe.clinical.domain.exception.TriageNotFoundException;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.Appointment.AppointmentStatus;
import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.port.in.GetAppointmentTriageUseCase;
import com.medframe.clinical.domain.port.in.ListPendingTriageAppointmentsUseCase;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.domain.port.out.PatientServiceClient;
import com.medframe.clinical.domain.service.AppointmentManager;
import com.medframe.clinical.domain.service.PaymentValidationError;
import com.medframe.clinical.domain.service.PaymentValidationResult;
import com.medframe.clinical.infrastructure.client.BillingServiceClient;
import com.medframe.clinical.infrastructure.client.dto.ChargeRequest;
import com.medframe.clinical.infrastructure.client.dto.CreateInvoiceRequest;
import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import com.medframe.clinical.infrastructure.client.dto.PatientDTO;
import com.medframe.clinical.infrastructure.rest.dto.request.CreateAppointmentRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.HoldSlotRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.UpdateInvoiceIdRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentWithPaymentStatusResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.AvailableSlotsResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.QRStatusResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.TriageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clinical/appointments")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {
    
    private final ManageAppointmentUseCase manageAppointmentUseCase;
    private final AppointmentManager appointmentManager;
    private final PatientServiceClient patientServiceClient;
    private final DoctorRepository doctorRepository;
    private final GetAppointmentTriageUseCase getAppointmentTriageUseCase;
    private final ListPendingTriageAppointmentsUseCase listPendingTriageAppointmentsUseCase;
    private final BillingServiceClient billingServiceClient;
    private final ConsultationPriceConfig consultationPriceConfig;
    private final com.medframe.clinical.domain.service.PaymentValidator paymentValidator;
    private final AppointmentRepository appointmentRepository;
    
    @Value("${billing.service.enabled:true}")
    private boolean billingServiceEnabled;
    
    private static final DateTimeFormatter INVOICE_TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /** 
     * All appointments — ADMISSION / ADMIN.
     * 
     * <p>Soporta filtro opcional para reconciliación manual:</p>
     * <ul>
     *   <li><code>?missingInvoice=true</code> - Retorna solo citas sin factura (invoiceId NULL)</li>
     * </ul>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-12.1: Endpoint para consultar citas sin factura</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> listAll(
            @RequestParam(required = false, defaultValue = "false") boolean missingInvoice) {
        
        List<Appointment> appointments;
        
        if (missingInvoice) {
            // Filtrar solo citas sin factura para reconciliación manual
            appointments = manageAppointmentUseCase.listAppointmentsWithoutInvoice();
            log.info("Consultando citas sin factura. Total encontradas: {}", appointments.size());
        } else {
            // Retornar todas las citas
            appointments = manageAppointmentUseCase.listAll();
        }
        
        List<AppointmentResponse> list = appointments.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(list);
    }

    /** Appointments for the authenticated patient. */
    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> listMine() {
        List<AppointmentResponse> list = manageAppointmentUseCase.listMyAppointments()
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Appointments assigned to the authenticated doctor. */
    @GetMapping("/doctor")
    public ResponseEntity<List<AppointmentResponse>> listDoctor() {
        List<AppointmentResponse> list = manageAppointmentUseCase.listDoctorAppointments()
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/clinical/appointments/pending-triage
     * Lists all active appointments that are waiting for triage.
     * Returns 200 with array of appointments (empty array if none).
     * Requirements: 4.3, 4.4
     */
    @GetMapping("/pending-triage")
    public ResponseEntity<List<AppointmentResponse>> listPendingTriageAppointments() {
        List<AppointmentResponse> list = listPendingTriageAppointmentsUseCase.listPendingTriageAppointments()
                .stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/clinical/appointments/today
     * Lista todas las citas del día actual con información de estado de pago.
     * 
     * <p>Este endpoint proporciona al personal de recepción una vista completa de todas
     * las citas del día con indicadores visuales del estado de pago, facilitando la
     * gestión y activación de citas.</p>
     * 
     * <p><strong>Indicadores visuales:</strong></p>
     * <ul>
     *   <li>Verde (PAID): Cita pagada - Puede activarse</li>
     *   <li>Naranja (PENDING): Cita no pagada - Debe pagar en caja primero</li>
     *   <li>Rojo (CANCELLED): Factura cancelada - Contacte administración</li>
     *   <li>Gris (NO_INVOICE): Cita sin factura - Activar bajo responsabilidad</li>
     * </ul>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-11.1-11.8: Reception UI con indicadores visuales de estado de pago</li>
     * </ul>
     * 
     * @return Lista de citas del día con información de estado de pago
     */
    @GetMapping("/today")
    public ResponseEntity<List<AppointmentWithPaymentStatusResponse>> listTodayAppointments() {
        log.info("Listing today's appointments with payment status");
        
        // 1. Obtener todas las citas del día actual
        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = manageAppointmentUseCase.listAll().stream()
                .filter(appt -> appt.getAppointmentDate().equals(today))
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Found {} appointments for today", todayAppointments.size());
        
        // 2. Mapear cada cita con su estado de pago
        List<AppointmentWithPaymentStatusResponse> response = todayAppointments.stream()
                .map(this::mapToAppointmentWithPaymentStatus)
                .collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mapea una cita a AppointmentWithPaymentStatusResponse con información de estado de pago.
     * 
     * @param appointment la cita a mapear
     * @return AppointmentWithPaymentStatusResponse con indicadores visuales
     */
    private AppointmentWithPaymentStatusResponse mapToAppointmentWithPaymentStatus(Appointment appointment) {
        // 1. Obtener información del paciente
        String patientName = "Paciente";
        String patientDpi = null;
        try {
            PatientDTO patient = (PatientDTO) patientServiceClient.getPatient(appointment.getPatientId());
            patientName = patient.getFullName();
            patientDpi = patient.getDpi();
        } catch (Exception e) {
            log.warn("Could not fetch patient data for appointment {}: {}", 
                     appointment.getId(), e.getMessage());
        }
        
        // 2. Obtener información del doctor
        String doctorName = doctorRepository.findById(appointment.getDoctorId())
                .map(d -> "Dr. " + d.getName())
                .orElse("Dr. Asignado");
        
        // 3. Validar estado de pago
        PaymentValidationResult validationResult = paymentValidator.validatePayment(
            appointment.getInvoiceId(), 
            appointment.getId()
        );
        
        // 4. Construir respuesta con indicadores visuales
        AppointmentWithPaymentStatusResponse.AppointmentWithPaymentStatusResponseBuilder builder = 
                AppointmentWithPaymentStatusResponse.builder()
                .id(appointment.getId())
                .patientName(patientName)
                .patientDpi(patientDpi)
                .doctorName(doctorName)
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus().name());
        
        // 5. Asignar indicadores según resultado de validación
        if (validationResult.isAllowed()) {
            if (validationResult.isHasWarning()) {
                // Caso: Cita sin factura (NO_INVOICE)
                builder
                        .paymentStatus("NO_INVOICE")
                        .paymentStatusLabel("SIN FACTURA")
                        .paymentStatusColor("gray")
                        .canActivate(true)
                        .activateButtonTooltip("Cita sin factura - Activar bajo responsabilidad");
            } else {
                // Caso: Factura PAID
                builder
                        .paymentStatus("PAID")
                        .paymentStatusLabel("PAGADA")
                        .paymentStatusColor("green")
                        .canActivate(true)
                        .activateButtonTooltip("Cita pagada - Puede activarse")
                        .invoiceNumber(validationResult.getInvoice() != null ? 
                                validationResult.getInvoice().getInvoiceNumber() : null);
            }
        } else {
            // Validación falló
            PaymentValidationError error = validationResult.getError();
            
            if (error == PaymentValidationError.PAYMENT_PENDING) {
                builder
                        .paymentStatus("PENDING")
                        .paymentStatusLabel("PENDIENTE")
                        .paymentStatusColor("orange")
                        .canActivate(false)
                        .activateButtonTooltip("El paciente debe pagar en caja primero");
                
            } else if (error == PaymentValidationError.INVOICE_CANCELLED) {
                builder
                        .paymentStatus("CANCELLED")
                        .paymentStatusLabel("CANCELADA")
                        .paymentStatusColor("red")
                        .canActivate(false)
                        .activateButtonTooltip("Factura cancelada - Contacte administración");
                
            } else {
                // Otros errores (SERVICE_TIMEOUT, SERVICE_ERROR, INVOICE_NOT_FOUND, etc.)
                builder
                        .paymentStatus("ERROR")
                        .paymentStatusLabel("ERROR")
                        .paymentStatusColor("red")
                        .canActivate(false)
                        .activateButtonTooltip("Error al validar pago - Contacte administración");
            }
        }
        
        return builder.build();
    }

    /** Available slots for a date — excludes slots held by other sessions. */
    @GetMapping("/available")
    public ResponseEntity<List<String>> getAvailableSlotsForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sessionId) {
        List<LocalTime> slots = ((ManageAppointmentUseCaseImpl) manageAppointmentUseCase)
                .findAvailableSlotsForDate(date, sessionId);
        List<String> formatted = slots.stream()
            .map(t -> String.format("%02d:%02d", t.getHour(), t.getMinute()))
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(formatted);
    }

    /** Hold a (date, time) slot for 10 minutes. */
    @PostMapping("/hold")
    public ResponseEntity<Void> holdSlot(@Valid @RequestBody HoldSlotRequest request) {
        boolean held = manageAppointmentUseCase.holdSlot(
                request.getSessionId(), request.getDate(), request.getTime());
        return held
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    /** Release a hold by sessionId. */
    @DeleteMapping("/hold")
    public ResponseEntity<Void> releaseHold(@RequestParam String sessionId) {
        manageAppointmentUseCase.releaseHold(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @RequestParam String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<LocalTime> slots = manageAppointmentUseCase.findAvailableSlots(doctorId, date);
        
        AvailableSlotsResponse response = new AvailableSlotsResponse(
            doctorId,
            date,
            slots
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.error("=== createAppointment - userId: {}, request.patientId: {} ===", userId, request.getPatientId());

        // 1. Determine patient ID
        String patientId;
        String patientEmail = "no-email@medflow.com";
        String patientFirstName = "Paciente";
        
        if (request.getPatientId() != null && !request.getPatientId().isBlank()) {
            log.error("=== Usando patientId del request: {} ===", request.getPatientId());
            // Patient ID provided in request (admission flow)
            patientId = request.getPatientId();
            try {
                PatientDTO patient = (PatientDTO) patientServiceClient.getPatient(patientId);
                patientEmail = patient.getEmail();
                patientFirstName = patient.getFirstName();
            } catch (Exception e) {
                log.warn("No se pudo obtener info del paciente {}: {}", patientId, e.getMessage());
            }
        } else {
            // No patient ID provided, use authenticated user (patient self-booking flow)
            try {
                log.error("=== Buscando paciente por auth_user_id: {} ===", userId);
                PatientDTO patient = (PatientDTO) patientServiceClient.getPatientByAuthUserId(userId);
                patientId = patient.getId(); // Use the real patient ID from patient-service
                log.error("=== Paciente encontrado. patient_id: {}, auth_user_id: {} ===", patientId, patient.getAuthUserId());
                patientEmail = patient.getEmail();
                patientFirstName = patient.getFirstName();
            } catch (Exception e) {
                log.error("No se pudo obtener info del paciente con auth_user_id {}: {}", userId, e.getMessage());
                throw new RuntimeException("No se pudo obtener información del paciente");
            }
        }

        // 2. Create appointment (manual or auto-assignment) — exactly once
        log.error("=== Antes de crear cita. patientId: {} ===", patientId);
        Appointment appointment;
        if (request.getDoctorId() != null && !request.getDoctorId().trim().isEmpty()) {
            appointment = appointmentManager.createAppointment(
                patientId, request.getDoctorId(),
                request.getAppointmentDate(), request.getAppointmentTime(),
                request.getNotes(), userId, true);
        } else {
            appointment = manageAppointmentUseCase.createAppointmentWithAutoAssignment(
                patientId,
                request.getAppointmentDate(), request.getAppointmentTime(),
                request.getNotes(), userId);
        }
        log.error("=== Después de crear cita. appointment.patientId: {} ===", appointment.getPatientId());

        // 3. Release slot hold if provided
        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            manageAppointmentUseCase.releaseHold(request.getSessionId());
        }

        // 4. Create invoice in Billing Service (with resilience)
        // REQ-1.1, REQ-1.2, REQ-1.3, REQ-2.1, REQ-2.3, REQ-11.4, REQ-11.5
        String invoiceNumber;
        InvoiceResponse invoiceResponse = null;
        
        // Verificar feature flag antes de intentar crear factura
        if (billingServiceEnabled) {
            try {
                // Construir request de factura con cargo de consulta
                ChargeRequest consultationCharge = new ChargeRequest(
                        "CONSULTATION",
                        "Consulta médica general",
                        1,
                        consultationPriceConfig.getPrice()
                );
                
                CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest(
                        patientId,
                        appointment.getId(),
                        Collections.singletonList(consultationCharge)
                );
                
                // Llamar a Billing Service (con circuit breaker y retry)
                invoiceResponse = billingServiceClient.createInvoice(invoiceRequest, userId);
                
                if (invoiceResponse != null) {
                    // Factura creada exitosamente
                    appointment.setInvoiceId(invoiceResponse.getId());
                    invoiceNumber = invoiceResponse.getInvoiceNumber();
                    log.info("Factura creada exitosamente para cita {}. InvoiceId: {}, InvoiceNumber: {}",
                            appointment.getId(), invoiceResponse.getId(), invoiceNumber);
                } else {
                    // Fallback ejecutado - Billing Service no disponible
                    appointment.setInvoiceId(null);
                    invoiceNumber = generateTemporaryInvoiceNumber();
                    log.warn("Billing Service no disponible. Cita {} creada sin factura. Número temporal: {}",
                            appointment.getId(), invoiceNumber);
                }
                
            } catch (Exception e) {
                // Error inesperado - usar estrategia de compensación
                appointment.setInvoiceId(null);
                invoiceNumber = generateTemporaryInvoiceNumber();
                log.error("Error inesperado al crear factura para cita {}. Usando número temporal: {}",
                        appointment.getId(), invoiceNumber, e);
            }
        } else {
            // Feature flag desactivado - usar comportamiento anterior
            appointment.setInvoiceId(null);
            invoiceNumber = generateTemporaryInvoiceNumber();
            log.info("Integración con Billing Service desactivada (feature flag). " +
                    "Cita {} creada con número temporal: {}", appointment.getId(), invoiceNumber);
        }

        // 5. Resolve doctor name from repository (fallback to placeholder)
        String doctorName = doctorRepository.findById(appointment.getDoctorId())
                .map(d -> "Dr. " + d.getName())
                .orElse("Dr. Asignado");

        // 6. Attach QR code and send confirmation email (graceful — never blocks the response)
        // REQ-1.4, REQ-1.5, REQ-2.4
        appointmentManager.attachQRAndNotify(appointment, patientEmail, patientFirstName, doctorName, invoiceNumber);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(appointment));
    }
    
    /**
     * Genera un número de factura temporal cuando Billing Service no está disponible.
     * 
     * <p>Este método es parte de la estrategia de compensación que permite al Clinical Service
     * continuar funcionando incluso cuando el Billing Service falla. El número temporal
     * sigue el formato estándar pero será reemplazado por un número real cuando se realice
     * la reconciliación manual.</p>
     * 
     * <p><strong>Formato:</strong> INV-yyyyMMddHHmmss (ejemplo: INV-20260424143025)</p>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-2.3: Generar número temporal si billing falla</li>
     *   <li>BR-6: Formato de número de factura</li>
     * </ul>
     * 
     * @return Número de factura temporal con timestamp
     */
    private String generateTemporaryInvoiceNumber() {
        return "INV-" + LocalDateTime.now().format(INVOICE_TIMESTAMP_FORMATTER);
    }
    
    /**
     * Generates a unique invoice number using timestamp format.
     * Format: INV-yyyyMMddHHmmss (e.g., INV-20260422143025)
     * 
     * @deprecated Use {@link #generateTemporaryInvoiceNumber()} instead
     */
    @Deprecated
    private String generateInvoiceNumber() {
        return generateTemporaryInvoiceNumber();
    }
    
    /** QR scan — validates time window and activates the appointment if within window. */
    @PostMapping("/{id}/scan")
    public ResponseEntity<Map<String, Object>> scanAppointment(@PathVariable String id) {
        com.medframe.clinical.domain.model.ScanResult result =
                appointmentManager.validateAndActivateAppointment(id, LocalDateTime.now());
        Appointment appt = result.getAppointment();
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("status", result.getStatus().name());
        body.put("message", result.getMessage());
        body.put("appointmentId", appt.getId());
        body.put("patientId", appt.getPatientId());
        body.put("doctorId", appt.getDoctorId());
        body.put("date", appt.getAppointmentDate().toString());
        body.put("time", appt.getAppointmentTime().toString());
        body.put("appointmentStatus", appt.getStatus().name());
        return ResponseEntity.ok(body);
    }

    /**
     * PUT /api/clinical/appointments/{id}/activate
     * Activa una cita médica después de validar el estado de pago.
     * 
     * <p>Este endpoint implementa la validación de pago antes de activar una cita.
     * Solo permite activar citas que cumplan con las siguientes condiciones:</p>
     * <ul>
     *   <li>La cita debe estar en estado SCHEDULED</li>
     *   <li>La factura asociada debe estar en estado PAID (o invoice_id = NULL para casos de compensación)</li>
     *   <li>El Billing Service debe estar disponible (o validación deshabilitada)</li>
     * </ul>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-1.1-1.8: Validación de estado de pago al activar cita</li>
     *   <li>REQ-2.1-2.4: Manejo de citas sin factura (compensation case)</li>
     *   <li>REQ-3.1-3.5: Resiliencia ante fallos de Billing Service</li>
     *   <li>REQ-5.1-5.8: Integración de validación en endpoint de activación</li>
     * </ul>
     * 
     * @param id ID de la cita a activar
     * @return 200 OK si la activación fue exitosa
     * @throws InvalidAppointmentStatusException si la cita no está en estado SCHEDULED (400)
     * @throws PaymentValidationException si la validación de pago falla (400)
     * @throws ServiceUnavailableException si el Billing Service no está disponible (503)
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateAppointment(@PathVariable String id) {
        log.info("Attempting to activate appointment {}", id);
        
        // 1. Validar que la cita existe
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Appointment {} not found", id);
                    return new RuntimeException("Cita no encontrada: " + id);
                });
        
        // 2. Validar que la cita está en estado SCHEDULED
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            log.error("Cannot activate appointment {} - current status is {}", 
                      id, appointment.getStatus());
            throw new InvalidAppointmentStatusException(
                "La cita no puede activarse. Estado actual: " + appointment.getStatus()
            );
        }
        
        // 3. Validar el estado de pago
        PaymentValidationResult validationResult = paymentValidator.validatePayment(
            appointment.getInvoiceId(), 
            appointment.getId()
        );
        
        // 4. Manejar resultado de validación
        if (!validationResult.isAllowed()) {
            // Validación falló - determinar tipo de error
            PaymentValidationError error = validationResult.getError();
            
            if (error == PaymentValidationError.SERVICE_TIMEOUT || 
                error == PaymentValidationError.SERVICE_ERROR) {
                // Error de servicio - retornar 503
                log.error("Cannot activate appointment {} - Billing Service unavailable: {}", 
                          id, validationResult.getErrorMessage());
                throw new ServiceUnavailableException(validationResult.getErrorMessage());
            } else {
                // Error de validación de negocio - retornar 400
                log.error("Cannot activate appointment {} - Payment validation failed: {} - {}", 
                          id, error, validationResult.getErrorMessage());
                throw new PaymentValidationException(
                    validationResult.getErrorMessage(), 
                    error
                );
            }
        }
        
        // 5. Validación exitosa - activar la cita
        if (validationResult.isHasWarning()) {
            log.warn("Activating appointment {} with warning: {}", 
                     id, validationResult.getWarningMessage());
        } else {
            log.info("Payment validation SUCCESS for appointment {} - Proceeding with activation", id);
        }
        
        manageAppointmentUseCase.activateAppointment(id);
        
        log.info("Appointment {} activated successfully", id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable String id) {
        manageAppointmentUseCase.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * PATCH /api/clinical/appointments/{id}/invoice
     * Actualiza el invoiceId de una cita durante reconciliación manual.
     * 
     * <p>Este endpoint se utiliza cuando el Billing Service no estaba disponible
     * durante la creación de la cita y se necesita vincular manualmente una factura
     * existente. Realiza las siguientes validaciones:</p>
     * <ul>
     *   <li>Verifica que la factura existe en el Billing Service</li>
     *   <li>Verifica que la factura corresponde al mismo patientId</li>
     *   <li>Actualiza el invoiceId en la cita</li>
     *   <li>Actualiza el appointmentId en la factura (Billing Service)</li>
     * </ul>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-12.2: Actualizar invoiceId manualmente</li>
     *   <li>REQ-12.3: Validar que factura existe</li>
     *   <li>REQ-12.4: Validar que patientId coincide</li>
     *   <li>REQ-12.5: Actualizar appointmentId en factura</li>
     *   <li>REQ-12.7: Registrar log de reconciliación</li>
     * </ul>
     * 
     * @param id ID de la cita a actualizar
     * @param request Request con el invoiceId a vincular
     * @param userId ID del usuario que realiza la reconciliación (para auditoría)
     * @return 200 OK con la cita actualizada si la operación fue exitosa
     * @throws RuntimeException si la cita no existe, la factura no existe, o el patientId no coincide
     */
    @PatchMapping("/{id}/invoice")
    public ResponseEntity<AppointmentResponse> updateInvoiceId(
            @PathVariable String id,
            @Valid @RequestBody UpdateInvoiceIdRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Iniciando reconciliación manual. AppointmentId: {}, InvoiceId: {}, UserId: {}",
                id, request.getInvoiceId(), userId);
        
        // 1. Obtener la cita
        Appointment appointment = manageAppointmentUseCase.listAll().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + id));
        
        // 2. Validar que la factura existe en Billing Service
        // REQ-12.3: Validar que factura existe
        InvoiceResponse invoice = billingServiceClient.getInvoice(request.getInvoiceId());
        
        if (invoice == null) {
            log.error("Factura no encontrada en Billing Service. InvoiceId: {}", request.getInvoiceId());
            throw new RuntimeException("Factura no encontrada en Billing Service: " + request.getInvoiceId());
        }
        
        // 3. Validar que el patientId coincide
        // REQ-12.4: Validar que patientId coincide
        if (!invoice.getPatientId().equals(appointment.getPatientId())) {
            log.error("PatientId no coincide. Appointment.patientId: {}, Invoice.patientId: {}",
                    appointment.getPatientId(), invoice.getPatientId());
            throw new RuntimeException("La factura no corresponde al mismo paciente de la cita");
        }
        
        // 4. Actualizar invoiceId en la cita
        // REQ-12.2: Actualizar invoiceId
        appointment = manageAppointmentUseCase.updateInvoiceId(id, request.getInvoiceId());
        
        // 5. Actualizar appointmentId en la factura (Billing Service)
        // REQ-12.5: Actualizar appointmentId en factura
        boolean invoiceUpdated = billingServiceClient.updateInvoiceAppointmentId(
                request.getInvoiceId(), id, userId);
        
        if (!invoiceUpdated) {
            log.warn("No se pudo actualizar appointmentId en Billing Service. " +
                    "La cita fue actualizada pero la factura no. InvoiceId: {}", request.getInvoiceId());
            // No lanzamos excepción porque la cita ya fue actualizada
        }
        
        // 6. Registrar log de reconciliación manual
        // REQ-12.7: Registrar log de reconciliación
        log.info("Reconciliación manual completada exitosamente. AppointmentId: {}, InvoiceId: {}, " +
                        "PatientId: {}, ReconciledBy: {}",
                id, request.getInvoiceId(), appointment.getPatientId(), userId);
        
        return ResponseEntity.ok(mapToResponse(appointment));
    }
    
    /**
     * GET /api/clinical/appointments/{id}/qr-status
     * Obtiene el estado de pago de una cita para mostrar en el QR Scanner.
     * 
     * <p>Este endpoint proporciona información completa sobre el estado de pago de una cita
     * cuando se escanea su código QR, permitiendo al personal de recepción tomar decisiones
     * informadas sobre si activar o no la cita.</p>
     * 
     * <p><strong>Casos manejados:</strong></p>
     * <ul>
     *   <li>PAID: Cita pagada - Puede activarse</li>
     *   <li>PENDING: Cita no pagada - Debe pagar en caja primero</li>
     *   <li>CANCELLED: Factura cancelada - Contacte administración</li>
     *   <li>NO_INVOICE: Cita sin factura - Activar bajo responsabilidad</li>
     *   <li>ERROR: Error al validar pago - Intente nuevamente</li>
     * </ul>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-10.1-10.8: QR inteligente con estado de pago</li>
     * </ul>
     * 
     * @param id ID de la cita
     * @return QRStatusResponse con información completa del estado de pago
     * @throws RuntimeException si la cita no existe (404)
     */
    @GetMapping("/{id}/qr-status")
    public ResponseEntity<QRStatusResponse> getQRStatus(@PathVariable String id) {
        log.info("Getting QR status for appointment {}", id);
        
        // 1. Validar que la cita existe
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Appointment {} not found for QR status", id);
                    return new RuntimeException("Cita no encontrada: " + id);
                });
        
        // 2. Obtener información del paciente
        String patientName = "Paciente";
        try {
            PatientDTO patient = (PatientDTO) patientServiceClient.getPatient(appointment.getPatientId());
            patientName = patient.getFullName();
        } catch (Exception e) {
            log.warn("Could not fetch patient data for QR status: {}", e.getMessage());
        }
        
        // 3. Obtener información del doctor
        String doctorName = doctorRepository.findById(appointment.getDoctorId())
                .map(d -> "Dr. " + d.getName())
                .orElse("Dr. Asignado");
        
        // 4. Validar estado de pago
        PaymentValidationResult validationResult = paymentValidator.validatePayment(
            appointment.getInvoiceId(), 
            appointment.getId()
        );
        
        // 5. Construir respuesta según resultado de validación
        QRStatusResponse.QRStatusResponseBuilder responseBuilder = QRStatusResponse.builder()
                .appointmentId(appointment.getId())
                .patientName(patientName)
                .doctorName(doctorName)
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .appointmentStatus(appointment.getStatus().name());
        
        if (validationResult.isAllowed()) {
            // Pago validado exitosamente o caso especial (sin factura)
            if (validationResult.isHasWarning()) {
                // Caso: Cita sin factura (compensation case)
                responseBuilder
                        .paymentStatus("NO_INVOICE")
                        .message("Cita sin factura - Activar bajo responsabilidad")
                        .canActivate(true)
                        .warning(validationResult.getWarningMessage());
                log.info("QR status for appointment {}: NO_INVOICE (can activate with warning)", id);
            } else {
                // Caso: Factura PAID
                responseBuilder
                        .paymentStatus("PAID")
                        .message("Cita pagada - Puede activarse")
                        .canActivate(true)
                        .invoiceNumber(validationResult.getInvoice() != null ? 
                                validationResult.getInvoice().getInvoiceNumber() : null);
                log.info("QR status for appointment {}: PAID (can activate)", id);
            }
        } else {
            // Validación falló
            PaymentValidationError error = validationResult.getError();
            
            if (error == PaymentValidationError.PAYMENT_PENDING) {
                responseBuilder
                        .paymentStatus("PENDING")
                        .message("Cita no pagada - Debe pagar en caja primero")
                        .canActivate(false);
                log.warn("QR status for appointment {}: PENDING (cannot activate)", id);
                
            } else if (error == PaymentValidationError.INVOICE_CANCELLED) {
                responseBuilder
                        .paymentStatus("CANCELLED")
                        .message("Factura cancelada - Contacte administración")
                        .canActivate(false);
                log.warn("QR status for appointment {}: CANCELLED (cannot activate)", id);
                
            } else if (error == PaymentValidationError.INVOICE_NOT_FOUND) {
                responseBuilder
                        .paymentStatus("ERROR")
                        .message("Factura no encontrada - Contacte administración")
                        .canActivate(false);
                log.error("QR status for appointment {}: INVOICE_NOT_FOUND (cannot activate)", id);
                
            } else if (error == PaymentValidationError.SERVICE_TIMEOUT || 
                       error == PaymentValidationError.SERVICE_ERROR) {
                responseBuilder
                        .paymentStatus("ERROR")
                        .message("Error al validar pago - Intente nuevamente")
                        .canActivate(false);
                log.error("QR status for appointment {}: SERVICE_ERROR (cannot activate)", id);
                
            } else {
                responseBuilder
                        .paymentStatus("ERROR")
                        .message("Error desconocido - Contacte administración")
                        .canActivate(false);
                log.error("QR status for appointment {}: UNKNOWN_ERROR (cannot activate)", id);
            }
        }
        
        return ResponseEntity.ok(responseBuilder.build());
    }
    
    /**
     * GET /api/clinical/appointments/{id}/triage
     * Retrieves the triage record associated with a specific appointment.
     * Returns 200 with TriageResponse if found, 404 if not found.
     * Requirements: 3.4, 3.5, 3.6
     */
    @GetMapping("/{id}/triage")
    public ResponseEntity<TriageResponse> getAppointmentTriage(@PathVariable String id) {
        Triage triage = getAppointmentTriageUseCase.getAppointmentTriage(id)
                .orElseThrow(() -> new TriageNotFoundException("No triage found for this appointment"));
        
        return ResponseEntity.ok(mapTriageToResponse(triage));
    }
    
    private AppointmentResponse mapToResponse(Appointment appointment) {
        // Fetch patient data to include name and DPI
        String patientName = null;
        String patientDpi = null;
        try {
            PatientDTO patient = (PatientDTO) patientServiceClient.getPatient(appointment.getPatientId());
            patientName = patient.getFullName();
            patientDpi = patient.getDpi();
        } catch (Exception e) {
            log.warn("Could not fetch patient data for appointment {}: {}", appointment.getId(), e.getMessage());
        }
        
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getPatientId(),
            patientName,
            patientDpi,
            appointment.getDoctorId(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getStatus().name(),
            appointment.getNotes(),
            appointment.getCreatedAt(),
            appointment.getQrCodeBase64()  // Include QR code if generated
        );
    }
    
    private TriageResponse mapTriageToResponse(Triage triage) {
        return new TriageResponse(
            triage.getId(),
            triage.getPatientId(),
            triage.getPriorityLevel().name(),
            triage.getPriorityLevel().getDescription(),
            triage.getMaxWaitTimeMinutes(),
            triage.getPerformedAt()
        );
    }
}
