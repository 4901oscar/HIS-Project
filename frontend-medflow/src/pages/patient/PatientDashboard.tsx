import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { getPatientByDpi } from '../../services/patientService';
import { listMyAppointments } from '../../services/appointmentService';
import type { AppointmentResponse } from '../../services/appointmentService';
import { Navbar } from '../../components';
import AppointmentDetailModal from '../../components/patient/AppointmentDetailModal';
import HistorialFloatingButton from '../../components/shared/HistorialFloatingButton';
import { usePatientHistory } from '../../context/PatientHistoryContext';

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Agendada',
  ACTIVE: 'Activa',
  PENDING_PAYMENT: 'Pendiente de Pago',
  TRIAGE: 'En Triaje',
  CONSULTATION: 'En Consulta',
  RE_EVALUATION: 'Reevaluación',
  PENDING_LAB_PAYMENT: 'Pendiente de Pago (Lab)',
  LAB_SAMPLE_COLLECTION: 'Toma de Muestra',
  LAB_PROCESSING: 'En Procesamiento',
  LAB_RESULTS_READY: 'Resultados Listos',
  PENDING_PHARMACY_PAYMENT: 'Pendiente de Pago (Farmacia)',
  PHARMACY: 'En Farmacia',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
  MISSED: 'No presentada',
};

const STATUS_COLOR: Record<string, string> = {
  SCHEDULED: 'bg-blue-100 text-blue-800',
  ACTIVE: 'bg-cyan-100 text-cyan-800',
  PENDING_PAYMENT: 'bg-yellow-100 text-yellow-800',
  TRIAGE: 'bg-orange-100 text-orange-800',
  CONSULTATION: 'bg-indigo-100 text-indigo-800',
  RE_EVALUATION: 'bg-purple-100 text-purple-800',
  PENDING_LAB_PAYMENT: 'bg-yellow-100 text-yellow-800',
  LAB_SAMPLE_COLLECTION: 'bg-violet-100 text-violet-800',
  LAB_PROCESSING: 'bg-violet-100 text-violet-800',
  LAB_RESULTS_READY: 'bg-violet-100 text-violet-800',
  PENDING_PHARMACY_PAYMENT: 'bg-yellow-100 text-yellow-800',
  PHARMACY: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-700',
  CANCELLED: 'bg-red-100 text-red-700',
  MISSED: 'bg-red-100 text-red-700',
};

const PatientDashboard: FC = () => {
  const { user } = useAuth();
  const { setPatient } = usePatientHistory();

  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [apptLoading, setApptLoading] = useState(true);
  const [selectedAppt, setSelectedAppt] = useState<AppointmentResponse | null>(null);

  useEffect(() => {
    listMyAppointments()
      .then(setAppointments)
      .catch(() => {})
      .finally(() => setApptLoading(false));
  }, []);

  useEffect(() => {
    if (!user?.username) return;
    getPatientByDpi(user.username)
      .then((pat) => {
        const fullName = [pat.firstName, pat.firstLastName].filter(Boolean).join(' ');
        setPatient(pat.id, fullName);
      })
      .catch(() => {});
  }, [user, setPatient]);

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Navbar />

      <main className="flex-1 max-w-4xl mx-auto w-full p-6">
        <h2 className="text-2xl font-semibold text-gray-800 mb-6">Mis Citas</h2>

        <div className="space-y-3">
          {apptLoading ? (
            <div className="text-center py-8">
              <div className="inline-block animate-spin rounded-full h-7 w-7 border-4 border-medin-cyan border-t-transparent"></div>
            </div>
          ) : appointments.length === 0 ? (
            <p className="text-gray-500 text-sm">No tienes citas registradas.</p>
          ) : (
            appointments
              .slice()
              .sort((a, b) => b.appointmentDate.localeCompare(a.appointmentDate) || b.appointmentTime.localeCompare(a.appointmentTime))
              .map((appt) => (
                <div key={appt.id} className="bg-white rounded-lg shadow p-4 flex items-start justify-between gap-4">
                  <div>
                    <p className="font-semibold text-gray-900 text-sm">
                      {new Date(appt.appointmentDate + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: '2-digit', year: 'numeric' })} — {appt.appointmentTime.substring(0, 5)}
                    </p>
                    {appt.notes && <p className="text-sm text-gray-600 mt-1">{appt.notes}</p>}
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLOR[appt.status] ?? 'bg-gray-100 text-gray-700'}`}>
                      {STATUS_LABEL[appt.status] ?? appt.status}
                    </span>
                    <button
                      onClick={() => setSelectedAppt(appt)}
                      className="text-sm font-medium text-medin-cyan hover:text-medin-blue transition-colors"
                    >
                      Ver
                    </button>
                  </div>
                </div>
              ))
          )}
        </div>
      </main>

      <HistorialFloatingButton />

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
    </div>
  );
};

export default PatientDashboard;
