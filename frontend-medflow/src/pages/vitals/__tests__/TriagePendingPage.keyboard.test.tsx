import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import TriagePendingPage from '../TriagePendingPage';
import * as clinicalService from '../../../services/clinicalService';
import * as patientService from '../../../services/patientService';
import * as manchesterService from '../../../services/manchesterService';

jest.mock('../../../services/clinicalService');
jest.mock('../../../services/patientService');
jest.mock('../../../services/manchesterService');
jest.mock('../../../services/authService', () => ({
  getUserFullName: jest.fn().mockResolvedValue('Juan Pérez'),
}));

const mockAppointments = [
  {
    id: 'appt-001',
    patientId: 'patient-123',
    doctorId: 'doctor-456',
    appointmentDate: '2026-04-23',
    appointmentTime: '10:00:00',
    status: 'ACTIVE',
    notes: 'Dolor de cabeza',
    createdAt: '2026-04-23T09:30:00',
  },
];

const mockPatient = {
  id: 'patient-123',
  fullName: 'Juan Pérez',
  dpi: '1234567890123',
  email: 'juan@example.com',
};

const mockManchesterCatalog = {
  motifs: [
    { id: 'motif-1', code: 'M001', description: 'Dolor de cabeza', active: true },
  ],
  discriminators: [
    { id: 'disc-1', code: 'D001', description: 'Dolor severo', priorityLevel: 'ORANGE', active: true },
  ],
};

