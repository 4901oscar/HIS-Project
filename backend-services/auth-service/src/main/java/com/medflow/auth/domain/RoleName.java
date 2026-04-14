package com.medflow.auth.domain;

/**
 * Enum representing the 8 roles in the MedFlow HIS system
 */
public enum RoleName {
    ADMIN,          // Súper Usuario
    ADMISSION,      // Personal de Admisión
    VITAL_SIGNS,    // Personal de Signos Vitales
    DOCTOR,         // Médico
    LABORATORY,     // Personal de Laboratorio
    PHARMACY,       // Farmacéutico
    CASHIER,        // Cajero
    PATIENT         // Paciente
}
