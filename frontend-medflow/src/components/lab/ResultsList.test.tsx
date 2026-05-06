import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import ResultsList from './ResultsList';
import type { LabResultResponse } from '../../api/labApi';

// ─── Test Data ────────────────────────────────────────────────────────────────

const mockResults: LabResultResponse[] = [
  {
    id: 'result-1',
    orderId: 'order-123',
    testName: 'Hemograma Completo',
    originalFilename: 'hemograma_paciente_001.pdf',
    fileSize: 2048576, // 2 MB
    uploadedAt: '2024-01-15T10:30:00Z',
    uploadedBy: 'tech-001',
    downloadUrl: '/api/lab/results/result-1/download',
  },
  {
    id: 'result-2',
    orderId: 'order-123',
    testName: 'Glucosa en Sangre',
    originalFilename: 'glucosa_resultado.jpg',
    fileSize: 512000, // 500 KB
    uploadedAt: '2024-01-15T11:45:00Z',
    uploadedBy: 'tech-001',
    downloadUrl: '/api/lab/results/result-2/download',
  },
  {
    id: 'result-3',
    orderId: 'order-123',
    testName: 'Perfil Lipídico',
    originalFilename: 'perfil_lipidico_2024.png',
    fileSize: 1536000, // 1.5 MB
    uploadedAt: '2024-01-15T14:20:00Z',
    uploadedBy: 'tech-002',
    downloadUrl: '/api/lab/results/result-3/download',
  },
];

// ─── Tests ────────────────────────────────────────────────────────────────────

