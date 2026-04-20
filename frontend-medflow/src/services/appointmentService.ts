import api from '../api';

export interface AppointmentRequest {
  patientId: string;
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  notes?: string;
}

export interface AppointmentResponse {
  id: string;
  patientId: string;
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  createdAt: string;
}

export const createAppointment = async (data: AppointmentRequest): Promise<AppointmentResponse> => {
  const response = await api.post<AppointmentResponse>('/api/clinical/appointments', data);
  return response.data;
};

export const activateAppointment = async (id: string): Promise<void> => {
  await api.put(`/api/clinical/appointments/${id}/activate`);
};

export const cancelAppointment = async (id: string): Promise<void> => {
  await api.delete(`/api/clinical/appointments/${id}`);
};

export default { createAppointment, activateAppointment, cancelAppointment };
