package com.medflow.pharmacy.exception;

/**
 * Excepción lanzada cuando no se encuentra una prescripción.
 */
public class PrescriptionNotFoundException extends RuntimeException {
    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}
