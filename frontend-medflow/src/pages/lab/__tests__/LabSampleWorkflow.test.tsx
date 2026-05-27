import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LabSampleWorkflow from '../LabSampleWorkflow';
import * as appointmentService from '../../../services/appointmentService';
import * as labApi from '../../../api/labApi';
import { AuthProvider } from '../../../context/AuthContext';

// Mock the services
vi.mock('../../../services/appointmentService');
vi.mock('../../../api/labApi');

// Mock auth service
vi.mock('../../../services/authService', () => ({
  getCurrentUser: () => ({ id: 'user-1', name: 'Test User', role: 'LABORATORY' }),
  isAuthenticated: () => true,
}));

// Mock useParams
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ appointmentId: 'test-appointment-123' }),
    useNavigate: () => vi.fn(),
  };
});

// Helper to render with providers
const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <AuthProvider>
      <BrowserRouter>
        {component}
      </BrowserRouter>
    </AuthProvider>
  );
};

describe('LabSampleWorkflow', () => {
  const mockAppointment: appointmentService.AppointmentListItem = {
    id: 'test-appointment-123',
    appointmentDate: '2024-01-15',
    appointmentTime: '10:00',
    status: 'LAB_SAMPLE_COLLECTION',
    statusLabel: 'Recolección de Muestras',
    statusColor: 'bg-yellow-100',
    notes: '',
    createdAt: '2024-01-15T08:00:00',
    patient: {
      id: 'patient-1',
      fullName: 'Juan Pérez',
      dpi: '1234567890101',
    },
    doctor: {
      id: 'doctor-1',
      name: 'Dr. García',
    },
    payment: {
      status: 'PAID',
      statusLabel: 'Pagado',
      statusColor: 'bg-green-100',
      canActivate: true,
      tooltip: 'Pagado',
    },
    metadata: {
      isToday: true,
      isPast: false,
      isUpcoming: false,
      canEdit: false,
      canCancel: false,
      canActivate: true,
    },
  };

  const mockLabOrder: labApi.LabOrderWithTestsResponse = {
    id: 'order-123',
    orderCode: 'LAB-2024-001',
    patientId: 'patient-1',
    doctorId: 'doctor-1',
    appointmentId: 'test-appointment-123',
    tests: [
      {
        testName: 'Hemograma Completo',
        testType: 'HEMATOLOGY',
        sampleType: 'Sangre',
        hasResult: false,
      },
      {
        testName: 'Glucosa',
        testType: 'CHEMISTRY',
        sampleType: 'Sangre',
        hasResult: false,
      },
    ],
    status: 'PENDING',
    orderedAt: '2024-01-15T08:00:00',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders loading state initially', () => {
    vi.mocked(appointmentService.getAppointmentById).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    renderWithProviders(<LabSampleWorkflow />);

    expect(screen.getByText('Cargando flujo de laboratorio...')).toBeInTheDocument();
  });

  it('loads appointment and lab order data on mount', async () => {
    vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(mockAppointment);
    vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      expect(appointmentService.getAppointmentById).toHaveBeenCalledWith('test-appointment-123');
      expect(labApi.getLabOrderByAppointmentId).toHaveBeenCalledWith('test-appointment-123');
    });
  });

  it('displays patient information in header', async () => {
    vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(mockAppointment);
    vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
      expect(screen.getByText('1234567890101')).toBeInTheDocument();
    });
  });

  it('maps LAB_SAMPLE_COLLECTION status to step 1', async () => {
    const appointment = { ...mockAppointment, status: 'LAB_SAMPLE_COLLECTION' };
    vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(appointment);
    vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
    });
  });

  it('maps LAB_SAMPLE_PENDING status to step 2', async () => {
    const appointment = { ...mockAppointment, status: 'LAB_SAMPLE_PENDING' };
    vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(appointment);
    vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      expect(screen.getByText('Paso 2: Validar Muestras')).toBeInTheDocument();
    });
  });

  it('maps LAB_PROCESSING status to step 3', async () => {
    const appointment = { ...mockAppointment, status: 'LAB_PROCESSING' };
    vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(appointment);
    vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      expect(screen.getByText('Paso 3: Procesar Exámenes')).toBeInTheDocument();
    });
  });

  it('maps LAB_RESULTS_READY status to step 4', async () => {
    const appointment = { ...mockAppointment, status: 'LAB_RESULTS_READY' };
    vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(appointment);
    vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      expect(screen.getByText('Paso 4: Resultados Listos')).toBeInTheDocument();
    });
  });

  it('displays error message when appointment fetch fails', async () => {
    vi.mocked(appointmentService.getAppointmentById).mockRejectedValue(
      new Error('Cita no encontrada')
    );

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      expect(screen.getByText('Cita no encontrada')).toBeInTheDocument();
      expect(screen.getByText('Reintentar')).toBeInTheDocument();
      expect(screen.getByText('Volver a la lista')).toBeInTheDocument();
    });
  });

  it('displays error message when lab order fetch fails', async () => {
    vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(mockAppointment);
    vi.mocked(labApi.getLabOrderByAppointmentId).mockRejectedValue(
      new Error('Orden de laboratorio no encontrada')
    );

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      expect(screen.getByText('Orden de laboratorio no encontrada')).toBeInTheDocument();
    });
  });

  it('renders WizardProgressBar with correct current step', async () => {
    const appointment = { ...mockAppointment, status: 'LAB_PROCESSING' };
    vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(appointment);
    vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

    renderWithProviders(<LabSampleWorkflow />);

    await waitFor(() => {
      // Check that step 3 is marked as current
      const step3 = screen.getByLabelText('Paso 3: Procesar Exámenes');
      expect(step3).toHaveAttribute('aria-current', 'step');
    });
  });
});
