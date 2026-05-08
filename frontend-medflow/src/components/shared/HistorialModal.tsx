import { useEffect, useState } from 'react';
import type { FC } from 'react';
import { XMarkIcon, EyeIcon } from '@heroicons/react/24/outline';
import { listMyAppointments, listAppointments } from '../../services/appointmentService';
import type { AppointmentResponse, AppointmentListItem } from '../../services/appointmentService';
import AppointmentDetailModal from '../patient/AppointmentDetailModal';
import { useAuth } from '../../hooks/useAuth';

interface Props {
  patientId: string;
  patientName?: string | null;
  onClose: () => void;
}

const STATUS_COLOR: Record<string, string> = {
  SCHEDULED: 'bg-blue-100 text-blue-800',
  ACTIVE: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-700',
  CANCELLED: 'bg-red-100 text-red-700',
  PENDING_PAYMENT: 'bg-yellow-100 text-yellow-800',
  TRIAGE: 'bg-orange-100 text-orange-800',
  CONSULTATION: 'bg-purple-100 text-purple-800',
  LAB_SAMPLE_COLLECTION: 'bg-blue-100 text-blue-800',
  LAB_PROCESSING: 'bg-blue-100 text-blue-800',
  LAB_RESULTS_READY: 'bg-green-100 text-green-800',
  PHARMACY: 'bg-teal-100 text-teal-800',
  PENDING_PHARMACY_PAYMENT: 'bg-yellow-100 text-yellow-800',
  DISCHARGE: 'bg-gray-100 text-gray-700',
};

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Agendada',
  ACTIVE: 'Activa',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
  PENDING_PAYMENT: 'Pago pendiente',
  TRIAGE: 'En triaje',
  CONSULTATION: 'En consulta',
  LAB_SAMPLE_COLLECTION: 'Laboratorio',
  LAB_PROCESSING: 'Procesando lab',
  LAB_RESULTS_READY: 'Resultados listos',
  PHARMACY: 'En farmacia',
  PENDING_PHARMACY_PAYMENT: 'Pago farmacia',
  DISCHARGE: 'Alta',
};

interface NormalizedAppt {
  id: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  notes?: string;
}

const HistorialModal: FC<Props> = ({ patientId, patientName, onClose }) => {
  const { user } = useAuth();
  const isPatient = user?.roles?.includes('PATIENT');

  const [appointments, setAppointments] = useState<NormalizedAppt[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAppt, setSelectedAppt] = useState<NormalizedAppt | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        if (isPatient) {
          const data = await listMyAppointments();
          setAppointments(data as AppointmentResponse[]);
        } else {
          const data = await listAppointments({ includeClinical: false });
          const filtered = (data as AppointmentListItem[])
            .filter((a) => a.patient?.id === patientId)
            .map((a) => ({
              id: a.id,
              appointmentDate: a.appointmentDate,
              appointmentTime: a.appointmentTime,
              status: a.status,
              notes: a.notes,
            }));
          setAppointments(filtered);
        }
      } catch {
        setError('No se pudo cargar el historial.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [patientId, isPatient]);

  return (
    <>
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col">
          <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between rounded-t-xl">
            <div>
              <h3 className="text-lg font-semibold text-gray-900">Historial Clinico</h3>
              {patientName && <p className="text-sm text-gray-500">{patientName}</p>}
            </div>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
              <XMarkIcon className="h-6 w-6" />
            </button>
          </div>

          <div className="overflow-y-auto flex-1 p-6">
            {loading && (
              <div className="text-center py-12">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
                <p className="mt-3 text-gray-500 text-sm">Cargando citas...</p>
              </div>
            )}

            {!loading && error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>
            )}

            {!loading && !error && (
              <div className="space-y-3">
                {appointments.length === 0 ? (
                  <p className="text-gray-400 text-sm italic">Sin citas registradas.</p>
                ) : (
                  appointments
                    .slice()
                    .sort((a, b) => b.appointmentDate.localeCompare(a.appointmentDate) || b.appointmentTime.localeCompare(a.appointmentTime))
                    .map((appt) => (
                      <div key={appt.id} className="bg-white border border-gray-200 rounded-lg p-4 flex items-center justify-between gap-4 hover:border-medin-cyan transition-colors">
                        <div>
                          <p className="font-semibold text-gray-900 text-sm">
                            {appt.appointmentDate} — {appt.appointmentTime.substring(0, 5)}
                          </p>
                          {appt.notes && <p className="text-xs text-gray-500 mt-0.5">{appt.notes}</p>}
                        </div>
                        <div className="flex items-center gap-3 shrink-0">
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLOR[appt.status] ?? 'bg-gray-100 text-gray-700'}`}>
                            {STATUS_LABEL[appt.status] ?? appt.status}
                          </span>
                          <button
                            onClick={() => setSelectedAppt(appt)}
                            className="text-medin-cyan hover:text-medin-blue transition-colors"
                            title="Ver detalle de cita"
                          >
                            <EyeIcon className="h-5 w-5" />
                          </button>
                        </div>
                      </div>
                    ))
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {selectedAppt && (
        <AppointmentDetailModal
          appointmentId={selectedAppt.id}
          appointmentDate={selectedAppt.appointmentDate}
          appointmentTime={selectedAppt.appointmentTime}
          status={selectedAppt.status}
          notes={selectedAppt.notes}
          onClose={() => setSelectedAppt(null)}
        />
      )}
    </>
  );
};

export default HistorialModal;
