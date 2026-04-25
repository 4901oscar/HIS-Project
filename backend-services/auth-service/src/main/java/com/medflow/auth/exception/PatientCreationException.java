package com.medflow.auth.exception;

/**
 * Excepción lanzada cuando falla la creación de un paciente en patient-service.
 * Esta excepción indica que el usuario fue creado en auth-service pero
 * el registro de paciente en patient-service falló.
 */
public class PatientCreationException extends RuntimeException {

    public PatientCreationException(String message) {
        super(message);
    }

    public PatientCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
