package com.medflow.pharmacy.exception;

/**
 * Excepción lanzada cuando se intenta una operación inválida sobre una prescripción
 * debido a su estado actual.
 */
public class InvalidPrescriptionStatusException extends RuntimeException {
    public InvalidPrescriptionStatusException(String message) {
        super(message);
    }
}
