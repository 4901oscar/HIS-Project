import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import SampleValidationStep from './SampleValidationStep';
import type { AppointmentListItem } from '../../services/appointmentService';
import type { LabOrderWithTestsResponse } from '../../api/labApi';
import * as clinicalApi from '../../api/clinicalApi';

// Mock the clinical API
vi.mock('../../api/clinicalApi', () => ({
  acceptLabSamples: vi.fn(),
  rejectLabSamples: vi.fn(),
}));

describe('SampleValidationStep', () => {
  const mockAppointment: AppointmentListItem = {
    id: 'appt-123',
    appointmentDate: '2024-01-15',
    appointmentTime: '10:00',
    status: 'LAB_SAMPLE_PENDING',
    statusLabel: 'Muestras Pendientes',
    statusColor: 'yellow',
    createdAt: '2024-01-15T08:00:00',
    patient: {
      id: 'patient-123',
      fullName: 'Juan Pérez',
      dpi: '1234567890101',
    },
    doctor: {
      id: 'doctor-123',
      name: 'Dr. García',
    },
    payment: {
      status: 'PAID',
      statusLabel: 'Pagado',
      statusColor: 'green',
      canActivate: true,
      tooltip: 'Pago completado',
    },
    metadata: {
      isToday: true,
      isPast: false,
      isUpcoming: false,
      canEdit: false,
      canCancel: false,
      canActivate: false,
    },
  };

  const mockLabOrder: LabOrderWithTestsResponse = {
    id: 'order-123',
    orderCode: 'LAB-2024-001',
    patientId: 'patient-123',
    doctorId: 'doctor-123',
    appointmentId: 'appt-123',
    tests: [
      {
        testName: 'Hemograma Completo',
        testType: 'Hematología',
        sampleType: 'Sangre',
        hasResult: false,
      },
      {
        testName: 'Glucosa en Ayunas',
        testType: 'Química Clínica',
        sampleType: 'Sangre',
        hasResult: false,
      },
      {
        testName: 'Examen General de Orina',
        testType: 'Urianálisis',
        sampleType: 'Orina',
        hasResult: false,
      },
    ],
    status: 'PENDING',
    orderedAt: '2024-01-15T08:00:00',
  };

  const mockOnRefresh = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Rendering', () => {
    it('should render the step heading', () => {
      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('Paso 2: Validar Muestras')).toBeInTheDocument();
    });

    it('should display the number of pending samples', () => {
      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('Muestras pendientes de validación (3)')).toBeInTheDocument();
    });

    it('should render all tests with their details', () => {
      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      // Check test names
      expect(screen.getByText('Hemograma Completo')).toBeInTheDocument();
      expect(screen.getByText('Glucosa en Ayunas')).toBeInTheDocument();
      expect(screen.getByText('Examen General de Orina')).toBeInTheDocument();

      // Check test types
      expect(screen.getByText('Hematología')).toBeInTheDocument();
      expect(screen.getByText('Química Clínica')).toBeInTheDocument();
      expect(screen.getByText('Urianálisis')).toBeInTheDocument();

      // Check sample types (there are multiple "Sangre" and one "Orina")
      const sangreElements = screen.getAllByText('Sangre');
      expect(sangreElements).toHaveLength(2);
      expect(screen.getByText('Orina')).toBeInTheDocument();
    });

    it('should render both action buttons', () => {
      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });

      expect(acceptButton).toBeInTheDocument();
      expect(rejectButton).toBeInTheDocument();
      expect(acceptButton).not.toBeDisabled();
      expect(rejectButton).not.toBeDisabled();
    });

    it('should display message when there are no tests', () => {
      const emptyLabOrder = { ...mockLabOrder, tests: [] };

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={emptyLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('No hay exámenes en esta orden')).toBeInTheDocument();
    });

    it('should disable both buttons when there are no tests', () => {
      const emptyLabOrder = { ...mockLabOrder, tests: [] };

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={emptyLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });

      expect(acceptButton).toBeDisabled();
      expect(rejectButton).toBeDisabled();
    });
  });

  describe('Accept Samples', () => {
    it('should call acceptLabSamples API when accept button is clicked', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockResolvedValueOnce({
        id: 'appt-123',
        patientId: 'patient-123',
        doctorId: 'doctor-123',
        appointmentDate: '2024-01-15',
        appointmentTime: '10:00',
        status: 'LAB_PROCESSING',
        createdAt: '2024-01-15T08:00:00',
      });

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        expect(mockAcceptLabSamples).toHaveBeenCalledWith('appt-123');
      });
    });

    it('should call onRefresh after successful sample acceptance', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockResolvedValueOnce({
        id: 'appt-123',
        patientId: 'patient-123',
        doctorId: 'doctor-123',
        appointmentDate: '2024-01-15',
        appointmentTime: '10:00',
        status: 'LAB_PROCESSING',
        createdAt: '2024-01-15T08:00:00',
      });

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        expect(mockOnRefresh).toHaveBeenCalledTimes(1);
      });
    });

    it('should disable both buttons during accept API request', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100))
      );

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      
      fireEvent.click(acceptButton);

      // Both buttons should be disabled immediately
      expect(acceptButton).toBeDisabled();
      expect(rejectButton).toBeDisabled();

      // Wait for the request to complete
      await waitFor(() => {
        expect(mockAcceptLabSamples).toHaveBeenCalled();
      });
    });

    it('should show loading indicator on accept button during API request', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100))
      );

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      // Loading text should appear
      expect(screen.getByText('Aceptando...')).toBeInTheDocument();

      // Wait for the request to complete
      await waitFor(() => {
        expect(mockAcceptLabSamples).toHaveBeenCalled();
      });
    });
  });

  describe('Reject Samples', () => {
    it('should call rejectLabSamples API when reject button is clicked', async () => {
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      mockRejectLabSamples.mockResolvedValueOnce({
        id: 'appt-123',
        patientId: 'patient-123',
        doctorId: 'doctor-123',
        appointmentDate: '2024-01-15',
        appointmentTime: '10:00',
        status: 'LAB_SAMPLE_COLLECTION',
        createdAt: '2024-01-15T08:00:00',
      });

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      fireEvent.click(rejectButton);

      await waitFor(() => {
        expect(mockRejectLabSamples).toHaveBeenCalledWith('appt-123');
      });
    });

    it('should call onRefresh after successful sample rejection', async () => {
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      mockRejectLabSamples.mockResolvedValueOnce({
        id: 'appt-123',
        patientId: 'patient-123',
        doctorId: 'doctor-123',
        appointmentDate: '2024-01-15',
        appointmentTime: '10:00',
        status: 'LAB_SAMPLE_COLLECTION',
        createdAt: '2024-01-15T08:00:00',
      });

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      fireEvent.click(rejectButton);

      await waitFor(() => {
        expect(mockOnRefresh).toHaveBeenCalledTimes(1);
      });
    });

    it('should disable both buttons during reject API request', async () => {
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      mockRejectLabSamples.mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100))
      );

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      
      fireEvent.click(rejectButton);

      // Both buttons should be disabled immediately
      expect(acceptButton).toBeDisabled();
      expect(rejectButton).toBeDisabled();

      // Wait for the request to complete
      await waitFor(() => {
        expect(mockRejectLabSamples).toHaveBeenCalled();
      });
    });

    it('should show loading indicator on reject button during API request', async () => {
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      mockRejectLabSamples.mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100))
      );

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      fireEvent.click(rejectButton);

      // Loading text should appear
      expect(screen.getByText('Solicitando...')).toBeInTheDocument();

      // Wait for the request to complete
      await waitFor(() => {
        expect(mockRejectLabSamples).toHaveBeenCalled();
      });
    });
  });

  describe('Error Handling - Accept Samples', () => {
    it('should display error message when accept API call fails', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockRejectedValueOnce(
        new Error('Transición de estado inválida')
      );

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        expect(screen.getByText('Transición de estado inválida')).toBeInTheDocument();
      });
    });

    it('should display generic error message for unknown accept errors', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockRejectedValueOnce('Unknown error');

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        expect(
          screen.getByText('Error al aceptar muestras. Por favor, intente nuevamente.')
        ).toBeInTheDocument();
      });
    });

    it('should log error to console when accept API call fails', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      const testError = new Error('Test error');
      mockAcceptLabSamples.mockRejectedValueOnce(testError);

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          'Error al aceptar muestras:',
          testError
        );
      });

      consoleErrorSpy.mockRestore();
    });

    it('should not call onRefresh when accept API call fails', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockRejectedValueOnce(new Error('Test error'));

      const localMockOnRefresh = vi.fn();

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={localMockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        expect(screen.getByText('Test error')).toBeInTheDocument();
      });

      expect(localMockOnRefresh).not.toHaveBeenCalled();
    });
  });

  describe('Error Handling - Reject Samples', () => {
    it('should display error message when reject API call fails', async () => {
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      mockRejectLabSamples.mockRejectedValueOnce(
        new Error('Transición de estado inválida')
      );

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      fireEvent.click(rejectButton);

      await waitFor(() => {
        expect(screen.getByText('Transición de estado inválida')).toBeInTheDocument();
      });
    });

    it('should display generic error message for unknown reject errors', async () => {
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      mockRejectLabSamples.mockRejectedValueOnce('Unknown error');

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      fireEvent.click(rejectButton);

      await waitFor(() => {
        expect(
          screen.getByText('Error al solicitar nueva muestra. Por favor, intente nuevamente.')
        ).toBeInTheDocument();
      });
    });

    it('should log error to console when reject API call fails', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      const testError = new Error('Test error');
      mockRejectLabSamples.mockRejectedValueOnce(testError);

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      fireEvent.click(rejectButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          'Error al solicitar nueva muestra:',
          testError
        );
      });

      consoleErrorSpy.mockRestore();
    });

    it('should not call onRefresh when reject API call fails', async () => {
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      mockRejectLabSamples.mockRejectedValueOnce(new Error('Test error'));

      const localMockOnRefresh = vi.fn();

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={localMockOnRefresh}
        />
      );

      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      fireEvent.click(rejectButton);

      await waitFor(() => {
        expect(screen.getByText('Test error')).toBeInTheDocument();
      });

      expect(localMockOnRefresh).not.toHaveBeenCalled();
    });
  });

  describe('Error Message Dismissal', () => {
    it('should allow dismissing error message', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockRejectedValueOnce(new Error('Test error'));

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        expect(screen.getByText('Test error')).toBeInTheDocument();
      });

      // Click the close button
      const closeButton = screen.getByRole('button', { name: /cerrar mensaje/i });
      fireEvent.click(closeButton);

      // Wait for exit animation (300ms) before checking if error is removed
      await waitFor(() => {
        expect(screen.queryByText('Test error')).not.toBeInTheDocument();
      }, { timeout: 500 });
    });

    it('should clear previous error when new action is attempted', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockRejectedValueOnce(new Error('First error'));

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        expect(screen.getByText('First error')).toBeInTheDocument();
      });

      // Now try reject action
      const mockRejectLabSamples = vi.mocked(clinicalApi.rejectLabSamples);
      mockRejectLabSamples.mockResolvedValueOnce({
        id: 'appt-123',
        patientId: 'patient-123',
        doctorId: 'doctor-123',
        appointmentDate: '2024-01-15',
        appointmentTime: '10:00',
        status: 'LAB_SAMPLE_COLLECTION',
        createdAt: '2024-01-15T08:00:00',
      });

      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });
      fireEvent.click(rejectButton);

      // First error should be cleared immediately
      expect(screen.queryByText('First error')).not.toBeInTheDocument();
    });
  });

  describe('Test Information Completeness', () => {
    it('should display all required fields for each test', () => {
      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      mockLabOrder.tests.forEach((test) => {
        // Test name should be visible
        expect(screen.getByText(test.testName)).toBeInTheDocument();

        // Test type should be visible
        expect(screen.getByText(test.testType)).toBeInTheDocument();

        // Sample type should be visible
        const sampleTypeElements = screen.getAllByText(test.sampleType);
        expect(sampleTypeElements.length).toBeGreaterThan(0);
      });
    });

    it('should render test with all fields even if some are empty strings', () => {
      const labOrderWithEmptyFields: LabOrderWithTestsResponse = {
        ...mockLabOrder,
        tests: [
          {
            testName: 'Test Name',
            testType: '',
            sampleType: '',
            hasResult: false,
          },
        ],
      };

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={labOrderWithEmptyFields}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('Test Name')).toBeInTheDocument();
      // Empty strings should still render the labels
      expect(screen.getByText('Tipo:')).toBeInTheDocument();
      expect(screen.getByText('Muestra:')).toBeInTheDocument();
    });
  });

  describe('Button Visual Styles', () => {
    it('should have different visual styles for accept and reject buttons', () => {
      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });

      // Accept button should have purple background (primary)
      expect(acceptButton).toHaveClass('bg-purple-600');
      expect(acceptButton).toHaveClass('text-white');

      // Reject button should have red border and white background (secondary)
      expect(rejectButton).toHaveClass('bg-white');
      expect(rejectButton).toHaveClass('text-red-600');
      expect(rejectButton).toHaveClass('border-red-600');
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels for both buttons', () => {
      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      const rejectButton = screen.getByRole('button', { name: /solicitar nueva muestra/i });

      expect(acceptButton).toHaveAttribute('aria-label', 'Aceptar muestras');
      expect(rejectButton).toHaveAttribute('aria-label', 'Solicitar nueva muestra');
    });

    it('should have proper ARIA label for error close button', async () => {
      const mockAcceptLabSamples = vi.mocked(clinicalApi.acceptLabSamples);
      mockAcceptLabSamples.mockRejectedValueOnce(new Error('Test error'));

      render(
        <SampleValidationStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const acceptButton = screen.getByRole('button', { name: /aceptar muestras/i });
      fireEvent.click(acceptButton);

      await waitFor(() => {
        const closeButton = screen.getByRole('button', {
          name: /cerrar mensaje/i,
        });
        expect(closeButton).toHaveAttribute('aria-label', 'Cerrar mensaje');
      });
    });
  });
});
