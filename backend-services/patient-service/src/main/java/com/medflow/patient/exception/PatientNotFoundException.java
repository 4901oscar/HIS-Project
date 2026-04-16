package com.medflow.patient.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String id) {
        super("Paciente no encontrado: " + id);
    }
}
