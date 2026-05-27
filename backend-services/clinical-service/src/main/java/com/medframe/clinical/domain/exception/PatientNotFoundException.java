package com.medframe.clinical.domain.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(String message) { super(message); }
}
