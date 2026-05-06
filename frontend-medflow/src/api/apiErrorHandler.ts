/**
 * Typed Axios error handler for API modules.
 * Replaces `catch (error: any)` with proper type narrowing.
 */
import { isAxiosError } from 'axios';

interface ApiErrorBody {
  message?: string;
}

/**
 * Extracts a Spanish error message from an Axios error response.
 * Falls back to the provided default message if no specific message is found.
 */
export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (isAxiosError(error)) {
    const data = error.response?.data as ApiErrorBody | undefined;
    if (data?.message) return data.message;

    switch (error.response?.status) {
      case 400: return 'Solicitud inválida. Verifique los datos ingresados.';
      case 404: return 'Recurso no encontrado.';
      case 409: return 'Conflicto con el estado actual del recurso.';
    }
  }
  return fallback;
}

/**
 * Returns the HTTP status code from an Axios error, or undefined.
 */
export function getApiErrorStatus(error: unknown): number | undefined {
  if (isAxiosError(error)) return error.response?.status;
  return undefined;
}
