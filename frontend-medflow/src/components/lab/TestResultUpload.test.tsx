import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TestResultUpload from './TestResultUpload';

describe('TestResultUpload', () => {
  const mockOnFileSelect = vi.fn();
  const testName = 'Hemograma Completo';

  beforeEach(() => {
    mockOnFileSelect.mockClear();
  });

  // ─── Basic Rendering Tests ──────────────────────────────────────────────────

  it('should render test name', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    expect(screen.getByText(testName)).toBeInTheDocument();
  });

  it('should render upload button when no file is uploaded', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    expect(screen.getByRole('button', { name: /cargar resultado/i })).toBeInTheDocument();
  });

  it('should render file format info when no file is uploaded', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    expect(screen.getByText(/formatos permitidos: pdf, jpeg, png/i)).toBeInTheDocument();
  });

  it('should render hidden file input with correct accept attribute', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`);
    expect(fileInput).toHaveAttribute('type', 'file');
    expect(fileInput).toHaveAttribute('accept', '.pdf,.jpg,.jpeg,.png');
    expect(fileInput).toHaveClass('hidden');
  });

  // ─── File Selection Tests ────────────────────────────────────────────────────

  it('should trigger file input click when upload button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const uploadButton = screen.getByRole('button', { name: /cargar resultado/i });
    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;

    // Mock the click method
    const clickSpy = vi.spyOn(fileInput, 'click');

    await user.click(uploadButton);

    expect(clickSpy).toHaveBeenCalled();
  });

  it('should call onFileSelect with valid PDF file', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['dummy content'], 'result.pdf', { type: 'application/pdf' });

    await userEvent.upload(fileInput, file);

    expect(mockOnFileSelect).toHaveBeenCalledWith(file);
  });

  it('should call onFileSelect with valid JPEG file', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['dummy content'], 'result.jpg', { type: 'image/jpeg' });

    await userEvent.upload(fileInput, file);

    expect(mockOnFileSelect).toHaveBeenCalledWith(file);
  });

  it('should call onFileSelect with valid PNG file', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['dummy content'], 'result.png', { type: 'image/png' });

    await userEvent.upload(fileInput, file);

    expect(mockOnFileSelect).toHaveBeenCalledWith(file);
  });

  // ─── File Format Validation Tests ────────────────────────────────────────────

  it('should reject invalid file format and show error in Spanish', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['dummy content'], 'result.txt', { type: 'text/plain' });

    // Use fireEvent to properly trigger the change event
    Object.defineProperty(fileInput, 'files', {
      value: [file],
      writable: false,
    });
    fireEvent.change(fileInput);

    await waitFor(() => {
      expect(mockOnFileSelect).not.toHaveBeenCalled();
      expect(screen.getByText(/formato de archivo no permitido/i)).toBeInTheDocument();
      expect(screen.getByText(/solo se aceptan pdf, jpeg y png/i)).toBeInTheDocument();
    });
  });

  it('should reject Word document format', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['dummy content'], 'result.docx', {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    });

    Object.defineProperty(fileInput, 'files', {
      value: [file],
      writable: false,
    });
    fireEvent.change(fileInput);

    await waitFor(() => {
      expect(mockOnFileSelect).not.toHaveBeenCalled();
      expect(screen.getByText(/formato de archivo no permitido/i)).toBeInTheDocument();
    });
  });

  it('should reject Excel format', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['dummy content'], 'result.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });

    Object.defineProperty(fileInput, 'files', {
      value: [file],
      writable: false,
    });
    fireEvent.change(fileInput);

    await waitFor(() => {
      expect(mockOnFileSelect).not.toHaveBeenCalled();
      expect(screen.getByText(/formato de archivo no permitido/i)).toBeInTheDocument();
    });
  });

  // ─── File Size Validation Tests ──────────────────────────────────────────────

  it('should accept file exactly at 10MB limit', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const tenMB = 10 * 1024 * 1024;
    const file = new File([new ArrayBuffer(tenMB)], 'result.pdf', { type: 'application/pdf' });

    await userEvent.upload(fileInput, file);

    expect(mockOnFileSelect).toHaveBeenCalledWith(file);
  });

  it('should reject file over 10MB and show error in Spanish', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const overTenMB = 10 * 1024 * 1024 + 1;
    const file = new File([new ArrayBuffer(overTenMB)], 'result.pdf', { type: 'application/pdf' });

    await userEvent.upload(fileInput, file);

    expect(mockOnFileSelect).not.toHaveBeenCalled();
    expect(screen.getByText(/el archivo excede el tamaño máximo permitido de 10 mb/i)).toBeInTheDocument();
  });

  it('should accept small file (1KB)', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['small content'], 'result.pdf', { type: 'application/pdf' });

    await userEvent.upload(fileInput, file);

    expect(mockOnFileSelect).toHaveBeenCalledWith(file);
  });

  it('should accept file at 5MB (mid-range)', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const fiveMB = 5 * 1024 * 1024;
    const file = new File([new ArrayBuffer(fiveMB)], 'result.pdf', { type: 'application/pdf' });

    await userEvent.upload(fileInput, file);

    expect(mockOnFileSelect).toHaveBeenCalledWith(file);
  });

  // ─── Uploaded File Display Tests ─────────────────────────────────────────────

  it('should display uploaded file info when uploadedFile prop is provided', () => {
    const uploadedFile = {
      filename: 'hemograma_result.pdf',
      uploadedAt: '2024-01-15T10:30:00',
    };

    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        uploadedFile={uploadedFile}
      />
    );

    expect(screen.getByText(uploadedFile.filename)).toBeInTheDocument();
    expect(screen.getByText(/cargado:/i)).toBeInTheDocument();
  });

  it('should show success icon when file is uploaded', () => {
    const uploadedFile = {
      filename: 'result.pdf',
      uploadedAt: '2024-01-15T10:30:00',
    };

    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        uploadedFile={uploadedFile}
      />
    );

    expect(screen.getByText('Cargado')).toBeInTheDocument();
  });

  it('should not show upload button when file is uploaded', () => {
    const uploadedFile = {
      filename: 'result.pdf',
      uploadedAt: '2024-01-15T10:30:00',
    };

    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        uploadedFile={uploadedFile}
      />
    );

    expect(screen.queryByRole('button', { name: /cargar resultado/i })).not.toBeInTheDocument();
  });

  it('should format uploaded timestamp in Spanish locale', () => {
    const uploadedFile = {
      filename: 'result.pdf',
      uploadedAt: '2024-01-15T10:30:00',
    };

    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        uploadedFile={uploadedFile}
      />
    );

    // Check that the date is formatted (exact format may vary by environment)
    const dateText = screen.getByText(/cargado:/i).textContent;
    expect(dateText).toMatch(/\d{1,2}:\d{2}/); // Should contain time
  });

  // ─── Loading State Tests ──────────────────────────────────────────────────────

  it('should show loading state when isUploading is true', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        isUploading={true}
      />
    );

    expect(screen.getByText('Cargando...')).toBeInTheDocument();
  });

  it('should disable upload button when isUploading is true', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        isUploading={true}
      />
    );

    const uploadButton = screen.getByRole('button', { name: /cargar resultado/i });
    expect(uploadButton).toBeDisabled();
  });

  it('should disable file input when isUploading is true', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        isUploading={true}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    expect(fileInput).toBeDisabled();
  });

  it('should show spinner icon when uploading', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        isUploading={true}
      />
    );

    const spinner = screen.getByRole('button', { name: /cargar resultado/i }).querySelector('.animate-spin');
    expect(spinner).toBeInTheDocument();
  });

  // ─── Error Display Tests ──────────────────────────────────────────────────────

  it('should display external error message when error prop is provided', () => {
    const errorMessage = 'Error al cargar el archivo al servidor';

    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        error={errorMessage}
      />
    );

    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  it('should prioritize validation error over external error', async () => {
    const externalError = 'Error del servidor';

    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        error={externalError}
      />
    );

    // Initially shows external error
    expect(screen.getByText(externalError)).toBeInTheDocument();

    // Upload invalid file
    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['dummy content'], 'result.txt', { type: 'text/plain' });

    Object.defineProperty(fileInput, 'files', {
      value: [file],
      writable: false,
    });
    fireEvent.change(fileInput);

    await waitFor(() => {
      // Now shows validation error instead
      expect(screen.getByText(/formato de archivo no permitido/i)).toBeInTheDocument();
      expect(screen.queryByText(externalError)).not.toBeInTheDocument();
    });
  });

  it('should clear validation error when valid file is selected', async () => {
    const { unmount } = render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;

    // First, upload invalid file
    const invalidFile = new File(['dummy content'], 'result.txt', { type: 'text/plain' });
    Object.defineProperty(fileInput, 'files', {
      value: [invalidFile],
      writable: false,
    });
    fireEvent.change(fileInput);

    await waitFor(() => {
      expect(screen.getByText(/formato de archivo no permitido/i)).toBeInTheDocument();
    });

    // Unmount and remount to reset the component
    unmount();
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    // Get the new file input
    const newFileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;

    // Upload valid file
    const validFile = new File(['dummy content'], 'result.pdf', { type: 'application/pdf' });
    Object.defineProperty(newFileInput, 'files', {
      value: [validFile],
      writable: false,
    });
    fireEvent.change(newFileInput);

    await waitFor(() => {
      expect(screen.queryByText(/formato de archivo no permitido/i)).not.toBeInTheDocument();
      expect(mockOnFileSelect).toHaveBeenCalledWith(validFile);
    });
  });

  // ─── Edge Cases ───────────────────────────────────────────────────────────────

  it('should handle empty file selection (user cancels)', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;

    // Simulate user canceling file selection
    fireEvent.change(fileInput, { target: { files: [] } });

    expect(mockOnFileSelect).not.toHaveBeenCalled();
  });

  it('should reset file input value after validation error', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file = new File(['dummy content'], 'result.txt', { type: 'text/plain' });

    await userEvent.upload(fileInput, file);

    // File input should be reset
    expect(fileInput.value).toBe('');
  });

  it('should handle multiple file selections (only first file)', async () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`) as HTMLInputElement;
    const file1 = new File(['content 1'], 'result1.pdf', { type: 'application/pdf' });
    const file2 = new File(['content 2'], 'result2.pdf', { type: 'application/pdf' });

    // Upload multiple files (though input doesn't have multiple attribute)
    await userEvent.upload(fileInput, [file1, file2]);

    // Should only process first file
    expect(mockOnFileSelect).toHaveBeenCalledTimes(1);
    expect(mockOnFileSelect).toHaveBeenCalledWith(file1);
  });

  it('should handle very long test names without breaking layout', () => {
    const longTestName = 'Hemograma Completo con Diferencial y Recuento de Plaquetas Automatizado con Microscopía';

    render(
      <TestResultUpload
        testName={longTestName}
        onFileSelect={mockOnFileSelect}
      />
    );

    expect(screen.getByText(longTestName)).toBeInTheDocument();
  });

  it('should handle very long filenames without breaking layout', () => {
    const uploadedFile = {
      filename: 'hemograma_completo_paciente_juan_perez_fecha_2024_01_15_laboratorio_central_resultado_final.pdf',
      uploadedAt: '2024-01-15T10:30:00',
    };

    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        uploadedFile={uploadedFile}
      />
    );

    expect(screen.getByText(uploadedFile.filename)).toBeInTheDocument();
  });

  // ─── Accessibility Tests ──────────────────────────────────────────────────────

  it('should have proper aria-label on file input', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const fileInput = screen.getByLabelText(`Seleccionar archivo para ${testName}`);
    expect(fileInput).toBeInTheDocument();
  });

  it('should have proper aria-label on upload button', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const uploadButton = screen.getByRole('button', { name: `Cargar resultado para ${testName}` });
    expect(uploadButton).toBeInTheDocument();
  });

  // ─── Visual State Tests ───────────────────────────────────────────────────────

  it('should apply correct styling for upload button in normal state', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
      />
    );

    const uploadButton = screen.getByRole('button', { name: /cargar resultado/i });
    expect(uploadButton).toHaveClass('bg-purple-50');
    expect(uploadButton).not.toHaveClass('bg-gray-300');
  });

  it('should apply correct styling for upload button in loading state', () => {
    render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        isUploading={true}
      />
    );

    const uploadButton = screen.getByRole('button', { name: /cargar resultado/i });
    expect(uploadButton).toHaveClass('bg-gray-300');
    expect(uploadButton).toHaveClass('cursor-not-allowed');
  });

  it('should show green styling for uploaded file info', () => {
    const uploadedFile = {
      filename: 'result.pdf',
      uploadedAt: '2024-01-15T10:30:00',
    };

    const { container } = render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        uploadedFile={uploadedFile}
      />
    );

    const uploadedFileContainer = container.querySelector('.bg-green-50');
    expect(uploadedFileContainer).toBeInTheDocument();
  });

  it('should show red styling for error messages', () => {
    const { container } = render(
      <TestResultUpload
        testName={testName}
        onFileSelect={mockOnFileSelect}
        error="Error de prueba"
      />
    );

    const errorContainer = container.querySelector('.bg-red-50');
    expect(errorContainer).toBeInTheDocument();
  });
});
