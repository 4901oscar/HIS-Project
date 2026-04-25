import api from '../api';

export type OrderStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

export interface LabOrderResponse {
  id: string;
  orderCode: string;
  patientId: string;
  doctorId: string;
  testNames: string[];
  status: OrderStatus;
  collectedAt?: string;
  resultPath?: string;
  orderedAt: string;
}

export const getLabOrders = async (status?: OrderStatus): Promise<LabOrderResponse[]> => {
  const params = status ? { status } : {};
  const response = await api.get<LabOrderResponse[]>('/api/lab/orders', { params });
  return response.data;
};

export const getLabOrderById = async (id: string): Promise<LabOrderResponse> => {
  const response = await api.get<LabOrderResponse>(`/api/lab/orders/${id}`);
  return response.data;
};

export const collectSample = async (id: string): Promise<LabOrderResponse> => {
  const response = await api.put<LabOrderResponse>(`/api/lab/orders/${id}/collect`);
  return response.data;
};

export const uploadResult = async (id: string, file: File): Promise<LabOrderResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await api.put<LabOrderResponse>(`/api/lab/orders/${id}/result`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
};

export default { getLabOrders, getLabOrderById, collectSample, uploadResult };