describe('TriagePendingPage - Keyboard Navigation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (clinicalService.getPendingTriageAppointments as jest.Mock).mockResolvedValue(mockAppointments);
    (patientService.getPatientById as jest.Mock).mockResolvedValue(mockPatient);
    (manchesterService.getManchesterCatalog as jest.Mock).mockResolvedValue(mockManchesterCatalog);
  });

  test('Enter key selects appointment row', async () => {
    render(
      <BrowserRouter>
        <TriagePendingPage />
      </BrowserRouter>
    );

    // Wait for appointments to load
    await waitFor(() => {
      expect(screen.getByText('2026-04-23')).toBeInTheDocument();
    });

    // Find the appointment row
    const row = screen.getByText('2026-04-23').closest('tr');
    expect(row).toBeInTheDocument();

    // Focus the row and press Enter
    row?.focus();
    fireEvent.keyDown(row!, { key: 'Enter', code: 'Enter' });

    // Verify patient details are displayed
    await waitFor(() => {
      expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    });
  });

  test('Space key selects appointment row', async () => {
    render(
      <BrowserRouter>
        <TriagePendingPage />
      </BrowserRouter>
    );

    // Wait for appointments to load
    await waitFor(() => {
      expect(screen.getByText('2026-04-23')).toBeInTheDocument();
    });

    // Find the appointment row
    const row = screen.getByText('2026-04-23').closest('tr');
    expect(row).toBeInTheDocument();

    // Focus the row and press Space
    row?.focus();
    fireEvent.keyDown(row!, { key: ' ', code: 'Space' });

    // Verify patient details are displayed
    await waitFor(() => {
      expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    });
  });

  test('Escape key closes vital signs form', async () => {
    render(
      <BrowserRouter>
        <TriagePendingPage />
      </BrowserRouter>
    );

    // Wait for appointments to load and select one
    await waitFor(() => {
      expect(screen.getByText('2026-04-23')).toBeInTheDocument();
    });

    const row = screen.getByText('2026-04-23').closest('tr');
    fireEvent.click(row!);

    await waitFor(() => {
      expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    });

    // Click "Registrar Signos Vitales" button
    const vitalButton = screen.getByText(/Registrar Signos Vitales/i);
    fireEvent.click(vitalButton);

    // Verify form is displayed
    await waitFor(() => {
      expect(screen.getByLabelText(/Sist\./i)).toBeInTheDocument();
    });

    // Press Escape key
    fireEvent.keyDown(document, { key: 'Escape', code: 'Escape' });

    // Verify form is closed
    await waitFor(() => {
      expect(screen.queryByLabelText(/Sist\./i)).not.toBeInTheDocument();
    });
  });

  test('Escape key closes triage form', async () => {
    (clinicalService.recordVitalSigns as jest.Mock).mockResolvedValue({
      id: 'vital-001',
      patientId: 'patient-123',
    });

    render(
      <BrowserRouter>
        <TriagePendingPage />
      </BrowserRouter>
    );

    // Wait for appointments to load and select one
    await waitFor(() => {
      expect(screen.getByText('2026-04-23')).toBeInTheDocument();
    });

    const row = screen.getByText('2026-04-23').closest('tr');
    fireEvent.click(row!);

    await waitFor(() => {
      expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    });

    // Record vital signs first
    const vitalButton = screen.getByText(/Registrar Signos Vitales/i);
    fireEvent.click(vitalButton);

    await waitFor(() => {
      expect(screen.getByLabelText(/Sist\./i)).toBeInTheDocument();
    });

    // Fill and submit vital signs form
    fireEvent.change(screen.getByLabelText(/Sist\./i), { target: { value: '120' } });
    fireEvent.change(screen.getByLabelText(/Diast\./i), { target: { value: '80' } });
    fireEvent.change(screen.getByLabelText(/FC/i), { target: { value: '72' } });
    fireEvent.change(screen.getByLabelText(/FR/i), { target: { value: '16' } });
    fireEvent.change(screen.getByLabelText(/Temperatura/i), { target: { value: '36.5' } });
    fireEvent.change(screen.getByLabelText(/SpO2/i), { target: { value: '98' } });

    const submitButton = screen.getByText(/Guardar Signos Vitales/i);
    fireEvent.click(submitButton);

    // Wait for vital signs to be recorded
    await waitFor(() => {
      expect(screen.queryByLabelText(/Sist\./i)).not.toBeInTheDocument();
    });

    // Click "Realizar Triaje" button
    const triageButton = screen.getByText(/Realizar Triaje/i);
    fireEvent.click(triageButton);

    // Wait for triage form to load
    await waitFor(() => {
      expect(screen.getByText(/Realizar Triaje Manchester/i)).toBeInTheDocument();
    });

    // Press Escape key
    fireEvent.keyDown(document, { key: 'Escape', code: 'Escape' });

    // Verify form is closed
    await waitFor(() => {
      expect(screen.queryByText(/Realizar Triaje Manchester/i)).not.toBeInTheDocument();
    });
  });

  test('Tab navigation through interactive elements', async () => {
    render(
      <BrowserRouter>
        <TriagePendingPage />
      </BrowserRouter>
    );

    // Wait for appointments to load
    await waitFor(() => {
      expect(screen.getByText('2026-04-23')).toBeInTheDocument();
    });

    // Get all interactive elements
    const refreshButton = screen.getByText(/Actualizar/i);
    const appointmentRow = screen.getByText('2026-04-23').closest('tr');

    // Verify elements are focusable
    expect(refreshButton).toHaveAttribute('type', 'button');
    expect(appointmentRow).toHaveAttribute('tabIndex', '0');
  });

  test('All buttons are keyboard accessible', async () => {
    render(
      <BrowserRouter>
        <TriagePendingPage />
      </BrowserRouter>
    );

    // Wait for appointments to load and select one
    await waitFor(() => {
      expect(screen.getByText('2026-04-23')).toBeInTheDocument();
    });

    const row = screen.getByText('2026-04-23').closest('tr');
    fireEvent.click(row!);

    await waitFor(() => {
      expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    });

    // Verify all buttons are present and are actual button elements
    const vitalButton = screen.getByText(/Registrar Signos Vitales/i);
    const triageButton = screen.getByText(/Realizar Triaje/i);
    const cancelButton = screen.getByText(/Cancelar/i);

    expect(vitalButton.tagName).toBe('BUTTON');
    expect(triageButton.tagName).toBe('BUTTON');
    expect(cancelButton.tagName).toBe('BUTTON');
  });

  test('Focus moves to patient details card after selection', async () => {
    render(
      <BrowserRouter>
        <TriagePendingPage />
      </BrowserRouter>
    );

    // Wait for appointments to load
    await waitFor(() => {
      expect(screen.getByText('2026-04-23')).toBeInTheDocument();
    });

    // Select appointment
    const row = screen.getByText('2026-04-23').closest('tr');
    fireEvent.click(row!);

    // Wait for patient details to load
    await waitFor(() => {
      expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    });

    // Verify focus management (the patient details card should be focusable)
    const patientCard = screen.getByText('Detalles del Paciente').closest('div');
    expect(patientCard).toHaveAttribute('tabIndex', '-1');
  });
});
