import type { FC } from 'react';
import { XCircleIcon, XMarkIcon } from '@heroicons/react/24/outline';

interface ErrorAlertProps {
  message: string;
  onDismiss?: () => void;
  onRetry?: () => void;
}

const ErrorAlert: FC<ErrorAlertProps> = ({ message, onDismiss, onRetry }) => (
  <div
    className="flex items-start gap-3 p-4 bg-red-50 border border-red-200 rounded-lg"
    role="alert"
  >
    <XCircleIcon className="h-5 w-5 text-red-600 shrink-0 mt-0.5" aria-hidden="true" />
    <p className="flex-1 text-sm text-red-800">{message}</p>
    <div className="flex items-center gap-2 shrink-0">
      {onRetry && (
        <button
          onClick={onRetry}
          className="text-xs font-medium text-red-700 hover:text-red-900 underline transition-colors"
          aria-label="Reintentar operación"
        >
          Reintentar
        </button>
      )}
      {onDismiss && (
        <button
          onClick={onDismiss}
          className="text-red-600 hover:text-red-800 transition-colors"
          aria-label="Cerrar alerta"
        >
          <XMarkIcon className="h-4 w-4" aria-hidden="true" />
        </button>
      )}
    </div>
  </div>
);

export default ErrorAlert;
