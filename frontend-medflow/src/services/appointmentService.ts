/**
 * appointmentService - Servicio para gestión de citas
 * PLACEHOLDER MODE: Mock data hasta que el backend esté disponible
 */


// Mock data para desarrollo
const mockAppointments = [
  {
    id: 'APT-001',
    patientName: 'Juan Pérez García',
    patientDPI: '2345678901234',
    date: '2026-03-12',
    time: '09:00 AM',
    status: 'PENDING',
    doctor: 'Dr. María López',
    specialty: 'Cardiología',
  },
  {
    id: 'APT-002',
    patientName: 'Ana Martínez Rodríguez',
    patientDPI: '3456789012345',
    date: '2026-03-12',
    time: '10:00 AM',
    status: 'PENDING',
    doctor: 'Dr. Carlos Hernández',
    specialty: 'Pediatría',
  },
  {
    id: 'APT-003',
    patientName: 'Luis González Morales',
    patientDPI: '4567890123456',
    date: '2026-03-12',
    time: '11:00 AM',
    status: 'ACTIVATED',
    doctor: 'Dr. Laura Sánchez',
    specialty: 'Medicina General',
  },
  {
    id: 'APT-004',
    patientName: 'María Fernández Cruz',
    patientDPI: '5678901234567',
    date: '2026-03-13',
    time: '09:30 AM',
    status: 'PENDING',
    doctor: 'Dr. Roberto Díaz',
    specialty: 'Neurología',
  },
  {
    id: 'APT-005',
    patientName: 'Carlos Ramírez Torres',
    patientDPI: '6789012345678',
    date: '2026-03-13',
    time: '14:00 PM',
    status: 'CANCELLED',
    doctor: 'Dr. Patricia Jiménez',
    specialty: 'Dermatología',
  },
];

/**
 * Obtener todas las citas
 * @returns Promise con array de citas
 */
export const getAppointments = async () => {
  // BACKEND READY: Descomentar cuando el endpoint esté disponible
  // try {
  //   const response = await axios.get(`${API_URL}/appointments`);
  //   return response.data;
  // } catch (error) {
  //   console.error('Error fetching appointments:', error);
  //   throw error;
  // }

  // Mock: Simular delay de red
  console.log('Mock: Fetching appointments from API...');
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(mockAppointments);
    }, 800);
  });
};

/**
 * Activar una cita
 * @param id - ID de la cita a activar
 * @returns Promise con resultado de la operación
 */
export const activateAppointment = async (id: string) => {
  // BACKEND READY: Descomentar cuando el endpoint esté disponible
  // try {
  //   const response = await axios.post(`${API_URL}/appointments/${id}/activate`);
  //   return response;
  // } catch (error) {
  //   console.error('Error activating appointment:', error);
  //   throw error;
  // }

  // Mock: Simular activación
  console.log(`Mock: Activating appointment ${id}`);
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ 
        status: 200, 
        data: { 
          message: 'Appointment activated successfully',
          appointmentId: id,
        } 
      });
    }, 500);
  });
};

/**
 * Crear nueva cita
 * @param appointmentData - Datos de la cita
 * @returns Promise con la cita creada
 */
export const createAppointment = async (appointmentData: Record<string, unknown>) => {
  // BACKEND READY: Descomentar cuando el endpoint esté disponible
  // try {
  //   const response = await axios.post(`${API_URL}/appointments`, appointmentData);
  //   return response.data;
  // } catch (error) {
  //   console.error('Error creating appointment:', error);
  //   throw error;
  // }

  // Mock: Simular creación
  console.log('Mock: Creating appointment with data:', appointmentData);
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        id: `APT-${Date.now()}`,
        ...appointmentData,
        status: 'PENDING',
      });
    }, 500);
  });
};

/**
 * Validar una cita
 * @param id - ID de la cita a validar
 * @returns Promise con resultado de validación
 */
export const validateAppointment = async (id: string) => {
  // BACKEND READY: Descomentar cuando el endpoint esté disponible
  // try {
  //   const response = await axios.post(`${API_URL}/appointments/${id}/validate`);
  //   return response.data;
  // } catch (error) {
  //   console.error('Error validating appointment:', error);
  //   throw error;
  // }

  // Mock: Simular validación
  console.log(`Mock: Validating appointment ${id}`);
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        valid: true,
        message: 'Appointment is valid',
      });
    }, 500);
  });
};

export default {
  getAppointments,
  activateAppointment,
  createAppointment,
  validateAppointment,
};
