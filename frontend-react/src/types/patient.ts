/**
 * Tipos e interfaces para el dominio de Pacientes
 * Módulo: Gestión de Pacientes
 */

export interface PatientBiographicData {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  dateOfBirth: string; // ISO 8601: YYYY-MM-DD
  address: string;
}

export interface PatientRegistration extends PatientBiographicData {
  id?: string;
  fingerprint?: string; // Datos codificados de huella dactilar (base64)
  qrCode?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PatientRegistrationRequest {
  biographicData: PatientBiographicData;
  fingerprintData?: string;
}

export interface PatientRegistrationResponse {
  success: boolean;
  message: string;
  data?: PatientRegistration;
  errors?: Record<string, string[]>;
}

export interface ValidationError {
  field: string;
  message: string;
}
