import api from './index';
import { getCurrentUser } from '../services/authService';
import { getApiErrorMessage, getApiErrorStatus } from './apiErrorHandler';

/**
 * Clinical API — Laboratory Workflow
 * Manages appointment status transitions for the lab workflow.
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

function requireUser() {
  const user = getCurrentUser();
  if (!user) throw new Error('Usuario no autenticado');
  return user;
}

async function labTransition(
  appointmentId: string,
  endpoint: string,
  fallbackMessage: string
): Promise<AppointmentListItemResponse> {
  const user = requireUser();
  try {
    const response = await api.put<AppointmentListItemResponse>(
      `/api/clinical/appointments/${appointmentId}/lab/${endpoint}`,
      {},
      { headers: { 'X-User-Id': user.id } }
    );
    return response.data;
  } catch (error: unknown) {
    const status = getApiErrorStatus(error);
    if (status === 400) {
      throw new Error(
        getApiErrorMessage(error, 'Transición de estado inválida. Verifique el estado actual de la cita.')
      );
    }
    throw new Error(getApiErrorMessage(error, fallbackMessage));
  }
}

/** LAB_SAMPLE_COLLECTION → LAB_SAMPLE_PENDING */
export const collectLabSamples = (appointmentId: string) =>
  labTransition(appointmentId, 'collect-samples', 'Error al recolectar muestras. Por favor, intente nuevamente.');

/** LAB_SAMPLE_PENDING → LAB_PROCESSING */
export const acceptLabSamples = (appointmentId: string) =>
  labTransition(appointmentId, 'accept-samples', 'Error al aceptar muestras. Por favor, intente nuevamente.');

/** LAB_SAMPLE_PENDING → LAB_SAMPLE_COLLECTION */
export const rejectLabSamples = (appointmentId: string) =>
  labTransition(appointmentId, 'reject-samples', 'Error al rechazar muestras. Por favor, intente nuevamente.');

/** LAB_PROCESSING → LAB_RESULTS_READY */
export const completeLabProcessing = (appointmentId: string) =>
  labTransition(appointmentId, 'complete-processing', 'Error al completar procesamiento. Por favor, intente nuevamente.');

/** LAB_RESULTS_READY → CONSULTATION */
export const sendLabResultsToDoctor = (appointmentId: string) =>
  labTransition(appointmentId, 'send-to-doctor', 'Error al enviar resultados al doctor. Por favor, intente nuevamente.');

export default {
  collectLabSamples,
  acceptLabSamples,
  rejectLabSamples,
  completeLabProcessing,
  sendLabResultsToDoctor,
};
