/**
 * Tipos para el sistema de citas
 */

export interface AppointmentFormData {
  dpi: number;
  nit: number;
  name: string;
  gender: string;
  email: string;
  phone: string;
  date: string;
  time: string;
  
  
  message: string;
}

export interface AppointmentRequest extends AppointmentFormData {
  patientId?: string;
}

export interface AppointmentResponse {
  success: boolean;
  message: string;
  data?: {
    appointmentId: string;
    confirmationCode: string;
  };
  errors?: Record<string, string[]>;
}
