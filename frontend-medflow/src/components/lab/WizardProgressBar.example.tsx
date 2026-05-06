import { useState } from 'react';
import WizardProgressBar from './WizardProgressBar';

/**
 * Example usage of WizardProgressBar component
 * 
 * This example demonstrates how the progress bar changes as the user
 * moves through the 4 steps of the laboratory workflow.
 */
const WizardProgressBarExample = () => {
  const [currentStep, setCurrentStep] = useState(1);

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">WizardProgressBar Example</h1>
      
      <WizardProgressBar currentStep={currentStep} />
      
      <div className="mt-8 flex gap-4">
        <button
          onClick={() => setCurrentStep(1)}
          className={`px-4 py-2 rounded ${
            currentStep === 1 ? 'bg-purple-600 text-white' : 'bg-gray-200'
          }`}
        >
          Step 1
        </button>
        <button
          onClick={() => setCurrentStep(2)}
          className={`px-4 py-2 rounded ${
            currentStep === 2 ? 'bg-purple-600 text-white' : 'bg-gray-200'
          }`}
        >
          Step 2
        </button>
        <button
          onClick={() => setCurrentStep(3)}
          className={`px-4 py-2 rounded ${
            currentStep === 3 ? 'bg-purple-600 text-white' : 'bg-gray-200'
          }`}
        >
          Step 3
        </button>
        <button
          onClick={() => setCurrentStep(4)}
          className={`px-4 py-2 rounded ${
            currentStep === 4 ? 'bg-purple-600 text-white' : 'bg-gray-200'
          }`}
        >
          Step 4
        </button>
      </div>
      
      <div className="mt-8 p-4 bg-gray-50 rounded">
        <h2 className="font-semibold mb-2">Current Step: {currentStep}</h2>
        <p className="text-sm text-gray-600">
          {currentStep === 1 && 'Recolección de Muestras - Collect samples from the patient'}
          {currentStep === 2 && 'Validar Muestras - Validate that samples are adequate'}
          {currentStep === 3 && 'Procesar Exámenes - Process tests and upload results'}
          {currentStep === 4 && 'Resultados Listos - Results ready to send to doctor'}
        </p>
      </div>
    </div>
  );
};

export default WizardProgressBarExample;
