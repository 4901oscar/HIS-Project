import { useState } from 'react';
import type { FC } from 'react';
import type { AppointmentListItem } from '../../services/appointmentService';
import type { LabOrderWithTestsResponse } from '../../api/labApi';
import { collectLabSamples } from '../../api/clinicalApi';
import ErrorMessage from '../common/ErrorMessage';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface SampleCollectionStepProps {
  appointment: AppointmentListItem;
  labOrder: LabOrderWithTestsResponse;
  onRefresh: () => void;
}

// ─── Component ────────────────────────────────────────────────────────────────

const SampleCollectionStep: FC<SampleCollectionStepProps> = ({
  appointment,
  labOrder,
  onRefresh,
}) => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Handle collect samples button click
  const handleCollectSamples = async () => {
    setIsLoading(true);
    setError(null);

    try {
      // Call API to transition appointment status
      await collectLabSamples(appointment.id);

      // Refresh wizard to show step 2
      onRefresh();
    } catch (err) {
      console.error('Error al recolectar muestras:', err);
      const errorMessage =
        err instanceof Error
          ? err.message
          : 'Error al recolectar muestras. Por favor, intente nuevamente.';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 lg:p-6">
      {/* Heading */}
      <h3 className="text-base lg:text-lg font-semibold text-gray-900 mb-4 lg:mb-6">
        Paso 1: Recolección de Muestras
      </h3>

      {/* Error Message */}
      {error && (
        <div className="mb-4 lg:mb-6">
          <ErrorMessage
            message={error}
            onDismiss={() => setError(null)}
            severity="error"
          />
        </div>
      )}

      {/* Test List */}
      <div className="mb-4 lg:mb-6">
        <h4 className="text-sm font-medium text-gray-700 mb-3">
          Exámenes solicitados ({labOrder.tests.length})
        </h4>

        {labOrder.tests.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <p className="text-sm">No hay exámenes en esta orden</p>
          </div>
        ) : (
          <div className="space-y-3">
            {labOrder.tests.map((test, index) => (
              <div
                key={`${test.testName}-${index}`}
                className="border border-gray-200 rounded-lg p-3 lg:p-4 hover:border-purple-300 hover:shadow-sm transition-all duration-200"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h5 className="font-medium text-gray-900 mb-1 text-sm lg:text-base">
                      {test.testName}
                    </h5>
                    <div className="flex flex-wrap gap-2 lg:gap-3 text-xs lg:text-sm text-gray-600">
                      <div className="flex items-center gap-1">
                        <svg
                          className="w-4 h-4 text-gray-400"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                          />
                        </svg>
                        <span>
                          <span className="text-gray-500">Tipo:</span>{' '}
                          {test.testType}
                        </span>
                      </div>
                      <div className="flex items-center gap-1">
                        <svg
                          className="w-4 h-4 text-gray-400"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"
                          />
                        </svg>
                        <span>
                          <span className="text-gray-500">Muestra:</span>{' '}
                          {test.sampleType}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Action Button */}
      <div className="flex justify-end">
        <button
          onClick={handleCollectSamples}
          disabled={isLoading || labOrder.tests.length === 0}
          className={`
            px-5 lg:px-6 py-2.5 lg:py-3 rounded-lg font-medium text-white text-sm lg:text-base
            transition-all duration-200
            flex items-center gap-2
            focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2
            ${
              isLoading || labOrder.tests.length === 0
                ? 'bg-gray-300 cursor-not-allowed opacity-60'
                : 'bg-purple-600 hover:bg-purple-700 hover:shadow-md active:bg-purple-800 active:scale-95 shadow-sm'
            }
          `}
          aria-label="Recolectar muestras"
        >
          {isLoading ? (
            <>
              <svg
                className="animate-spin h-5 w-5 text-white"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
              <span>Recolectando...</span>
            </>
          ) : (
            <>
              <svg
                className="w-5 h-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <span>Recolectar Muestras</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default SampleCollectionStep;
