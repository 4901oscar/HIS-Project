import api from '../api';

export interface CreatePatientRequest {
  dpi: string;
  nit?: string;
  firstName: string;
  secondName?: string;
  firstLastName: string;
  secondLastName?: string;
  birthDate: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  email: string;
  phone: string;
  department?: string;
  municipality?: string;
  zone?: string;
  address?: string;
}

export interface PatientResponse {
  id: string;
  dpi: string;
  nit?: string;
  firstName: string;
  secondName?: string;
  firstLastName: string;
  secondLastName?: string;
  fullName: string;
  birthDate: string;
  gender: string;
  email: string;
  phone: string;
  address?: string;
  department?: string;
  municipality?: string;
}

export const getPatientById = async (id: string): Promise<PatientResponse> => {
  const response = await api.get<PatientResponse>(`/api/patients/${id}`);
  return response.data;
};

export const getPatientByDpi = async (dpi: string): Promise<PatientResponse> => {
  const response = await api.get<PatientResponse>(`/api/patients/dpi/${dpi}`);
  return response.data;
};

export const updatePatient = async (id: string, data: Partial<CreatePatientRequest>): Promise<PatientResponse> => {
  const response = await api.put<PatientResponse>(`/api/patients/${id}`, data);
  return response.data;
};

export default { getPatientById, getPatientByDpi, updatePatient };
