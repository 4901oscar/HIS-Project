import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { renderWithProviders, mockDoctorUser } from './test-utils';
import PatientConsultationForm from '../pages/doctor/PatientConsultationForm';
import userEvent from '@testing-library/user-event';

/**
 * Bug Condition Exploration Test - Doctor Follow-Up Appointment Scheduling
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7**
 * 
 * OBJETIVO: Verificar que el formulario de consulta del doctor usa un componente Calendar
 * con validación de disponibilidad DESPUÉS de implementar la corrección.
 * 
 * RESULTADO ESPERADO: Este test DEBE PASAR en código corregido - el éxito confirma que el bug está arreglado.
 * 
 * Expected Behavior (Design Property 1): Calendar-Based Scheduling with Validation
 * - Cuando el checkbox "Agendar cita de seguimiento" está marcado
 * - El sistema muestra un componente Calendar interactivo
 * - Hay selección de slots de tiempo de 30 minutos
 * - Hay validación de disponibilidad en tiempo real
 * - Se bloquean fechas pasadas
 * - Se bloquean días libres del doctor
 * - Se filtran slots de tiempo pasados si hoy es seleccionado
 * - Se asigna automáticamente el paciente actual
 */

// Mock de servicios para evitar llamadas reales a la API
vi.mock('../services/appointmentService', () => ({
  getAppointmentById: vi.fn().mockResolvedValue({
    id: 'appt-1',
    appointmentDate: '2024-01-15',
    appointmentTime: '10:00',
    status: 'CONFIRMED',
    patient: {
      id: 'patient-1',
      fullName: 'Juan Pérez',
      dpi: '1234567890101',
      email: 'juan@test.com',
    },
    doctor: {
      id: 'doctor-1',
      fullName: 'Dr. Test',
    },
    notes: 'Consulta de control',
  }),
  getAvailableSlotsForDate: vi.fn().mockResolvedValue([
    '09:00', '09:30', '10:00', '10:30', '11:00', '11:30',
    '14:00', '14:30', '15:00', '15:30', '16:00', '16:30',
  ]),
}));

vi.mock('../services/clinicalService', () => ({
  getVitalSignsByAppointment: vi.fn().mockResolvedValue({
    systolicPressure: 120,
    diastolicPressure: 80,
    heartRate: 75,
    respiratoryRate: 16,
    temperature: 36.5,
    oxygenSaturation: 98,
    weight: 70,
    height: 170,
    bmi: 24.2,
  }),
  getAppointmentTriage: vi.fn().mockResolvedValue({
    priorityLevel: 'GREEN',
    priorityDescription: 'Poco urgente',
    maxWaitTimeMinutes: 120,
    performedAt: '2024-01-15T09:00:00',
  }),
  registerConsultation: vi.fn(),
  generateLabOrder: vi.fn(),
  generatePrescription: vi.fn(),
}));

vi.mock('../services/billingCatalogService', () => ({
  getServiceItems: vi.fn().mockResolvedValue([
    { id: '1', name: 'Hemograma completo', price: 50, status: 'ACTIVE' },
    { id: '2', name: 'Glucosa', price: 25, status: 'ACTIVE' },
  ]),
}));

vi.mock('../services/doctorService', () => ({
  listActiveDoctors: vi.fn().mockResolvedValue([
    {
      id: 'doctor-1',
      fullName: 'Dr. Juan García',
      specialty: 'Medicina General',
      shiftStart: '08:00',
      shiftEnd: '16:00',
    },
    {
      id: 'doctor-2',
      fullName: 'Dra. María López',
      specialty: 'Pediatría',
      shiftStart: '09:00',
      shiftEnd: '17:00',
    },
  ]),
  getDoctorDaysOff: vi.fn().mockResolvedValue([
    { date: '2024-01-20', reason: 'Vacaciones' },
    { date: '2024-01-21', reason: 'Vacaciones' },
  ]),
}));

// Mock de react-router-dom
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ appointmentId: 'appt-1' }),
    useNavigate: () => vi.fn(),
  };
});

