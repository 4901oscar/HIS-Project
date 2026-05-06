import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import WizardProgressBar from './WizardProgressBar';

describe('WizardProgressBar', () => {
  const steps = [
    'Recolección de Muestras',
    'Validar Muestras',
    'Procesar Exámenes',
    'Resultados Listos',
  ];

  it('renders all 4 steps with Spanish labels', () => {
    render(<WizardProgressBar currentStep={1} />);
    
    steps.forEach((label) => {
      expect(screen.getByText(label)).toBeInTheDocument();
    });
  });

  it('highlights the current step (step 2)', () => {
    render(<WizardProgressBar currentStep={2} />);
    
    const step2 = screen.getByLabelText('Paso 2: Validar Muestras');
    expect(step2).toHaveAttribute('aria-current', 'step');
    expect(step2).toHaveClass('bg-purple-600');
  });

  it('shows completed steps with checkmark (step 3 current)', () => {
    const { container } = render(<WizardProgressBar currentStep={3} />);
    
    const step1 = screen.getByLabelText('Paso 1: Recolección de Muestras');
    const step2 = screen.getByLabelText('Paso 2: Validar Muestras');
    
    expect(step1).toHaveClass('bg-green-500');
    expect(step2).toHaveClass('bg-green-500');
    
    // Check for checkmark SVG in completed steps
    const checkmarks = container.querySelectorAll('svg[aria-hidden="true"]');
    expect(checkmarks.length).toBeGreaterThanOrEqual(2);
  });

  it('shows pending steps with gray styling (step 1 current)', () => {
    render(<WizardProgressBar currentStep={1} />);
    
    const step2 = screen.getByLabelText('Paso 2: Validar Muestras');
    const step3 = screen.getByLabelText('Paso 3: Procesar Exámenes');
    const step4 = screen.getByLabelText('Paso 4: Resultados Listos');
    
    expect(step2).toHaveClass('bg-gray-200');
    expect(step3).toHaveClass('bg-gray-200');
    expect(step4).toHaveClass('bg-gray-200');
  });

  it('displays step numbers for current and pending steps', () => {
    render(<WizardProgressBar currentStep={2} />);
    
    // Step 2 (current) should show number
    expect(screen.getByLabelText('Paso 2: Validar Muestras')).toHaveTextContent('2');
    
    // Step 3 (pending) should show number
    expect(screen.getByLabelText('Paso 3: Procesar Exámenes')).toHaveTextContent('3');
    
    // Step 4 (pending) should show number
    expect(screen.getByLabelText('Paso 4: Resultados Listos')).toHaveTextContent('4');
  });

  it('applies distinct styling for completed, current, and pending steps', () => {
    render(<WizardProgressBar currentStep={3} />);
    
    // Completed steps (1, 2)
    const step1 = screen.getByLabelText('Paso 1: Recolección de Muestras');
    const step2 = screen.getByLabelText('Paso 2: Validar Muestras');
    expect(step1).toHaveClass('bg-green-500', 'text-white');
    expect(step2).toHaveClass('bg-green-500', 'text-white');
    
    // Current step (3)
    const step3 = screen.getByLabelText('Paso 3: Procesar Exámenes');
    expect(step3).toHaveClass('bg-purple-600', 'text-white');
    
    // Pending step (4)
    const step4 = screen.getByLabelText('Paso 4: Resultados Listos');
    expect(step4).toHaveClass('bg-gray-200', 'text-gray-500');
  });

  it('renders progress lines between steps', () => {
    const { container } = render(<WizardProgressBar currentStep={2} />);
    
    // There should be 3 progress lines (between 4 steps)
    const progressLines = container.querySelectorAll('.h-1');
    expect(progressLines).toHaveLength(3);
  });

  it('colors progress lines based on completion status', () => {
    const { container } = render(<WizardProgressBar currentStep={3} />);
    
    const progressLines = container.querySelectorAll('.h-1');
    
    // First two lines should be green (completed)
    expect(progressLines[0]).toHaveClass('bg-green-500');
    expect(progressLines[1]).toHaveClass('bg-green-500');
    
    // Third line should be gray (pending)
    expect(progressLines[2]).toHaveClass('bg-gray-200');
  });

  it('handles edge case: step 1 (all pending)', () => {
    render(<WizardProgressBar currentStep={1} />);
    
    const step1 = screen.getByLabelText('Paso 1: Recolección de Muestras');
    expect(step1).toHaveClass('bg-purple-600'); // Current
    expect(step1).toHaveAttribute('aria-current', 'step');
  });

  it('handles edge case: step 4 (all completed except last)', () => {
    render(<WizardProgressBar currentStep={4} />);
    
    const step4 = screen.getByLabelText('Paso 4: Resultados Listos');
    expect(step4).toHaveClass('bg-purple-600'); // Current
    expect(step4).toHaveAttribute('aria-current', 'step');
    
    // All previous steps should be completed
    const step1 = screen.getByLabelText('Paso 1: Recolección de Muestras');
    const step2 = screen.getByLabelText('Paso 2: Validar Muestras');
    const step3 = screen.getByLabelText('Paso 3: Procesar Exámenes');
    
    expect(step1).toHaveClass('bg-green-500');
    expect(step2).toHaveClass('bg-green-500');
    expect(step3).toHaveClass('bg-green-500');
  });
});
