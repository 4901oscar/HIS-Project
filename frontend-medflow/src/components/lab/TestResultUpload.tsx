import { useState, useRef } from 'react';
import type { FC, ChangeEvent } from 'react';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface TestResultUploadProps {
  testName: string;
  onFileSelect: (file: File) => void;
  uploadedFile?: { filename: string; uploadedAt: string } | null;
  isUploading?: boolean;
  error?: string | null;
}

// ─── Component ────────────────────────────────────────────────────────────────

/**
 * TestResultUpload Component
 * 
 * A reusable component for uploading test result files (PDF, JPEG, PNG).
 * Handles file selection, validation, and displays upload status.
 * 
 * Validates:
 * - File format: Only PDF, JPEG, PNG allowed
 * - File size: Maximum 10MB
 * 
 * All validation errors are displayed in Spanish.
 * 
 * @param testName - The name of the test
 * @param onFileSelect - Callback when a valid file is selected
 * @param uploadedFile - Optional uploaded file info to display
 * @param isUploading - Optional flag to show upload in progress
 * @param error - Optional error message to display
 */
const TestResultUpload: FC<TestResultUploadProps> = ({
  testName,
  onFileSelect,
  uploadedFile,
  isUploading = false,
  error = null,
}) => {
  const [validationError, setValidationError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // File validation constants
  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
  const ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/png'];
  const ALLOWED_EXTENSIONS = ['.pdf', '.jpg', '.jpeg', '.png'];

  /**
   * Validates file format by checking MIME type
   */
  const validateFileFormat = (file: File): boolean => {
    if (!ALLOWED_TYPES.includes(file.type)) {
      setValidationError(
        'Formato de archivo no permitido. Solo se aceptan PDF, JPEG y PNG.'
      );
      return false;
    }
    return true;
  };

  /**
   * Validates file size (max 10MB)
   */
  const validateFileSize = (file: File): boolean => {
    if (file.size > MAX_FILE_SIZE) {
      setValidationError(
        'El archivo excede el tamaño máximo permitido de 10 MB.'
      );
      return false;
    }
    return true;
  };

  /**
   * Handles file selection from input
   */
  const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
    // Clear previous validation errors
    setValidationError(null);

    const files = event.target.files;
    if (!files || files.length === 0) {
      return;
    }

    const file = files[0];

    // Validate file format
    if (!validateFileFormat(file)) {
      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      return;
    }

    // Validate file size
    if (!validateFileSize(file)) {
      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      return;
    }

    // File is valid, notify parent
    onFileSelect(file);
  };

  /**
   * Handles click on upload button
   */
  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  // Determine which error to display (validation or upload error)
  const displayError = validationError || error;

  return (
    <div className="border border-gray-200 rounded-lg p-3 lg:p-4 hover:border-purple-300 hover:shadow-sm transition-all duration-200">
      {/* Test Name Header */}
      <div className="flex items-center justify-between mb-3">
        <h5 className="font-medium text-gray-900 text-sm lg:text-base">{testName}</h5>
        
        {/* Upload Status Icon */}
        {uploadedFile && !isUploading && (
          <div className="flex items-center gap-1 text-green-600">
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
            <span className="text-xs lg:text-sm font-medium">Cargado</span>
          </div>
        )}
      </div>

      {/* File Input (Hidden) */}
      <input
        ref={fileInputRef}
        type="file"
        accept={ALLOWED_EXTENSIONS.join(',')}
        onChange={handleFileChange}
        className="hidden"
        disabled={isUploading}
        aria-label={`Seleccionar archivo para ${testName}`}
      />

      {/* Upload Button */}
      {!uploadedFile && (
        <button
          onClick={handleUploadClick}
          disabled={isUploading}
          className={`
            w-full px-4 py-2 lg:py-2.5 rounded-lg font-medium text-xs lg:text-sm
            transition-all duration-200
            flex items-center justify-center gap-2
            focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2
            ${
              isUploading
                ? 'bg-gray-300 cursor-not-allowed text-gray-600 opacity-60'
                : 'bg-purple-50 hover:bg-purple-100 hover:shadow-sm active:bg-purple-200 text-purple-700 border border-purple-200'
            }
          `}
          aria-label={`Cargar resultado para ${testName}`}
        >
          {isUploading ? (
            <>
              <svg
                className="animate-spin h-4 w-4"
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
              <span>Cargando...</span>
            </>
          ) : (
            <>
              <svg
                className="w-4 h-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                />
              </svg>
              <span>Cargar resultado</span>
            </>
          )}
        </button>
      )}

      {/* Uploaded File Info */}
      {uploadedFile && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-3">
          <div className="flex items-start gap-3">
            <svg
              className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
            <div className="flex-1 min-w-0">
              <p className="text-xs lg:text-sm font-medium text-green-900 truncate">
                {uploadedFile.filename}
              </p>
              <p className="text-xs text-green-700 mt-1">
                Cargado: {new Date(uploadedFile.uploadedAt).toLocaleString('es-ES', {
                  year: 'numeric',
                  month: 'short',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Error Message */}
      {displayError && (
        <div className="mt-3 bg-red-50 border border-red-200 rounded-lg p-3">
          <div className="flex items-start gap-2">
            <svg
              className="w-4 h-4 text-red-600 flex-shrink-0 mt-0.5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p className="text-xs text-red-700">{displayError}</p>
          </div>
        </div>
      )}

      {/* File Format Info */}
      {!uploadedFile && !displayError && (
        <p className="mt-2 text-xs text-gray-500">
          Formatos permitidos: PDF, JPEG, PNG (máx. 10 MB)
        </p>
      )}
    </div>
  );
};

export default TestResultUpload;
