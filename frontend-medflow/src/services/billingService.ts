import api from '../api';

export interface ChargeItem {
  id?: string;
  type: 'CONSULTATION' | 'LABORATORY' | 'MEDICATION' | 'OTHER';
  description: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Invoice {
  id: string;
  invoiceNumber: string;
  patientId: string;
  charges: ChargeItem[];
  subtotal: number;
  discountAmount: number;
  total: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
  createdBy: string;
  updatedAt?: string;
}

export interface PaymentRequest {
  amount: number;
  method: 'CASH' | 'CARD' | 'TRANSFER';
  nit?: string;
  customerName?: string;
}

export interface PaymentResponse {
  id: string;
  invoiceId: string;
  amount: number;
  change: number;
  method: string;
  paidAt: string;
  receivedBy: string;
}

export interface CreateInvoiceRequest {
  patientId: string;
  charges: {
    type: 'CONSULTATION' | 'LABORATORY' | 'MEDICATION' | 'OTHER';
    description: string;
    quantity: number;
    unitPrice: number;
  }[];
}

/**
 * Crea una nueva factura para un paciente
 */
export const createInvoice = async (request: CreateInvoiceRequest): Promise<Invoice> => {
  const response = await api.post('/api/billing/invoices', request);
  return response.data;
};

/**
 * Obtiene todas las facturas, opcionalmente filtradas por estado
 */
export const getInvoices = async (status?: 'PENDING' | 'PAID' | 'CANCELLED'): Promise<Invoice[]> => {
  const params = status ? { status } : {};
  const response = await api.get('/api/billing/invoices', { params });
  return response.data;
};

/**
 * Obtiene una factura por ID
 */
export const getInvoiceById = async (id: string): Promise<Invoice> => {
  const response = await api.get(`/api/billing/invoices/${id}`);
  return response.data;
};

/**
 * Procesa el pago de una factura
 */
export const processPayment = async (
  invoiceId: string,
  paymentData: PaymentRequest
): Promise<PaymentResponse> => {
  const response = await api.post(`/api/billing/invoices/${invoiceId}/pay`, paymentData);
  return response.data;
};

/**
 * Obtiene las facturas de un paciente específico
 */
export const getPatientInvoices = async (patientId: string): Promise<Invoice[]> => {
  const response = await api.get(`/api/billing/invoices/patient/${patientId}`);
  return response.data;
};

/**
 * Cancela una factura
 */
export const cancelInvoice = async (invoiceId: string): Promise<Invoice> => {
  const response = await api.delete(`/api/billing/invoices/${invoiceId}`);
  return response.data;
};
