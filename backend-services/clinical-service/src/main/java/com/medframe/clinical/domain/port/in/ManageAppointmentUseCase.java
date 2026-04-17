package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ManageAppointmentUseCase {
    List<LocalTime> findAvailableSlots(String doctorId, LocalDate date);
    Appointment createAppointment(String patientId, String doctorId,
                                  LocalDate date, LocalTime time,
                                  String notes, String createdBy);
    void activateAppointment(String appointmentId);
    void cancelAppointment(String appointmentId);
}
