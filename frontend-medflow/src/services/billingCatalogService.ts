import api from '../api';

export interface ServiceItemResponse {
  id: string;
  code: string;
  name: string;
  description: string;
  category: string;
  price: number;
  active: boolean;
}

export interface ServiceItemRequest {
  code: string;
  name: string;
  description?: string;
  category: string;
  price: number;
}

export const SERVICE_CATEGORIES: Record<string, string> = {
  CONSULTATION: 'Consulta',
  LABORATORY: 'Laboratorio',
  MEDICATION: 'Medicamento',
  PROCEDURE: 'Procedimiento',
  OTHER: 'Otro',
};

export const getServiceItems = async (category?: string): Promise<ServiceItemResponse[]> => {
  const r = await api.get<ServiceItemResponse[]>('/api/billing/services', { params: category ? { category } : {} });
  return r.data;
};

export const createServiceItem = async (data: ServiceItemRequest): Promise<ServiceItemResponse> => {
  const r = await api.post<ServiceItemResponse>('/api/billing/services', data);
  return r.data;
};

export const updateServiceItem = async (id: string, data: ServiceItemRequest): Promise<ServiceItemResponse> => {
  const r = await api.put<ServiceItemResponse>(`/api/billing/services/${id}`, data);
  return r.data;
};

export const toggleServiceItem = async (id: string): Promise<ServiceItemResponse> => {
  const r = await api.patch<ServiceItemResponse>(`/api/billing/services/${id}/toggle`);
  return r.data;
};