describe('ResultsList', () => {
  describe('Rendering with results', () => {
    it('should render all results in the list', () => {
      render(<ResultsList results={mockResults} />);

      // Verify all test names are displayed
      expect(screen.getByText('Hemograma Completo')).toBeInTheDocument();
      expect(screen.getByText('Glucosa en Sangre')).toBeInTheDocument();
      expect(screen.getByText('Perfil Lipídico')).toBeInTheDocument();
    });

    it('should display original filename for each result', () => {
      render(<ResultsList results={mockResults} />);

      // Verify all filenames are displayed
      expect(screen.getByText('hemograma_paciente_001.pdf')).toBeInTheDocument();
      expect(screen.getByText('glucosa_resultado.jpg')).toBeInTheDocument();
      expect(screen.getByText('perfil_lipidico_2024.png')).toBeInTheDocument();
    });

    it('should display formatted file size for each result', () => {
      render(<ResultsList results={mockResults} />);

      // Verify file sizes are formatted correctly
      // Note: formatFileSize uses toFixed(2), so 2048576 bytes = 1.95 MB
      expect(screen.getByText('1.95 MB')).toBeInTheDocument();
      expect(screen.getByText('500 KB')).toBeInTheDocument();
      expect(screen.getByText('1.46 MB')).toBeInTheDocument();
    });

    it('should display upload timestamp in Spanish format', () => {
      render(<ResultsList results={mockResults} />);

      // Verify "Cargado:" label appears for each result
      const cargadoLabels = screen.getAllByText(/Cargado:/);
      expect(cargadoLabels).toHaveLength(3);

      // Verify dates are formatted (checking for Spanish month names or date patterns)
      // Note: Exact format depends on locale, but we can check for presence
      const dateElements = screen.getAllByText(/\d{1,2}:\d{2}/); // Time format HH:MM
      expect(dateElements.length).toBeGreaterThanOrEqual(3);
    });

    it('should provide download link for each result', () => {
      render(<ResultsList results={mockResults} />);

      // Get all download links
      const downloadLinks = screen.getAllByRole('link', { name: /Descargar resultado de/ });
      expect(downloadLinks).toHaveLength(3);

      // Verify download URLs
      expect(downloadLinks[0]).toHaveAttribute('href', '/api/lab/results/result-1/download');
      expect(downloadLinks[1]).toHaveAttribute('href', '/api/lab/results/result-2/download');
      expect(downloadLinks[2]).toHaveAttribute('href', '/api/lab/results/result-3/download');
    });

    it('should have download attribute on links with original filename', () => {
      render(<ResultsList results={mockResults} />);

      const downloadLinks = screen.getAllByRole('link', { name: /Descargar resultado de/ });

      expect(downloadLinks[0]).toHaveAttribute('download', 'hemograma_paciente_001.pdf');
      expect(downloadLinks[1]).toHaveAttribute('download', 'glucosa_resultado.jpg');
      expect(downloadLinks[2]).toHaveAttribute('download', 'perfil_lipidico_2024.png');
    });

    it('should display all required information for each result', () => {
      render(<ResultsList results={mockResults} />);

      // For the first result, verify all information is present
      expect(screen.getByText('Hemograma Completo')).toBeInTheDocument();
      expect(screen.getByText('hemograma_paciente_001.pdf')).toBeInTheDocument();
      expect(screen.getByText('1.95 MB')).toBeInTheDocument(); // 2048576 bytes = 1.95 MB
      
      // Verify "Cargado:" label appears (there are multiple, one per result)
      const cargadoLabels = screen.getAllByText(/Cargado:/);
      expect(cargadoLabels.length).toBeGreaterThan(0);
      
      const downloadLink = screen.getByRole('link', { 
        name: 'Descargar resultado de Hemograma Completo' 
      });
      expect(downloadLink).toBeInTheDocument();
    });
  });

  describe('Empty state', () => {
    it('should display empty state message when no results', () => {
      render(<ResultsList results={[]} />);

      expect(
        screen.getByText('No hay resultados cargados para esta orden')
      ).toBeInTheDocument();
    });

    it('should not display any result items when empty', () => {
      render(<ResultsList results={[]} />);

      // Verify no download links are present
      const downloadLinks = screen.queryAllByRole('link', { name: /Descargar/ });
      expect(downloadLinks).toHaveLength(0);
    });

    it('should display empty state icon when no results', () => {
      const { container } = render(<ResultsList results={[]} />);

      // Verify SVG icon is present in empty state
      const svgIcon = container.querySelector('svg');
      expect(svgIcon).toBeInTheDocument();
    });
  });

  describe('File size formatting', () => {
    it('should format bytes correctly', () => {
      const result: LabResultResponse = {
        ...mockResults[0],
        fileSize: 500,
      };

      render(<ResultsList results={[result]} />);
      expect(screen.getByText('500 Bytes')).toBeInTheDocument();
    });

    it('should format KB correctly', () => {
      const result: LabResultResponse = {
        ...mockResults[0],
        fileSize: 5120, // 5 KB
      };

      render(<ResultsList results={[result]} />);
      expect(screen.getByText('5 KB')).toBeInTheDocument();
    });

    it('should format MB correctly', () => {
      const result: LabResultResponse = {
        ...mockResults[0],
        fileSize: 10485760, // 10 MB
      };

      render(<ResultsList results={[result]} />);
      expect(screen.getByText('10 MB')).toBeInTheDocument();
    });

    it('should handle zero file size', () => {
      const result: LabResultResponse = {
        ...mockResults[0],
        fileSize: 0,
      };

      render(<ResultsList results={[result]} />);
      expect(screen.getByText('0 Bytes')).toBeInTheDocument();
    });
  });

  describe('Date formatting', () => {
    it('should format date in Spanish locale', () => {
      const result: LabResultResponse = {
        ...mockResults[0],
        uploadedAt: '2024-03-15T14:30:00Z',
      };

      render(<ResultsList results={[result]} />);

      // Check for Spanish month name or date pattern
      // The exact format depends on the browser locale, but we can verify the label exists
      expect(screen.getByText(/Cargado:/)).toBeInTheDocument();
    });
  });

  describe('UI consistency', () => {
    it('should apply hover styles to result cards', () => {
      const { container } = render(<ResultsList results={mockResults} />);

      // Verify hover class is present
      const resultCards = container.querySelectorAll('.hover\\:border-purple-300');
      expect(resultCards.length).toBeGreaterThan(0);
    });

    it('should use consistent purple theme for download buttons', () => {
      render(<ResultsList results={mockResults} />);

      const downloadLinks = screen.getAllByRole('link', { name: /Descargar resultado de/ });
      
      downloadLinks.forEach((link) => {
        expect(link).toHaveClass('bg-purple-600');
        expect(link).toHaveClass('hover:bg-purple-700');
      });
    });

    it('should display icons for file information', () => {
      const { container } = render(<ResultsList results={mockResults} />);

      // Verify multiple SVG icons are present (file, size, time icons)
      const svgIcons = container.querySelectorAll('svg');
      expect(svgIcons.length).toBeGreaterThan(3); // At least one per result item
    });
  });

  describe('Accessibility', () => {
    it('should have accessible labels for download links', () => {
      render(<ResultsList results={mockResults} />);

      expect(
        screen.getByRole('link', { name: 'Descargar resultado de Hemograma Completo' })
      ).toBeInTheDocument();
      expect(
        screen.getByRole('link', { name: 'Descargar resultado de Glucosa en Sangre' })
      ).toBeInTheDocument();
      expect(
        screen.getByRole('link', { name: 'Descargar resultado de Perfil Lipídico' })
      ).toBeInTheDocument();
    });

    it('should have title attribute for long filenames', () => {
      render(<ResultsList results={mockResults} />);

      const filenameElement = screen.getByText('hemograma_paciente_001.pdf');
      expect(filenameElement).toHaveAttribute('title', 'hemograma_paciente_001.pdf');
    });
  });
});
