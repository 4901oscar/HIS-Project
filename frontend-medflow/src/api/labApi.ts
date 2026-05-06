import api from './index';
import { getCurrentUser } from '../services/authService';

/**
 * Lab API - Laboratory Service Functions
 * 
 * This module provides functions for interacting with the Lab Service.
 * Handles lab order retrieval, test result uploads, and validation.
 * All functions require authentication and include the X-User-Id header for audit trail.
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
  valid: boolean;  // Jackson serializes boolean isValid as "valid"
  missingTests: string[];
  message?: string;
}

/**
 * Retrieves lab order and tests for a specific appointment.
 * 
 * @param appointmentId - The appointment ID
 * @returns Lab order with all test details
 * @throws Error with Spanish message if retrieval fails
 */
export const getLabOrderByAppointmentId = async (appointmentId: string): Promise<LabOrderWithTestsResponse> => {
  try {
    const response = await api.get<LabOrderWithTestsResponse>(
      `/api/lab/orders/by-appointment/${appointmentId}`
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Orden de laboratorio no encontrada para esta cita');
    }
    throw new Error('Error al obtener la orden de laboratorio. Por favor, intente nuevamente.');
  }
};

/**
 * Uploads a result file for a specific test within an order.
 * Accepts PDF, JPEG, and PNG files up to 10MB.
 * 
 * @param orderId - The lab order ID
 * @param testName - The test name
 * @param file - The result file to upload
 * @returns Lab result metadata including file ID and download URL
 * @throws Error with Spanish message if upload fails
 */
export const uploadTestResult = async (
  orderId: string,
  testName: string,
  file: File
): Promise<LabResultResponse> => {
  try {
    const currentUser = getCurrentUser();
    if (!currentUser) {
      throw new Error('Usuario no autenticado');
    }

    // Validate file format
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png'];
    if (!allowedTypes.includes(file.type)) {
      throw new Error('Formato de archivo no permitido. Solo se aceptan PDF, JPEG y PNG.');
    }

    // Validate file size (10MB = 10 * 1024 * 1024 bytes)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      throw new Error('El archivo excede el tamaño máximo permitido de 10 MB.');
    }

    // Create FormData for multipart/form-data upload
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post<LabResultResponse>(
      `/api/lab/orders/${orderId}/tests/${encodeURIComponent(testName)}/results`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
          'X-User-Id': currentUser.id,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    // If error already has a Spanish message (from validation above), re-throw it
    if (error.message && !error.response) {
      throw error;
    }

    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Orden de laboratorio o examen no encontrado');
    }
    if (error.response?.status === 400) {
      const message = error.response?.data?.message || 'Error de validación al cargar el archivo';
      throw new Error(message);
    }
    throw new Error('Error al cargar el resultado. Por favor, intente nuevamente.');
  }
};

/**
 * Retrieves all result files for a lab order.
 * 
 * @param orderId - The lab order ID
 * @returns List of lab results with metadata and download URLs
 * @throws Error with Spanish message if retrieval fails
 */
export const getLabOrderResults = async (orderId: string): Promise<LabResultResponse[]> => {
  try {
    const response = await api.get<LabResultResponse[]>(
      `/api/lab/orders/${orderId}/results`
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Orden de laboratorio no encontrada');
    }
    throw new Error('Error al obtener los resultados. Por favor, intente nuevamente.');
  }
};

/**
 * Validates that all tests in an order have uploaded results.
 * 
 * @param orderId - The lab order ID
 * @returns Validation result with isValid flag and list of missing tests
 * @throws Error with Spanish message if validation request fails
 */
export const validateAllTestsComplete = async (orderId: string): Promise<ValidationResponse> => {
  try {
    const response = await api.get<ValidationResponse>(
      `/api/lab/orders/${orderId}/validation/all-tests-complete`
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Orden de laboratorio no encontrada');
    }
    throw new Error('Error al validar los resultados. Por favor, intente nuevamente.');
  }
};

/**
 * Retrieves all result files for a lab order by appointment ID.
 * Gets ALL results from ALL orders associated with this appointment.
 * This includes results from multiple orders (e.g., when patient returns to lab for additional tests).
 * 
 * @param appointmentId - The appointment ID
 * @returns List of lab results with metadata and download URLs from all orders
 * @throws Error with Spanish message if retrieval fails
 */
export const getLabResultsByAppointmentId = async (appointmentId: string): Promise<LabResultResponse[]> => {
  try {
    // Use the new endpoint that gets ALL results from ALL orders for this appointment
    const response = await api.get<LabResultResponse[]>(
      `/api/lab/orders/appointment/${appointmentId}/results`
    );
    
    return response.data;
  } catch (error: any) {
    // If no lab order found, return empty array (no results yet)
    if (error.response?.status === 404 || error.message?.includes('no encontrada')) {
      return [];
    }
    
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    
    throw new Error('Error al obtener los resultados. Por favor, intente nuevamente.');
  }
};

/**
 * Gets the download URL for a lab result file.
 * 
 * @param resultId - The lab result ID
 * @returns Download URL
 */
export const getLabResultDownloadUrl = (resultId: string): string => {
  return `/api/lab/orders/results/${resultId}/download`;
};

/**
 * Opens a lab result file in a new tab for viewing.
 * Downloads the file with authentication and opens it in a blob URL.
 * 
 * @param resultId - The lab result ID
 */
export const viewLabResult = async (resultId: string): Promise<void> => {
  try {
    const url = getLabResultDownloadUrl(resultId);
    const response = await api.get(url, {
      responseType: 'blob',
    });

    // Create blob URL and open in new tab
    const blob = response.data;
    const blobUrl = window.URL.createObjectURL(blob);
    window.open(blobUrl, '_blank');

    // Clean up blob URL after a delay
    setTimeout(() => {
      window.URL.revokeObjectURL(blobUrl);
    }, 100);
  } catch (error) {
    console.error('Error viewing file:', error);
    throw new Error('Error al visualizar el archivo. Por favor, intente nuevamente.');
  }
};

/**
 * Downloads a lab result file.
 * Uses the authenticated API client to download the file and trigger browser download.
 * 
 * @param resultId - The lab result ID
 * @param filename - The filename to save as
 */
export const downloadLabResult = async (resultId: string, filename: string): Promise<void> => {
  try {
    const url = getLabResultDownloadUrl(resultId);
    const response = await api.get(url, {
      responseType: 'blob',
    });

    // Create blob URL and trigger download
    const blob = response.data;
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    // Clean up blob URL
    window.URL.revokeObjectURL(downloadUrl);
  } catch (error) {
    console.error('Error downloading file:', error);
    throw new Error('Error al descargar el archivo. Por favor, intente nuevamente.');
  }
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
