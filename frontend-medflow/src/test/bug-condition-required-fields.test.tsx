import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders, mockAdminUser } from './test-utils';
import DoctorForm from '../components/DoctorForm/DoctorForm';

/**
 * Bug Condition Exploration Test - Campos Obligatorios Sin Asterisco
 * 
 * **Validates: Requirements 2.1, 2.2**
 * 
 * OBJETIVO: Demostrar que los campos obligatorios en DoctorForm NO tienen asteriscos rojos
 * ANTES de implementar la corrección.
 * 
 * RESULTADO ESPERADO: Este test DEBE FALLAR en código sin corregir - la falla confirma que el bug existe.
 * 
 * Bug Condition 2 (Design): Campos obligatorios sin indicador visual
 * - Campo "Seleccionar Doctor" (modo creación) no muestra asterisco rojo
 * - Campo "Especialidad" no muestra asterisco rojo
 * - Campo "Hora de Inicio del Turno" no muestra asterisco rojo
 * - Campo "Hora de Fin del Turno" no muestra asterisco rojo
 * 
 * Expected Behavior (Design Property 2): Indicadores visuales en DoctorForm
 * - Todos los campos obligatorios deben mostrar <span className="text-red-500">*</span> después del texto del label
 */

// Mock de servicios para evitar llamadas reales a la API
vi.mock('../services/doctorService', () => ({
  createDoctor: vi.fn(),
  updateDoctor: vi.fn(),
  getDoctorEmployees: vi.fn().mockResolvedValue([
    {
      id: 'user-1',
      username: 'doctor1@test.com',
      fullName: 'Dr. Juan Pérez',
    },
    {
      id: 'user-2',
      username: 'doctor2@test.com',
      fullName: 'Dra. María García',
    },
  ]),
}));

describe('Bug Condition 2: Campos Obligatorios Sin Asterisco', () => {
  /**
   * Test 2.1: Campo "Seleccionar Doctor" debe tener asterisco rojo en modo creación
   * 
   * Bug Condition: El label muestra "Seleccionar Doctor *" como texto plano
   * Expected Behavior: Debe mostrar "Seleccionar Doctor " seguido de <span className="text-red-500">*</span>
   */
  it('should verify "Seleccionar Doctor" field has red asterisk in create mode', async () => {
    renderWithProviders(<DoctorForm />, { user: mockAdminUser });

    // Esperar a que se cargue la lista de doctores disponibles
    await screen.findByText('Vincular Doctor');

    // Buscar el label "Seleccionar Doctor"
    const label = screen.getByText((content, element) => {
      return element?.tagName === 'LABEL' && content.includes('Seleccionar Doctor');
    });

    expect(label).toBeInTheDocument();

    // Verificar que el label contiene un <span> con clase "text-red-500" para el asterisco
    const redAsterisk = label.querySelector('span.text-red-500');
    expect(redAsterisk).toBeInTheDocument();
    expect(redAsterisk?.textContent).toBe('*');
  });

  /**
   * Test 2.2: Campo "Especialidad" debe tener asterisco rojo
   * 
   * Bug Condition: El label muestra "Especialidad *" como texto plano
   * Expected Behavior: Debe mostrar "Especialidad " seguido de <span className="text-red-500">*</span>
   */
  it('should verify "Especialidad" field has red asterisk', async () => {
    renderWithProviders(<DoctorForm />, { user: mockAdminUser });

    await screen.findByText('Vincular Doctor');

    // Buscar el label "Especialidad"
    const label = screen.getByText((content, element) => {
      return element?.tagName === 'LABEL' && content.includes('Especialidad');
    });

    expect(label).toBeInTheDocument();

    // Verificar que el label contiene un <span> con clase "text-red-500" para el asterisco
    const redAsterisk = label.querySelector('span.text-red-500');
    expect(redAsterisk).toBeInTheDocument();
    expect(redAsterisk?.textContent).toBe('*');
  });

  /**
   * Test 2.3: Campo "Hora de Inicio del Turno" debe tener asterisco rojo
   * 
   * Bug Condition: El label muestra "Hora de Inicio del Turno *" como texto plano
   * Expected Behavior: Debe mostrar "Hora de Inicio del Turno " seguido de <span className="text-red-500">*</span>
   */
  it('should verify "Hora de Inicio del Turno" field has red asterisk', async () => {
    renderWithProviders(<DoctorForm />, { user: mockAdminUser });

    await screen.findByText('Vincular Doctor');

    // Buscar el label "Hora de Inicio del Turno"
    const label = screen.getByText((content, element) => {
      return element?.tagName === 'LABEL' && content.includes('Hora de Inicio del Turno');
    });

    expect(label).toBeInTheDocument();

    // Verificar que el label contiene un <span> con clase "text-red-500" para el asterisco
    const redAsterisk = label.querySelector('span.text-red-500');
    expect(redAsterisk).toBeInTheDocument();
    expect(redAsterisk?.textContent).toBe('*');
  });

  /**
   * Test 2.4: Campo "Hora de Fin del Turno" debe tener asterisco rojo
   * 
   * Bug Condition: El label muestra "Hora de Fin del Turno *" como texto plano
   * Expected Behavior: Debe mostrar "Hora de Fin del Turno " seguido de <span className="text-red-500">*</span>
   */
  it('should verify "Hora de Fin del Turno" field has red asterisk', async () => {
    renderWithProviders(<DoctorForm />, { user: mockAdminUser });

    await screen.findByText('Vincular Doctor');

    // Buscar el label "Hora de Fin del Turno"
    const label = screen.getByText((content, element) => {
      return element?.tagName === 'LABEL' && content.includes('Hora de Fin del Turno');
    });

    expect(label).toBeInTheDocument();

    // Verificar que el label contiene un <span> con clase "text-red-500" para el asterisco
    const redAsterisk = label.querySelector('span.text-red-500');
    expect(redAsterisk).toBeInTheDocument();
    expect(redAsterisk?.textContent).toBe('*');
  });

  /**
   * Test 2.5: Verificar que todos los campos obligatorios tienen asterisco rojo (test agregado)
   * 
   * Este test verifica todos los campos obligatorios en una sola ejecución para confirmar
   * que el patrón de asterisco rojo se aplica consistentemente.
   */
  it('should verify all required fields have red asterisk consistently', async () => {
    renderWithProviders(<DoctorForm />, { user: mockAdminUser });

    await screen.findByText('Vincular Doctor');

    const requiredFieldLabels = [
      'Seleccionar Doctor',
      'Especialidad',
      'Hora de Inicio del Turno',
      'Hora de Fin del Turno',
    ];

    requiredFieldLabels.forEach((fieldName) => {
      const label = screen.getByText((content, element) => {
        return element?.tagName === 'LABEL' && content.includes(fieldName);
      });

      expect(label).toBeInTheDocument();

      const redAsterisk = label.querySelector('span.text-red-500');
      expect(redAsterisk).toBeInTheDocument();
      expect(redAsterisk?.textContent).toBe('*');
    });
  });
});
