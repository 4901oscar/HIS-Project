import { useState, useEffect, useCallback, useRef } from 'react';
import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import { listAppointments } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';

const AUTO_REFRESH_INTERVAL_MS = 30_000;
const REFRESH_DEBOUNCE_MS = 500;

const TriagePendingPage: FC = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<AppointmentListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refreshDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (refreshDebounceRef.current) clearTimeout(refreshDebounceRef.current);
    };
  }, []);

  const fetchAppointments = useCallback(async (showLoading = true) => {
    if (showLoading) setLoading(true);
    setError(null);
    try {
      const data = await listAppointments({ queue: 'triage' });
      setAppointments(data);
    } catch (err) {
      if (showLoading) setError('No se pudo cargar la lista de citas pendientes de triaje.');
      console.error(err);
    } finally {
      if (showLoading) setLoading(false);
    }
  }, []);

  const handleManualRefresh = useCallback(() => {
    if (refreshDebounceRef.current) clearTimeout(refreshDebounceRef.current);
    refreshDebounceRef.current = setTimeout(() => fetchAppointments(true), REFRESH_DEBOUNCE_MS);
  }, [fetchAppointments]);

  useEffect(() => {
    fetchAppointments(true);
    const interval = setInterval(() => fetchAppointments(false), AUTO_REFRESH_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [fetchAppointments]);

  const handleAtender = (appt: AppointmentListItem) => {
    const utterance = new SpeechSynthesisUtterance(
      `${appt.patient.fullName}, por favor pasar a sala de triaje`
    );
    utterance.lang = 'es-GT';
    utterance.rate = 0.9;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);

    navigate('/vitals/triage/capture', {
      state: { appointmentId: appt.id, patientId: appt.patient.id },
    });
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Triaje — Signos Vitales</h2>
            <p className="text-gray-500 text-sm">Pacientes pendientes de registro de signos vitales</p>
          </div>
          <button
            onClick={handleManualRefresh}
            disabled={loading}
            className="text-sm text-medin-navy hover:text-medin-navy/80 transition-colors disabled:opacity-50"
          >
            {loading ? (
              <span className="flex items-center gap-1">
                <span className="inline-block h-3 w-3 animate-spin rounded-full border-2 border-medin-cyan border-t-transparent" />
                Actualizando...
              </span>
            ) : 'Actualizar'}
          </button>
        </div>

        {/* Error */}
        {error && (
          <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">
            {error}
          </div>
        )}

        {/* Table */}
        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" />
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
              <h3 className="text-lg font-semibold text-gray-900">
                Pacientes en Espera ({appointments.length})
              </h3>
            </div>

            {appointments.length === 0 ? (
              <div className="text-center py-16 text-gray-500">
                <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <p className="text-lg font-medium">No hay pacientes en espera</p>
                <p className="text-sm text-gray-400 mt-1">Todos los pacientes han sido atendidos</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
                      <th className="pb-2 pr-4 pl-6">Fecha</th>
                      <th className="pb-2 pr-4">Hora</th>
                      <th className="pb-2 pr-4">Paciente</th>
                      <th className="pb-2 pr-4">DPI</th>
                      <th className="pb-2 pr-4">Estado</th>
                      <th className="pb-2 pr-4">Motivo</th>
                      <th className="pb-2 pr-6">Acción</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {appointments
                      .slice()
                      .sort((a, b) => {
                        const d = a.appointmentDate.toString().localeCompare(b.appointmentDate.toString());
                        if (d !== 0) return d;
                        return a.appointmentTime.toString().localeCompare(b.appointmentTime.toString());
                      })
                      .map((appt) => (
                        <tr key={appt.id} className="hover:bg-gray-50">
                          <td className="py-3 pr-4 pl-6 whitespace-nowrap text-xs">
                            {new Date(appt.appointmentDate + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: '2-digit', year: 'numeric' })}
                          </td>
                          <td className="py-3 pr-4 whitespace-nowrap font-medium">
                            {appt.appointmentTime.toString().substring(0, 5)}
                          </td>
                          <td className="py-3 pr-4">{appt.patient.fullName}</td>
                          <td className="py-3 pr-4 font-mono text-xs">{appt.patient.dpi ?? '—'}</td>
                          <td className="py-3 pr-4">
                            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                              appt.statusColor === 'green' ? 'bg-green-100 text-green-800' :
                              appt.statusColor === 'orange' ? 'bg-orange-100 text-orange-800' :
                              appt.statusColor === 'blue' ? 'bg-blue-100 text-blue-800' :
                              'bg-gray-100 text-gray-600'
                            }`}>
                              {appt.statusLabel}
                            </span>
                          </td>
                          <td className="py-3 pr-4 max-w-xs truncate text-gray-500 text-xs">
                            {appt.notes ?? '—'}
                          </td>
                          <td className="py-3 pr-6 whitespace-nowrap text-right">
                            <button
                              onClick={() => handleAtender(appt)}
                              className="px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm"
                            >
                              Atender
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default TriagePendingPage;
