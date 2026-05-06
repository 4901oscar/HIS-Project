package com.medframe.clinical.domain.port.out;

public interface PatientServiceClient {
    /** Busca paciente probando authUserId primero y luego patientId (para IDs ambiguos). */
    Object getPatient(String patientId);
    /** Busca paciente directamente por su UUID en patient-service (una sola llamada HTTP). */
    Object getPatientById(String patientId);
    Object getPatientByAuthUserId(String authUserId);
    void validatePatientExists(String patientId);
}
