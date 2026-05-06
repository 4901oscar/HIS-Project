import { useState, useEffect, useRef } from 'react';
import type { FC } from 'react';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface ErrorMessageProps {
  message: string;
  onDismiss?: () => void;
  autoDismiss?: boolean;
  autoDismissDelay?: number; // milliseconds, default 5000ms
  severity?: 'error' | 'warning' | 'info'; // default 'error'
}

// ─── Component ────────────────────────────────────────────────────────────────

/**
 * ErrorMessage Component
 * 
 * A reusable component for displaying error, warning, and info messages.
 * Supports auto-dismiss functionality and manual dismissal.
 * All messages are displayed in Spanish.
 * 
 * Features:
 * - Three severity levels: error (red), warning (yellow), info (blue)
 * - Auto-dismiss after configurable delay (default 5 seconds)
 * - Manual dismiss button
 * - Smooth enter/exit animations
 * - Consistent with MedFlow design system
 * 
 * @param message - The message to display (in Spanish)
 * @param onDismiss - Optional callback when message is dismissed
 * @param autoDismiss - Whether to auto-dismiss (default: false)
 * @param autoDismissDelay - Delay in ms before auto-dismiss (default: 5000)
 * @param severity - Message severity level (default: 'error')
 */
const ErrorMessage: FC<ErrorMessageProps> = ({
  message,
  onDismiss,
  autoDismiss = false,
  autoDismissDelay = 5000,
  severity = 'error',
}) => {
  const [isVisible, setIsVisible] = useState(true);
  const [isExiting, setIsExiting] = useState(false);
  const timerRef = useRef<number | null>(null);

  // Auto-dismiss logic
  useEffect(() => {
    if (autoDismiss && isVisible) {
      timerRef.current = setTimeout(() => {
        handleDismiss();
      }, autoDismissDelay);
    }

    // Cleanup timer on unmount
    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, [autoDismiss, autoDismissDelay, isVisible]);

  /**
   * Handles dismiss with exit animation
   */
  const handleDismiss = () => {
    // Start exit animation
    setIsExiting(true);

    // Wait for animation to complete before hiding
    setTimeout(() => {
      setIsVisible(false);
      if (onDismiss) {
        onDismiss();
      }
    }, 300); // Match animation duration
  };

  // Don't render if not visible
  if (!isVisible) {
    return null;
  }

  // Severity-based styling
  const severityStyles = {
    error: {
      container: 'bg-red-50 border-red-200',
      icon: 'text-red-600',
      text: 'text-red-700',
      button: 'text-red-400 hover:text-red-600',
    },
    warning: {
      container: 'bg-yellow-50 border-yellow-200',
      icon: 'text-yellow-600',
      text: 'text-yellow-700',
      button: 'text-yellow-400 hover:text-yellow-600',
    },
    info: {
      container: 'bg-blue-50 border-blue-200',
      icon: 'text-blue-600',
      text: 'text-blue-700',
      button: 'text-blue-400 hover:text-blue-600',
    },
  };

  const styles = severityStyles[severity];

  // Severity-based icons
  const renderIcon = () => {
    switch (severity) {
      case 'error':
        return (
          <svg
            className={`w-5 h-5 ${styles.icon} flex-shrink-0 mt-0.5`}
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        );
      case 'warning':
        return (
          <svg
            className={`w-5 h-5 ${styles.icon} flex-shrink-0 mt-0.5`}
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        );
      case 'info':
        return (
          <svg
            className={`w-5 h-5 ${styles.icon} flex-shrink-0 mt-0.5`}
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        );
    }
  };

  // ARIA role based on severity
  const ariaRole = severity === 'error' ? 'alert' : 'status';

  return (
    <div
      className={`
        border rounded-lg p-4
        transition-all duration-300 ease-in-out
        ${styles.container}
        ${isExiting ? 'opacity-0 scale-95' : 'opacity-100 scale-100'}
        ${!isExiting ? 'animate-slideIn' : ''}
      `}
      role={ariaRole}
      aria-live={severity === 'error' ? 'assertive' : 'polite'}
    >
      <div className="flex items-start gap-3">
        {/* Icon */}
        {renderIcon()}

        {/* Message */}
        <div className="flex-1 min-w-0">
          <p className={`text-sm ${styles.text}`}>{message}</p>
        </div>

        {/* Dismiss Button */}
        {onDismiss && (
          <button
            onClick={handleDismiss}
            className={`${styles.button} transition-colors flex-shrink-0`}
            aria-label="Cerrar mensaje"
          >
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
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        )}
      </div>
    </div>
  );
};

export default ErrorMessage;
