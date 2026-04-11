/**
 * Servicio de Pacientes
 * Comunicación con el backend para operaciones de pacientes
 */

import axios from 'axios';
import type { AxiosInstance } from 'axios';
import type {
  PatientRegistrationRequest,
  PatientRegistrationResponse,
  PatientRegistration,
} from '../types/patient';

class PatientService {
  private apiClient: AxiosInstance;

  constructor() {
    this.apiClient = axios.create({
      baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Interceptor para agregar token JWT
    this.apiClient.interceptors.request.use((config) => {
      const token = localStorage.getItem('authToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });
  }

  /**
   * Registra un nuevo paciente con datos biográficos y datos biométricos opcionales
   */
  async registerPatient(
    request: PatientRegistrationRequest
  ): Promise<PatientRegistrationResponse> {
    try {
      const response = await this.apiClient.post<PatientRegistrationResponse>(
        '/patients/register',
        request
      );
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.data) {
        return error.response.data as PatientRegistrationResponse;
      }
      throw new Error('Error al registrar paciente: ' + String(error));
    }
  }

  /**
   * Obtiene los datos de un paciente por ID
   */
  async getPatientById(patientId: string): Promise<PatientRegistration | null> {
    try {
      const response = await this.apiClient.get<PatientRegistration>(
        `/patients/${patientId}`
      );
      return response.data;
    } catch (error) {
      console.error('Error al obtener paciente:', error);
      return null;
    }
  }

  /**
   * Actualiza los datos de un paciente
   */
  async updatePatient(
    patientId: string,
    data: Partial<PatientRegistrationRequest>
  ): Promise<PatientRegistrationResponse> {
    try {
      const response = await this.apiClient.put<PatientRegistrationResponse>(
        `/patients/${patientId}`,
        data
      );
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.data) {
        return error.response.data as PatientRegistrationResponse;
      }
      throw new Error('Error al actualizar paciente: ' + String(error));
    }
  }
}

export default new PatientService();
