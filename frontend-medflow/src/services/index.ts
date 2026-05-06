export { default as appointmentService } from './appointmentService';
export { default as authService } from './authService';
export { default as patientService } from './patientService';
export { default as clinicalService } from './clinicalService';
export { default as labService } from './labService';
export { default as pharmacyService } from './pharmacyService';
export { default as manchesterService } from './manchesterService';
export { MOCK_USERS, getMockAuthResponse, validateMockCredentials, printAvailableUsers } from './mockData';

export type { LoginCredentials, AuthUser, AuthResponse, RegisterData, RegisterResponse } from './authService';
export type { CreatePatientRequest, PatientResponse } from './patientService';
export type { Invoice, ChargeItem, PaymentRequest, PaymentResponse } from './billingService';
export type { LabOrderResponse, OrderStatus } from './labService';
export type { PrescriptionResponse as PharmacyPrescriptionResponse, PrescriptionStatus } from './pharmacyService';
export type { ManchesterCatalog, ManchesterMotif, ManchesterDiscriminator } from '../types/triage';
