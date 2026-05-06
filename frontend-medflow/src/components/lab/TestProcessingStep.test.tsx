import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TestProcessingStep from './TestProcessingStep';
import * as labApi from '../../api/labApi';
import * as clinicalApi from '../../api/clinicalApi';
import type { AppointmentListItem } from '../../services/appointmentService';
import type { LabOrderWithTestsResponse, LabResultResponse, ValidationResponse } from '../../api/labApi';

// ─── Mocks ────────────────────────────────────────────────────────────────────

vi.mock('../../api/labApi');
vi.mock('../../api/clinicalApi');

// ─── Test Data ────────────────────────────────────────────────────────────────

const mockAppointment: AppointmentListItem = {
  id: 'appt-123',
  patientId: 'patient-456',
  patient: {
    id: 'patient-456',
    fullName: 'Juan Pérez',
    dpi: '1234567890101',
  },
  doctorId: 'doctor-789',
  appointmentDate: '2024-01-15',
  appointmentTime: '10:00',
  status: 'LAB_PROCESSING',
  notes: '',
  createdAt: '2024-01-15T08:00:00Z',
};

const mockLabOrder: LabOrderWithTestsResponse = {
  id: 'order-123',
  orderCode: 'LAB-2024-001',
  patientId: 'patient-456',
  doctorId: 'doctor-789',
  appointmentId: 'appt-123',
  status: 'PENDING',
  orderedAt: '2024-01-15T08:00:00Z',
  tests: [
    {
      testName: 'Hemograma Completo',
      testType: 'HEMATOLOGY',
      sampleType: 'Sangre',
      hasResult: false,
    },
    {
      testName: 'Glucosa en Ayunas',
      testType: 'BIOCHEMISTRY',
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
};

const mockLabResult: LabResultResponse = {
  id: 'result-123',
  orderId: 'order-123',
  testName: 'Hemograma Completo',
  originalFilename: 'hemograma.pdf',
  fileSize: 1024000,
  uploadedAt: '2024-01-15T10:30:00Z',
  uploadedBy: 'user-123',
  downloadUrl: '/api/lab/results/result-123/download',
};

// ─── Test Suite ───────────────────────────────────────────────────────────────

describe('TestProcessingStep', () => {
  const mockOnRefresh = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // ─── Rendering Tests ────────────────────────────────────────────────────────

  describe('Component Rendering', () => {
    it('should render the component with heading', () => {
      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('Paso 3: Procesar Exámenes')).toBeInTheDocument();
    });

    it('should display list of tests from lab order', () => {
      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('Exámenes a procesar (3)')).toBeInTheDocument();
      expect(screen.getByText('Hemograma Completo')).toBeInTheDocument();
      expect(screen.getByText('Glucosa en Ayunas')).toBeInTheDocument();
      expect(screen.getByText('Examen General de Orina')).toBeInTheDocument();
    });

    it('should render TestResultUpload component for each test', () => {
      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      // Each test should have a "Cargar resultado" button
      const uploadButtons = screen.getAllByText('Cargar resultado');
      expect(uploadButtons).toHaveLength(3);
    });

    it('should display "Marcar como completado" button', () => {
      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('Marcar como completado')).toBeInTheDocument();
    });

    it('should display message when no tests in order', () => {
      const emptyLabOrder = { ...mockLabOrder, tests: [] };

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={emptyLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      expect(screen.getByText('No hay exámenes en esta orden')).toBeInTheDocument();
    });
  });

  // ─── File Upload Tests ──────────────────────────────────────────────────────

  describe('File Upload Functionality', () => {
    it('should upload file successfully when file is selected', async () => {
      const user = userEvent.setup();
      const mockFile = new File(['test content'], 'test.pdf', { type: 'application/pdf' });

      vi.mocked(labApi.uploadTestResult).mockResolvedValue(mockLabResult);

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      // Find the first test's upload button
      const uploadButtons = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtons[0]);

      // Find the hidden file input and upload file
      const fileInputs = document.querySelectorAll('input[type="file"]');
      const firstFileInput = fileInputs[0] as HTMLInputElement;
      await user.upload(firstFileInput, mockFile);

      // Wait for upload to complete
      await waitFor(() => {
        expect(labApi.uploadTestResult).toHaveBeenCalledWith(
          'order-123',
          'Hemograma Completo',
          mockFile
        );
      });

      // Verify success indicator is shown
      await waitFor(() => {
        expect(screen.getByText('Cargado')).toBeInTheDocument();
      });
    });

    it('should display loading indicator during file upload', async () => {
      const user = userEvent.setup();
      const mockFile = new File(['test content'], 'test.pdf', { type: 'application/pdf' });

      // Mock a delayed upload
      vi.mocked(labApi.uploadTestResult).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockLabResult), 100))
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const uploadButtons = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtons[0]);

      const fileInputs = document.querySelectorAll('input[type="file"]');
      const firstFileInput = fileInputs[0] as HTMLInputElement;
      await user.upload(firstFileInput, mockFile);

      // Check for loading state
      await waitFor(() => {
        expect(screen.getByText('Cargando...')).toBeInTheDocument();
      });

      // Wait for upload to complete
      await waitFor(() => {
        expect(screen.getByText('Cargado')).toBeInTheDocument();
      });
    });

    it('should display error message when file upload fails', async () => {
      const user = userEvent.setup();
      const mockFile = new File(['test content'], 'test.pdf', { type: 'application/pdf' });

      vi.mocked(labApi.uploadTestResult).mockRejectedValue(
        new Error('Error al cargar el archivo')
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const uploadButtons = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtons[0]);

      const fileInputs = document.querySelectorAll('input[type="file"]');
      const firstFileInput = fileInputs[0] as HTMLInputElement;
      await user.upload(firstFileInput, mockFile);

      // Wait for error to be displayed
      await waitFor(() => {
        expect(screen.getByText('Error al cargar el archivo')).toBeInTheDocument();
      });
    });

    it('should track multiple file uploads independently', async () => {
      const user = userEvent.setup();
      const mockFile1 = new File(['test 1'], 'test1.pdf', { type: 'application/pdf' });
      const mockFile2 = new File(['test 2'], 'test2.pdf', { type: 'application/pdf' });

      const mockResult1 = { ...mockLabResult, testName: 'Hemograma Completo' };
      const mockResult2 = { ...mockLabResult, testName: 'Glucosa en Ayunas' };

      vi.mocked(labApi.uploadTestResult)
        .mockResolvedValueOnce(mockResult1)
        .mockResolvedValueOnce(mockResult2);

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      // Upload first file
      const uploadButtons = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtons[0]);

      const fileInputs = document.querySelectorAll('input[type="file"]');
      await user.upload(fileInputs[0] as HTMLInputElement, mockFile1);

      await waitFor(() => {
        expect(labApi.uploadTestResult).toHaveBeenCalledWith(
          'order-123',
          'Hemograma Completo',
          mockFile1
        );
      });

      // Upload second file
      const uploadButtons2 = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtons2[0]);

      await user.upload(fileInputs[1] as HTMLInputElement, mockFile2);

      await waitFor(() => {
        expect(labApi.uploadTestResult).toHaveBeenCalledWith(
          'order-123',
          'Glucosa en Ayunas',
          mockFile2
        );
      });

      // Both should show as uploaded
      await waitFor(() => {
        const uploadedIndicators = screen.getAllByText('Cargado');
        expect(uploadedIndicators).toHaveLength(2);
      });
    });

    it('should display upload progress indicator when multiple uploads are in progress', async () => {
      const user = userEvent.setup();
      const mockFile = new File(['test'], 'test.pdf', { type: 'application/pdf' });

      // Mock delayed uploads
      vi.mocked(labApi.uploadTestResult).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockLabResult), 200))
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      // Start two uploads
      const uploadButtons = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtons[0]);
      const fileInputs = document.querySelectorAll('input[type="file"]');
      await user.upload(fileInputs[0] as HTMLInputElement, mockFile);

      await user.click(uploadButtons[1]);
      await user.upload(fileInputs[1] as HTMLInputElement, mockFile);

      // Check for progress indicator
      await waitFor(() => {
        expect(screen.getByText(/Cargando resultados\.\.\./)).toBeInTheDocument();
      });
    });
  });

  // ─── Validation Tests ───────────────────────────────────────────────────────

  describe('Validation and Completion', () => {
    it('should validate and complete successfully when all tests have results', async () => {
      const user = userEvent.setup();

      const mockValidation: ValidationResponse = {
        isValid: true,
        missingTests: [],
      };

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidation);
      vi.mocked(clinicalApi.completeLabProcessing).mockResolvedValue(mockAppointment);

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      // Wait for validation and completion
      await waitFor(() => {
        expect(labApi.validateAllTestsComplete).toHaveBeenCalledWith('order-123');
      });

      await waitFor(() => {
        expect(clinicalApi.completeLabProcessing).toHaveBeenCalledWith('appt-123');
      });

      await waitFor(() => {
        expect(mockOnRefresh).toHaveBeenCalled();
      });
    });

    it('should display error when validation fails with missing tests', async () => {
      const user = userEvent.setup();

      const mockValidation: ValidationResponse = {
        isValid: false,
        missingTests: ['Hemograma Completo', 'Glucosa en Ayunas'],
      };

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidation);

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      // Wait for validation error
      await waitFor(() => {
        expect(
          screen.getByText(/No se puede completar el procesamiento/)
        ).toBeInTheDocument();
      });

      await waitFor(() => {
        expect(
          screen.getByText(/Hemograma Completo, Glucosa en Ayunas/)
        ).toBeInTheDocument();
      });

      // Should not call completeLabProcessing
      expect(clinicalApi.completeLabProcessing).not.toHaveBeenCalled();
      expect(mockOnRefresh).not.toHaveBeenCalled();
    });

    it('should display error when completeLabProcessing API fails', async () => {
      const user = userEvent.setup();

      const mockValidation: ValidationResponse = {
        isValid: true,
        missingTests: [],
      };

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidation);
      vi.mocked(clinicalApi.completeLabProcessing).mockRejectedValue(
        new Error('Error al completar procesamiento')
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      // Wait for error
      await waitFor(() => {
        expect(screen.getByText('Error al completar procesamiento')).toBeInTheDocument();
      });

      expect(mockOnRefresh).not.toHaveBeenCalled();
    });

    it('should close error message when close button is clicked', async () => {
      const user = userEvent.setup();

      const mockValidation: ValidationResponse = {
        isValid: false,
        missingTests: ['Hemograma Completo'],
      };

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidation);

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      // Wait for error to appear
      await waitFor(() => {
        expect(screen.getByText(/No se puede completar el procesamiento/)).toBeInTheDocument();
      });

      // Click close button
      const closeButton = screen.getByLabelText('Cerrar mensaje');
      await user.click(closeButton);

      // Error should be removed
      await waitFor(() => {
        expect(
          screen.queryByText(/No se puede completar el procesamiento/)
        ).not.toBeInTheDocument();
      });
    });
  });

  // ─── Button State Tests ─────────────────────────────────────────────────────

  describe('Button States', () => {
    it('should disable complete button during API request', async () => {
      const user = userEvent.setup();

      const mockValidation: ValidationResponse = {
        isValid: true,
        missingTests: [],
      };

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidation);
      vi.mocked(clinicalApi.completeLabProcessing).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockAppointment), 100))
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      // Button should be disabled during request
      await waitFor(() => {
        expect(screen.getByText('Completando...')).toBeInTheDocument();
        const button = screen.getByRole('button', { name: /Marcar como completado/i });
        expect(button).toBeDisabled();
      });
    });

    it('should disable complete button when uploads are in progress', async () => {
      const user = userEvent.setup();
      const mockFile = new File(['test'], 'test.pdf', { type: 'application/pdf' });

      // Mock delayed upload
      vi.mocked(labApi.uploadTestResult).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockLabResult), 200))
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      // Start an upload
      const uploadButtons = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtons[0]);

      const fileInputs = document.querySelectorAll('input[type="file"]');
      await user.upload(fileInputs[0] as HTMLInputElement, mockFile);

      // Complete button should be disabled
      await waitFor(() => {
        const completeButton = screen.getByRole('button', { name: /Marcar como completado/i });
        expect(completeButton).toBeDisabled();
      });
    });

    it('should disable complete button when no tests in order', () => {
      const emptyLabOrder = { ...mockLabOrder, tests: [] };

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={emptyLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const completeButton = screen.getByRole('button', { name: /Marcar como completado/i });
      expect(completeButton).toBeDisabled();
    });
  });

  // ─── Error Handling Tests ───────────────────────────────────────────────────

  describe('Error Handling', () => {
    it('should display error in Spanish when network error occurs', async () => {
      const user = userEvent.setup();

      vi.mocked(labApi.validateAllTestsComplete).mockRejectedValue(
        new Error('Network error')
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      await waitFor(() => {
        expect(screen.getByText(/Network error/)).toBeInTheDocument();
      });
    });

    it('should clear previous errors when new upload starts for same test', async () => {
      const user = userEvent.setup();
      const mockFile1 = new File(['test1'], 'test1.pdf', { type: 'application/pdf' });
      const mockFile2 = new File(['test2'], 'test2.pdf', { type: 'application/pdf' });

      // First upload fails
      vi.mocked(labApi.uploadTestResult).mockRejectedValueOnce(
        new Error('Error al cargar')
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const uploadButtons = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtons[0]);

      const fileInputs = document.querySelectorAll('input[type="file"]');
      await user.upload(fileInputs[0] as HTMLInputElement, mockFile1);

      // Wait for error
      await waitFor(() => {
        expect(screen.getByText('Error al cargar')).toBeInTheDocument();
      });

      // Second upload succeeds
      vi.mocked(labApi.uploadTestResult).mockResolvedValueOnce(mockLabResult);

      // Click the same test's upload button again
      const uploadButtonsAfterError = screen.getAllByText('Cargar resultado');
      await user.click(uploadButtonsAfterError[0]);
      await user.upload(fileInputs[0] as HTMLInputElement, mockFile2);

      // Error should be cleared and success indicator shown
      await waitFor(() => {
        expect(screen.queryByText('Error al cargar')).not.toBeInTheDocument();
        expect(screen.getByText('Cargado')).toBeInTheDocument();
      });
    });

    it('should log errors to console for debugging', async () => {
      const user = userEvent.setup();
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      vi.mocked(labApi.validateAllTestsComplete).mockRejectedValue(
        new Error('Test error')
      );

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      await waitFor(() => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          'Error al completar procesamiento:',
          expect.any(Error)
        );
      });

      consoleErrorSpy.mockRestore();
    });
  });

  // ─── Integration Tests ──────────────────────────────────────────────────────

  describe('Integration Scenarios', () => {
    it('should handle complete workflow: upload all results and mark complete', async () => {
      const user = userEvent.setup();
      const mockFile = new File(['test'], 'test.pdf', { type: 'application/pdf' });

      const mockResult1 = { ...mockLabResult, testName: 'Hemograma Completo' };
      const mockResult2 = { ...mockLabResult, testName: 'Glucosa en Ayunas' };
      const mockResult3 = { ...mockLabResult, testName: 'Examen General de Orina' };

      vi.mocked(labApi.uploadTestResult)
        .mockResolvedValueOnce(mockResult1)
        .mockResolvedValueOnce(mockResult2)
        .mockResolvedValueOnce(mockResult3);

      const mockValidation: ValidationResponse = {
        isValid: true,
        missingTests: [],
      };

      vi.mocked(labApi.validateAllTestsComplete).mockResolvedValue(mockValidation);
      vi.mocked(clinicalApi.completeLabProcessing).mockResolvedValue(mockAppointment);

      render(
        <TestProcessingStep
          appointment={mockAppointment}
          labOrder={mockLabOrder}
          onRefresh={mockOnRefresh}
        />
      );

      // Upload all three results
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

      // Mark as complete
      const completeButton = screen.getByText('Marcar como completado');
      await user.click(completeButton);

      // Verify workflow completion
      await waitFor(() => {
        expect(mockOnRefresh).toHaveBeenCalled();
      });
    });
  });
});
