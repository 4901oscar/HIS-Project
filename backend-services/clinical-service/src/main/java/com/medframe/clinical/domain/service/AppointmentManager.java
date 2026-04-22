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
}
