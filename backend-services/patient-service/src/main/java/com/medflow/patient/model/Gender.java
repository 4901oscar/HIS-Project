package com.medflow.patient.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    M("MALE"),
    F("FEMALE");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        String upperValue = value.trim().toUpperCase();
        
        // Accept both short and long forms
        switch (upperValue) {
            case "M":
            case "MALE":
                return M;
            case "F":
            case "FEMALE":
                return F;
            default:
                throw new IllegalArgumentException("Invalid gender value: " + value + ". Accepted values: M, MALE, F, FEMALE");
        }
    }
}
