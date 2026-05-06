import { useState, useEffect } from 'react';
import type { FC } from 'react';
import type { AppointmentListItem } from '../../services/appointmentService';
import type { LabOrderWithTestsResponse, LabResultResponse } from '../../api/labApi';
import { uploadTestResult, validateAllTestsComplete, getLabOrderResults } from '../../api/labApi';
import { completeLabProcessing } from '../../api/clinicalApi';
import TestResultUpload from './TestResultUpload';
import ErrorMessage from '../common/ErrorMessage';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface TestProcessingStepProps {
  appointment: AppointmentListItem;
  labOrder: LabOrderWithTestsResponse;
  onRefresh: () => void;
}

// ─── Types ────────────────────────────────────────────────────────────────────

interface UploadedResult {
  file: File;
  result: LabResultResponse;
}

// ─── Component ────────────────────────────────────────────────────────────────

const TestProcessingStep: FC<TestProcessingStepProps> = ({
  appointment,
  labOrder,
  onRefresh,
}) => {
  // State for tracking uploaded results per test
  const [uploadedResults, setUploadedResults] = useState<Map<string, UploadedResult>>(new Map());
  
  // State for existing results from database
  const [existingResults, setExistingResults] = useState<Map<string, LabResultResponse>>(new Map());
  
  // State for tracking which tests are currently uploading
  const [uploadingTests, setUploadingTests] = useState<Set<string>>(new Set());
  
  // State for the "Marcar como completado" button
  const [isCompleting, setIsCompleting] = useState(false);
  
  // State for error messages
  const [error, setError] = useState<string | null>(null);
  
  // State for upload errors per test
  const [uploadErrors, setUploadErrors] = useState<Map<string, string>>(new Map());

  /**
   * Load existing results from the database on component mount
   */
  useEffect(() => {
    const loadExistingResults = async () => {
      try {
        const results = await getLabOrderResults(labOrder.id);
        const resultsMap = new Map<string, LabResultResponse>();
        results.forEach(result => {
          resultsMap.set(result.testName, result);
        });
        setExistingResults(resultsMap);
      } catch (err) {
        console.error('Error al cargar resultados existentes:', err);
      }
    };

    loadExistingResults();
  }, [labOrder.id]);

  /**
   * Handles file selection for a specific test.
   * Uploads the file to the server and tracks the result.
   */
  const handleFileSelect = async (testName: string, file: File) => {
    // Clear any previous errors for this test
    setUploadErrors(prev => {
      const newErrors = new Map(prev);
      newErrors.delete(testName);
      return newErrors;
    });
    setError(null);

    // Mark test as uploading
    setUploadingTests(prev => new Set(prev).add(testName));

    try {
      // Call API to upload the file
      const result = await uploadTestResult(labOrder.id, testName, file);

      // Store the uploaded result
      setUploadedResults(prev => {
        const newResults = new Map(prev);
        newResults.set(testName, { file, result });
        return newResults;
      });
    } catch (err) {
      console.error(`Error al cargar resultado para ${testName}:`, err);
      const errorMessage =
        err instanceof Error
          ? err.message
          : 'Error al cargar el resultado. Por favor, intente nuevamente.';
      
      // Store error for this specific test
      setUploadErrors(prev => {
        const newErrors = new Map(prev);
        newErrors.set(testName, errorMessage);
        return newErrors;
      });
    } finally {
      // Remove test from uploading set
      setUploadingTests(prev => {
        const newSet = new Set(prev);
        newSet.delete(testName);
        return newSet;
      });
    }
  };

  /**
   * Handles the "Marcar como completado" button click.
   * Validates that all tests have results, then completes the processing.
   */
  const handleMarkComplete = async () => {
    console.log('=== handleMarkComplete called ===');
    setIsCompleting(true);
    setError(null);

    try {
      console.log('About to call validateAllTestsComplete with orderId:', labOrder.id);
      
      // Step 1: Validate that all tests have uploaded results
      const validation = await validateAllTestsComplete(labOrder.id);

      console.log('Validation result:', validation);

      if (!validation.valid) {
        // Validation failed - display error with missing test names
        const missingTestsList = validation.missingTests?.length > 0 
          ? validation.missingTests.join(', ')
          : 'No especificados';
        console.log('Validation failed. Missing tests:', missingTestsList);
        setError(
          `No se puede completar el procesamiento. Faltan resultados para los siguientes exámenes: ${missingTestsList}`
        );
        return;
      }

      console.log('Validation passed! Calling completeLabProcessing...');

      // Step 2: Validation passed - call completeLabProcessing API
      await completeLabProcessing(appointment.id);

      console.log('Lab processing completed successfully!');

      // Step 3: Refresh wizard to show step 4
      onRefresh();
    } catch (err) {
      console.error('Error al completar procesamiento:', err);
      const errorMessage =
        err instanceof Error
          ? err.message
          : 'Error al completar el procesamiento. Por favor, intente nuevamente.';
      setError(errorMessage);
    } finally {
      setIsCompleting(false);
    }
  };

  // Check if any uploads are in progress
  const hasUploadsInProgress = uploadingTests.size > 0;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 lg:p-6">
      {/* Heading */}
      <h3 className="text-base lg:text-lg font-semibold text-gray-900 mb-4 lg:mb-6">
        Paso 3: Procesar Exámenes
      </h3>

      {/* Global Error Message */}
      {error && (
        <div className="mb-4 lg:mb-6">
          <ErrorMessage
            message={error}
            onDismiss={() => setError(null)}
            severity="error"
          />
        </div>
      )}

      {/* Test List with Upload Controls */}
      <div className="mb-4 lg:mb-6">
        <h4 className="text-sm font-medium text-gray-700 mb-3">
          Exámenes a procesar ({labOrder.tests.length})
        </h4>

        {labOrder.tests.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <p className="text-sm">No hay exámenes en esta orden</p>
          </div>
        ) : (
          <div className="space-y-4">
            {labOrder.tests.map((test) => {
              const uploadedResult = uploadedResults.get(test.testName);
              const existingResult = existingResults.get(test.testName);
              const isUploading = uploadingTests.has(test.testName);
              const uploadError = uploadErrors.get(test.testName);

              // Determine which result to display (newly uploaded or existing)
              const resultToDisplay = uploadedResult 
                ? {
                    filename: uploadedResult.result.originalFilename,
                    uploadedAt: uploadedResult.result.uploadedAt,
                  }
                : existingResult
                ? {
                    filename: existingResult.originalFilename,
                    uploadedAt: existingResult.uploadedAt,
                  }
                : null;

              return (
                <TestResultUpload
                  key={test.testName}
                  testName={test.testName}
                  onFileSelect={(file) => handleFileSelect(test.testName, file)}
                  uploadedFile={resultToDisplay}
                  isUploading={isUploading}
                  error={uploadError || null}
                />
              );
            })}
          </div>
        )}
      </div>

      {/* Loading Indicator for Uploads in Progress */}
      {hasUploadsInProgress && (
        <div className="mb-4 lg:mb-6 bg-blue-50 border border-blue-200 rounded-lg p-3 lg:p-4">
          <div className="flex items-center gap-3">
            <svg
              className="animate-spin h-5 w-5 text-blue-600"
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
            <p className="text-xs lg:text-sm text-blue-700">
              Cargando resultados... ({uploadingTests.size} en progreso)
            </p>
          </div>
        </div>
      )}

      {/* Action Button */}
      <div className="flex justify-end">
        <button
          onClick={handleMarkComplete}
          disabled={isCompleting || hasUploadsInProgress || labOrder.tests.length === 0}
          className={`
            px-5 lg:px-6 py-2.5 lg:py-3 rounded-lg font-medium text-white text-sm lg:text-base
            transition-all duration-200
            flex items-center gap-2
            focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2
            ${
              isCompleting || hasUploadsInProgress || labOrder.tests.length === 0
                ? 'bg-gray-300 cursor-not-allowed opacity-60'
                : 'bg-purple-600 hover:bg-purple-700 hover:shadow-md active:bg-purple-800 active:scale-95 shadow-sm'
            }
          `}
          aria-label="Marcar como completado"
        >
          {isCompleting ? (
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
              <span>Completando...</span>
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
              <span>Marcar como completado</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default TestProcessingStep;
