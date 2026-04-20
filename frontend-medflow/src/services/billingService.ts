import api from '../api';

export type ChargeType = 'CONSULTATION' | 'LABORATORY' | 'MEDICATION' | 'OTHER';
export type PaymentMethod = 'CASH' | 'CARD' | 'TRANSFER';
export type InvoiceStatus = 'PENDING' | 'PAID' | 'CANCELLED';

export interface ChargeRequest {
  type: ChargeType;
  description: string;
  quantity: number;
  unitPrice: number;
}

export interface ChargeResponse {
  id: string;
  type: ChargeType;
  description: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface InvoiceResponse {
  id: string;
  invoiceNumber: string;
  patientId: string;
  charges: ChargeResponse[];
  subtotal: number;
  discount: number;
  total: number;
  status: InvoiceStatus;
  createdAt: string;
  paidAt?: string;
}

export interface PaymentResponse {
  invoiceId: string;
  amountPaid: number;
  change: number;
  method: PaymentMethod;
  paidAt: string;
}

export const createInvoice = async (patientId: string, charges: ChargeRequest[]): Promise<InvoiceResponse> => {
  const response = await api.post<InvoiceResponse>('/api/billing/invoices', { patientId, charges });
  return response.data;
};

export const getInvoices = async (status?: InvoiceStatus): Promise<InvoiceResponse[]> => {
  const params = status ? { status } : {};
  const response = await api.get<InvoiceResponse[]>('/api/billing/invoices', { params });
  return response.data;
};

export const getInvoiceById = async (id: string): Promise<InvoiceResponse> => {
  const response = await api.get<InvoiceResponse>(`/api/billing/invoices/${id}`);
  return response.data;
};

export const processPayment = async (
  id: string,
  amount: number,
  method: PaymentMethod
): Promise<PaymentResponse> => {
  const response = await api.post<PaymentResponse>(`/api/billing/invoices/${id}/pay`, { amount, method });
  return response.data;
};

export const applyDiscount = async (
  id: string,
  discountData: { amount?: number; percentage?: number }
): Promise<InvoiceResponse> => {
  const response = await api.put<InvoiceResponse>(`/api/billing/invoices/${id}/discount`, discountData);
  return response.data;
};

export const cancelInvoice = async (id: string): Promise<InvoiceResponse> => {
  const response = await api.delete<InvoiceResponse>(`/api/billing/invoices/${id}`);
  return response.data;
};

export const getPatientInvoices = async (patientId: string): Promise<InvoiceResponse[]> => {
  const response = await api.get<InvoiceResponse[]>(`/api/billing/invoices/patient/${patientId}`);
  return response.data;
};

export default {
  createInvoice,
  getInvoices,
  getInvoiceById,
  processPayment,
  applyDiscount,
  cancelInvoice,
  getPatientInvoices,
};
