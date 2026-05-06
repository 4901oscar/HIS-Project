import api from './index';
import { getCurrentUser } from '../services/authService';
import { getApiErrorMessage, getApiErrorStatus } from './apiErrorHandler';

/**
 * Lab API — Laboratory Service Functions
 * Handles lab order retrieval, test result uploads, and validation.
 */

export interface TestDetail {
  testName: string;
  testType: string;
  sampleType: string;
  hasResult: boolean;
}

export interface LabOrderWithTestsResponse {
  id: string;
  orderCode: string;
  patientId: string;
  doctorId: string;
  appointmentId: string;
  tests: TestDetail[];
  status: string;
  orderedAt: string;
}

export interface LabResultResponse {
  id: string;
  orderId: string;
  testName: string;
  originalFilename: string;
  fileSize: number;
  uploadedAt: string;
  uploadedBy: string;
  downloadUrl: string;
}

export interface ValidationResponse {
  valid: boolean;
  missingTests: string[];
  message?: string;
}

function requireUser() {
  const user = getCurrentUser();
  if (!user) throw new Error('Usuario no autenticado');
  return user;
}

export const getLabOrderByAppointmentId = async (appointmentId: string): Promise<LabOrderWithTestsResponse> => {
  try {
    const response = await api.get<LabOrderWithTestsResponse>(`/api/lab/orders/by-appointment/${appointmentId}`);
    return response.data;
  } catch (error: unknown) {
    if (getApiErrorStatus(error) === 404) throw new Error('Orden de laboratorio no encontrada para esta cita');
    throw new Error(getApiErrorMessage(error, 'Error al obtener la orden de laboratorio. Por favor, intente nuevamente.'));
  }
};

const ALLOWED_FILE_TYPES = ['application/pdf', 'image/jpeg', 'image/png'];
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

export const uploadTestResult = async (
  orderId: string,
  testName: string,
  file: File
): Promise<LabResultResponse> => {
  if (!ALLOWED_FILE_TYPES.includes(file.type)) {
    throw new Error('Formato de archivo no permitido. Solo se aceptan PDF, JPEG y PNG.');
  }
  if (file.size > MAX_FILE_SIZE) {
    throw new Error('El archivo excede el tamaño máximo permitido de 10 MB.');
  }

  const user = requireUser();
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await api.post<LabResultResponse>(
      `/api/lab/orders/${orderId}/tests/${encodeURIComponent(testName)}/results`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data', 'X-User-Id': user.id } }
    );
    return response.data;
  } catch (error: unknown) {
    const status = getApiErrorStatus(error);
    if (status === 404) throw new Error('Orden de laboratorio o examen no encontrado');
    throw new Error(getApiErrorMessage(error, 'Error al cargar el resultado. Por favor, intente nuevamente.'));
  }
};

export const getLabOrderResults = async (orderId: string): Promise<LabResultResponse[]> => {
  try {
    const response = await api.get<LabResultResponse[]>(`/api/lab/orders/${orderId}/results`);
    return response.data;
  } catch (error: unknown) {
    if (getApiErrorStatus(error) === 404) throw new Error('Orden de laboratorio no encontrada');
    throw new Error(getApiErrorMessage(error, 'Error al obtener los resultados. Por favor, intente nuevamente.'));
  }
};

export const validateAllTestsComplete = async (orderId: string): Promise<ValidationResponse> => {
  try {
    const response = await api.get<ValidationResponse>(`/api/lab/orders/${orderId}/validation/all-tests-complete`);
    return response.data;
  } catch (error: unknown) {
    if (getApiErrorStatus(error) === 404) throw new Error('Orden de laboratorio no encontrada');
    throw new Error(getApiErrorMessage(error, 'Error al validar los resultados. Por favor, intente nuevamente.'));
  }
};

export const getLabResultsByAppointmentId = async (appointmentId: string): Promise<LabResultResponse[]> => {
  try {
    const response = await api.get<LabResultResponse[]>(`/api/lab/orders/appointment/${appointmentId}/results`);
    return response.data;
  } catch (error: unknown) {
    if (getApiErrorStatus(error) === 404) return [];
    throw new Error(getApiErrorMessage(error, 'Error al obtener los resultados. Por favor, intente nuevamente.'));
  }
};

export const getLabResultDownloadUrl = (resultId: string): string =>
  `/api/lab/orders/results/${resultId}/download`;

export const viewLabResult = async (resultId: string): Promise<void> => {
  const response = await api.get(getLabResultDownloadUrl(resultId), { responseType: 'blob' });
  const blobUrl = window.URL.createObjectURL(response.data as Blob);
  window.open(blobUrl, '_blank');
  setTimeout(() => window.URL.revokeObjectURL(blobUrl), 100);
};

export const downloadLabResult = async (resultId: string, filename: string): Promise<void> => {
  const response = await api.get(getLabResultDownloadUrl(resultId), { responseType: 'blob' });
  const downloadUrl = window.URL.createObjectURL(response.data as Blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(downloadUrl);
};

export default {
  getLabOrderByAppointmentId,
  uploadTestResult,
  getLabOrderResults,
  validateAllTestsComplete,
  getLabResultsByAppointmentId,
  getLabResultDownloadUrl,
  viewLabResult,
  downloadLabResult,
};
