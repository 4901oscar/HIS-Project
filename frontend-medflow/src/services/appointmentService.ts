import api from '../api';

export interface AppointmentRequest {
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
  qrCodeBase64?: string;  // NEW: QR code for appointment confirmation
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

export const listAllAppointments = async (): Promise<AppointmentResponse[]> => {
  const response = await api.get<AppointmentResponse[]>('/api/clinical/appointments');
  return response.data;
};

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
