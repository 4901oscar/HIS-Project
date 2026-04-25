import api from '../api';

// ── Medicamentos ──────────────────────────────────────────────────────────────

export interface MedicationResponse {
  id: string;
  name: string;
  description: string;
  unit: string;
  currentStock: number;
  minStock: number;
  active: boolean;
  lowStock: boolean;
}

export interface MedicationRequest {
  name: string;
  description?: string;
  unit: string;
  currentStock: number;
  minStock: number;
}

export const getMedications = async (): Promise<MedicationResponse[]> => {
  const response = await api.get<MedicationResponse[]>('/api/pharmacy/medications');
  return response.data;
};

export const createMedication = async (data: MedicationRequest): Promise<MedicationResponse> => {
  const response = await api.post<MedicationResponse>('/api/pharmacy/medications', data);
  return response.data;
};

export const updateStock = async (id: string, newStock: number): Promise<MedicationResponse> => {
  const response = await api.put<MedicationResponse>(`/api/pharmacy/medications/${id}/stock`, { newStock });
  return response.data;
};

export const getLowStockMedications = async (): Promise<MedicationResponse[]> => {
  const response = await api.get<MedicationResponse[]>('/api/pharmacy/medications/low-stock');
  return response.data;
};

// ── Prescripciones ────────────────────────────────────────────────────────────

export type PrescriptionStatus = 'PENDING' | 'DISPENSED' | 'CANCELLED';

export interface PharmacyMedication {
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
  doctorId?: string;
  medications: PharmacyMedication[];
  status: PrescriptionStatus;
  dispensedBy?: string;
  dispensedAt?: string;
  createdAt: string;
}

export const getPrescriptions = async (status?: PrescriptionStatus): Promise<PrescriptionResponse[]> => {
  const params = status ? { status } : {};
  const response = await api.get<PrescriptionResponse[]>('/api/pharmacy/prescriptions', { params });
  return response.data;
};

export const getPrescriptionByCode = async (code: string): Promise<PrescriptionResponse> => {
  const response = await api.get<PrescriptionResponse>(`/api/pharmacy/prescriptions/${code}`);
  return response.data;
};

export const dispensePrescription = async (id: string): Promise<PrescriptionResponse> => {
  const response = await api.put<PrescriptionResponse>(`/api/pharmacy/prescriptions/${id}/dispense`);
  return response.data;
};

export const getPatientPrescriptions = async (patientId: string): Promise<PrescriptionResponse[]> => {
  const response = await api.get<PrescriptionResponse[]>(`/api/pharmacy/prescriptions/patient/${patientId}`);
  return response.data;
};

export default { getPrescriptions, getPrescriptionByCode, dispensePrescription, getPatientPrescriptions };
