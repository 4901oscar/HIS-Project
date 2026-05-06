package com.medframe.clinical.infrastructure.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that an appointment (date + time) is in the future.
 * Must be applied at class level to access both appointmentDate and appointmentTime fields.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureAppointmentValidator.class)
@Documented
public @interface FutureAppointment {
    String message() default "La cita debe ser al menos 30 minutos en el futuro";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
