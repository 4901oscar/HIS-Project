package com.medflow.patient.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Ya existe un paciente registrado con el correo: " + email);
    }
}
