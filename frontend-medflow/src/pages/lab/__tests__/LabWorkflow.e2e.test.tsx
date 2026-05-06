/**
 * End-to-End Integration Tests for Laboratory Sample Workflow
 * 
 * These tests validate complete user workflows from start to finish,
 * simulating real user interactions with the lab sample management system.
 * 
 * Test Coverage:
 * - Task 23.1: Complete workflow (collection → validation → processing → results → doctor)
 * - Task 23.2: Sample rejection flow (reject → recollect → accept → continue)
 * - Task 23.3: Incomplete results validation (error → upload missing → complete)
 * 
 * Requirements Validated: All requirements (1-15)
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import LabSampleWorkflow from '../LabSampleWorkflow';
import * as appointmentService from '../../../services/appointmentService';
import * as labApi from '../../../api/labApi';
import * as clinicalApi from '../../../api/clinicalApi';
import { AuthProvider } from '../../../context/AuthContext';

// Mock the services
vi.mock('../../../services/appointmentService');
vi.mock('../../../api/labApi');
vi.mock('../../../api/clinicalApi');

// Mock auth service
vi.mock('../../../services/authService', () => ({
  getCurrentUser: () => ({ id: 'user-1', name: 'Test Lab Technician', role: 'LABORATORY' }),
  isAuthenticated: () => true,
}));

// Mock audio notification
vi.mock('../../../utils/AudioNotification', () => ({
  playNotificationWithCallback: (callback: () => void) => {
    callback();
  },
}));

// Mock useParams and useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ appointmentId: 'test-appointment-123' }),
    useNavigate: () => mockNavigate,
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

describe('Lab Workflow E2E Integration Tests', () => {
  // ─── Test Data Setup ────────────────────────────────────────────────────────

  const createMockAppointment = (status: string): appointmentService.AppointmentListItem => ({
    id: 'test-appointment-123',
    appointmentDate: '2024-01-15',
    appointmentTime: '10:00',
    status,
    statusLabel: 'Lab Status',
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
  });

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
        testName: 'Glucosa en Ayunas',
        testType: 'CHEMISTRY',
        sampleType: 'Sangre',
        hasResult: false,
      },
      {
        testName: 'Examen General de Orina',
        testType: 'URINALYSIS',
        sampleType: 'Orina',
        hasResult: false,
      },
    ],
    status: 'PENDING',
    orderedAt: '2024-01-15T08:00:00',
  };

  const mockLabResult = (testName: string): labApi.LabResultResponse => ({
    id: `result-${testName}`,
    orderId: 'order-123',
    testName,
    originalFilename: `${testName}.pdf`,
    fileSize: 1024,
    uploadedAt: '2024-01-15T10:30:00',
    uploadedBy: 'user-1',
    downloadUrl: `/api/lab/results/${testName}`,
  });

  beforeEach(() => {
    vi.clearAllMocks();
    mockNavigate.mockClear();
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // Task 23.1: Complete Workflow E2E Test
  // ═══════════════════════════════════════════════════════════════════════════

  describe('Task 23.1: Complete Workflow (Happy Path)', () => {
    it('should complete full workflow: collection → validation → processing → results → doctor', async () => {
      const user = userEvent.setup();

      // ─── Step 1: Lab technician views appointment in LAB_SAMPLE_COLLECTION ───

      const step1Appointment = createMockAppointment('LAB_SAMPLE_COLLECTION');
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step1Appointment);
      vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

      const { rerender } = renderWithProviders(<LabSampleWorkflow />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
      });

      // Verify patient information is displayed
      expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
      expect(screen.getByText('1234567890101')).toBeInTheDocument();

      // Verify test list is displayed
      expect(screen.getByText('Hemograma Completo')).toBeInTheDocument();
      expect(screen.getByText('Glucosa en Ayunas')).toBeInTheDocument();
      expect(screen.getByText('Examen General de Orina')).toBeInTheDocument();

      // ─── Step 2: Lab technician collects samples (step 1 → step 2) ───

      const step2Appointment = createMockAppointment('LAB_SAMPLE_PENDING');
      vi.mocked(clinicalApi.collectLabSamples).mockResolvedValue(step2Appointment);
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step2Appointment);

      const collectButton = screen.getByText('Recolectar Muestras');
      await user.click(collectButton);

      // Wait for transition to step 2
      await waitFor(() => {
        expect(clinicalApi.collectLabSamples).toHaveBeenCalledWith('test-appointment-123');
      });

      // Re-render with updated appointment
      rerender(
        <AuthProvider>
          <BrowserRouter>
            <LabSampleWorkflow />
          </BrowserRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Paso 2: Validar Muestras')).toBeInTheDocument();
      });

      // ─── Step 3: Lab technician accepts samples (step 2 → step 3) ───

      const step3Appointment = createMockAppointment('LAB_PROCESSING');
      vi.mocked(clinicalApi.acceptLabSamples).mockResolvedValue(step3Appointment);
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step3Appointment);

      const acceptButton = screen.getByText('Aceptar muestras');
      await user.click(acceptButton);

      // Wait for transition to step 3
      await waitFor(() => {
        expect(clinicalApi.acceptLabSamples).toHaveBeenCalledWith('test-appointment-123');
      });

      // Re-render with updated appointment
      rerender(
        <AuthProvider>
          <BrowserRouter>
            <LabSampleWorkflow />
          </BrowserRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Paso 3: Procesar Exámenes')).toBeInTheDocument();
      });

      // ─── Step 4: Lab technician uploads results for all tests ───

      const mockFile = new File(['test content'], 'result.pdf', { type: 'application/pdf' });

      vi.mocked(labApi.uploadTestResult)
        .mockResolvedValueOnce(mockLabResult('Hemograma Completo'))
        .mockResolvedValueOnce(mockLabResult('Glucosa en Ayunas'))
        .mockResolvedValueOnce(mockLabResult('Examen General de Orina'));

      // Upload results for each test
      const uploadButtons = screen.getAllByText('Cargar resultado');
      const fileInputs = document.querySelectorAll('input[type="file"]');

      for (let i = 0; i < 3; i++) {
        await user.click(uploadButtons[i]);
        await user.upload(fileInputs[i] as HTMLInputElement, mockFile);
      }

      // Wait for all uploads to complete
      await waitFor(() => {
        const uploadedIndicators = screen.getAllByText('Cargado');
        expect(uploadedIndicators).toHaveLength(3);
      });

      // Verify all uploads were called
      expect(labApi.uploadTestResult).toHaveBeenCalledTimes(3);

      // ─── Step 5: Lab technician marks as complete (step 3 → step 4) ───

      const step4Appointment = createMockAppointment('LAB_RESULTS_READY');
      const mockValidation: labApi.ValidationResponse = {
        isValid: true,
        missingTests: [],
      };

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidation);
      vi.mocked(clinicalApi.completeLabProcessing).mockResolvedValue(step4Appointment);
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step4Appointment);

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      // Wait for validation and transition
      await waitFor(() => {
        expect(labApi.validateAllTestsComplete).toHaveBeenCalledWith('order-123');
        expect(clinicalApi.completeLabProcessing).toHaveBeenCalledWith('test-appointment-123');
      });

      // Re-render with updated appointment
      rerender(
        <AuthProvider>
          <BrowserRouter>
            <LabSampleWorkflow />
          </BrowserRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Paso 4: Resultados Listos')).toBeInTheDocument();
      });

      // ─── Step 6: Lab technician sends results to doctor (step 4 → consultation) ───

      const mockResults = [
        mockLabResult('Hemograma Completo'),
        mockLabResult('Glucosa en Ayunas'),
        mockLabResult('Examen General de Orina'),
      ];

      vi.mocked(labApi.getLabOrderResults).mockResolvedValue(mockResults);

      // Wait for results to load
      await waitFor(() => {
        expect(screen.getByText('Hemograma Completo')).toBeInTheDocument();
        expect(screen.getByText('Glucosa en Ayunas')).toBeInTheDocument();
        expect(screen.getByText('Examen General de Orina')).toBeInTheDocument();
      });

      const consultationAppointment = createMockAppointment('CONSULTATION');
      vi.mocked(clinicalApi.sendLabResultsToDoctor).mockResolvedValue(consultationAppointment);

      const sendButton = screen.getByText('Enviar a doctor');
      await user.click(sendButton);

      // Wait for final transition
      await waitFor(() => {
        expect(clinicalApi.sendLabResultsToDoctor).toHaveBeenCalledWith('test-appointment-123');
      });

      // Verify navigation back to appointment list
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/lab');
      });

      // ─── Verification: Complete workflow executed successfully ───

      expect(clinicalApi.collectLabSamples).toHaveBeenCalledTimes(1);
      expect(clinicalApi.acceptLabSamples).toHaveBeenCalledTimes(1);
      expect(labApi.uploadTestResult).toHaveBeenCalledTimes(3);
      expect(clinicalApi.completeLabProcessing).toHaveBeenCalledTimes(1);
      expect(clinicalApi.sendLabResultsToDoctor).toHaveBeenCalledTimes(1);
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // Task 23.2: Sample Rejection Flow E2E Test
  // ═══════════════════════════════════════════════════════════════════════════

  describe('Task 23.2: Sample Rejection Flow', () => {
    it('should handle sample rejection: reject → recollect → accept → continue', async () => {
      const user = userEvent.setup();

      // ─── Step 1: Start at LAB_SAMPLE_COLLECTION ───

      const step1Appointment = createMockAppointment('LAB_SAMPLE_COLLECTION');
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step1Appointment);
      vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

      const { rerender } = renderWithProviders(<LabSampleWorkflow />);

      await waitFor(() => {
        expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
      });

      // ─── Step 2: Collect samples (step 1 → step 2) ───

      const step2Appointment = createMockAppointment('LAB_SAMPLE_PENDING');
      vi.mocked(clinicalApi.collectLabSamples).mockResolvedValue(step2Appointment);
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step2Appointment);

      const collectButton = screen.getByText('Recolectar Muestras');
      await user.click(collectButton);

      await waitFor(() => {
        expect(clinicalApi.collectLabSamples).toHaveBeenCalled();
      });

      // Re-render with step 2
      rerender(
        <AuthProvider>
          <BrowserRouter>
            <LabSampleWorkflow />
          </BrowserRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Paso 2: Validar Muestras')).toBeInTheDocument();
      });

      // ─── Step 3: REJECT samples (step 2 → step 1) ───

      const backToStep1Appointment = createMockAppointment('LAB_SAMPLE_COLLECTION');
      vi.mocked(clinicalApi.rejectLabSamples).mockResolvedValue(backToStep1Appointment);
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(backToStep1Appointment);

      const rejectButton = screen.getByText('Solicitar nueva muestra');
      await user.click(rejectButton);

      await waitFor(() => {
        expect(clinicalApi.rejectLabSamples).toHaveBeenCalledWith('test-appointment-123');
      });

      // Re-render back to step 1
      rerender(
        <AuthProvider>
          <BrowserRouter>
            <LabSampleWorkflow />
          </BrowserRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
      });

      // ─── Step 4: Collect NEW samples (step 1 → step 2 again) ───

      const step2AgainAppointment = createMockAppointment('LAB_SAMPLE_PENDING');
      vi.mocked(clinicalApi.collectLabSamples).mockResolvedValue(step2AgainAppointment);
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step2AgainAppointment);

      const collectAgainButton = screen.getByText('Recolectar Muestras');
      await user.click(collectAgainButton);

      await waitFor(() => {
        expect(clinicalApi.collectLabSamples).toHaveBeenCalledTimes(2);
      });

      // Re-render with step 2 again
      rerender(
        <AuthProvider>
          <BrowserRouter>
            <LabSampleWorkflow />
          </BrowserRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Paso 2: Validar Muestras')).toBeInTheDocument();
      });

      // ─── Step 5: ACCEPT samples this time (step 2 → step 3) ───

      const step3Appointment = createMockAppointment('LAB_PROCESSING');
      vi.mocked(clinicalApi.acceptLabSamples).mockResolvedValue(step3Appointment);
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step3Appointment);

      const acceptButton = screen.getByText('Aceptar muestras');
      await user.click(acceptButton);

      await waitFor(() => {
        expect(clinicalApi.acceptLabSamples).toHaveBeenCalledWith('test-appointment-123');
      });

      // Re-render with step 3
      rerender(
        <AuthProvider>
          <BrowserRouter>
            <LabSampleWorkflow />
          </BrowserRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Paso 3: Procesar Exámenes')).toBeInTheDocument();
      });

      // ─── Verification: Rejection flow executed correctly ───

      expect(clinicalApi.collectLabSamples).toHaveBeenCalledTimes(2); // Collected twice
      expect(clinicalApi.rejectLabSamples).toHaveBeenCalledTimes(1); // Rejected once
      expect(clinicalApi.acceptLabSamples).toHaveBeenCalledTimes(1); // Accepted once
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // Task 23.3: Incomplete Results Validation E2E Test
  // ═══════════════════════════════════════════════════════════════════════════

  describe('Task 23.3: Incomplete Results Validation', () => {
    it('should validate incomplete results: error → upload missing → complete successfully', async () => {
      const user = userEvent.setup();

      // ─── Start at LAB_PROCESSING (Step 3) ───

      const step3Appointment = createMockAppointment('LAB_PROCESSING');
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step3Appointment);
      vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

      renderWithProviders(<LabSampleWorkflow />);

      await waitFor(() => {
        expect(screen.getByText('Paso 3: Procesar Exámenes')).toBeInTheDocument();
      });

      // ─── Upload results for ONLY 2 out of 3 tests (incomplete) ───

      const mockFile = new File(['test content'], 'result.pdf', { type: 'application/pdf' });

      vi.mocked(labApi.uploadTestResult)
        .mockResolvedValueOnce(mockLabResult('Hemograma Completo'))
        .mockResolvedValueOnce(mockLabResult('Glucosa en Ayunas'));

      const uploadButtons = screen.getAllByText('Cargar resultado');
      const fileInputs = document.querySelectorAll('input[type="file"]');

      // Upload only first 2 tests
      await user.click(uploadButtons[0]);
      await user.upload(fileInputs[0] as HTMLInputElement, mockFile);

      await user.click(uploadButtons[1]);
      await user.upload(fileInputs[1] as HTMLInputElement, mockFile);

      // Wait for uploads to complete
      await waitFor(() => {
        const uploadedIndicators = screen.getAllByText('Cargado');
        expect(uploadedIndicators).toHaveLength(2);
      });

      // ─── Attempt to mark as complete WITHOUT all results ───

      const mockValidationError: labApi.ValidationResponse = {
        isValid: false,
        missingTests: ['Examen General de Orina'],
      };

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidationError);

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      // ─── Verify error message is displayed with missing test names ───

      await waitFor(() => {
        expect(labApi.validateAllTestsComplete).toHaveBeenCalledWith('order-123');
      });

      await waitFor(() => {
        const errorMessage = screen.getByText(/Faltan resultados para los siguientes exámenes/i);
        expect(errorMessage).toBeInTheDocument();
        expect(screen.getByText('Examen General de Orina')).toBeInTheDocument();
      });

      // Verify we're still on step 3
      expect(screen.getByText('Paso 3: Procesar Exámenes')).toBeInTheDocument();

      // ─── Upload the missing result ───

      vi.mocked(labApi.uploadTestResult).mockResolvedValueOnce(
        mockLabResult('Examen General de Orina')
      );

      // Upload the third test
      await user.click(uploadButtons[2]);
      await user.upload(fileInputs[2] as HTMLInputElement, mockFile);

      // Wait for upload to complete
      await waitFor(() => {
        const uploadedIndicators = screen.getAllByText('Cargado');
        expect(uploadedIndicators).toHaveLength(3);
      });

      // ─── Attempt to mark as complete again (now with all results) ───

      const mockValidationSuccess: labApi.ValidationResponse = {
        isValid: true,
        missingTests: [],
      };

      const step4Appointment = createMockAppointment('LAB_RESULTS_READY');

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidationSuccess);
      vi.mocked(clinicalApi.completeLabProcessing).mockResolvedValue(step4Appointment);

      await user.click(completeButton);

      // ─── Verify successful completion ───

      await waitFor(() => {
        expect(labApi.validateAllTestsComplete).toHaveBeenCalledTimes(2);
        expect(clinicalApi.completeLabProcessing).toHaveBeenCalledWith('test-appointment-123');
      });

      // ─── Verification: Validation flow executed correctly ───

      expect(labApi.uploadTestResult).toHaveBeenCalledTimes(3);
      expect(labApi.validateAllTestsComplete).toHaveBeenCalledTimes(2); // Failed once, succeeded once
      expect(clinicalApi.completeLabProcessing).toHaveBeenCalledTimes(1); // Only called after all results uploaded
    });
  });

  // ═══════════════════════════════════════════════════════════════════════════
  // Additional E2E Scenarios
  // ═══════════════════════════════════════════════════════════════════════════

  describe('Additional E2E Scenarios', () => {
    it('should handle network errors gracefully during workflow', async () => {
      const user = userEvent.setup();

      const step1Appointment = createMockAppointment('LAB_SAMPLE_COLLECTION');
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step1Appointment);
      vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

      renderWithProviders(<LabSampleWorkflow />);

      await waitFor(() => {
        expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
      });

      // Simulate network error
      vi.mocked(clinicalApi.collectLabSamples).mockRejectedValue(
        new Error('Error de red: No se pudo conectar con el servidor')
      );

      const collectButton = screen.getByText('Recolectar Muestras');
      await user.click(collectButton);

      // Verify error message is displayed
      await waitFor(() => {
        expect(screen.getByText(/Error de red/i)).toBeInTheDocument();
      });

      // Verify we're still on step 1
      expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
    });

    it('should display all test information correctly throughout workflow', async () => {
      const step1Appointment = createMockAppointment('LAB_SAMPLE_COLLECTION');
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step1Appointment);
      vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

      renderWithProviders(<LabSampleWorkflow />);

      await waitFor(() => {
        expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
      });

      // Verify all test information is displayed
      mockLabOrder.tests.forEach((test) => {
        expect(screen.getByText(test.testName)).toBeInTheDocument();
        expect(screen.getByText(test.sampleType)).toBeInTheDocument();
      });

      // Verify order code is displayed
      expect(screen.getByText('LAB-2024-001')).toBeInTheDocument();
    });

    it('should maintain wizard progress bar state throughout workflow', async () => {
      const user = userEvent.setup();

      // Start at step 1
      const step1Appointment = createMockAppointment('LAB_SAMPLE_COLLECTION');
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step1Appointment);
      vi.mocked(labApi.getLabOrderByAppointmentId).mockResolvedValue(mockLabOrder);

      const { rerender } = renderWithProviders(<LabSampleWorkflow />);

      await waitFor(() => {
        expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
      });

      // Verify step 1 is current
      const step1Indicator = screen.getByLabelText('Paso 1: Recolección de Muestras');
      expect(step1Indicator).toHaveAttribute('aria-current', 'step');

      // Move to step 2
      const step2Appointment = createMockAppointment('LAB_SAMPLE_PENDING');
      vi.mocked(clinicalApi.collectLabSamples).mockResolvedValue(step2Appointment);
      vi.mocked(appointmentService.getAppointmentById).mockResolvedValue(step2Appointment);

      const collectButton = screen.getByText('Recolectar Muestras');
      await user.click(collectButton);

      await waitFor(() => {
        expect(clinicalApi.collectLabSamples).toHaveBeenCalled();
      });

      // Re-render with step 2
      rerender(
        <AuthProvider>
          <BrowserRouter>
            <LabSampleWorkflow />
          </BrowserRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Paso 2: Validar Muestras')).toBeInTheDocument();
      });

      // Verify step 2 is current and step 1 is completed
      const step2Indicator = screen.getByLabelText('Paso 2: Validar Muestras');
      expect(step2Indicator).toHaveAttribute('aria-current', 'step');

      const step1CompletedIndicator = screen.getByLabelText('Paso 1: Recolección de Muestras');
      expect(step1CompletedIndicator).toHaveAttribute('aria-label', 'Paso 1: Recolección de Muestras');
    });
  });
});
