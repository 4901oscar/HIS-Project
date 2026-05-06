import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import ErrorMessage from './ErrorMessage';

describe('ErrorMessage Component', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  describe('Rendering', () => {
    it('should render error message with default severity', () => {
      render(<ErrorMessage message="Error de prueba" />);
      
      expect(screen.getByText('Error de prueba')).toBeInTheDocument();
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    it('should render with error severity styling', () => {
      render(<ErrorMessage message="Error crítico" severity="error" />);
      
      const container = screen.getByRole('alert');
      expect(container).toHaveClass('bg-red-50', 'border-red-200');
    });

    it('should render with warning severity styling', () => {
      render(<ErrorMessage message="Advertencia" severity="warning" />);
      
      const container = screen.getByRole('status');
      expect(container).toHaveClass('bg-yellow-50', 'border-yellow-200');
    });

    it('should render with info severity styling', () => {
      render(<ErrorMessage message="Información" severity="info" />);
      
      const container = screen.getByRole('status');
      expect(container).toHaveClass('bg-blue-50', 'border-blue-200');
    });

    it('should render dismiss button when onDismiss is provided', () => {
      const onDismiss = vi.fn();
      render(<ErrorMessage message="Test" onDismiss={onDismiss} />);
      
      expect(screen.getByLabelText('Cerrar mensaje')).toBeInTheDocument();
    });

    it('should not render dismiss button when onDismiss is not provided', () => {
      render(<ErrorMessage message="Test" />);
      
      expect(screen.queryByLabelText('Cerrar mensaje')).not.toBeInTheDocument();
    });
  });

  describe('Manual Dismissal', () => {
    it('should call onDismiss when dismiss button is clicked', async () => {
      const onDismiss = vi.fn();
      
      render(<ErrorMessage message="Test" onDismiss={onDismiss} />);
      
      const dismissButton = screen.getByLabelText('Cerrar mensaje');
      
      // Use fireEvent instead of userEvent with fake timers
      dismissButton.click();

      // Wait for exit animation
      await vi.advanceTimersByTimeAsync(300);

      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should hide message after dismiss animation completes', async () => {
      const onDismiss = vi.fn();
      
      render(
        <ErrorMessage message="Test" onDismiss={onDismiss} />
      );
      
      const dismissButton = screen.getByLabelText('Cerrar mensaje');
      
      // Click dismiss button
      act(() => {
        dismissButton.click();
      });

      // Message should still be visible during animation
      expect(screen.getByText('Test')).toBeInTheDocument();

      // Advance past animation duration and state update
      await act(async () => {
        await vi.advanceTimersByTimeAsync(400);
      });

      // Component should be removed from DOM
      expect(screen.queryByText('Test')).not.toBeInTheDocument();
    });
  });

  describe('Auto-Dismiss', () => {
    it('should auto-dismiss after default delay (5000ms)', async () => {
      const onDismiss = vi.fn();
      
      render(
        <ErrorMessage 
          message="Test" 
          onDismiss={onDismiss} 
          autoDismiss={true}
        />
      );

      // Message should be visible initially
      expect(screen.getByText('Test')).toBeInTheDocument();

      // Advance to just before auto-dismiss
      await vi.advanceTimersByTimeAsync(4999);
      expect(onDismiss).not.toHaveBeenCalled();

      // Advance to trigger auto-dismiss
      await vi.advanceTimersByTimeAsync(1);
      
      // Wait for exit animation
      await vi.advanceTimersByTimeAsync(300);

      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should auto-dismiss after custom delay', async () => {
      const onDismiss = vi.fn();
      
      render(
        <ErrorMessage 
          message="Test" 
          onDismiss={onDismiss} 
          autoDismiss={true}
          autoDismissDelay={3000}
        />
      );

      // Advance to custom delay
      await vi.advanceTimersByTimeAsync(3000);
      
      // Wait for exit animation
      await vi.advanceTimersByTimeAsync(300);

      expect(onDismiss).toHaveBeenCalledTimes(1);
    });

    it('should not auto-dismiss when autoDismiss is false', () => {
      const onDismiss = vi.fn();
      
      render(
        <ErrorMessage 
          message="Test" 
          onDismiss={onDismiss} 
          autoDismiss={false}
        />
      );

      // Advance well past default delay
      vi.advanceTimersByTime(10000);

      expect(onDismiss).not.toHaveBeenCalled();
      expect(screen.getByText('Test')).toBeInTheDocument();
    });

    it('should cleanup timer on unmount', () => {
      const onDismiss = vi.fn();
      
      const { unmount } = render(
        <ErrorMessage 
          message="Test" 
          onDismiss={onDismiss} 
          autoDismiss={true}
        />
      );

      // Unmount before auto-dismiss triggers
      unmount();

      // Advance timers
      vi.advanceTimersByTime(5000);

      // onDismiss should not be called after unmount
      expect(onDismiss).not.toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('should use alert role for error severity', () => {
      render(<ErrorMessage message="Error" severity="error" />);
      
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    it('should use status role for warning severity', () => {
      render(<ErrorMessage message="Warning" severity="warning" />);
      
      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should use status role for info severity', () => {
      render(<ErrorMessage message="Info" severity="info" />);
      
      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should have aria-live assertive for errors', () => {
      render(<ErrorMessage message="Error" severity="error" />);
      
      const alert = screen.getByRole('alert');
      expect(alert).toHaveAttribute('aria-live', 'assertive');
    });

    it('should have aria-live polite for warnings and info', () => {
      const { rerender } = render(
        <ErrorMessage message="Warning" severity="warning" />
      );
      
      let status = screen.getByRole('status');
      expect(status).toHaveAttribute('aria-live', 'polite');

      rerender(<ErrorMessage message="Info" severity="info" />);
      
      status = screen.getByRole('status');
      expect(status).toHaveAttribute('aria-live', 'polite');
    });
  });

  describe('Icons', () => {
    it('should render error icon for error severity', () => {
      const { container } = render(
        <ErrorMessage message="Error" severity="error" />
      );
      
      const icon = container.querySelector('svg');
      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass('text-red-600');
    });

    it('should render warning icon for warning severity', () => {
      const { container } = render(
        <ErrorMessage message="Warning" severity="warning" />
      );
      
      const icon = container.querySelector('svg');
      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass('text-yellow-600');
    });

    it('should render info icon for info severity', () => {
      const { container } = render(
        <ErrorMessage message="Info" severity="info" />
      );
      
      const icon = container.querySelector('svg');
      expect(icon).toBeInTheDocument();
      expect(icon).toHaveClass('text-blue-600');
    });
  });

  describe('Spanish Messages', () => {
    it('should display messages in Spanish', () => {
      render(<ErrorMessage message="Error al cargar datos" />);
      
      expect(screen.getByText('Error al cargar datos')).toBeInTheDocument();
    });

    it('should have Spanish aria-label for dismiss button', () => {
      render(<ErrorMessage message="Test" onDismiss={vi.fn()} />);
      
      expect(screen.getByLabelText('Cerrar mensaje')).toBeInTheDocument();
    });
  });
});
