package com.medframe.clinical.infrastructure.rest.validation;

import com.medframe.clinical.infrastructure.rest.dto.request.CreateAppointmentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Validator for @FutureAppointment annotation.
 * Ensures that the appointment date + time is not in the past.
 * Uses Guatemala timezone (America/Guatemala / GMT-6) for validation.
 */
public class FutureAppointmentValidator implements ConstraintValidator<FutureAppointment, CreateAppointmentRequest> {

    private static final Logger log = LoggerFactory.getLogger(FutureAppointmentValidator.class);
    private static final ZoneId GUATEMALA_ZONE = ZoneId.of("America/Guatemala");

    @Override
    public boolean isValid(CreateAppointmentRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getAppointmentDate() == null || request.getAppointmentTime() == null) {
            return true; // Let @NotNull handle null validation
        }

        LocalDate appointmentDate = request.getAppointmentDate();
        LocalTime appointmentTime = request.getAppointmentTime();
        
        // Get current date and time in Guatemala timezone
        ZonedDateTime nowInGuatemala = ZonedDateTime.now(GUATEMALA_ZONE);
        LocalDate today = nowInGuatemala.toLocalDate();
        LocalTime currentTime = nowInGuatemala.toLocalTime();
        
        log.debug("Validating appointment: date={}, time={}, today={}, currentTime={} (Guatemala timezone)", 
                appointmentDate, appointmentTime, today, currentTime);
        
        // If appointment is in the future (not today), it's valid
        if (appointmentDate.isAfter(today)) {
            log.debug("Appointment is in the future - VALID");
            return true;
        }
        
        // If appointment is in the past, it's invalid
        if (appointmentDate.isBefore(today)) {
            log.warn("Appointment date is in the past - INVALID");
            return false;
        }
        
        // If appointment is today, check that time is in the future (no minimum buffer needed)
        // The frontend already filters slots to be at least 30 minutes ahead when displaying them
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
        LocalDateTime nowDateTime = LocalDateTime.of(today, currentTime);
        
        boolean isValid = appointmentDateTime.isAfter(nowDateTime);
        
        log.debug("Same-day appointment validation: appointmentDateTime={}, nowDateTime={}, isValid={}", 
                appointmentDateTime, nowDateTime, isValid);
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("La cita debe ser en el futuro. Hora actual: %s, Hora de cita: %s", 
                            currentTime, appointmentTime))
                    .addConstraintViolation();
        }
        
        return isValid;
    }
}
