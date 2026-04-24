import type { FC } from 'react';
import { CheckCircleIcon, XMarkIcon } from '@heroicons/react/24/outline';

interface SuccessAlertProps {
  message: string;
  onDismiss?: () => void;
}

const SuccessAlert: FC<SuccessAlertProps> = ({ message, onDismiss }) => (
  <div
    className="flex items-start gap-3 p-4 bg-green-50 border border-green-200 rounded-lg"
    role="status"
  >
    <CheckCircleIcon className="h-5 w-5 text-green-600 shrink-0 mt-0.5" aria-hidden="true" />
    <p className="flex-1 text-sm text-green-800">{message}</p>
    {onDismiss && (
      <button
        onClick={onDismiss}
        className="text-green-600 hover:text-green-800 transition-colors shrink-0"
        aria-label="Cerrar alerta"
      >
        <XMarkIcon className="h-4 w-4" aria-hidden="true" />
      </button>
    )}
  </div>
);

export default SuccessAlert;
