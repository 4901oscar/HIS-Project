import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import WizardProgressBar from '../../components/lab/WizardProgressBar';
import SampleCollectionStep from '../../components/lab/SampleCollectionStep';
import SampleValidationStep from '../../components/lab/SampleValidationStep';
import TestProcessingStep from '../../components/lab/TestProcessingStep';
import ResultsReadyStep from '../../components/lab/ResultsReadyStep';
import ErrorMessage from '../../components/common/ErrorMessage';
import { getAppointmentById } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import { getLabOrderByAppointmentId } from '../../api/labApi';
import type { LabOrderWithTestsResponse } from '../../api/labApi';

// ─── Step Mapping Function ────────────────────────────────────────────────────

/**
 * Maps appointment status to wizard step number.
 * 
 * @param status - The appointment status
 * @returns Step number (1-4)
 */
const getStepFromStatus = (status: string): number => {
  const stepMap: Record<string, number> = {
    'LAB_SAMPLE_COLLECTION': 1,
    'LAB_SAMPLE_PENDING': 2,
    'LAB_PROCESSING': 3,
    'LAB_RESULTS_READY': 4,
  };
  return stepMap[status] || 1;
};

// ─── Step Props Interface ─────────────────────────────────────────────────────

interface StepProps {
  appointment: AppointmentListItem;
  labOrder: LabOrderWithTestsResponse;
  onRefresh: () => void;
}

// ─── Main Container Component ─────────────────────────────────────────────────

const LabSampleWorkflow: FC = () => {
  const { appointmentId } = useParams<{ appointmentId: string }>();
  const navigate = useNavigate();
  
  const [appointment, setAppointment] = useState<AppointmentListItem | null>(null);
  const [labOrder, setLabOrder] = useState<LabOrderWithTestsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentStep, setCurrentStep] = useState(1);

  // Load appointment and lab order data
  const loadData = async () => {
    if (!appointmentId) {
      setError('ID de cita no proporcionado');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Fetch appointment data
      const apptData = await getAppointmentById(appointmentId);
      setAppointment(apptData);

      // Fetch lab order data
      const orderData = await getLabOrderByAppointmentId(appointmentId);
      setLabOrder(orderData);

      // Determine current step from appointment status
      const step = getStepFromStatus(apptData.status);
      setCurrentStep(step);
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Error al cargar los datos. Por favor, intente nuevamente.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appointmentId]);

  // Refresh handler for step components
  const handleRefresh = () => {
    loadData();
  };

  // Render step component based on current step
  const renderStep = () => {
    if (!appointment || !labOrder) {
      return null;
    }

    const stepProps: StepProps = {
      appointment,
      labOrder,
      onRefresh: handleRefresh,
    };

    switch (currentStep) {
      case 1:
        return <SampleCollectionStep {...stepProps} />;
      case 2:
        return <SampleValidationStep {...stepProps} />;
      case 3:
        return <TestProcessingStep {...stepProps} />;
      case 4:
        return <ResultsReadyStep {...stepProps} />;
      default:
        return null;
    }
  };

  // Loading state
  if (loading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-purple-600 border-t-transparent mb-4" />
            <p className="text-gray-600">Cargando flujo de laboratorio...</p>
          </div>
        </div>
      </MainLayout>
    );
  }

  // Error state
  if (error) {
    return (
      <MainLayout>
        <div className="max-w-2xl mx-auto mt-8">
          <ErrorMessage
            message={error}
            severity="error"
          />
          <div className="mt-4 flex gap-3">
            <button
              onClick={handleRefresh}
              className="px-4 py-2 bg-purple-600 text-white text-sm font-medium rounded-lg hover:bg-purple-700 hover:shadow-md transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 active:bg-purple-800 active:scale-95"
            >
              Reintentar
            </button>
            <button
              onClick={() => navigate('/lab')}
              className="px-4 py-2 bg-white text-purple-600 text-sm font-medium rounded-lg border border-purple-300 hover:bg-purple-50 hover:border-purple-400 hover:shadow-sm transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 active:bg-purple-100"
            >
              Volver a la lista
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  // Main wizard interface
  return (
    <MainLayout>
      <div className="space-y-4 lg:space-y-6">
        {/* Header */}
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3 lg:gap-0">
          <div>
            <h2 className="text-xl lg:text-2xl font-bold text-gray-900">Flujo de Laboratorio</h2>
            <p className="text-gray-500 text-sm lg:text-base mt-1">
              {appointment && (
                <>
                  Paciente: <span className="font-medium">{appointment.patient.fullName}</span>
                  {appointment.patient.dpi && (
                    <> · DPI: <span className="font-mono text-xs">{appointment.patient.dpi}</span></>
                  )}
                </>
              )}
            </p>
          </div>
          <button
            onClick={() => navigate('/lab')}
            className="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 active:bg-gray-200"
          >
            ← Volver a la lista
          </button>
        </div>

        {/* Wizard Progress Bar */}
        <WizardProgressBar currentStep={currentStep} />

        {/* Step Content */}
        <div className="min-h-[400px]">
          {renderStep()}
        </div>
      </div>
    </MainLayout>
  );
};

export default LabSampleWorkflow;
