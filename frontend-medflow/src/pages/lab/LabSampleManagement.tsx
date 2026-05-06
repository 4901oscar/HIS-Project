import { useState, useEffect, useCallback } from 'react';
import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import { listAppointments } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import { playNotificationWithCallback } from '../../utils/AudioNotification';

// ─── Tipos y constantes ───────────────────────────────────────────────────────

// Estados de laboratorio según Requirements 1.1 y 2.6
const LAB_STATUSES = [
  'LAB_SAMPLE_COLLECTION',
  'LAB_SAMPLE_PENDING',
  'LAB_PROCESSING',
  'LAB_RESULTS_READY',
] as const;

// ─── Tabla de citas de laboratorio ────────────────────────────────────────────

interface LabAppointmentTableProps {
  appointments: AppointmentListItem[];
  loading: boolean;
  emptyText: string;
  onAtender: (appointment: AppointmentListItem) => void;
}

const LabAppointmentTable: FC<LabAppointmentTableProps> = ({ 
  appointments, 
  loading, 
  emptyText,
  onAtender 
}) => {
  if (loading) {
    return (
      <div className="text-center py-12">
        <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" />
      </div>
    );
  }

  if (appointments.length === 0) {
    return (
      <div className="text-center py-12 text-gray-400">
        <svg className="mx-auto h-10 w-10 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p className="text-sm">{emptyText}</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-sm">
        <thead>
          <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
            <th className="pb-2 pr-4 pl-6">Fecha Cita</th>
            <th className="pb-2 pr-4">Hora Cita</th>
            <th className="pb-2 pr-4">Paciente</th>
            <th className="pb-2 pr-4">DPI</th>
            <th className="pb-2 pr-4">Doctor</th>
            <th className="pb-2 pr-4">Estado</th>
            <th className="pb-2 pr-4">Acciones</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {appointments
            .slice()
            .sort((a, b) => {
              // Ordenar por fecha y hora de cita
              const dateCompare = a.appointmentDate.localeCompare(b.appointmentDate);
              if (dateCompare !== 0) return dateCompare;
              return a.appointmentTime.localeCompare(b.appointmentTime);
            })
            .map((appointment) => (
              <tr key={appointment.id} className="hover:bg-gray-50">
                <td className="py-3 pr-4 pl-6 whitespace-nowrap text-xs">
                  {new Date(appointment.appointmentDate + 'T00:00:00').toLocaleDateString('es-GT', { 
                    day: '2-digit', 
                    month: '2-digit', 
                    year: 'numeric' 
                  })}
                </td>
                <td className="py-3 pr-4 whitespace-nowrap font-medium">
                  {appointment.appointmentTime.toString().substring(0, 5)}
                </td>
                <td className="py-3 pr-4">
                  {appointment.patient.fullName}
                </td>
                <td className="py-3 pr-4 font-mono text-xs">
                  {appointment.patient.dpi ?? '—'}
                </td>
                <td className="py-3 pr-4">
                  {appointment.doctor.name}
                </td>
                <td className="py-3 pr-4">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${appointment.statusColor}`}>
                    {appointment.statusLabel}
                  </span>
                </td>
                <td className="py-3 pr-4">
                  <button
                    onClick={() => onAtender(appointment)}
                    className="px-3 py-1.5 bg-purple-600 text-white text-xs font-medium rounded-lg hover:bg-purple-700 hover:shadow-sm transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 active:bg-purple-800 active:scale-95"
                  >
                    Atender
                  </button>
                </td>
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
};

// ─── Página principal ─────────────────────────────────────────────────────────

const LabSampleManagement: FC = () => {
  const navigate = useNavigate();
  const [labAppointments, setLabAppointments] = useState<AppointmentListItem[]>([]);
  const [loading, setLoading] = useState(true);

  /**
   * Carga las citas de laboratorio filtrando por estados LAB_*
   * Requirements: 2.1, 2.6 - Filtrar solo citas con estados de laboratorio
   */
  const loadLabAppointments = useCallback(async () => {
    setLoading(true);
    try {
      // Obtener citas con estados de laboratorio usando el endpoint unificado
      const appointments = await listAppointments({
        status: [...LAB_STATUSES],
        includeClinical: true,
      });
      
      setLabAppointments(appointments);
    } catch (err) {
      console.error('Error cargando citas de laboratorio:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Handler para el botón "Atender"
   * Requirements: 2.4, 2.5, 8.2, 8.3 - Reproducir audio y navegar al wizard
   */
  const handleAtender = useCallback((appointment: AppointmentListItem) => {
    // Reproducir notificación de audio y navegar después de 500ms
    // El audio se reproduce de forma no bloqueante (continúa incluso si falla)
    playNotificationWithCallback(() => {
      navigate(`/lab/workflow/${appointment.id}`);
    }, 500);
  }, [navigate]);

  useEffect(() => {
    loadLabAppointments();
  }, [loadLabAppointments]);

  return (
    <MainLayout>
      <div className="space-y-6 lg:space-y-8">
        {/* Header */}
        <div>
          <h2 className="text-xl lg:text-2xl font-bold text-gray-900">Gestión de Laboratorio</h2>
          <p className="text-gray-500 text-sm mt-1">Citas de laboratorio pendientes de atención</p>
        </div>

        {/* ── Lista: Citas de Laboratorio ── */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gradient-to-r from-purple-500/5 to-transparent">
            <div className="flex items-center gap-3">
              <span className="text-xl">🔬</span>
              <div>
                <h3 className="text-sm font-semibold text-gray-900">Citas de Laboratorio</h3>
                <p className="text-xs text-gray-500">Pacientes con muestras pendientes, en validación o procesamiento</p>
              </div>
              <span className="ml-2 px-2 py-0.5 bg-purple-100 text-purple-700 rounded-full text-xs font-semibold">
                {loading ? '…' : labAppointments.length}
              </span>
            </div>
            <button
              onClick={loadLabAppointments}
              disabled={loading}
              className="text-xs text-purple-700 hover:text-purple-500 hover:bg-purple-50 px-2 py-1 rounded transition-all duration-200 disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2"
            >
              Actualizar
            </button>
          </div>
          <LabAppointmentTable
            appointments={labAppointments}
            loading={loading}
            emptyText="No hay citas de laboratorio pendientes"
            onAtender={handleAtender}
          />
        </div>
      </div>
    </MainLayout>
  );
};

export default LabSampleManagement;
