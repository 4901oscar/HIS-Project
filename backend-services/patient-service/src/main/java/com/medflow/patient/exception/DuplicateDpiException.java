package com.medflow.patient.exception;

public class DuplicateDpiException extends RuntimeException {
    public DuplicateDpiException(String dpi) {
        super("Ya existe un paciente registrado con el DPI: " + dpi);
    }
}
