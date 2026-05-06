import { AxiosError } from 'axios';

/**
 * Extrae un mensaje de error amigable en español desde un error de API.
 * Mapea códigos de estado HTTP a mensajes específicos en español.
 *
 * @param error - El error capturado (puede ser AxiosError, Error, o unknown)
 * @returns Mensaje de error en español para mostrar al usuario
 */
export function extractErrorMessage(error: unknown): string {
  // Verificar si es un error de Axios con respuesta del servidor
  if (isAxiosError(error) && error.response) {
    const status = error.response.status;
    
    // Mapear códigos de estado HTTP a mensajes en español
    switch (status) {
      case 400:
        return 'Solicitud inválida';
      case 404:
        return 'Recurso no encontrado';
      case 409:
        return 'Esta cita ya tiene triaje registrado';
      case 500:
        return 'Error del servidor';
      default:
        // Si hay un mensaje específico del servidor, usarlo
        if (error.response.data && typeof error.response.data === 'object') {
          const data = error.response.data as { message?: string };
          if (data.message) {
            return data.message;
          }
        }
        return `Error del servidor (${status})`;
    }
  }

  // Verificar si es un error de red (sin respuesta del servidor)
  if (isAxiosError(error) && error.request && !error.response) {
    return 'Error al conectar con el servidor';
  }

  // Verificar si es un error de Axios sin respuesta (timeout, cancelación, etc.)
  if (isAxiosError(error) && error.code === 'ECONNABORTED') {
    return 'Error al conectar con el servidor';
  }

  // Si es un Error estándar de JavaScript
  if (error instanceof Error) {
    return error.message;
  }

  // Fallback para errores desconocidos
  return 'Ha ocurrido un error inesperado';
}

/**
 * Verifica si un error es un error HTTP con un código de estado específico.
 *
 * @param error - El error a verificar
 * @param status - El código de estado HTTP a comparar
 * @returns true si el error es un AxiosError con el código de estado especificado
 */
export function isHttpError(error: unknown, status: number): boolean {
  return isAxiosError(error) && error.response?.status === status;
}

/**
 * Type guard para verificar si un error es un AxiosError.
 *
 * @param error - El error a verificar
 * @returns true si el error es un AxiosError
 */
function isAxiosError(error: unknown): error is AxiosError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'isAxiosError' in error &&
    (error as AxiosError).isAxiosError === true
  );
}

export default {
  extractErrorMessage,
  isHttpError,
};
