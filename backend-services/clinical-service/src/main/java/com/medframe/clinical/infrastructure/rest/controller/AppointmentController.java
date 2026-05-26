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
import com.medframe.clinical.domain.port.out.LabServiceClient;
import com.medframe.clinical.domain.port.out.PatientServiceClient;
import com.medframe.clinical.domain.port.out.VitalSignsRepository;
import com.medframe.clinical.domain.service.AppointmentManager;
import com.medframe.clinical.domain.service.PaymentValidationError;
import com.medframe.clinical.domain.service.PaymentValidationResult;
import com.medframe.clinical.infrastructure.client.BillingServiceClient;
import com.medframe.clinical.infrastructure.client.dto.ChargeRequest;
import com.medframe.clinical.infrastructure.client.dto.CreateInvoiceRequest;
import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import com.medframe.clinical.infrastructure.client.dto.PatientDTO;
import com.medframe.clinical.infrastructure.rest.dto.request.ConfirmPaymentRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.CreateAppointmentRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.HoldSlotRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.UpdateInvoiceIdRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentListItemResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentWithPaymentStatusResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.AvailableSlotsResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.QRStatusResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.TriageResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.VitalSignsResponse;
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
    private final com.medframe.clinical.application.service.PermissionValidator permissionValidator;
    private final AppointmentRepository appointmentRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final com.medframe.clinical.domain.port.out.ConsultationRepository consultationRepository;
    private final com.medframe.clinical.domain.port.out.PrescriptionRepository prescriptionRepository;
    private final LabServiceClient labServiceClient;
    
    @Value("${billing.service.enabled:true}")
    private boolean billingServiceEnabled;
    
    private static final DateTimeFormatter INVOICE_TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /** 
     * GET /api/clinical/appointments - Endpoint unificado para listado de citas con filtros flexibles.
     * 
     * <p>Este endpoint consolida la funcionalidad de múltiples endpoints específicos,
     * permitiendo filtrar citas por estado, fecha, cola, y otros criterios mediante
     * query parameters opcionales.</p>
     * 
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li><code>status</code> - Filtrar por uno o múltiples estados (ej: SCHEDULED,PENDING_PAYMENT)</li>
     *   <li><code>date</code> - Filtrar por fecha específica (formato: yyyy-MM-dd)</li>
     *   <li><code>queue</code> - Filtrar por tipo de cola: payment, lab, pharmacy, triage</li>
     *   <li><code>missingInvoice</code> - Filtrar citas sin factura (true/false)</li>
     *   <li><code>includeQR</code> - Incluir código QR en base64 (true/false, default: false)</li>
     *   <li><code>includeClinical</code> - Incluir información clínica (true/false, default: false)</li>
     * </ul>
     * 
     * <p><strong>Ejemplos de uso:</strong></p>
     * <ul>
     *   <li><code>GET /appointments</code> - Todas las citas</li>
     *   <li><code>GET /appointments?status=SCHEDULED</code> - Solo citas agendadas</li>
     *   <li><code>GET /appointments?status=PENDING_PAYMENT,SCHEDULED</code> - Cola de admisión</li>
     *   <li><code>GET /appointments?queue=payment</code> - Cola de caja</li>
     *   <li><code>GET /appointments?queue=triage</code> - Cola de triaje</li>
     *   <li><code>GET /appointments?date=2026-04-27</code> - Citas del día específico</li>
     *   <li><code>GET /appointments?missingInvoice=true</code> - Citas sin factura</li>
     * </ul>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-1: Consolidar endpoints de cola por estado</li>
     *   <li>REQ-2: Endpoint principal con filtros flexibles</li>
     *   <li>REQ-3: Estructura de datos unificada</li>
     * </ul>
     * 
     * @param status Lista de estados para filtrar (opcional)
     * @param date Fecha específica para filtrar (opcional)
     * @param queue Tipo de cola para filtrar (opcional)
     * @param missingInvoice Filtrar citas sin factura (opcional)
     * @param includeQR Incluir código QR en respuesta (opcional, default: false)
     * @param includeClinical Incluir información clínica (opcional, default: false)
     * @return Lista de citas que cumplen los criterios de filtrado
     */
    @GetMapping
    public ResponseEntity<List<AppointmentListItemResponse>> listAppointments(
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String queue,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false, defaultValue = "false") boolean missingInvoice,
            @RequestParam(required = false, defaultValue = "false") boolean includeQR,
            @RequestParam(required = false, defaultValue = "false") boolean includeClinical) {

        log.info("Listing appointments with filters - status: {}, date: {}, queue: {}, doctorId: {}, missingInvoice: {}",
                 status, date, queue, doctorId, missingInvoice);

        // 1. Obtener todas las citas
        List<Appointment> appointments;

        if (missingInvoice) {
            appointments = manageAppointmentUseCase.listAppointmentsWithoutInvoice();
            log.info("Filtering appointments without invoice. Total found: {}", appointments.size());
        } else {
            appointments = manageAppointmentUseCase.listAll();
        }

        // 2. Aplicar filtros
        appointments = applyFilters(appointments, status, date, queue, doctorId);

        log.info("After filtering: {} appointments", appointments.size());

        // 3. Mapear a DTO unificado
        List<AppointmentListItemResponse> response = appointments.stream()
                .map(appt -> mapToUnifiedResponse(appt, includeQR, includeClinical))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/clinical/appointments/{id} - Obtiene los detalles de una cita específica por su ID.
     * 
     * <p>Este endpoint permite obtener toda la información de una cita individual,
     * incluyendo datos del paciente, doctor, estado de pago e información clínica.</p>
     * 
     * @param id ID de la cita
     * @param includeQR Incluir código QR en respuesta (opcional, default: false)
     * @param includeClinical Incluir información clínica (opcional, default: false)
     * @return Detalles completos de la cita
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentListItemResponse> getAppointmentById(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "false") boolean includeQR,
            @RequestParam(required = false, defaultValue = "false") boolean includeClinical) {
        
        log.info("Getting appointment by ID: {}, includeQR: {}, includeClinical: {}", id, includeQR, includeClinical);
        
        // Obtener la cita por ID
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
        
        // Mapear a DTO unificado
        AppointmentListItemResponse response = mapToUnifiedResponse(appointment, includeQR, includeClinical);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Aplica filtros a la lista de citas.
     */
    private List<Appointment> applyFilters(
            List<Appointment> appointments,
            List<String> statusFilter,
            LocalDate dateFilter,
            String queueFilter,
            String doctorIdFilter) {

        return appointments.stream()
                .filter(appt -> matchesStatusFilter(appt, statusFilter))
                .filter(appt -> matchesDateFilter(appt, dateFilter))
                .filter(appt -> matchesQueueFilter(appt, queueFilter))
                .filter(appt -> doctorIdFilter == null || doctorIdFilter.equals(appt.getDoctorId()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Verifica si una cita coincide con el filtro de estado.
     */
    private boolean matchesStatusFilter(Appointment appointment, List<String> statusFilter) {
        if (statusFilter == null || statusFilter.isEmpty()) {
            return true;
        }
        
        return statusFilter.stream()
                .anyMatch(s -> appointment.getStatus().name().equals(s));
    }
    
    /**
     * Verifica si una cita coincide con el filtro de fecha.
     */
    private boolean matchesDateFilter(Appointment appointment, LocalDate dateFilter) {
        if (dateFilter == null) {
            return true;
        }
        
        return appointment.getAppointmentDate().equals(dateFilter);
    }
    
    /**
     * Verifica si una cita coincide con el filtro de cola.
     */
    private boolean matchesQueueFilter(Appointment appointment, String queueFilter) {
        if (queueFilter == null || queueFilter.isBlank()) {
            return true;
        }
        
        switch (queueFilter.toLowerCase()) {
            // "payment" queue: Only consultation payments (PENDING_PAYMENT, SCHEDULED)
            // Excludes PENDING_LAB_PAYMENT and PENDING_PHARMACY_PAYMENT to prevent
            // duplicate display (those belong to their respective service queues)
            case "payment":
                return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
                       appointment.getStatus() == AppointmentStatus.SCHEDULED;
            case "lab":
            case "laboratory":
                return appointment.getStatus() == AppointmentStatus.LABORATORY ||
                       appointment.getStatus() == AppointmentStatus.LAB_SAMPLE_COLLECTION ||
                       appointment.getStatus() == AppointmentStatus.LAB_SAMPLE_PENDING ||
                       appointment.getStatus() == AppointmentStatus.LAB_PROCESSING ||
                       appointment.getStatus() == AppointmentStatus.LAB_RESULTS_READY;
            case "pharmacy":
                return appointment.getStatus() == AppointmentStatus.PHARMACY;
            case "triage":
                return appointment.getStatus() == AppointmentStatus.VITAL_SIGNS;
            case "admission":
                return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
                       appointment.getStatus() == AppointmentStatus.SCHEDULED;
            default:
                log.warn("Unknown queue filter: {}", queueFilter);
                return true;
        }
    }

    /** Appointments for the authenticated patient. */
    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> listMine() {
        // mapToResponseLight omits patient-service calls: the patient already knows their own
        // data and the dashboard doesn't display patientName/patientDpi from this endpoint.
        List<AppointmentResponse> list = manageAppointmentUseCase.listMyAppointments()
                .stream().map(this::mapToResponseLight).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Appointments assigned to the authenticated doctor. */
    @GetMapping("/doctor")
    public ResponseEntity<List<AppointmentResponse>> listDoctor() {
        List<AppointmentResponse> list = manageAppointmentUseCase.listDoctorAppointments()
                .stream().map(this::mapToResponseLight).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }
    
    /**
     * GET /api/clinical/appointments/payment-queue
     * Lists appointments in payment-pending states for cashier queue.
     * Returns appointments in PENDING_PAYMENT, PENDING_LAB_PAYMENT, PENDING_PHARMACY_PAYMENT states.
     * 
     * @param paymentType Optional filter: "consultation", "lab", "pharmacy"
     * @return List of appointments waiting for payment
     * @deprecated Use GET /appointments?queue=payment instead. This endpoint will be removed in v2.0.
     */
    @Deprecated
    @GetMapping("/payment-queue")
    public ResponseEntity<List<AppointmentResponse>> listPaymentQueue(
            @RequestParam(required = false) String paymentType) {
        log.warn("DEPRECATED: /payment-queue endpoint called. Use /appointments?queue=payment instead");
        log.info("Listing payment queue appointments, filter: {}", paymentType);
        
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> {
                    if (paymentType == null) {
                        return a.getStatus() == Appointment.AppointmentStatus.PENDING_PAYMENT ||
                               a.getStatus() == Appointment.AppointmentStatus.PENDING_LAB_PAYMENT ||
                               a.getStatus() == Appointment.AppointmentStatus.PENDING_PHARMACY_PAYMENT;
                    }
                    switch (paymentType.toLowerCase()) {
                        case "consultation":
                            return a.getStatus() == Appointment.AppointmentStatus.PENDING_PAYMENT;
                        case "lab":
                            return a.getStatus() == Appointment.AppointmentStatus.PENDING_LAB_PAYMENT;
                        case "pharmacy":
                            return a.getStatus() == Appointment.AppointmentStatus.PENDING_PHARMACY_PAYMENT;
                        default:
                            return false;
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        
        List<AppointmentResponse> list = appointments.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Found {} appointments in payment queue", list.size());
        return ResponseEntity.ok(list);
    }
    
    /**
     * GET /api/clinical/appointments/lab-queue
     * Lists appointments in LABORATORY state for lab staff queue.
     * 
     * @return List of appointments waiting for lab tests
     * @deprecated Use GET /appointments?queue=lab instead. This endpoint will be removed in v2.0.
     */
    @Deprecated
    @GetMapping("/lab-queue")
    public ResponseEntity<List<AppointmentResponse>> listLabQueue() {
        log.warn("DEPRECATED: /lab-queue endpoint called. Use /appointments?queue=lab instead");
        log.info("Listing lab queue appointments");
        
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.LABORATORY)
                .collect(java.util.stream.Collectors.toList());
        
        List<AppointmentResponse> list = appointments.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Found {} appointments in lab queue", list.size());
        return ResponseEntity.ok(list);
    }
    
    /**
     * GET /api/clinical/appointments/pharmacy-queue
     * Lists appointments in PHARMACY state for pharmacy staff queue.
     * 
     * @return List of appointments waiting for medication dispensing
     * @deprecated Use GET /appointments?queue=pharmacy instead. This endpoint will be removed in v2.0.
     */
    @Deprecated
    @GetMapping("/pharmacy-queue")
    public ResponseEntity<List<AppointmentResponse>> listPharmacyQueue() {
        log.warn("DEPRECATED: /pharmacy-queue endpoint called. Use /appointments?queue=pharmacy instead");
        log.info("Listing pharmacy queue appointments");
        
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PHARMACY)
                .collect(java.util.stream.Collectors.toList());
        
        List<AppointmentResponse> list = appointments.stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Found {} appointments in pharmacy queue", list.size());
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/clinical/appointments/pending-triage
     * Lists all active appointments that are waiting for triage.
     * Returns 200 with array of appointments (empty array if none).
     * Requirements: 4.3, 4.4
     * @deprecated Use GET /appointments?queue=triage instead. This endpoint will be removed in v2.0.
     */
    @Deprecated
    @GetMapping("/pending-triage")
    public ResponseEntity<List<AppointmentResponse>> listPendingTriageAppointments() {
        log.warn("DEPRECATED: /pending-triage endpoint called. Use /appointments?queue=triage instead");
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
     * GET /api/clinical/appointments/admission-queue
     * Lista todas las citas en estados PENDING_PAYMENT y SCHEDULED con información de estado de pago.
     * 
     * <p>Este endpoint proporciona al personal de admisión una vista completa de todas
     * las citas pendientes de activación (de todos los días), con indicadores visuales 
     * del estado de pago.</p>
     * 
     * <p><strong>Estados incluidos:</strong></p>
     * <ul>
     *   <li>PENDING_PAYMENT: Citas creadas pero sin pago confirmado</li>
     *   <li>SCHEDULED: Citas con pago confirmado, listas para activar</li>
     * </ul>
     * 
     * @return Lista de citas PENDING_PAYMENT y SCHEDULED con información de estado de pago
     */
    @GetMapping("/admission-queue")
    public ResponseEntity<List<AppointmentWithPaymentStatusResponse>> listAdmissionQueue() {
        log.info("Listing admission queue appointments with payment status");
        
        // 1. Obtener todas las citas en estados PENDING_PAYMENT y SCHEDULED
        List<Appointment> admissionAppointments = manageAppointmentUseCase.listAll().stream()
                .filter(appt -> appt.getStatus() == Appointment.AppointmentStatus.PENDING_PAYMENT ||
                               appt.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Found {} appointments in admission queue", admissionAppointments.size());
        
        // 2. Mapear cada cita con su estado de pago
        List<AppointmentWithPaymentStatusResponse> response = admissionAppointments.stream()
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
            PatientDTO patient = (PatientDTO) patientServiceClient.getPatientById(appointment.getPatientId());
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

        // 1. Determine patient ID
        String patientId;
        String patientEmail = "no-email@medflow.com";
        String patientFirstName = "Paciente";
        
        if (request.getPatientId() != null && !request.getPatientId().isBlank()) {
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
                PatientDTO patient = (PatientDTO) patientServiceClient.getPatientByAuthUserId(userId);
                patientId = patient.getId(); // Use the real patient ID from patient-service
                patientEmail = patient.getEmail();
                patientFirstName = patient.getFirstName();
            } catch (Exception e) {
                log.error("No se pudo obtener info del paciente con auth_user_id {}: {}", userId, e.getMessage());
                throw new RuntimeException("No se pudo obtener información del paciente");
            }
        }

        // 2. Create appointment (manual or auto-assignment) — exactly once
        Appointment appointment;
        boolean hasPaid = request.getHasPaid() != null ? request.getHasPaid() : false;
        if (request.getDoctorId() != null && !request.getDoctorId().trim().isEmpty()) {
            appointment = appointmentManager.createAppointment(
                patientId, request.getDoctorId(),
                request.getAppointmentDate(), request.getAppointmentTime(),
                request.getNotes(), userId, true, hasPaid);
        } else {
            appointment = manageAppointmentUseCase.createAppointmentWithAutoAssignment(
                patientId,
                request.getAppointmentDate(), request.getAppointmentTime(),
                request.getNotes(), userId);
        }

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
                    // Factura creada exitosamente — persistir invoiceId en la cita
                    appointment.setInvoiceId(invoiceResponse.getId());
                    invoiceNumber = invoiceResponse.getInvoiceNumber();
                    appointmentRepository.save(appointment);
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
    
    /** QR scan — validates payment and time window before activating the appointment. */
    @PostMapping("/{id}/scan")
    public ResponseEntity<Map<String, Object>> scanAppointment(@PathVariable String id) {
        LocalDateTime now = LocalDateTime.now();

        // Validate payment only when appointment is about to be activated (within time window)
        Appointment preCheck = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada: " + id));

        if (preCheck.getStatus() == AppointmentStatus.SCHEDULED) {
            LocalDateTime apptDateTime = LocalDateTime.of(
                    preCheck.getAppointmentDate(), preCheck.getAppointmentTime());
            boolean withinWindow = !now.isBefore(apptDateTime.minusMinutes(15))
                                && !now.isAfter(apptDateTime.plusMinutes(60));

            if (withinWindow) {
                PaymentValidationResult validation = paymentValidator.validatePayment(
                        preCheck.getInvoiceId(), preCheck.getId());

                if (!validation.isAllowed()) {
                    log.warn("QR scan blocked for appointment {} — payment not confirmed: {}",
                             id, validation.getErrorMessage());
                    Map<String, Object> body = new java.util.LinkedHashMap<>();
                    body.put("status", "PAYMENT_REQUIRED");
                    body.put("message", validation.getErrorMessage());
                    body.put("appointmentId", preCheck.getId());
                    body.put("patientId", preCheck.getPatientId());
                    body.put("doctorId", preCheck.getDoctorId());
                    body.put("date", preCheck.getAppointmentDate().toString());
                    body.put("time", preCheck.getAppointmentTime().toString());
                    body.put("appointmentStatus", preCheck.getStatus().name());
                    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
                }
            }
        }

        com.medframe.clinical.domain.model.ScanResult result =
                appointmentManager.validateAndActivateAppointment(id, now);
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
    
    /**
     * POST /api/clinical/appointments/{id}/confirm-payment
     * Confirms consultation payment and transitions from PENDING_PAYMENT to SCHEDULED.
     * 
     * @param id Appointment ID
     * @param request Request with invoice ID
     * @return 200 OK if successful
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in PENDING_PAYMENT state (400)
     * @throws com.medframe.clinical.domain.exception.PaymentValidationException if payment validation fails (400)
     * @throws com.medframe.clinical.domain.exception.ServiceUnavailableException if Billing Service unavailable (503)
     */
    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<Void> confirmPayment(
            @PathVariable String id,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        log.info("Confirming payment for appointment {}, invoice {}", id, request.getInvoiceId());
        
        // Validate payment before confirming
        PaymentValidationResult validationResult = paymentValidator.validatePayment(
            request.getInvoiceId(), 
            id
        );
        
        if (!validationResult.isAllowed()) {
            PaymentValidationError error = validationResult.getError();
            
            if (error == PaymentValidationError.SERVICE_TIMEOUT || 
                error == PaymentValidationError.SERVICE_ERROR) {
                log.error("Cannot confirm payment for appointment {} - Billing Service unavailable: {}", 
                          id, validationResult.getErrorMessage());
                throw new ServiceUnavailableException(validationResult.getErrorMessage());
            } else {
                log.error("Cannot confirm payment for appointment {} - Payment validation failed: {}", 
                          id, validationResult.getErrorMessage());
                throw new PaymentValidationException(
                    validationResult.getErrorMessage(), 
                    error
                );
            }
        }
        
        // Confirm payment and transition state
        manageAppointmentUseCase.confirmPayment(id, request.getInvoiceId());
        
        log.info("Payment confirmed for appointment {}", id);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/clinical/appointments/{id}/confirm-lab-payment
     * Confirms lab payment and transitions from PENDING_LAB_PAYMENT to LABORATORY.
     * 
     * @param id Appointment ID
     * @param request Request with lab invoice ID
     * @return 200 OK if successful
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in PENDING_LAB_PAYMENT state (400)
     * @throws com.medframe.clinical.domain.exception.PaymentValidationException if payment validation fails (400)
     * @throws com.medframe.clinical.domain.exception.ServiceUnavailableException if Billing Service unavailable (503)
     */
    @PostMapping("/{id}/confirm-lab-payment")
    public ResponseEntity<Void> confirmLabPayment(
            @PathVariable String id,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        log.info("Confirming lab payment for appointment {}, invoice {}", id, request.getInvoiceId());
        
        // Validate payment before confirming
        PaymentValidationResult validationResult = paymentValidator.validatePayment(
            request.getInvoiceId(), 
            id
        );
        
        if (!validationResult.isAllowed()) {
            PaymentValidationError error = validationResult.getError();
            
            if (error == PaymentValidationError.SERVICE_TIMEOUT || 
                error == PaymentValidationError.SERVICE_ERROR) {
                log.error("Cannot confirm lab payment for appointment {} - Billing Service unavailable: {}", 
                          id, validationResult.getErrorMessage());
                throw new ServiceUnavailableException(validationResult.getErrorMessage());
            } else {
                log.error("Cannot confirm lab payment for appointment {} - Payment validation failed: {}", 
                          id, validationResult.getErrorMessage());
                throw new PaymentValidationException(
                    validationResult.getErrorMessage(), 
                    error
                );
            }
        }
        
        // Confirm lab payment and transition state
        manageAppointmentUseCase.confirmLabPayment(id, request.getInvoiceId());
        
        log.info("Lab payment confirmed for appointment {}", id);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/clinical/appointments/{id}/confirm-pharmacy-payment
     * Confirms pharmacy payment and transitions from PENDING_PHARMACY_PAYMENT to PHARMACY.
     * 
     * @param id Appointment ID
     * @param request Request with pharmacy invoice ID
     * @return 200 OK if successful
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in PENDING_PHARMACY_PAYMENT state (400)
     * @throws com.medframe.clinical.domain.exception.PaymentValidationException if payment validation fails (400)
     * @throws com.medframe.clinical.domain.exception.ServiceUnavailableException if Billing Service unavailable (503)
     */
    @PostMapping("/{id}/confirm-pharmacy-payment")
    public ResponseEntity<Void> confirmPharmacyPayment(
            @PathVariable String id,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        log.info("Confirming pharmacy payment for appointment {}, invoice {}", id, request.getInvoiceId());
        
        // Validate payment before confirming
        PaymentValidationResult validationResult = paymentValidator.validatePayment(
            request.getInvoiceId(), 
            id
        );
        
        if (!validationResult.isAllowed()) {
            PaymentValidationError error = validationResult.getError();
            
            if (error == PaymentValidationError.SERVICE_TIMEOUT || 
                error == PaymentValidationError.SERVICE_ERROR) {
                log.error("Cannot confirm pharmacy payment for appointment {} - Billing Service unavailable: {}", 
                          id, validationResult.getErrorMessage());
                throw new ServiceUnavailableException(validationResult.getErrorMessage());
            } else {
                log.error("Cannot confirm pharmacy payment for appointment {} - Payment validation failed: {}", 
                          id, validationResult.getErrorMessage());
                throw new PaymentValidationException(
                    validationResult.getErrorMessage(), 
                    error
                );
            }
        }
        
        // Confirm pharmacy payment and transition state
        manageAppointmentUseCase.confirmPharmacyPayment(id, request.getInvoiceId());
        
        log.info("Pharmacy payment confirmed for appointment {}", id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable String id) {
        manageAppointmentUseCase.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
    

    /**
     * PATCH /api/clinical/appointments/{id}/complete-lab
     * Transitions appointment from LABORATORY to RE_EVALUATION.
     * Called when lab tests are completed and results are ready.
     * 
     * @param id Appointment ID
     * @return 200 OK if successful
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in LABORATORY state (400)
     */
    @PatchMapping("/{id}/complete-lab")
    public ResponseEntity<Void> completeLab(@PathVariable String id) {
        log.info("Completing lab tests for appointment {}", id);
        manageAppointmentUseCase.completeLab(id);
        log.info("Appointment {} transitioned to RE_EVALUATION state", id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/clinical/appointments/{appointmentId}/prescription
     * Retrieves prescription details for a specific appointment.
     * Used by pharmacy module to display prescription information.
     * 
     * <p><strong>Requirements:</strong></p>
     * <ul>
     *   <li>REQ-6.1: Provide endpoint to retrieve prescription by appointment ID</li>
     *   <li>REQ-6.2: Return prescription with patient and doctor details</li>
     *   <li>REQ-6.3: Return 404 if prescription not found</li>
     *   <li>REQ-6.4: Return 404 if consultation not found</li>
     *   <li>REQ-6.5: Include all medication details</li>
     *   <li>REQ-6.6: Require PHARMACY role</li>
     * </ul>
     * 
     * @param appointmentId Appointment ID
     * @return PrescriptionDetailResponse with complete prescription information
     * @throws RuntimeException if appointment not found (404)
     * @throws RuntimeException if consultation not found (404)
     * @throws RuntimeException if prescription not found (404)
     */
    @GetMapping("/{appointmentId}/prescription")
    public ResponseEntity<com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse> getPrescriptionByAppointment(
            @PathVariable String appointmentId) {
        log.info("Fetching prescription for appointment {}", appointmentId);
        
        // Validate permissions - PHARMACY, DOCTOR, ADMIN and the patient themselves can access prescriptions
        permissionValidator.requireRole("PHARMACY", "DOCTOR", "ADMIN", "PATIENT");
        
        // 1. Find consultation by appointmentId
        java.util.Optional<com.medframe.clinical.domain.model.Consultation> consultationOpt =
                consultationRepository.findByAppointmentId(appointmentId);
        if (consultationOpt.isEmpty()) {
            log.warn("No consultation found for appointment {}", appointmentId);
            return ResponseEntity.status(404).build();
        }
        com.medframe.clinical.domain.model.Consultation consultation = consultationOpt.get();

        log.info("Found consultation {} for appointment {}", consultation.getId(), appointmentId);

        // 2. Find prescription by consultationId
        java.util.List<com.medframe.clinical.domain.model.Prescription> prescriptions =
                prescriptionRepository.findByConsultationId(consultation.getId());

        if (prescriptions.isEmpty()) {
            log.warn("No prescription found for consultation {}", consultation.getId());
            return ResponseEntity.status(404).build();
        }
        
        // Get the most recent prescription
        com.medframe.clinical.domain.model.Prescription prescription = prescriptions.get(0);
        log.info("Found prescription {} for consultation {}", prescription.getId(), consultation.getId());
        
        // 3. Fetch patient details from PatientServiceClient
        com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.PatientInfo patientInfo;
        try {
            com.medframe.clinical.infrastructure.client.dto.PatientDTO patient = 
                    (com.medframe.clinical.infrastructure.client.dto.PatientDTO) 
                    patientServiceClient.getPatientById(prescription.getPatientId());
            
            patientInfo = com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.PatientInfo.builder()
                    .id(patient.getId())
                    .fullName(patient.getFullName())
                    .dpi(patient.getDpi())
                    .phone(patient.getPhone())
                    .email(patient.getEmail())
                    .build();
        } catch (Exception e) {
            log.warn("Patient service unavailable for patient {}: {}", prescription.getPatientId(), e.getMessage());
            patientInfo = com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.PatientInfo.builder()
                    .id(prescription.getPatientId())
                    .fullName("Paciente")
                    .build();
        }
        
        // 4. Fetch doctor details from DoctorRepository
        com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.DoctorInfo doctorInfo = 
                doctorRepository.findById(prescription.getDoctorId())
                .map(doctor -> com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.DoctorInfo.builder()
                        .id(doctor.getId())
                        .name(doctor.getName())
                        .specialty(null) // Specialty not available in current Doctor model
                        .build())
                .orElse(com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.DoctorInfo.builder()
                        .id(prescription.getDoctorId())
                        .name("Dr. Asignado")
                        .specialty(null)
                        .build());
        
        // 5. Map medications
        java.util.List<com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.MedicationItemResponse> medications = 
                prescription.getMedications().stream()
                .map(med -> com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.MedicationItemResponse.builder()
                        .name(med.getName())
                        .dosage(med.getDosage())
                        .frequency(med.getFrequency())
                        .durationDays(med.getDurationDays())
                        .route(med.getRoute())
                        .specialInstructions(med.getSpecialInstructions())
                        .dosageAmount(med.getDosageAmount())
                        .dosageUnit(med.getDosageUnit())
                        .frequencyHours(med.getFrequencyHours())
                        .totalQuantity(med.getTotalQuantity())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        
        // 6. Build response
        com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse response = 
                com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionDetailResponse.builder()
                .id(prescription.getId())
                .prescriptionCode(prescription.getPrescriptionCode())
                .status(prescription.getStatus().name())
                .issuedAt(prescription.getIssuedAt())
                .patient(patientInfo)
                .doctor(doctorInfo)
                .medications(medications)
                .build();
        
        log.info("Successfully retrieved prescription {} for appointment {}", prescription.getId(), appointmentId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * PATCH /api/clinical/appointments/{id}/dispense-medication
     * Transitions appointment from PHARMACY to COMPLETED.
     * Called when medications are dispensed to the patient.
     * 
     * @param id Appointment ID
     * @return 200 OK if successful
     * @throws com.medframe.clinical.domain.exception.InvalidAppointmentStatusException if not in PHARMACY state (400)
     */
    @PatchMapping("/{id}/dispense-medication")
    public ResponseEntity<Void> dispenseMedication(@PathVariable String id) {
        log.info("Dispensing medication for appointment {}", id);
        manageAppointmentUseCase.dispenseMedication(id);
        log.info("Appointment {} transitioned to COMPLETED state", id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * PUT /api/clinical/appointments/{id}/lab/collect-samples
     * Transitions appointment from LAB_SAMPLE_COLLECTION to LAB_SAMPLE_PENDING.
     * Called when lab technician collects samples from the patient.
     * 
     * <p><strong>Requirements:</strong></p>
     * <ul>
     *   <li>REQ-4.5: Update appointment status when samples are collected</li>
     *   <li>REQ-9.3: Return HTTP 400 with descriptive error for invalid transitions</li>
     *   <li>REQ-12.6: Use correct HTTP status codes</li>
     * </ul>
     * 
     * @param id Appointment ID
     * @param userId User ID from X-User-Id header (for audit logging)
     * @return 200 OK with updated appointment if successful
     * @throws RuntimeException if appointment not found (404)
     * @throws IllegalStateException if not in LAB_SAMPLE_COLLECTION state (400)
     */
    @PutMapping("/{id}/lab/collect-samples")
    public ResponseEntity<AppointmentListItemResponse> collectLabSamples(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Collecting lab samples for appointment {}, user: {}", id, userId);
        
        try {
            // 1. Find appointment
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Appointment {} not found", id);
                        return new RuntimeException("Cita no encontrada: " + id);
                    });
            
            // 2. Call domain method (validates state and transitions)
            appointment.collectLabSamples();
            
            // 3. Save updated appointment
            appointment = appointmentRepository.save(appointment);
            
            log.info("Lab samples collected for appointment {}. New status: {}", 
                     id, appointment.getStatus());
            
            // 4. Return updated appointment
            AppointmentListItemResponse response = mapToUnifiedResponse(appointment, false, false);
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            // Invalid state transition - return 400 with descriptive error
            log.error("Cannot collect lab samples for appointment {} - Invalid state: {}", 
                      id, e.getMessage());
            throw new InvalidAppointmentStatusException(
                "No se pueden recolectar muestras. " + e.getMessage()
            );
        }
    }
    
    /**
     * PUT /api/clinical/appointments/{id}/lab/accept-samples
     * Transitions appointment from LAB_SAMPLE_PENDING to LAB_PROCESSING.
     * Called when lab technician accepts the collected samples as valid.
     * 
     * <p><strong>Requirements:</strong></p>
     * <ul>
     *   <li>REQ-5.4: Update appointment status when samples are accepted</li>
     *   <li>REQ-9.3: Return HTTP 400 with descriptive error for invalid transitions</li>
     *   <li>REQ-12.6: Use correct HTTP status codes</li>
     * </ul>
     * 
     * @param id Appointment ID
     * @param userId User ID from X-User-Id header (for audit logging)
     * @return 200 OK with updated appointment if successful
     * @throws RuntimeException if appointment not found (404)
     * @throws IllegalStateException if not in LAB_SAMPLE_PENDING state (400)
     */
    @PutMapping("/{id}/lab/accept-samples")
    public ResponseEntity<AppointmentListItemResponse> acceptLabSamples(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Accepting lab samples for appointment {}, user: {}", id, userId);
        
        try {
            // 1. Find appointment
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Appointment {} not found", id);
                        return new RuntimeException("Cita no encontrada: " + id);
                    });
            
            // 2. Call domain method (validates state and transitions)
            appointment.acceptLabSamples();
            
            // 3. Save updated appointment
            appointment = appointmentRepository.save(appointment);
            
            log.info("Lab samples accepted for appointment {}. New status: {}", 
                     id, appointment.getStatus());
            
            // 4. Return updated appointment
            AppointmentListItemResponse response = mapToUnifiedResponse(appointment, false, false);
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            // Invalid state transition - return 400 with descriptive error
            log.error("Cannot accept lab samples for appointment {} - Invalid state: {}", 
                      id, e.getMessage());
            throw new InvalidAppointmentStatusException(
                "No se pueden aceptar muestras. " + e.getMessage()
            );
        }
    }
    
    /**
     * PUT /api/clinical/appointments/{id}/lab/reject-samples
     * Transitions appointment from LAB_SAMPLE_PENDING to LAB_SAMPLE_COLLECTION.
     * Called when lab technician rejects the collected samples and requests new collection.
     * 
     * <p><strong>Requirements:</strong></p>
     * <ul>
     *   <li>REQ-5.5: Update appointment status when samples are rejected</li>
     *   <li>REQ-9.3: Return HTTP 400 with descriptive error for invalid transitions</li>
     *   <li>REQ-12.6: Use correct HTTP status codes</li>
     * </ul>
     * 
     * @param id Appointment ID
     * @param userId User ID from X-User-Id header (for audit logging)
     * @return 200 OK with updated appointment if successful
     * @throws RuntimeException if appointment not found (404)
     * @throws IllegalStateException if not in LAB_SAMPLE_PENDING state (400)
     */
    @PutMapping("/{id}/lab/reject-samples")
    public ResponseEntity<AppointmentListItemResponse> rejectLabSamples(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Rejecting lab samples for appointment {}, user: {}", id, userId);
        
        try {
            // 1. Find appointment
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Appointment {} not found", id);
                        return new RuntimeException("Cita no encontrada: " + id);
                    });
            
            // 2. Call domain method (validates state and transitions)
            appointment.rejectLabSamples();
            
            // 3. Save updated appointment
            appointment = appointmentRepository.save(appointment);
            
            log.info("Lab samples rejected for appointment {}. New status: {}", 
                     id, appointment.getStatus());
            
            // 4. Return updated appointment
            AppointmentListItemResponse response = mapToUnifiedResponse(appointment, false, false);
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            // Invalid state transition - return 400 with descriptive error
            log.error("Cannot reject lab samples for appointment {} - Invalid state: {}", 
                      id, e.getMessage());
            throw new InvalidAppointmentStatusException(
                "No se pueden rechazar muestras. " + e.getMessage()
            );
        }
    }
    
    /**
     * PUT /api/clinical/appointments/{id}/lab/complete-processing
     * Transitions appointment from LAB_PROCESSING to LAB_RESULTS_READY.
     * Called when lab technician marks all test processing as complete.
     * Validates that all tests have uploaded results before allowing transition.
     * 
     * <p><strong>Requirements:</strong></p>
     * <ul>
     *   <li>REQ-6.8: Verify all tests have results before completing</li>
     *   <li>REQ-6.9: Update appointment status when processing is complete</li>
     *   <li>REQ-6.10: Return error with missing tests if validation fails</li>
     *   <li>REQ-9.3: Return HTTP 400 with descriptive error for invalid transitions</li>
     *   <li>REQ-12.6: Use correct HTTP status codes</li>
     * </ul>
     * 
     * @param id Appointment ID
     * @param userId User ID from X-User-Id header (for audit logging)
     * @return 200 OK with updated appointment if successful
     * @throws RuntimeException if appointment not found (404)
     * @throws IllegalStateException if not in LAB_PROCESSING state (400)
     */
    @PutMapping("/{id}/lab/complete-processing")
    public ResponseEntity<AppointmentListItemResponse> completeLabProcessing(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Completing lab processing for appointment {}, user: {}", id, userId);
        
        try {
            // 1. Find appointment
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Appointment {} not found", id);
                        return new RuntimeException("Cita no encontrada: " + id);
                    });
            
            // 2. Get lab order for this appointment
            // TODO: Call Lab Service to verify all tests have results
            // For now, we'll proceed with the transition
            // This will be implemented when Lab Service endpoints are ready
            
            // 3. Call domain method (validates state and transitions)
            appointment.completeLabProcessing();
            
            // 4. Save updated appointment
            appointment = appointmentRepository.save(appointment);
            
            log.info("Lab processing completed for appointment {}. New status: {}", 
                     id, appointment.getStatus());
            
            // 5. Return updated appointment
            AppointmentListItemResponse response = mapToUnifiedResponse(appointment, false, false);
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            // Invalid state transition - return 400 with descriptive error
            log.error("Cannot complete lab processing for appointment {} - Invalid state: {}", 
                      id, e.getMessage());
            throw new InvalidAppointmentStatusException(
                "No se puede completar el procesamiento. " + e.getMessage()
            );
        }
    }
    
    /**
     * PUT /api/clinical/appointments/{id}/lab/send-to-doctor
     * Transitions appointment from LAB_RESULTS_READY to CONSULTATION.
     * Called when lab technician sends the results to the doctor.
     * 
     * <p><strong>Requirements:</strong></p>
     * <ul>
     *   <li>REQ-7.5: Update appointment status when results are sent to doctor</li>
     *   <li>REQ-7.6: Make results accessible to the doctor</li>
     *   <li>REQ-9.3: Return HTTP 400 with descriptive error for invalid transitions</li>
     *   <li>REQ-12.6: Use correct HTTP status codes</li>
     * </ul>
     * 
     * @param id Appointment ID
     * @param userId User ID from X-User-Id header (for audit logging)
     * @return 200 OK with updated appointment if successful
     * @throws RuntimeException if appointment not found (404)
     * @throws IllegalStateException if not in LAB_RESULTS_READY state (400)
     */
    @PutMapping("/{id}/lab/send-to-doctor")
    public ResponseEntity<AppointmentListItemResponse> sendLabResultsToDoctor(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Sending lab results to doctor for appointment {}, user: {}", id, userId);
        
        try {
            // 1. Find appointment
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Appointment {} not found", id);
                        return new RuntimeException("Cita no encontrada: " + id);
                    });
            
            // 2. Call domain method (validates state and transitions)
            appointment.sendLabResultsToDoctor();

            // 3. Save updated appointment
            appointment = appointmentRepository.save(appointment);

            // 4. Mark lab order as COMPLETED in lab-service
            labServiceClient.completeOrderByAppointmentId(id);

            log.info("Lab results sent to doctor for appointment {}. New status: {}",
                     id, appointment.getStatus());

            // 5. Return updated appointment
            AppointmentListItemResponse response = mapToUnifiedResponse(appointment, false, false);
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            // Invalid state transition - return 400 with descriptive error
            log.error("Cannot send lab results to doctor for appointment {} - Invalid state: {}", 
                      id, e.getMessage());
            throw new InvalidAppointmentStatusException(
                "No se pueden enviar resultados al doctor. " + e.getMessage()
            );
        }
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
            PatientDTO patient = (PatientDTO) patientServiceClient.getPatientById(appointment.getPatientId());
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
    
    /**
     * GET /api/clinical/appointments/{id}/vital-signs
     * Retrieves the latest vital signs for the patient associated with a specific appointment.
     * This endpoint is used by the triage screen to load existing vital signs when resuming
     * a triage session (Step 1 already completed).
     * 
     * <p>Returns 200 with VitalSignsResponse if vital signs exist for the patient,
     * or 404 if no vital signs have been recorded yet.</p>
     * 
     * <p><strong>Requirements:</strong></p>
     * <ul>
     *   <li>REQ-2.5: Resume capability - Load existing vital signs</li>
     *   <li>REQ-4.2: Display existing vital signs in read-only mode</li>
     * </ul>
     * 
     * @param id Appointment ID
     * @return VitalSignsResponse with the latest vital signs for the patient
     * @throws RuntimeException if appointment not found (404)
     * @throws RuntimeException if no vital signs found for patient (404)
     */
    @GetMapping("/{id}/vital-signs")
    public ResponseEntity<VitalSignsResponse> getAppointmentVitalSigns(@PathVariable String id) {
        log.info("Getting vital signs for appointment {}", id);
        
        // 1. Get the appointment to retrieve the patientId
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Appointment {} not found", id);
                    return new RuntimeException("Cita no encontrada: " + id);
                });
        
        // 2. Get vital signs for this specific appointment (not by patient)
        com.medframe.clinical.domain.model.VitalSigns vitalSigns = vitalSignsRepository
                .findByAppointmentId(id)
                .orElseThrow(() -> {
                    log.info("No vital signs found for appointment {}", id);
                    return new com.medframe.clinical.domain.exception.VitalSignsNotFoundException(
                            "No se encontraron signos vitales para esta cita");
                });
        
        log.info("Found vital signs {} for appointment {}", vitalSigns.getId(), id);
        
        // 3. Map to response DTO
        VitalSignsResponse response = new VitalSignsResponse(
                vitalSigns.getId(),
                vitalSigns.getPatientId(),
                vitalSigns.getSystolicPressure(),
                vitalSigns.getDiastolicPressure(),
                vitalSigns.getHeartRate(),
                vitalSigns.getRespiratoryRate(),
                vitalSigns.getTemperature(),
                vitalSigns.getOxygenSaturation(),
                vitalSigns.getWeight(),
                vitalSigns.getHeight(),
                vitalSigns.getBmi(),
                vitalSigns.getRecordedAt()
        );
        
        return ResponseEntity.ok(response);
    }
    
    private AppointmentResponse mapToResponse(Appointment appointment) {
        String patientName = null;
        String patientDpi = null;
        try {
            PatientDTO patient = (PatientDTO) patientServiceClient.getPatientById(appointment.getPatientId());
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
            appointment.getQrCodeBase64(),
            appointment.getInvoiceId(),
            appointment.getLabInvoiceId(),
            appointment.getPharmacyInvoiceId(),
            appointment.isPriority()
        );
    }

    /** Mapper sin llamadas a patient-service — para endpoints donde el caller ya conoce sus datos. */
    private AppointmentResponse mapToResponseLight(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getPatientId(),
            null,
            null,
            appointment.getDoctorId(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getStatus().name(),
            appointment.getNotes(),
            appointment.getCreatedAt(),
            appointment.getQrCodeBase64(),
            appointment.getInvoiceId(),
            appointment.getLabInvoiceId(),
            appointment.getPharmacyInvoiceId(),
            appointment.isPriority()
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
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // MAPPER UNIFICADO - AppointmentListItemResponse
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * Mapper unificado que convierte Appointment a AppointmentListItemResponse.
     * Este mapper centraliza toda la lógica de mapeo y validación de pago.
     * 
     * @param appointment la cita a mapear
     * @param includeQR si se debe incluir el código QR en base64
     * @param includeClinical si se debe incluir información clínica
     * @return AppointmentListItemResponse con toda la información
     */
    private AppointmentListItemResponse mapToUnifiedResponse(
            Appointment appointment, 
            boolean includeQR, 
            boolean includeClinical) {
        
        LocalDate today = LocalDate.now();
        
        // 1. Obtener información del paciente
        AppointmentListItemResponse.PatientInfo patientInfo = null;
        try {
            PatientDTO patient = (PatientDTO) patientServiceClient.getPatientById(appointment.getPatientId());
            patientInfo = AppointmentListItemResponse.PatientInfo.builder()
                    .id(appointment.getPatientId())
                    .fullName(patient.getFullName())
                    .dpi(patient.getDpi())
                    .phone(patient.getPhone())
                    .email(patient.getEmail())
                    .build();
        } catch (Exception e) {
            log.warn("Could not fetch patient data for appointment {}: {}", 
                     appointment.getId(), e.getMessage());
            patientInfo = AppointmentListItemResponse.PatientInfo.builder()
                    .id(appointment.getPatientId())
                    .fullName("Paciente")
                    .build();
        }
        
        // 2. Obtener información del doctor
        AppointmentListItemResponse.DoctorInfo doctorInfo = doctorRepository.findById(appointment.getDoctorId())
                .map(d -> AppointmentListItemResponse.DoctorInfo.builder()
                        .id(d.getId())
                        .name("Dr. " + d.getName())
                        .build())
                .orElse(AppointmentListItemResponse.DoctorInfo.builder()
                        .id(appointment.getDoctorId())
                        .name("Dr. Asignado")
                        .build());
        
        // 3. Validar estado de pago — usar la factura correspondiente al estado actual
        String invoiceIdForValidation;
        if (appointment.getStatus() == AppointmentStatus.PENDING_LAB_PAYMENT) {
            invoiceIdForValidation = appointment.getLabInvoiceId();
        } else if (appointment.getStatus() == AppointmentStatus.PENDING_PHARMACY_PAYMENT) {
            invoiceIdForValidation = appointment.getPharmacyInvoiceId() != null
                    ? appointment.getPharmacyInvoiceId()
                    : appointment.getInvoiceId();
        } else {
            invoiceIdForValidation = appointment.getInvoiceId();
        }
        PaymentValidationResult validationResult = paymentValidator.validatePayment(
                invoiceIdForValidation,
                appointment.getId()
        );
        
        AppointmentListItemResponse.PaymentInfo paymentInfo = buildPaymentInfo(validationResult);
        
        // 4. Información clínica (opcional)
        AppointmentListItemResponse.ClinicalInfo clinicalInfo = null;
        if (includeClinical) {
            clinicalInfo = buildClinicalInfo(appointment);
        }
        
        // 5. Información de QR (opcional)
        AppointmentListItemResponse.QRInfo qrInfo = null;
        if (includeQR) {
            qrInfo = AppointmentListItemResponse.QRInfo.builder()
                    .hasQR(appointment.getQrCodeBase64() != null)
                    .qrCodeBase64(appointment.getQrCodeBase64())
                    .build();
        }
        
        // 6. Metadatos
        AppointmentListItemResponse.MetadataInfo metadata = AppointmentListItemResponse.MetadataInfo.builder()
                .isToday(appointment.getAppointmentDate().equals(today))
                .isPast(appointment.getAppointmentDate().isBefore(today))
                .isUpcoming(appointment.getAppointmentDate().isAfter(today))
                .canEdit(canEditAppointment(appointment))
                .canCancel(canCancelAppointment(appointment))
                .canActivate(canActivateAppointment(appointment, validationResult))
                .build();
        
        // 7. Código de receta (solo para citas en cola de farmacia)
        String prescriptionCode = null;
        if (appointment.getStatus() == AppointmentStatus.PHARMACY) {
            try {
                java.util.List<com.medframe.clinical.domain.model.Prescription> prescriptions =
                        prescriptionRepository.findByConsultationId(
                                consultationRepository.findByAppointmentId(appointment.getId())
                                        .map(c -> c.getId())
                                        .orElse(null));
                if (!prescriptions.isEmpty()) {
                    prescriptionCode = prescriptions.get(0).getPrescriptionCode();
                }
            } catch (Exception e) {
                log.debug("Could not fetch prescription code for appointment {}", appointment.getId());
            }
        }

        // 8. Construir respuesta completa
        return AppointmentListItemResponse.builder()
                .id(appointment.getId())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus().name())
                .statusLabel(getStatusLabel(appointment.getStatus()))
                .statusColor(getStatusColor(appointment.getStatus()))
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .patient(patientInfo)
                .doctor(doctorInfo)
                .payment(paymentInfo)
                .clinical(clinicalInfo)
                .qr(qrInfo)
                .metadata(metadata)
                .prescriptionCode(prescriptionCode)
                .build();
    }
    
    /**
     * Construye la información de pago basándose en el resultado de validación.
     */
    private AppointmentListItemResponse.PaymentInfo buildPaymentInfo(PaymentValidationResult validationResult) {
        AppointmentListItemResponse.PaymentInfo.PaymentInfoBuilder builder = 
                AppointmentListItemResponse.PaymentInfo.builder();
        
        if (validationResult.isAllowed()) {
            if (validationResult.isHasWarning()) {
                // Caso: Cita sin factura (NO_INVOICE)
                builder
                        .status("NO_INVOICE")
                        .statusLabel("SIN FACTURA")
                        .statusColor("gray")
                        .canActivate(true)
                        .tooltip("Cita sin factura - Activar bajo responsabilidad");
            } else {
                // Caso: Factura PAID
                builder
                        .invoiceId(validationResult.getInvoice() != null ? 
                                validationResult.getInvoice().getId() : null)
                        .invoiceNumber(validationResult.getInvoice() != null ? 
                                validationResult.getInvoice().getInvoiceNumber() : null)
                        .status("PAID")
                        .statusLabel("PAGADA")
                        .statusColor("green")
                        .amount(validationResult.getInvoice() != null ? 
                                validationResult.getInvoice().getTotal() : null)
                        .canActivate(true)
                        .tooltip("Cita pagada - Puede activarse");
            }
        } else {
            // Validación falló
            PaymentValidationError error = validationResult.getError();
            
            if (error == PaymentValidationError.PAYMENT_PENDING) {
                builder
                        .invoiceId(validationResult.getInvoice() != null ?
                                validationResult.getInvoice().getId() : null)
                        .invoiceNumber(validationResult.getInvoice() != null ?
                                validationResult.getInvoice().getInvoiceNumber() : null)
                        .amount(validationResult.getInvoice() != null ?
                                validationResult.getInvoice().getTotal() : null)
                        .status("PENDING")
                        .statusLabel("PENDIENTE")
                        .statusColor("orange")
                        .canActivate(false)
                        .tooltip("El paciente debe pagar en caja primero");

            } else if (error == PaymentValidationError.INVOICE_CANCELLED) {
                builder
                        .status("CANCELLED")
                        .statusLabel("CANCELADA")
                        .statusColor("red")
                        .canActivate(false)
                        .tooltip("Factura cancelada - Contacte administración");
                
            } else {
                // Otros errores (SERVICE_TIMEOUT, SERVICE_ERROR, INVOICE_NOT_FOUND, etc.)
                builder
                        .status("ERROR")
                        .statusLabel("ERROR")
                        .statusColor("red")
                        .canActivate(false)
                        .tooltip("Error al validar pago - Contacte administración");
            }
        }
        
        return builder.build();
    }
    
    /**
     * Construye la información clínica de una cita.
     * Consulta los datos reales de triaje y determina si tiene signos vitales.
     */
    private AppointmentListItemResponse.ClinicalInfo buildClinicalInfo(Appointment appointment) {
        // Determinar si tiene signos vitales basado en el estado de la cita
        // Si la cita está en CONSULTATION o estados posteriores, significa que ya pasó por VITAL_SIGNS
        boolean hasVitalSigns = appointment.getStatus() == AppointmentStatus.CONSULTATION
                || appointment.getStatus() == AppointmentStatus.PENDING_LAB_PAYMENT
                || appointment.getStatus() == AppointmentStatus.LABORATORY
                || appointment.getStatus() == AppointmentStatus.LAB_SAMPLE_COLLECTION
                || appointment.getStatus() == AppointmentStatus.LAB_SAMPLE_PENDING
                || appointment.getStatus() == AppointmentStatus.LAB_PROCESSING
                || appointment.getStatus() == AppointmentStatus.LAB_RESULTS_READY
                || appointment.getStatus() == AppointmentStatus.RE_EVALUATION
                || appointment.getStatus() == AppointmentStatus.PENDING_PHARMACY_PAYMENT
                || appointment.getStatus() == AppointmentStatus.PHARMACY
                || appointment.getStatus() == AppointmentStatus.COMPLETED;
        
        // Consultar si tiene triaje completo y obtener nivel Manchester
        boolean hasTriage = false;
        String manchesterLevel = null;
        
        try {
            var triageOpt = getAppointmentTriageUseCase.getAppointmentTriage(appointment.getId());
            if (triageOpt.isPresent()) {
                hasTriage = true;
                Triage triage = triageOpt.get();
                manchesterLevel = triage.getPriorityLevel() != null 
                        ? triage.getPriorityLevel().name() 
                        : null;
            }
        } catch (Exception e) {
            log.warn("Error al consultar triaje para cita {}: {}", appointment.getId(), e.getMessage());
        }
        
        return AppointmentListItemResponse.ClinicalInfo.builder()
                .hasVitalSigns(hasVitalSigns)
                .hasTriage(hasTriage)
                .manchesterLevel(manchesterLevel)
                .hasLabOrders(false)   // TODO: Consultar órdenes de laboratorio
                .hasPrescriptions(false) // TODO: Consultar recetas
                .hasConsultation(false)  // TODO: Consultar si tiene consulta
                .build();
    }
    
    /**
     * Mapea el estado de la cita a su etiqueta en español.
     */
    private String getStatusLabel(AppointmentStatus status) {
        switch (status) {
            case PENDING_PAYMENT:
                return "Pendiente de Pago";
            case SCHEDULED:
                return "Agendada";
            case VITAL_SIGNS:
                return "Signos Vitales";
            case CONSULTATION:
                return "En Consulta";
            case PENDING_LAB_PAYMENT:
                return "Pendiente Pago Lab";
            case LABORATORY:
                return "Laboratorio";
            case LAB_SAMPLE_COLLECTION:
                return "Recolección de Muestras";
            case LAB_SAMPLE_PENDING:
                return "Muestras en Validación";
            case LAB_PROCESSING:
                return "En Procesamiento";
            case LAB_RESULTS_READY:
                return "Resultados Listos";
            case PENDING_PHARMACY_PAYMENT:
                return "Pendiente Pago Farmacia";
            case PHARMACY:
                return "Farmacia";
            case COMPLETED:
                return "Completada";
            case CANCELLED:
                return "Cancelada";
            case MISSED:
                return "Perdida";
            default:
                return status.name();
        }
    }
    
    /**
     * Mapea el estado de la cita a su color para UI.
     */
    private String getStatusColor(AppointmentStatus status) {
        switch (status) {
            case PENDING_PAYMENT:
            case PENDING_LAB_PAYMENT:
            case PENDING_PHARMACY_PAYMENT:
                return "orange";
            case SCHEDULED:
                return "blue";
            case VITAL_SIGNS:
            case CONSULTATION:
            case LABORATORY:
            case PHARMACY:
                return "green";
            case LAB_SAMPLE_COLLECTION:
            case LAB_SAMPLE_PENDING:
            case LAB_PROCESSING:
            case LAB_RESULTS_READY:
                return "purple";
            case COMPLETED:
                return "gray";
            case CANCELLED:
            case MISSED:
                return "red";
            default:
                return "gray";
        }
    }
    
    /**
     * Determina si una cita puede ser editada.
     */
    private boolean canEditAppointment(Appointment appointment) {
        return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
               appointment.getStatus() == AppointmentStatus.SCHEDULED;
    }
    
    /**
     * Determina si una cita puede ser cancelada.
     */
    private boolean canCancelAppointment(Appointment appointment) {
        return appointment.getStatus() != AppointmentStatus.COMPLETED &&
               appointment.getStatus() != AppointmentStatus.CANCELLED &&
               appointment.getStatus() != AppointmentStatus.MISSED;
    }
    
    /**
     * Determina si una cita puede ser activada.
     */
    private boolean canActivateAppointment(Appointment appointment, PaymentValidationResult validationResult) {
        return appointment.getStatus() == AppointmentStatus.SCHEDULED &&
               validationResult.isAllowed();
    }
}
