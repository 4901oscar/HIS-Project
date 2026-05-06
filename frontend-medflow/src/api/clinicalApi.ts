import api from './index';
import { getCurrentUser } from '../services/authService';

/**
 * Clinical API - Laboratory Workflow
 * 
 * This module provides functions for managing laboratory workflow appointment status transitions.
 * All functions require authentication and include the X-User-Id header for audit trail.
 */

export interface AppointmentListItemResponse {
  id: string;
  patientId: string;
  patientName?: string;
  patientDpi?: string;
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  notes?: string;
  createdAt: string;
}

/**
 * Transition: LAB_SAMPLE_COLLECTION → LAB_SAMPLE_PENDING
 * Marks samples as collected for a laboratory appointment.
 * 
 * @param appointmentId - The appointment ID
 * @returns Updated appointment data
 * @throws Error with Spanish message if transition fails
 */
export const collectLabSamples = async (appointmentId: string): Promise<AppointmentListItemResponse> => {
  try {
    const currentUser = getCurrentUser();
    if (!currentUser) {
      throw new Error('Usuario no autenticado');
    }

    const response = await api.put<AppointmentListItemResponse>(
      `/api/clinical/appointments/${appointmentId}/lab/collect-samples`,
      {},
      {
        headers: {
          'X-User-Id': currentUser.id,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Cita no encontrada');
    }
    if (error.response?.status === 400) {
      throw new Error('Transición de estado inválida. Verifique el estado actual de la cita.');
    }
    throw new Error('Error al recolectar muestras. Por favor, intente nuevamente.');
  }
};

/**
 * Transition: LAB_SAMPLE_PENDING → LAB_PROCESSING
 * Accepts the collected samples and moves to processing stage.
 * 
 * @param appointmentId - The appointment ID
 * @returns Updated appointment data
 * @throws Error with Spanish message if transition fails
 */
export const acceptLabSamples = async (appointmentId: string): Promise<AppointmentListItemResponse> => {
  try {
    const currentUser = getCurrentUser();
    if (!currentUser) {
      throw new Error('Usuario no autenticado');
    }

    const response = await api.put<AppointmentListItemResponse>(
      `/api/clinical/appointments/${appointmentId}/lab/accept-samples`,
      {},
      {
        headers: {
          'X-User-Id': currentUser.id,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Cita no encontrada');
    }
    if (error.response?.status === 400) {
      throw new Error('Transición de estado inválida. Verifique el estado actual de la cita.');
    }
    throw new Error('Error al aceptar muestras. Por favor, intente nuevamente.');
  }
};

/**
 * Transition: LAB_SAMPLE_PENDING → LAB_SAMPLE_COLLECTION
 * Rejects the collected samples and requests new sample collection.
 * 
 * @param appointmentId - The appointment ID
 * @returns Updated appointment data
 * @throws Error with Spanish message if transition fails
 */
export const rejectLabSamples = async (appointmentId: string): Promise<AppointmentListItemResponse> => {
  try {
    const currentUser = getCurrentUser();
    if (!currentUser) {
      throw new Error('Usuario no autenticado');
    }

    const response = await api.put<AppointmentListItemResponse>(
      `/api/clinical/appointments/${appointmentId}/lab/reject-samples`,
      {},
      {
        headers: {
          'X-User-Id': currentUser.id,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Cita no encontrada');
    }
    if (error.response?.status === 400) {
      throw new Error('Transición de estado inválida. Verifique el estado actual de la cita.');
    }
    throw new Error('Error al rechazar muestras. Por favor, intente nuevamente.');
  }
};

/**
 * Transition: LAB_PROCESSING → LAB_RESULTS_READY
 * Marks laboratory processing as complete. Validates that all tests have uploaded results.
 * 
 * @param appointmentId - The appointment ID
 * @returns Updated appointment data
 * @throws Error with Spanish message if transition fails or validation fails
 */
export const completeLabProcessing = async (appointmentId: string): Promise<AppointmentListItemResponse> => {
  try {
    const currentUser = getCurrentUser();
    if (!currentUser) {
      throw new Error('Usuario no autenticado');
    }

    const response = await api.put<AppointmentListItemResponse>(
      `/api/clinical/appointments/${appointmentId}/lab/complete-processing`,
      {},
      {
        headers: {
          'X-User-Id': currentUser.id,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Cita no encontrada');
    }
    if (error.response?.status === 400) {
      const message = error.response?.data?.message || 'Transición de estado inválida. Verifique que todos los exámenes tengan resultados cargados.';
      throw new Error(message);
    }
    throw new Error('Error al completar procesamiento. Por favor, intente nuevamente.');
  }
};

/**
 * Transition: LAB_RESULTS_READY → CONSULTATION
 * Sends laboratory results to the doctor and completes the lab workflow.
 * 
 * @param appointmentId - The appointment ID
 * @returns Updated appointment data
 * @throws Error with Spanish message if transition fails
 */
export const sendLabResultsToDoctor = async (appointmentId: string): Promise<AppointmentListItemResponse> => {
  try {
    const currentUser = getCurrentUser();
    if (!currentUser) {
      throw new Error('Usuario no autenticado');
    }

    const response = await api.put<AppointmentListItemResponse>(
      `/api/clinical/appointments/${appointmentId}/lab/send-to-doctor`,
      {},
      {
        headers: {
          'X-User-Id': currentUser.id,
        },
      }
    );
    return response.data;
  } catch (error: any) {
    if (error.response?.data?.message) {
      throw new Error(error.response.data.message);
    }
    if (error.response?.status === 404) {
      throw new Error('Cita no encontrada');
    }
    if (error.response?.status === 400) {
      throw new Error('Transición de estado inválida. Verifique el estado actual de la cita.');
    }
    throw new Error('Error al enviar resultados al doctor. Por favor, intente nuevamente.');
  }
};

export default {
  collectLabSamples,
  acceptLabSamples,
  rejectLabSamples,
  completeLabProcessing,
  sendLabResultsToDoctor,
};
