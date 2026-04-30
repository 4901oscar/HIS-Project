import api from '../api';

export interface AppointmentRequest {
  patientId?: string;
  doctorId?: string;
  appointmentDate: string;
  appointmentTime: string;
  notes?: string;
  sessionId?: string;
}

export interface AppointmentResponse {
  id: string;
  patientId: string;
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  notes?: string;
  createdAt: string;
  qrCodeBase64?: string;
  invoiceId?: string;
}

export interface AvailableSlotsResponse {
  doctorId: string;
  date: string;
  availableSlots: string[]; // HH:mm:ss strings from backend
}

export const createAppointment = async (data: AppointmentRequest): Promise<AppointmentResponse> => {
  const response = await api.post<AppointmentResponse>('/api/clinical/appointments', data);
  return response.data;
};

export const getAvailableSlots = async (
  doctorId: string,
  date: string
): Promise<string[]> => {
  const response = await api.get<AvailableSlotsResponse>('/api/clinical/appointments/slots', {
    params: { doctorId, date },
  });
  return response.data.availableSlots.map((s) => s.substring(0, 5));
};

/** Available slots across all active doctors for a date — excludes holds by other sessions. */
export const getAvailableSlotsForDate = async (date: string, sessionId?: string): Promise<string[]> => {
  const response = await api.get<string[]>('/api/clinical/appointments/available', {
    params: { date, ...(sessionId ? { sessionId } : {}) },
  });
  return response.data.map((s) => s.substring(0, 5));
};

export const activateAppointment = async (id: string): Promise<void> => {
  await api.put(`/api/clinical/appointments/${id}/activate`);
};

export const cancelAppointment = async (id: string): Promise<void> => {
  await api.delete(`/api/clinical/appointments/${id}`);
};

/** Hold a slot for 10 minutes. Returns true if held, false if taken by another session. */
export const holdSlot = async (sessionId: string, date: string, time: string): Promise<boolean> => {
  try {
    await api.post('/api/clinical/appointments/hold', { sessionId, date, time });
    return true;
  } catch (err: any) {
    if (err.response?.status === 409) return false;
    throw err;
  }
};

/** Release the hold for a session. */
export const releaseHold = async (sessionId: string): Promise<void> => {
  await api.delete('/api/clinical/appointments/hold', { params: { sessionId } });
};

/**
 * Lista citas usando el nuevo endpoint unificado con filtros flexibles.
 * 
 * @param params Parámetros de filtrado opcionales
 * @returns Lista de citas con estructura unificada
 * 
 * @example
 * // Todas las citas
 * listAppointments()
 * 
 * // Cola de admisión
 * listAppointments({ queue: 'admission' })
 * 
 * // Cola de triaje
 * listAppointments({ queue: 'triage' })
 * 
 * // Citas del día
 * listAppointments({ date: '2026-04-27' })
 * 
 * // Citas por estado
 * listAppointments({ status: ['SCHEDULED', 'PENDING_PAYMENT'] })
 */
export const listAppointments = async (params?: ListAppointmentsParams): Promise<AppointmentListItem[]> => {
  const queryParams: Record<string, any> = {};
  
  if (params?.status && params.status.length > 0) {
    queryParams.status = params.status.join(',');
  }
  
  if (params?.date) {
    queryParams.date = params.date;
  }
  
  if (params?.queue) {
    queryParams.queue = params.queue;
  }
  
  if (params?.missingInvoice !== undefined) {
    queryParams.missingInvoice = params.missingInvoice;
  }
  
  if (params?.includeQR !== undefined) {
    queryParams.includeQR = params.includeQR;
  }
  
  if (params?.includeClinical !== undefined) {
    queryParams.includeClinical = params.includeClinical;
  }
  
  const response = await api.get<AppointmentListItem[]>('/api/clinical/appointments', {
    params: queryParams,
  });
  
  return response.data;
};

export const listAllAppointments = async (): Promise<AppointmentResponse[]> => {
  const response = await api.get<AppointmentResponse[]>('/api/clinical/appointments');
  return response.data;
};

// ═══════════════════════════════════════════════════════════════════════════════
// NUEVO DTO UNIFICADO - AppointmentListItem
// ═══════════════════════════════════════════════════════════════════════════════

export interface PatientInfo {
  id: string;
  fullName: string;
  dpi?: string;
  phone?: string;
  email?: string;
}

export interface DoctorInfo {
  id: string;
  name: string;
  specialty?: string;
}

export interface PaymentInfo {
  invoiceId?: string;
  invoiceNumber?: string;
  status: 'PAID' | 'PENDING' | 'CANCELLED' | 'NO_INVOICE' | 'ERROR';
  statusLabel: string;
  statusColor: string;
  amount?: number;
  canActivate: boolean;
  tooltip: string;
}

export interface ClinicalInfo {
  hasVitalSigns: boolean;
  hasTriage: boolean;
  manchesterLevel?: string;
  hasLabOrders: boolean;
  hasPrescriptions: boolean;
  hasConsultation: boolean;
}

export interface QRInfo {
  hasQR: boolean;
  qrCodeBase64?: string;
}

export interface MetadataInfo {
  isToday: boolean;
  isPast: boolean;
  isUpcoming: boolean;
  canEdit: boolean;
  canCancel: boolean;
  canActivate: boolean;
}

export interface AppointmentListItem {
  id: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  statusLabel: string;
  statusColor: string;
  notes?: string;
  createdAt: string;
  patient: PatientInfo;
  doctor: DoctorInfo;
  payment: PaymentInfo;
  clinical?: ClinicalInfo;
  qr?: QRInfo;
  metadata: MetadataInfo;
}

export interface ListAppointmentsParams {
  status?: string[];
  date?: string;
  queue?: 'payment' | 'lab' | 'pharmacy' | 'triage' | 'admission';
  missingInvoice?: boolean;
  includeQR?: boolean;
  includeClinical?: boolean;
}

export const listMyAppointments = async (): Promise<AppointmentResponse[]> => {
  const response = await api.get<AppointmentResponse[]>('/api/clinical/appointments/my');
  return response.data;
};

export const listDoctorAppointments = async (): Promise<AppointmentResponse[]> => {
  const response = await api.get<AppointmentResponse[]>('/api/clinical/appointments/doctor');
  return response.data;
};

export type ScanStatus = 'EARLY' | 'ACTIVE' | 'MISSED';

export interface ScanResult {
  status: ScanStatus;
  message: string;
  appointmentId: string;
  patientId: string;
  doctorId: string;
  date: string;
  time: string;
  appointmentStatus: string;
}

export const scanAppointment = async (id: string): Promise<ScanResult> => {
  const response = await api.post<ScanResult>(`/api/clinical/appointments/${id}/scan`);
  return response.data;
};

export default { createAppointment, getAvailableSlots, activateAppointment, cancelAppointment };
