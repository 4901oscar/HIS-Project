package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.exception.AppointmentNotFoundException;
import com.medframe.clinical.domain.exception.SlotNotAvailableException;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.AppointmentQRData;
import com.medframe.clinical.domain.model.ScanResult;
import com.medframe.clinical.domain.port.out.AppointmentEmailSender;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import com.medframe.clinical.domain.port.out.PatientServiceClient;
import com.medframe.clinical.domain.port.out.QRCodeGenerator;
import com.medframe.clinical.infrastructure.client.dto.AppointmentEmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * AppointmentManager - Business logic for appointment slot management.
 * Pure domain service with no Spring annotations.
 * 
 * Slots: 08:00 - 16:30, every 30 minutes = 18 slots per day.
 */
public class AppointmentManager {

    private static final Logger log = LoggerFactory.getLogger(AppointmentManager.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    static final LocalTime START_TIME = LocalTime.of(8, 0);
    static final LocalTime END_TIME = LocalTime.of(17, 0);
    static final int SLOT_DURATION_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotCache slotCache;
    private final PatientServiceClient patientServiceClient;
    private final QRCodeGenerator qrGenerator;
    private final AppointmentEmailSender emailSender;

    public AppointmentManager(AppointmentRepository appointmentRepository,
                               AppointmentSlotCache slotCache,
                               PatientServiceClient patientServiceClient,
                               QRCodeGenerator qrGenerator,
                               AppointmentEmailSender emailSender) {
        this.appointmentRepository = appointmentRepository;
        this.slotCache = slotCache;
        this.patientServiceClient = patientServiceClient;
        this.qrGenerator = qrGenerator;
        this.emailSender = emailSender;
    }

    public List<LocalTime> generateDailySlots() {
        return generateSlots(START_TIME, END_TIME);
    }

    private List<LocalTime> generateSlots(LocalTime start, LocalTime end) {
        List<LocalTime> slots = new ArrayList<>();
        int startMin = start.getHour() * 60 + start.getMinute();
        int endMin   = end.getHour()   * 60 + end.getMinute();
        if (endMin == 0) endMin = 24 * 60;
        if (endMin <= startMin) endMin += 24 * 60;
        for (int m = startMin; m < endMin; m += SLOT_DURATION_MINUTES) {
            int actual = m % (24 * 60);
            slots.add(LocalTime.of(actual / 60, actual % 60));
        }
        return slots;
    }

    public List<LocalTime> findAvailableSlots(String doctorId, LocalDate date) {
        return findAvailableSlots(doctorId, date, START_TIME, END_TIME);
    }

    public List<LocalTime> findAvailableSlots(String doctorId, LocalDate date,
                                               LocalTime shiftStart, LocalTime shiftEnd) {
        List<LocalTime> allSlots = generateSlots(shiftStart, shiftEnd);
        Set<LocalTime> occupied = slotCache.getOccupiedSlots(doctorId, date);
        allSlots.removeIf(occupied::contains);
        return allSlots;
    }

    public Appointment createAppointment(String patientId, String doctorId,
                                          LocalDate date, LocalTime time,
                                          String notes, String createdBy) {
        return createAppointment(patientId, doctorId, date, time, notes, createdBy, false);
    }

    public Appointment createAppointment(String patientId, String doctorId,
                                          LocalDate date, LocalTime time,
                                          String notes, String createdBy,
                                          boolean skipPatientValidation) {

        // 1. Validate patient exists (HTTP call) — skipped when patient books for themselves (JWT proves identity)
        if (!skipPatientValidation) {
            patientServiceClient.validatePatientExists(patientId);
        }

        // 2. Reserve slot in Redis atomically
        boolean reserved = slotCache.reserveSlot(doctorId, date, time);
        if (!reserved) {
            throw new SlotNotAvailableException(
                    "El horario " + time + " del " + date +
                    " ya no está disponible. Por favor seleccione otro horario.");
        }

        try {
            // 3. Create appointment entity
            Appointment appointment = new Appointment();
            appointment.setPatientId(patientId);
            appointment.setDoctorId(doctorId);
            appointment.setAppointmentDate(date);
            appointment.setAppointmentTime(time);
            appointment.setNotes(notes);
            appointment.setCreatedBy(createdBy);

            // 4. Persist to database
            return appointmentRepository.save(appointment);

        } catch (Exception e) {
            // Rollback: release slot in Redis if DB save fails
            slotCache.releaseSlot(doctorId, date, time);
            throw e;
        }
    }

    public void cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Cita no encontrada con ID: " + appointmentId));

        // Transition state (throws if invalid)
        appointment.cancel();
        appointmentRepository.save(appointment);

        // Release slot in Redis
        slotCache.releaseSlot(
                appointment.getDoctorId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );
    }

