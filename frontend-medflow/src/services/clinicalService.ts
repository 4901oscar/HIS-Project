import api from '../api';

// ─── Appointments ────────────────────────────────────────────────────────────

export interface AppointmentResponse {
  id: string;
  patientId: string;
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  createdAt: string;
}

export const createAppointment = async (data: {
  patientId: string;
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  notes?: string;
}): Promise<AppointmentResponse> => {
  const response = await api.post<AppointmentResponse>('/api/clinical/appointments', data);
  return response.data;
};

export const activateAppointment = async (id: string): Promise<void> => {
  await api.put(`/api/clinical/appointments/${id}/activate`);
};

export const cancelAppointment = async (id: string): Promise<void> => {
  await api.delete(`/api/clinical/appointments/${id}`);
};

// ─── Vital Signs ─────────────────────────────────────────────────────────────

export interface VitalSignsRequest {
  patientId: string;
  systolicPressure: number;
  diastolicPressure: number;
  heartRate: number;
  respiratoryRate: number;
  temperature: number;
  oxygenSaturation: number;
  weight?: number;
  height?: number;
}

export interface VitalSignsResponse {
  id: string;
  patientId: string;
  systolicPressure: number;
  diastolicPressure: number;
  heartRate: number;
  respiratoryRate: number;
  temperature: number;
  oxygenSaturation: number;
  weight?: number;
  height?: number;
  bmi?: number;
  recordedAt: string;
}

export const recordVitalSigns = async (data: VitalSignsRequest): Promise<VitalSignsResponse> => {
  const response = await api.post<VitalSignsResponse>('/api/clinical/vital-signs', data);
  return response.data;
};

// ─── Triage ───────────────────────────────────────────────────────────────────

export interface TriageResponse {
  id: string;
  patientId: string;
  priorityLevel: string;
  description: string;
  maxWaitTimeMinutes: number;
  performedAt: string;
}

export const performTriage = async (data: {
  patientId: string;
  motifId: string;
  discriminatorIds: string[];
}): Promise<TriageResponse> => {
  const response = await api.post<TriageResponse>('/api/clinical/triage', data);
  return response.data;
};

// ─── Consultations ────────────────────────────────────────────────────────────

export interface ConsultationRequest {
  patientId: string;
  appointmentId?: string;
  chiefComplaint: string;
  symptoms: string[];
  primaryDiagnosis: string;
  secondaryDiagnoses?: string[];
  medicalNotes?: string;
  treatmentPlan?: string;
}

export interface ConsultationResponse {
  id: string;
  patientId: string;
  doctorId: string;
  chiefComplaint: string;
  primaryDiagnosis: string;
  secondaryDiagnoses: string[];
  consultationDate: string;
}

export const registerConsultation = async (data: ConsultationRequest): Promise<ConsultationResponse> => {
  const response = await api.post<ConsultationResponse>('/api/clinical/consultations', data);
  return response.data;
};

// ─── Prescriptions ────────────────────────────────────────────────────────────

export interface MedicationItem {
  name: string;
  dosage: string;
  frequency: string;
  durationDays: number;
  route: string;
  specialInstructions?: string;
}

export interface PrescriptionResponse {
  id: string;
  prescriptionCode: string;
  patientId: string;
  doctorId: string;
  medications: MedicationItem[];
  status: string;
  issuedAt: string;
}

export const generatePrescription = async (data: {
  consultationId: string;
  patientId: string;
  medications: MedicationItem[];
}): Promise<PrescriptionResponse> => {
  const response = await api.post<PrescriptionResponse>('/api/clinical/prescriptions', data);
  return response.data;
};

// ─── Lab Orders ───────────────────────────────────────────────────────────────

export interface LabOrderResponse {
  id: string;
  orderCode: string;
  patientId: string;
  doctorId: string;
  testNames: string[];
  status: string;
  orderedAt: string;
}

export const generateLabOrder = async (data: {
  consultationId: string;
  patientId: string;
  testNames: string[];
}): Promise<LabOrderResponse> => {
  const response = await api.post<LabOrderResponse>('/api/clinical/lab-orders', data);
  return response.data;
};

// ─── Medical History ──────────────────────────────────────────────────────────

export interface MedicalHistoryResponse {
  patient: unknown;
  consultations: ConsultationResponse[];
  vitalSigns: VitalSignsResponse[];
  prescriptions: PrescriptionResponse[];
  labOrders: LabOrderResponse[];
}

export const getMedicalHistory = async (patientId: string): Promise<MedicalHistoryResponse> => {
  const response = await api.get<MedicalHistoryResponse>(`/api/clinical/history/${patientId}`);
  return response.data;
};

export default {
  createAppointment,
  activateAppointment,
  cancelAppointment,
  recordVitalSigns,
  performTriage,
  registerConsultation,
  generatePrescription,
  generateLabOrder,
  getMedicalHistory,
};
