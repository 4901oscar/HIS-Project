import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { getLabResultsByAppointmentId, viewLabResult, downloadLabResult } from '../../api/labApi';
import type { LabResultResponse } from '../../api/labApi';

interface LabResultsSectionProps {
  appointmentId: string;
}

const LabResultsSection: FC<LabResultsSectionProps> = ({ appointmentId }) => {
  const [results, setResults] = useState<LabResultResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadResults = async () => {
      try {
        setLoading(true);
        const data = await getLabResultsByAppointmentId(appointmentId);
        setResults(data);
      } catch (err) {
        console.error('Error loading lab results:', err);
        setError(err instanceof Error ? err.message : 'Error al cargar resultados');
      } finally {
        setLoading(false);
      }
    };

    loadResults();
  }, [appointmentId]);

  const handleView = (result: LabResultResponse) => {
    viewLabResult(result.id);
  };

  const handleDownload = async (result: LabResultResponse) => {
    try {
      await downloadLabResult(result.id, result.originalFilename);
    } catch (err) {
      console.error('Error downloading file:', err);
      alert('Error al descargar el archivo. Por favor, intente nuevamente.');
    }
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <h3 className="text-sm font-semibold text-gray-900 mb-4">Resultados de Laboratorio</h3>
        <div className="flex items-center justify-center py-8">
          <div className="inline-block animate-spin rounded-full h-6 w-6 border-4 border-medin-cyan border-t-transparent"></div>
          <span className="ml-3 text-sm text-gray-500">Cargando resultados...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <h3 className="text-sm font-semibold text-gray-900 mb-4">Resultados de Laboratorio</h3>
        <div className="text-center py-8">
          <p className="text-sm text-red-600">{error}</p>
        </div>
      </div>
    );
  }

  if (results.length === 0) {
    return null; // Don't show section if no results
  }

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-gray-900">Resultados de Laboratorio</h3>
        <span className="px-2 py-0.5 rounded-full bg-green-100 text-green-700 text-xs font-medium">
          {results.length} {results.length === 1 ? 'resultado' : 'resultados'}
        </span>
      </div>

      <div className="space-y-3">
        {results.map((result) => (
          <div
            key={result.id}
            className="flex items-center justify-between p-4 bg-gray-50 rounded-lg border border-gray-200 hover:border-medin-cyan transition-colors"
          >
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">
                {result.testName}
              </p>
              <div className="flex items-center gap-3 mt-1">
                <p className="text-xs text-gray-500 truncate">
                  {result.originalFilename}
                </p>
                <span className="text-xs text-gray-400">•</span>
                <p className="text-xs text-gray-500">
                  {new Date(result.uploadedAt).toLocaleDateString('es-GT', {
                    day: '2-digit',
                    month: 'short',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2 ml-4">
              {/* View button */}
              <button
                type="button"
                onClick={() => handleView(result)}
                className="p-2 text-medin-cyan hover:bg-medin-cyan hover:text-white rounded-lg transition-colors"
                title="Ver resultado"
              >
                <svg
                  className="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                  />
                </svg>
              </button>

              {/* Download button */}
              <button
                type="button"
                onClick={() => handleDownload(result)}
                className="p-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors"
                title="Descargar resultado"
              >
                <svg
                  className="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                  />
                </svg>
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default LabResultsSection;
