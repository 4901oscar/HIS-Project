package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.exception.AppointmentNotFoundException;
import com.medframe.clinical.domain.exception.SlotNotAvailableException;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import com.medframe.clinical.domain.port.out.PatientServiceClient;

import java.time.LocalDate;
import java.time.LocalTime;
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

    static final LocalTime START_TIME = LocalTime.of(8, 0);
    static final LocalTime END_TIME = LocalTime.of(17, 0);
    static final int SLOT_DURATION_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotCache slotCache;
    private final PatientServiceClient patientServiceClient;

    public AppointmentManager(AppointmentRepository appointmentRepository,
                               AppointmentSlotCache slotCache,
                               PatientServiceClient patientServiceClient) {
        this.appointmentRepository = appointmentRepository;
        this.slotCache = slotCache;
        this.patientServiceClient = patientServiceClient;
    }

    /**
     * Generate all possible daily slots (pure function — no external dependencies).
     * Returns 18 slots: 08:00, 08:30, 09:00, ..., 16:30
     */
    public List<LocalTime> generateDailySlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = START_TIME;
        while (current.isBefore(END_TIME)) {
            slots.add(current);
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }
        return slots;
    }

    public List<LocalTime> findAvailableSlots(String doctorId, LocalDate date) {
        List<LocalTime> allSlots = generateDailySlots();
        Set<LocalTime> occupied = slotCache.getOccupiedSlots(doctorId, date);
        allSlots.removeIf(occupied::contains);
        return allSlots;
    }

    public Appointment createAppointment(String patientId, String doctorId,
                                          LocalDate date, LocalTime time,
                                          String notes, String createdBy) {

        // 1. Validate patient exists (HTTP call)
        patientServiceClient.validatePatientExists(patientId);

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
}