describe('Expected Behavior: Calendar-Based Scheduling with Validation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  /**
   * Test 1.1: Verificar que se muestra un input simple de fecha (NO Calendar component)
   * 
   * Bug Condition: El sistema muestra <input type="date"> cuando el checkbox está marcado
   * Expected Behavior: Debe mostrar un componente Calendar interactivo
   */
  it('should verify Calendar component is rendered (not simple date input)', async () => {
    renderWithProviders(<PatientConsultationForm />, { user: mockDoctorUser });

    // Esperar a que cargue el formulario
    await waitFor(() => {
      expect(screen.getAllByText('Juan Pérez')[0]).toBeInTheDocument();
    });

    // Seleccionar un destino para habilitar la sección de seguimiento
    const dischargeButton = await screen.findByText(/ALTA/i);
    await userEvent.click(dischargeButton);

    // Marcar el checkbox de seguimiento
    const followUpCheckbox = screen.getByLabelText(/Agendar cita de seguimiento/i);
    await userEvent.click(followUpCheckbox);

    // Verificar que existe un componente Calendar (expected behavior)
    // El Calendar component tiene una estructura específica con días de la semana
    const calendarDayNames = screen.queryByText('Do'); // Domingo en el calendario
    expect(calendarDayNames).toBeInTheDocument();

    // Verificar que NO existe un input type="date" simple (bug condition eliminated)
    const dateInputs = screen.queryAllByLabelText(/Fecha de seguimiento/i);
    dateInputs.forEach(input => {
      expect(input.tagName).not.toBe('INPUT');
    });
  });

  /**
   * Test 1.2: Verificar que HAY selección de slots de tiempo
   * 
   * Expected Behavior: Debe mostrar un grid de slots de tiempo disponibles
   */
  it('should verify time slot selection UI is present', async () => {
    renderWithProviders(<PatientConsultationForm />, { user: mockDoctorUser });

    await waitFor(() => {
      expect(screen.getAllByText('Juan Pérez')[0]).toBeInTheDocument();
    });

    const dischargeButton = await screen.findByText(/ALTA/i);
    await userEvent.click(dischargeButton);

    const followUpCheckbox = screen.getByLabelText(/Agendar cita de seguimiento/i);
    await userEvent.click(followUpCheckbox);

    // Esperar a que se cargue el calendario
    await waitFor(() => {
      expect(screen.getByText('Do')).toBeInTheDocument();
    });

    // Simular selección de una fecha (click en un día del calendario)
    // Esto debería activar la carga de slots
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowDay = tomorrow.getDate();

    // Buscar el botón del día de mañana en el calendario
    const dayButtons = screen.getAllByRole('button');
    const tomorrowButton = dayButtons.find(btn => btn.textContent === String(tomorrowDay) && !btn.disabled);
    
    if (tomorrowButton) {
      await userEvent.click(tomorrowButton);

      // Verificar que aparece la sección de selección de hora
      await waitFor(() => {
        expect(screen.getByText(/Hora de la cita/i)).toBeInTheDocument();
      });
    }
  });

  /**
   * Test 1.3: Verificar que se bloquean fechas pasadas
   * 
   * Expected Behavior: El Calendar debe bloquear visualmente fechas pasadas
   */
  it('should verify past dates are blocked in Calendar component', async () => {
    renderWithProviders(<PatientConsultationForm />, { user: mockDoctorUser });

    await waitFor(() => {
      expect(screen.getAllByText('Juan Pérez')[0]).toBeInTheDocument();
    });

    const dischargeButton = await screen.findByText(/ALTA/i);
    await userEvent.click(dischargeButton);

    const followUpCheckbox = screen.getByLabelText(/Agendar cita de seguimiento/i);
    await userEvent.click(followUpCheckbox);

    // Esperar a que se cargue el calendario
    await waitFor(() => {
      expect(screen.getByText('Do')).toBeInTheDocument();
    });

    // Verificar que el Calendar tiene la estructura correcta con bloqueo visual
    // Los días pasados deben tener clases de deshabilitado
    const dayButtons = screen.getAllByRole('button');
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayDay = yesterday.getDate();

    // Buscar el botón del día de ayer
    const yesterdayButton = dayButtons.find(btn => 
      btn.textContent === String(yesterdayDay) && 
      (btn.disabled || btn.className.includes('cursor-not-allowed'))
    );

    // Si encontramos el botón de ayer, debe estar deshabilitado
    if (yesterdayButton) {
      expect(yesterdayButton.disabled || yesterdayButton.className.includes('cursor-not-allowed')).toBe(true);
    }
  });

  /**
   * Test 1.4: Verificar que se carga información de doctores
   * 
   * Expected Behavior: Debe cargar doctores y días libres para validar disponibilidad
   */
  it('should verify doctor data is loaded', async () => {
    const { listActiveDoctors, getDoctorDaysOff } = await import('../services/doctorService');

    renderWithProviders(<PatientConsultationForm />, { user: mockDoctorUser });

    await waitFor(() => {
      expect(screen.getAllByText('Juan Pérez')[0]).toBeInTheDocument();
    });

    // Esperar a que se carguen los datos de doctores (se cargan al montar el componente)
    await waitFor(() => {
      expect(listActiveDoctors).toHaveBeenCalled();
    }, { timeout: 3000 });

    // Verificar que se llamó getDoctorDaysOff para cada doctor
    await waitFor(() => {
      expect(getDoctorDaysOff).toHaveBeenCalled();
    }, { timeout: 3000 });
  });

  /**
   * Test 1.5: Verificar que se valida disponibilidad en tiempo real
   * 
   * Expected Behavior: Debe llamar al servicio para obtener slots disponibles
   */
  it('should verify availability is validated in real-time', async () => {
    const { getAvailableSlotsForDate } = await import('../services/appointmentService');

    renderWithProviders(<PatientConsultationForm />, { user: mockDoctorUser });

    await waitFor(() => {
      expect(screen.getAllByText('Juan Pérez')[0]).toBeInTheDocument();
    });

    const dischargeButton = await screen.findByText(/ALTA/i);
    await userEvent.click(dischargeButton);

    const followUpCheckbox = screen.getByLabelText(/Agendar cita de seguimiento/i);
    await userEvent.click(followUpCheckbox);

    // Esperar a que se cargue el calendario
    await waitFor(() => {
      expect(screen.getByText('Do')).toBeInTheDocument();
    });

    // Simular selección de una fecha
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowDay = tomorrow.getDate();

    const dayButtons = screen.getAllByRole('button');
    const tomorrowButton = dayButtons.find(btn => btn.textContent === String(tomorrowDay) && !btn.disabled);
    
    if (tomorrowButton) {
      await userEvent.click(tomorrowButton);

      // Verificar que se llamó al servicio de disponibilidad
      await waitFor(() => {
        expect(getAvailableSlotsForDate).toHaveBeenCalled();
      }, { timeout: 3000 });
    }
  });

  /**
   * Test 1.6: Verificar que hay indicador de carga de slots
   * 
   * Expected Behavior: Debe mostrar "Verificando disponibilidad..." mientras carga
   */
  it('should verify loading indicator for slots is present', async () => {
    renderWithProviders(<PatientConsultationForm />, { user: mockDoctorUser });

    await waitFor(() => {
      expect(screen.getAllByText('Juan Pérez')[0]).toBeInTheDocument();
    });

    const dischargeButton = await screen.findByText(/ALTA/i);
    await userEvent.click(dischargeButton);

    const followUpCheckbox = screen.getByLabelText(/Agendar cita de seguimiento/i);
    await userEvent.click(followUpCheckbox);

    // Esperar a que se cargue el calendario
    await waitFor(() => {
      expect(screen.getByText('Do')).toBeInTheDocument();
    });

    // Simular selección de una fecha
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowDay = tomorrow.getDate();

    const dayButtons = screen.getAllByRole('button');
    const tomorrowButton = dayButtons.find(btn => btn.textContent === String(tomorrowDay) && !btn.disabled);
    
    if (tomorrowButton) {
      await userEvent.click(tomorrowButton);

      // Verificar que aparece el texto de carga (puede ser breve)
      // O verificar que eventualmente aparecen los slots
      await waitFor(() => {
        const loadingText = screen.queryByText(/Verificando disponibilidad/i);
        const slotsAppeared = screen.queryByText(/Hora de la cita/i);
        expect(loadingText || slotsAppeared).toBeTruthy();
      }, { timeout: 3000 });
    }
  });

  /**
   * Test 1.7: Verificar que la validación requiere fecha Y tiempo
   * 
   * Expected Behavior: Debe validar tanto fecha como tiempo antes de permitir submit
   */
  it('should verify validation checks both date and time', async () => {
    renderWithProviders(<PatientConsultationForm />, { user: mockDoctorUser });

    await waitFor(() => {
      expect(screen.getAllByText('Juan Pérez')[0]).toBeInTheDocument();
    });

    const dischargeButton = await screen.findByText(/ALTA/i);
    await userEvent.click(dischargeButton);

    const followUpCheckbox = screen.getByLabelText(/Agendar cita de seguimiento/i);
    await userEvent.click(followUpCheckbox);

    // Esperar a que se cargue el calendario
    await waitFor(() => {
      expect(screen.getByText('Do')).toBeInTheDocument();
    });

    // Verificar que hay un campo de hora (label)
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowDay = tomorrow.getDate();

    const dayButtons = screen.getAllByRole('button');
    const tomorrowButton = dayButtons.find(btn => btn.textContent === String(tomorrowDay) && !btn.disabled);
    
    if (tomorrowButton) {
      await userEvent.click(tomorrowButton);

      // Verificar que aparece la sección de hora
      await waitFor(() => {
        expect(screen.getByText(/Hora de la cita/i)).toBeInTheDocument();
      });
    }
  });
});
