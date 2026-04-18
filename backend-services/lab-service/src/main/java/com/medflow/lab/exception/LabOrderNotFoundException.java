package com.medflow.lab.exception;

public class LabOrderNotFoundException extends RuntimeException {
    public LabOrderNotFoundException(String message) {
        super(message);
    }
}
