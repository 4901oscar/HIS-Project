import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import type { AppointmentListItem } from '../../services/appointmentService';
import type { LabOrderWithTestsResponse, LabResultResponse } from '../../api/labApi';
import { getLabOrderResults } from '../../api/labApi';
import { sendLabResultsToDoctor } from '../../api/clinicalApi';
import ResultsList from './ResultsList';
import ErrorMessage from '../common/ErrorMessage';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface ResultsReadyStepProps {
  appointment: AppointmentListItem;
  labOrder: LabOrderWithTestsResponse;
  onRefresh: () => void;
  onBack: () => void;
}

// ─── Component ────────────────────────────────────────────────────────────────

const ResultsReadyStep: FC<ResultsReadyStepProps> = ({
  appointment,
  labOrder,
  onBack,
}) => {
  const navigate = useNavigate();

  // State for results - Initialize with empty array to prevent undefined errors
  const [results, setResults] = useState<LabResultResponse[]>([]);
  const [isLoadingResults, setIsLoadingResults] = useState(true);

  // State for sending to doctor
  const [isSending, setIsSending] = useState(false);

  // State for messages
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Load results on mount
  useEffect(() => {
    const loadResults = async () => {
      setIsLoadingResults(true);
      setError(null);

      try {
        const data = await getLabOrderResults(labOrder.id);
        setResults(data);
      } catch (err) {
  
        const errorMessage =
          err instanceof Error
            ? err.message
            : 'Error al cargar los resultados. Por favor, intente nuevamente.';
        setError(errorMessage);
      } finally {
        setIsLoadingResults(false);
      }
    };

    loadResults();
  }, [labOrder.id]);

  // Handle send to doctor button click
  const handleSendToDoctor = async () => {
    setIsSending(true);
    setError(null);
    setSuccessMessage(null);

    try {
      // Call API to transition appointment status to CONSULTATION
      await sendLabResultsToDoctor(appointment.id);

      // Display success message
      setSuccessMessage('Resultados enviados al doctor exitosamente');

      // Navigate to appointment list after 2 seconds
      setTimeout(() => {
        navigate('/lab');
      }, 2000);
    } catch (err) {

      const errorMessage =
        err instanceof Error
          ? err.message
          : 'Error al enviar resultados al doctor. Por favor, intente nuevamente.';
      setError(errorMessage);
      setIsSending(false);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 lg:p-6">
      {/* Heading */}
      <h3 className="text-base lg:text-lg font-semibold text-gray-900 mb-4 lg:mb-6">
        Paso 4: Resultados Listos
      </h3>

      {/* Success Message */}
      {successMessage && (
        <div className="mb-4 lg:mb-6">
          <ErrorMessage
            message={successMessage}
            severity="info"
          />
        </div>
      )}

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

      {/* Loading State */}
      {isLoadingResults ? (
        <div className="flex items-center justify-center py-12">
          <div className="text-center">
            <svg
              className="animate-spin h-10 w-10 text-purple-600 mx-auto mb-4"
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
            <p className="text-sm text-gray-600">Cargando resultados...</p>
          </div>
        </div>
      ) : (
        <>
          {/* Results List */}
          <div className="mb-4 lg:mb-6">
            <h4 className="text-sm font-medium text-gray-700 mb-3">
              Resultados cargados ({results.length})
            </h4>
            <ResultsList results={results} />
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={onBack}
              disabled={isSending}
              className="px-5 lg:px-6 py-2.5 lg:py-3 rounded-lg font-medium text-sm lg:text-base border border-gray-300 text-gray-700 bg-white hover:bg-gray-50 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 disabled:opacity-50"
            >
              Volver
            </button>
            <button
              onClick={handleSendToDoctor}
              disabled={isSending || results.length === 0}
              className={`
                px-5 lg:px-6 py-2.5 lg:py-3 rounded-lg font-medium text-white text-sm lg:text-base
                transition-all duration-200
                flex items-center gap-2
                focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2
                ${
                  isSending || results.length === 0
                    ? 'bg-gray-300 cursor-not-allowed opacity-60'
                    : 'bg-purple-600 hover:bg-purple-700 hover:shadow-md active:bg-purple-800 active:scale-95 shadow-sm'
                }
              `}
              aria-label="Enviar a doctor"
            >
              {isSending ? (
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
                  <span>Enviando...</span>
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
                      d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
                    />
                  </svg>
                  <span>Enviar a doctor</span>
                </>
              )}
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default ResultsReadyStep;
