import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import SampleCollectionStep from './SampleCollectionStep';
import type { AppointmentListItem } from '../../services/appointmentService';
import type { LabOrderWithTestsResponse } from '../../api/labApi';
import * as clinicalApi from '../../api/clinicalApi';

// Mock the clinical API
vi.mock('../../api/clinicalApi', () => ({
  collectLabSamples: vi.fn(),
}));

describe('SampleCollectionStep', () => {
  const mockAppointment: AppointmentListItem = {
    id: 'appt-123',
    appointmentDate: '2024-01-15',
    appointmentTime: '10:00',
    status: 'LAB_SAMPLE_COLLECTION',
    statusLabel: 'Recolección de Muestras',
    statusColor: 'purple',
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
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('Paso 1: Recolección de Muestras')).toBeInTheDocument();
    });

    it('should display the number of tests', () => {
      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('Exámenes solicitados (3)')).toBeInTheDocument();
    });

    it('should render all tests with their details', () => {
      render(
        <SampleCollectionStep
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

    it('should render the "Recolectar Muestras" button', () => {
      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      expect(button).toBeInTheDocument();
      expect(button).not.toBeDisabled();
    });

    it('should display message when there are no tests', () => {
      const emptyLabOrder = { ...mockLabOrder, tests: [] };

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={emptyLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('No hay exámenes en esta orden')).toBeInTheDocument();
    });

    it('should disable button when there are no tests', () => {
      const emptyLabOrder = { ...mockLabOrder, tests: [] };

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={emptyLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      expect(button).toBeDisabled();
    });
  });

  describe('Sample Collection', () => {
    it('should call collectLabSamples API when button is clicked', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockResolvedValueOnce({
        id: 'appt-123',
        patientId: 'patient-123',
        doctorId: 'doctor-123',
        appointmentDate: '2024-01-15',
        appointmentTime: '10:00',
        status: 'LAB_SAMPLE_PENDING',
        createdAt: '2024-01-15T08:00:00',
      });

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockCollectLabSamples).toHaveBeenCalledWith('appt-123');
      });
    });

    it('should call onRefresh after successful sample collection', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockResolvedValueOnce({
        id: 'appt-123',
        patientId: 'patient-123',
        doctorId: 'doctor-123',
        appointmentDate: '2024-01-15',
        appointmentTime: '10:00',
        status: 'LAB_SAMPLE_PENDING',
        createdAt: '2024-01-15T08:00:00',
      });

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      await waitFor(() => {
        expect(mockOnRefresh).toHaveBeenCalledTimes(1);
      });
    });

    it('should disable button during API request', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100))
      );

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      // Button should be disabled immediately
      expect(button).toBeDisabled();

      // Wait for the request to complete
      await waitFor(() => {
        expect(mockCollectLabSamples).toHaveBeenCalled();
      });
    });

    it('should show loading indicator during API request', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockImplementation(
        () => new Promise((resolve) => setTimeout(resolve, 100))
      );

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      // Loading text should appear
      expect(screen.getByText('Recolectando...')).toBeInTheDocument();

      // Wait for the request to complete
      await waitFor(() => {
        expect(mockCollectLabSamples).toHaveBeenCalled();
      });
    });
  });

  describe('Error Handling', () => {
    it('should display error message when API call fails', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockRejectedValueOnce(
        new Error('Transición de estado inválida')
      );

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      await waitFor(() => {
        expect(screen.getByText('Transición de estado inválida')).toBeInTheDocument();
      });
    });

    it('should display generic error message for unknown errors', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockRejectedValueOnce('Unknown error');

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      await waitFor(() => {
        expect(
          screen.getByText('Error al recolectar muestras. Por favor, intente nuevamente.')
        ).toBeInTheDocument();
      });
    });

    it('should log error to console when API call fails', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      const testError = new Error('Test error');
      mockCollectLabSamples.mockRejectedValueOnce(testError);

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          'Error al recolectar muestras:',
          testError
        );
      });

      consoleErrorSpy.mockRestore();
    });

    it('should allow dismissing error message', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockRejectedValueOnce(new Error('Test error'));

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

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

    it('should not call onRefresh when API call fails', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockRejectedValueOnce(new Error('Test error'));

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      await waitFor(() => {
        expect(screen.getByText('Test error')).toBeInTheDocument();
      });

      expect(mockOnRefresh).not.toHaveBeenCalled();
    });
  });

  describe('Test Information Completeness', () => {
    it('should display all required fields for each test', () => {
      render(
        <SampleCollectionStep
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
        <SampleCollectionStep
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

  describe('Accessibility', () => {
    it('should have proper ARIA labels for the button', () => {
      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      expect(button).toHaveAttribute('aria-label', 'Recolectar muestras');
    });

    it('should have proper ARIA label for error close button', async () => {
      const mockCollectLabSamples = vi.mocked(clinicalApi.collectLabSamples);
      mockCollectLabSamples.mockRejectedValueOnce(new Error('Test error'));

      render(
        <SampleCollectionStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const button = screen.getByRole('button', { name: /recolectar muestras/i });
      fireEvent.click(button);

      await waitFor(() => {
        const closeButton = screen.getByRole('button', {
          name: /cerrar mensaje/i,
        });
        expect(closeButton).toHaveAttribute('aria-label', 'Cerrar mensaje');
      });
    });
  });
});
