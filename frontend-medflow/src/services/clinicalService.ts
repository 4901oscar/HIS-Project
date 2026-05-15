import api from '../api';

// ─── Appointments ────────────────────────────────────────────────────────────

export interface AppointmentResponse {
  id: string;
  patientId: string;
  patientName?: string;  // NEW: Patient full name from backend
  patientDpi?: string;   // NEW: Patient DPI from backend
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  notes?: string;
  createdAt: string;
  qrCodeBase64?: string;
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

/**
 * Get list of pending triage appointments (VITAL_SIGNS appointments without triage)
 * @returns Array of pending triage appointments
 * @deprecated Use listAppointments({ queue: 'triage' }) from appointmentService instead
 */
export const getPendingTriageAppointments = async (): Promise<AppointmentResponse[]> => {
  const response = await api.get<AppointmentResponse[]>('/api/clinical/appointments', {
    params: { queue: 'triage' }
  });
  return response.data;
};

/**
 * Get triage for a specific appointment
 * @param appointmentId - The appointment ID
 * @returns Triage response
 * @throws Error if no triage found (404)
 */
export const getAppointmentTriage = async (appointmentId: string): Promise<TriageResponse> => {
  const response = await api.get<TriageResponse>(`/api/clinical/appointments/${appointmentId}/triage`);
  return response.data;
};

// ─── Vital Signs ─────────────────────────────────────────────────────────────

export interface VitalSignsRequest {
  appointmentId: string;
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

/**
 * Get existing vital signs for a specific appointment
 * @param appointmentId - The appointment ID
 * @returns Vital signs response
 * @throws Error if no vital signs found (404)
 */
export const getVitalSignsByAppointment = async (appointmentId: string): Promise<VitalSignsResponse> => {
  const response = await api.get<VitalSignsResponse>(`/api/clinical/appointments/${appointmentId}/vital-signs`);
  return response.data;
};

// ─── Triage ───────────────────────────────────────────────────────────────────

export interface TriageResponse {
  id: string;
  patientId: string;
  priorityLevel: string;
  priorityDescription: string;
  maxWaitTimeMinutes: number;
  performedAt: string;
}

export const performTriage = async (data: {
  appointmentId: string;
  patientId: string;
  motifId: string;
  discriminatorIds: string[];
}): Promise<TriageResponse> => {
  const response = await api.post<TriageResponse>('/api/clinical/triage', data);
  return response.data;
};

// ─── Consultations ────────────────────────────────────────────────────────────

export interface ServiceCharge {
  name: string;
  price: number;
}

export interface ConsultationRequest {
  patientId: string;
  appointmentId?: string;
  chiefComplaint: string;
  symptoms: string;
  primaryDiagnosis: string;
  secondaryDiagnoses?: string[];
  medicalNotes?: string;
  treatmentPlan?: string;
  hasLabOrders: boolean;
  hasPrescription: boolean;
  labCharges?: ServiceCharge[];
  pharmacyCharges?: ServiceCharge[];
  followUpDate?: string;
  followUpTime?: string;
}

export interface ConsultationResponse {
  id: string;
  patientId: string;
  doctorId: string;
  chiefComplaint: string;
  symptoms?: string;
  primaryDiagnosis: string;
  secondaryDiagnoses: string[];
  medicalNotes?: string;
  treatmentPlan?: string;
  consultationDate: string;
}

export const registerConsultation = async (data: ConsultationRequest): Promise<ConsultationResponse> => {
  const response = await api.post<ConsultationResponse>('/api/clinical/consultations', data);
  return response.data;
};

export const getConsultationByAppointment = async (appointmentId: string): Promise<ConsultationResponse | null> => {
  try {
    const response = await api.get<ConsultationResponse>(`/api/clinical/consultations/by-appointment/${appointmentId}`);
    return response.data;
  } catch {
    return null;
  }
};

// ─── Prescriptions ────────────────────────────────────────────────────────────

export interface MedicationItem {
  name: string;
  dosage: string;
  frequency: string;
  durationDays: number;
  route: string;
  specialInstructions?: string;
  // Campos adicionales para farmacia interna (cálculo automático)
  dosageAmount?: number;        // Cantidad numérica por toma (ej: 1, 2, 0.5)
  dosageUnit?: string;           // Unidad (pastilla, ml, cucharada)
  frequencyHours?: number;       // Horas entre tomas (ej: 8, 12, 24)
  totalQuantity?: number;        // Cantidad total calculada automáticamente
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

export interface PrescriptionDetailResponse {
  id: string;
  prescriptionCode: string;
  status: 'PENDING' | 'DISPENSED' | 'CANCELLED';
  issuedAt: string; // ISO 8601 datetime
  
  // Patient information
  patient: {
    id: string;
    fullName: string;
    dpi?: string;
    phone?: string;
    email?: string;
  };
  
  // Doctor information
  doctor: {
    id: string;
    name: string;
    specialty?: string;
  };
  
  // Medications
  medications: MedicationItem[];
}

export const generatePrescription = async (data: {
  consultationId: string;
  patientId: string;
  medications: MedicationItem[];
}): Promise<PrescriptionResponse> => {
  const response = await api.post<PrescriptionResponse>('/api/clinical/prescriptions', data);
  return response.data;
};

export const getPrescriptionByAppointment = async (appointmentId: string): Promise<PrescriptionDetailResponse> => {
  const response = await api.get<PrescriptionDetailResponse>(`/api/clinical/appointments/${appointmentId}/prescription`);
  return response.data;
};

export const dispenseMedication = async (appointmentId: string): Promise<void> => {
  await api.patch(`/api/clinical/appointments/${appointmentId}/dispense-medication`);
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
  appointmentId: string;
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
  getPendingTriageAppointments,
  getAppointmentTriage,
  recordVitalSigns,
  getVitalSignsByAppointment,
  performTriage,
  registerConsultation,
  generatePrescription,
  getPrescriptionByAppointment,
  dispenseMedication,
  generateLabOrder,
  getMedicalHistory,
};
