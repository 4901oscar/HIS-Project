import type { FC } from 'react';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface WizardProgressBarProps {
  currentStep: number; // 1-4
}

// ─── Step Configuration ───────────────────────────────────────────────────────

const STEPS = [
  { number: 1, label: 'Recolección de Muestras' },
  { number: 2, label: 'Validar Muestras' },
  { number: 3, label: 'Procesar Exámenes' },
  { number: 4, label: 'Resultados Listos' },
] as const;

// ─── Component ────────────────────────────────────────────────────────────────

const WizardProgressBar: FC<WizardProgressBarProps> = ({ currentStep }) => {
  return (
    <div className="mb-6 lg:mb-8">
      <div className="flex items-center justify-between">
        {STEPS.map((step, index) => {
          const isCompleted = step.number < currentStep;
          const isCurrent = step.number === currentStep;
          const isPending = step.number > currentStep;
          const isNotLast = index < STEPS.length - 1;

          return (
            <div key={step.number} className="flex items-center flex-1">
              {/* Step Circle */}
              <div className="flex flex-col items-center">
                <div
                  className={`
                    flex items-center justify-center w-10 h-10 lg:w-12 lg:h-12 rounded-full font-semibold text-sm lg:text-base
                    transition-all duration-200
                    ${isCompleted ? 'bg-green-500 text-white shadow-sm' : ''}
                    ${isCurrent ? 'bg-purple-600 text-white shadow-md ring-2 ring-purple-200 ring-offset-2' : ''}
                    ${isPending ? 'bg-gray-200 text-gray-500' : ''}
                  `}
                  aria-current={isCurrent ? 'step' : undefined}
                  aria-label={`Paso ${step.number}: ${step.label}`}
                >
                  {isCompleted ? (
                    <svg
                      className="w-5 h-5 lg:w-6 lg:h-6"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                      xmlns="http://www.w3.org/2000/svg"
                      aria-hidden="true"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M5 13l4 4L19 7"
                      />
                    </svg>
                  ) : (
                    step.number
                  )}
                </div>
                {/* Step Label */}
                <span
                  className={`
                    mt-2 text-xs lg:text-sm text-center max-w-[100px] lg:max-w-[140px]
                    transition-colors duration-200
                    ${isCurrent ? 'text-purple-600 font-semibold' : ''}
                    ${isCompleted ? 'text-green-600 font-medium' : ''}
                    ${isPending ? 'text-gray-500' : ''}
                  `}
                >
                  {step.label}
                </span>
              </div>

              {/* Progress Line */}
              {isNotLast && (
                <div
                  className={`
                    flex-1 h-1 mx-2 lg:mx-4 transition-all duration-300
                    ${isCompleted ? 'bg-green-500' : 'bg-gray-200'}
                  `}
                  aria-hidden="true"
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default WizardProgressBar;
