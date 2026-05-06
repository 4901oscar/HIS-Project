package com.medflow.auth.exception;

/**
 * Excepción lanzada cuando los datos médicos proporcionados son inválidos.
 * Ejemplos: fecha de nacimiento en el futuro, género inválido, formato de fecha incorrecto.
 */
public class InvalidMedicalDataException extends RuntimeException {

    public InvalidMedicalDataException(String message) {
        super(message);
    }

    public InvalidMedicalDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
