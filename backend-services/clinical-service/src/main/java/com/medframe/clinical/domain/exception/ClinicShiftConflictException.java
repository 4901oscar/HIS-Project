package com.medframe.clinical.domain.exception;

public class ClinicShiftConflictException extends RuntimeException {
    public ClinicShiftConflictException(String message) {
        super(message);
    }
}