    public void activateAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Cita no encontrada con ID: " + appointmentId));

        appointment.activate();
        appointmentRepository.save(appointment);
    }

    /**
     * Creates an appointment with QR code generation and email notification.
     * QR generation and email sending failures are handled gracefully and do not block appointment creation.
     * 
     * @param patientId Patient identifier
     * @param doctorId Doctor identifier
     * @param date Appointment date
     * @param time Appointment time
     * @param notes Optional notes
     * @param createdBy User who created the appointment
     * @param skipPatientValidation Whether to skip patient existence validation
     * @param patientEmail Patient's email address for confirmation
     * @param patientFirstName Patient's first name
     * @param doctorName Doctor's full name
     * @param invoiceNumber Invoice/billing number
     * @return Appointment with qrCodeBase64 field populated (if generation succeeded)
     */
    public Appointment createAppointmentWithNotification(
            String patientId, String doctorId, LocalDate date, LocalTime time,
            String notes, String createdBy, boolean skipPatientValidation,
            String patientEmail, String patientFirstName, String doctorName, String invoiceNumber) {

        // 1. Create appointment using existing logic
        Appointment appointment = createAppointment(
                patientId, doctorId, date, time, notes, createdBy, skipPatientValidation);

        // 2. Generate QR code (graceful failure)
        try {
            AppointmentQRData qrData = AppointmentQRData.fromAppointment(appointment);
            String qrCodeBase64 = qrGenerator.generateQRCode(qrData);
            if (qrCodeBase64 != null) {
                appointment.setQrCodeBase64(qrCodeBase64);
                log.info("QR code generated successfully for appointment {}", appointment.getId());
            } else {
                log.warn("QR code generation returned null for appointment {}", appointment.getId());
            }
        } catch (Exception e) {
            log.error("QR generation failed for appointment {}: {}", 
                    appointment.getId(), e.getMessage(), e);
            // Continue without QR - appointment creation should not fail
        }

        // 3. Calculate time window for QR validity
        LocalTime validFrom = time.minusMinutes(15);
        LocalTime validUntil = time.plusMinutes(60);
        String validFromTime = validFrom.format(TIME_FORMATTER);
        String validUntilTime = validUntil.format(TIME_FORMATTER);

        // 4. Send confirmation email asynchronously (graceful failure)
        try {
            AppointmentEmailRequest emailRequest = AppointmentEmailRequest.builder()
                    .toEmail(patientEmail)
                    .firstName(patientFirstName)
                    .appointmentDate(date.toString())
                    .appointmentTime(time.format(TIME_FORMATTER))
                    .doctorName(doctorName)
                    .invoiceNumber(invoiceNumber)
                    .qrCodeBase64(appointment.getQrCodeBase64())
                    .notes(notes)
                    .validFromTime(validFromTime)
                    .validUntilTime(validUntilTime)
                    .build();

            emailSender.sendAppointmentConfirmationEmail(emailRequest);
            log.info("Email confirmation request sent for appointment {}", appointment.getId());
        } catch (Exception e) {
            log.error("Failed to send email confirmation for appointment {}: {}", 
                    appointment.getId(), e.getMessage(), e);
            // Continue - email failure should not affect appointment creation
        }

        return appointment;
    }

    /**
     * Generates a QR code for an existing appointment and sends the confirmation email.
     * Does NOT create a new appointment. Safe to call after any creation path.
     */
    public void attachQRAndNotify(Appointment appointment,
                                  String patientEmail, String patientFirstName,
                                  String doctorName, String invoiceNumber) {
        // 1. Generate QR code (graceful failure)
        try {
            AppointmentQRData qrData = AppointmentQRData.fromAppointment(appointment);
            String qrCodeBase64 = qrGenerator.generateQRCode(qrData);
            if (qrCodeBase64 != null) {
                appointment.setQrCodeBase64(qrCodeBase64);
                log.info("QR code generated for appointment {}", appointment.getId());
            }
        } catch (Exception e) {
            log.error("QR generation failed for appointment {}: {}", appointment.getId(), e.getMessage());
        }

        // 2. Calculate validity window
        LocalTime time = appointment.getAppointmentTime();
        String validFromTime  = time.minusMinutes(15).format(TIME_FORMATTER);
        String validUntilTime = time.plusMinutes(60).format(TIME_FORMATTER);

        // 3. Send confirmation email (graceful failure)
        try {
            AppointmentEmailRequest emailRequest = AppointmentEmailRequest.builder()
                    .toEmail(patientEmail)
                    .firstName(patientFirstName)
                    .appointmentDate(appointment.getAppointmentDate().toString())
                    .appointmentTime(time.format(TIME_FORMATTER))
                    .doctorName(doctorName)
                    .invoiceNumber(invoiceNumber)
                    .qrCodeBase64(appointment.getQrCodeBase64())
                    .notes(appointment.getNotes())
                    .validFromTime(validFromTime)
                    .validUntilTime(validUntilTime)
                    .build();
            emailSender.sendAppointmentConfirmationEmail(emailRequest);
            log.info("Email confirmation queued for appointment {}", appointment.getId());
        } catch (Exception e) {
            log.error("Failed to send email for appointment {}: {}", appointment.getId(), e.getMessage());
        }
    }

    /**
     * Validates QR scan time against appointment time window and activates appointment if within window.
     * Time window: 15 minutes before to 60 minutes after appointment time.
     * 
     * State transitions:
     * - EARLY: scanTime < (appointmentTime - 15 min) → No state change
     * - ACTIVE: within window and status=SCHEDULED → SCHEDULED → ACTIVE
     * - ACTIVE: within window and status=ACTIVE → No state change (idempotent)
     * - MISSED: scanTime > (appointmentTime + 60 min) and status=SCHEDULED → SCHEDULED → MISSED
     * - MISSED: scanTime > (appointmentTime + 60 min) and status=MISSED → No state change (idempotent)
     * 
     * @param appointmentId Appointment identifier
     * @param scanTime Time when QR code was scanned
     * @return ScanResult with status (EARLY, ACTIVE, MISSED) and user-friendly message
     * @throws AppointmentNotFoundException if appointment does not exist
     */
    public ScanResult validateAndActivateAppointment(String appointmentId, LocalDateTime scanTime) {
        // 1. Retrieve appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Cita no encontrada con ID: " + appointmentId));

        // 2. Calculate appointment datetime and time window
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );
        LocalDateTime windowStart = appointmentDateTime.minusMinutes(15);
        LocalDateTime windowEnd = appointmentDateTime.plusMinutes(60);

        // 3. Determine scan result based on time window
        if (scanTime.isBefore(windowStart)) {
            // EARLY: Scanned too early
            String validFromTime = windowStart.toLocalTime().format(TIME_FORMATTER);
            String message = String.format("QR disponible a las %s", validFromTime);
            log.info("QR scan EARLY for appointment {}: scan at {}, valid from {}", 
                    appointmentId, scanTime, windowStart);
            return new ScanResult(ScanResult.Status.EARLY, message, appointment, scanTime);

        } else if (scanTime.isAfter(windowEnd)) {
            // MISSED: Scanned too late
            if (appointment.getStatus() == Appointment.AppointmentStatus.SCHEDULED) {
                appointment.markAsMissed();
                appointmentRepository.save(appointment);
                log.info("Appointment {} marked as MISSED: scan at {}, window ended at {}", 
                        appointmentId, scanTime, windowEnd);
            }
            String message = "Cita perdida";
            return new ScanResult(ScanResult.Status.MISSED, message, appointment, scanTime);

        } else {
            // WITHIN WINDOW: Activate if scheduled, or return current status if already active/missed
            if (appointment.getStatus() == Appointment.AppointmentStatus.SCHEDULED) {
                appointment.activate();
                appointmentRepository.save(appointment);
                log.info("Appointment {} activated: scan at {}, window {} to {}", 
                        appointmentId, scanTime, windowStart, windowEnd);
            } else if (appointment.getStatus() == Appointment.AppointmentStatus.ACTIVE) {
                log.info("Appointment {} already ACTIVE (idempotent scan)", appointmentId);
            } else if (appointment.getStatus() == Appointment.AppointmentStatus.MISSED) {
                log.info("Appointment {} already MISSED (idempotent scan)", appointmentId);
                String message = "Cita perdida";
                return new ScanResult(ScanResult.Status.MISSED, message, appointment, scanTime);
            }
            
            String message = "Cita activa";
            return new ScanResult(ScanResult.Status.ACTIVE, message, appointment, scanTime);
        }
    }
}
