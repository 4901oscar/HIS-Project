import { useState, useEffect, useCallback } from 'react';
import type { FC } from 'react';
import Swal from 'sweetalert2';
import { MainLayout } from '../../components/Layout';
import PharmacyQueue from '../../components/pharmacy/PharmacyQueue';
import PrescriptionDetail from '../../components/pharmacy/PrescriptionDetail';
import { listAppointments } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import { getPrescriptionByAppointment, dispenseMedication } from '../../services/clinicalService';
import type { PrescriptionDetailResponse } from '../../services/clinicalService';
import { getServiceItems } from '../../services/billingCatalogService';
import type { ServiceItemResponse } from '../../services/billingCatalogService';

type View = 'queue' | 'detail';

const PharmacyPage: FC = () => {
  // State
  const [appointments, setAppointments] = useState<AppointmentListItem[]>([]);
  const [selectedAppointment, setSelectedAppointment] = useState<AppointmentListItem | null>(null);
  const [prescriptionDetails, setPrescriptionDetails] = useState<PrescriptionDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadingPrescription, setLoadingPrescription] = useState(false);
  const [dispensing, setDispensing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<View>('queue');
  const [medicationCatalog, setMedicationCatalog] = useState<ServiceItemResponse[]>([]);

  // Load pharmacy queue
  const loadQueue = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await listAppointments({ queue: 'pharmacy' });
      setAppointments(data);
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Error al cargar la cola de farmacia';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, []);

  // Load queue and medication catalog on mount
  useEffect(() => {
    loadQueue();
    getServiceItems('MEDICATION').then(data => setMedicationCatalog(data.filter(m => m.status === 'ACTIVE'))).catch(() => {});
  }, [loadQueue]);

  // Select appointment and load prescription details
  const selectAppointment = async (appointment: AppointmentListItem) => {
    // Llamar al paciente por nombre
    const utterance = new SpeechSynthesisUtterance(
      `${appointment.patient.fullName}, por favor pasar a Farmacia`
    );
    utterance.lang = 'es-GT';
    utterance.rate = 0.9;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);

    setSelectedAppointment(appointment);
    setLoadingPrescription(true);
    setError(null);

    try {
      const prescription = await getPrescriptionByAppointment(appointment.id);
      setPrescriptionDetails(prescription);
      setView('detail');
    } catch (err: unknown) {
      const axiosErr = err as { response?: { status?: number } };
      if (axiosErr.response?.status === 404) {
        setError('Este paciente no tiene receta registrada en el sistema.');
      } else {
        const errorMessage = err instanceof Error ? err.message : 'Error al cargar los detalles de la receta';
        setError(errorMessage);
      }
      setSelectedAppointment(null);
    } finally {
      setLoadingPrescription(false);
    }
  };

  // Dispense medication
  const handleDispenseMedication = async (appointmentId: string) => {
    setDispensing(true);
    setError(null);
    
    try {
      await dispenseMedication(appointmentId);
      
      await Swal.fire({
        icon: 'success',
        title: 'Medicamento entregado exitosamente',
        confirmButtonText: 'Aceptar',
        confirmButtonColor: '#16a34a',
      });

      // Return to queue and refresh
      returnToQueue();
      await loadQueue();
    } catch (err: unknown) {
      const errorMessage = err instanceof Error
        ? err.message
        : 'Error al dispensar medicamentos. Por favor, intente nuevamente.';
      setError(errorMessage);
      await Swal.fire({
        icon: 'error',
        title: 'Error',
        text: errorMessage,
        confirmButtonText: 'Aceptar',
        confirmButtonColor: '#dc2626',
      });
    } finally {
      setDispensing(false);
    }
  };

  // Return to queue view
  const returnToQueue = () => {
    setView('queue');
    setSelectedAppointment(null);
    setPrescriptionDetails(null);
    setError(null);
  };

  // Render queue view
  const renderQueueView = () => {
    if (error) {
      return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
          <div className="text-center">
            <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-red-100 mb-4">
              <svg className="w-6 h-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Error al cargar la cola</h3>
            <p className="text-sm text-gray-600 mb-4">{error}</p>
            <button
              onClick={loadQueue}
              className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 hover:shadow-md transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
            >
              Reintentar
            </button>
          </div>
        </div>
      );
    }

    return (
      <PharmacyQueue
        appointments={appointments}
        loading={loading}
        onSelect={selectAppointment}
        onRefresh={loadQueue}
      />
    );
  };

  // Render detail view
  const renderDetailView = () => {
    if (loadingPrescription) {
      return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-green-600 border-t-transparent mb-4" />
            <p className="text-gray-600">Cargando detalles de la receta...</p>
          </div>
        </div>
      );
    }

    if (error) {
      return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
          <div className="text-center">
            <div className="inline-flex items-center justify-center w-12 h-12 rounded-full bg-red-100 mb-4">
              <svg className="w-6 h-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Error al cargar la receta</h3>
            <p className="text-sm text-gray-600 mb-4">{error}</p>
            <div className="flex gap-3 justify-center">
              <button
                onClick={() => selectedAppointment && selectAppointment(selectedAppointment)}
                className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 hover:shadow-md transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
              >
                Reintentar
              </button>
              <button
                onClick={returnToQueue}
                className="px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg border border-gray-300 hover:bg-gray-50 hover:border-gray-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
              >
                Volver a la Cola
              </button>
            </div>
          </div>
        </div>
      );
    }

    if (!selectedAppointment || !prescriptionDetails) {
      return null;
    }

    return (
      <PrescriptionDetail
        appointment={selectedAppointment}
        prescription={prescriptionDetails}
        onDispense={handleDispenseMedication}
        onBack={returnToQueue}
        dispensing={dispensing}
        medicationCatalog={medicationCatalog}
      />
    );
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Módulo de Farmacia</h2>
          <p className="text-gray-500 text-sm mt-1">Gestiona el despacho de medicamentos a pacientes</p>
        </div>

        {/* Content */}
        {view === 'queue' ? renderQueueView() : renderDetailView()}
      </div>
    </MainLayout>
  );
};

export default PharmacyPage;
