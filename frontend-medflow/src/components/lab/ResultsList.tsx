import type { FC } from 'react';
import type { LabResultResponse } from '../../api/labApi';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface ResultsListProps {
  results: LabResultResponse[];
}

// ─── Helper Functions ─────────────────────────────────────────────────────────

/**
 * Formats file size in bytes to human-readable format (KB, MB)
 */
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
};

/**
 * Formats ISO timestamp to Spanish locale date/time string
 */
const formatUploadDate = (isoString: string): string => {
  const date = new Date(isoString);
  return date.toLocaleString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

// ─── Component ────────────────────────────────────────────────────────────────

const ResultsList: FC<ResultsListProps> = ({ results }) => {
  // Empty state
  if (results.length === 0) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
        <div className="text-center py-8">
          <svg
            className="w-16 h-16 text-gray-300 mx-auto mb-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
          <p className="text-gray-500 text-sm">
            No hay resultados cargados para esta orden
          </p>
        </div>
      </div>
    );
  }

  // Results list
  return (
    <div className="space-y-3">
      {results.map((result) => (
        <div
          key={result.id}
          className="bg-white border border-gray-200 rounded-lg p-3 lg:p-4 hover:border-purple-300 hover:shadow-sm transition-all duration-200"
        >
          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3 sm:gap-4">
            {/* Left side: Test info and file details */}
            <div className="flex-1 min-w-0">
              {/* Test name */}
              <h5 className="font-medium text-gray-900 mb-2 text-sm lg:text-base">
                {result.testName}
              </h5>

              {/* File details */}
              <div className="space-y-1.5">
                {/* Original filename with icon */}
                <div className="flex items-center gap-2 text-xs lg:text-sm text-gray-600">
                  <svg
                    className="w-4 h-4 text-gray-400 flex-shrink-0"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"
                    />
                  </svg>
                  <span className="truncate" title={result.originalFilename}>
                    {result.originalFilename}
                  </span>
                </div>

                {/* File size */}
                <div className="flex items-center gap-2 text-xs lg:text-sm text-gray-600">
                  <svg
                    className="w-4 h-4 text-gray-400 flex-shrink-0"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4"
                    />
                  </svg>
                  <span>{formatFileSize(result.fileSize)}</span>
                </div>

                {/* Upload timestamp */}
                <div className="flex items-center gap-2 text-xs lg:text-sm text-gray-600">
                  <svg
                    className="w-4 h-4 text-gray-400 flex-shrink-0"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                  <span>
                    <span className="text-gray-500">Cargado:</span>{' '}
                    {formatUploadDate(result.uploadedAt)}
                  </span>
                </div>
              </div>
            </div>

            {/* Right side: Download button */}
            <div className="flex-shrink-0">
              <a
                href={result.downloadUrl}
                download={result.originalFilename}
                className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 hover:shadow-md active:bg-purple-800 active:scale-95 transition-all duration-200 text-xs lg:text-sm font-medium shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 w-full sm:w-auto"
                aria-label={`Descargar resultado de ${result.testName}`}
              >
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
                    d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                  />
                </svg>
                <span>Descargar</span>
              </a>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default ResultsList;
