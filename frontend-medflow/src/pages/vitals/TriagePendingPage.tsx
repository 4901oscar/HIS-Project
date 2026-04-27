import { useState, useEffect, useCallback, useRef } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import PendingAppointmentsList from '../../components/triage/PendingAppointmentsList';
import ErrorAlert from '../../components/common/ErrorAlert';
import { getPendingTriageAppointments } from '../../services/clinicalService';
import type { AppointmentResponse } from '../../services/clinicalService';
import { extractErrorMessage } from '../../utils/errorHandler';

const REFRESH_DEBOUNCE_MS = 500;
const AUTO_REFRESH_INTERVAL_MS = 30_000;

const TriagePendingPage: FC = () => {
  // Appointments list
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [appointmentsLoading, setAppointmentsLoading] = useState(false);
  const [appointmentsError, setAppointmentsError] = useState<string | null>(null);
  // Screen reader announcement for list changes (task 52)
  const [listAnnouncement, setListAnnouncement] = useState('');

  // Refs
  const refreshDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  // ─── Cleanup on unmount (task 63) ────────────────────────────────────────

  useEffect(() => {
    return () => {
      if (refreshDebounceRef.current) clearTimeout(refreshDebounceRef.current);
      abortControllerRef.current?.abort();
    };
  }, []);

  // ─── Data Fetching ────────────────────────────────────────────────────────

  const fetchPendingAppointments = useCallback(async (showLoading = true) => {
    // Abort any in-flight request (task 63)
    abortControllerRef.current?.abort();
    abortControllerRef.current = new AbortController();

    if (showLoading) setAppointmentsLoading(true);
    setAppointmentsError(null);

    try {
      const data = await getPendingTriageAppointments();

      setAppointments(data);

      // Announce list update to screen readers (task 52)
      setListAnnouncement(
        data.length === 0
          ? 'No hay citas pendientes de triaje'
          : `Lista actualizada. ${data.length} cita${data.length !== 1 ? 's' : ''} pendiente${data.length !== 1 ? 's' : ''} de triaje`
      );
    } catch (error) {
      if (showLoading) {
        setAppointmentsError(extractErrorMessage(error));
      } else {
        console.error('Auto-refresh error:', error);
      }
    } finally {
      if (showLoading) setAppointmentsLoading(false);
    }
  }, []);

  // ─── Call Patient ─────────────────────────────────────────────────────────

  const handleCallPatient = useCallback(async (appointment: AppointmentResponse) => {
    // Play audio announcement with patient name from appointment
    const patientName = appointment.patientName || 'Paciente';
    const utterance = new SpeechSynthesisUtterance(
      `${patientName}, por favor pasar a sala de triaje`
    );
    utterance.lang = 'es-GT';
    utterance.rate = 0.9;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  }, []);

  // ─── Manual Refresh with debounce (task 62) ───────────────────────────────

  const handleManualRefresh = useCallback(() => {
    if (refreshDebounceRef.current) clearTimeout(refreshDebounceRef.current);
    refreshDebounceRef.current = setTimeout(() => {
      fetchPendingAppointments(true);
    }, REFRESH_DEBOUNCE_MS);
  }, [fetchPendingAppointments]);

  // ─── Lifecycle ────────────────────────────────────────────────────────────

  useEffect(() => {
    fetchPendingAppointments(true);

    const interval = setInterval(() => {
      fetchPendingAppointments(false);
    }, AUTO_REFRESH_INTERVAL_MS);

    return () => clearInterval(interval);
  }, [fetchPendingAppointments]);

  // ─── Render ───────────────────────────────────────────────────────────────

  return (
    <MainLayout>
      <div className="min-w-[320px] space-y-6">
        {/* Screen reader live region (task 52) */}
        <div
          aria-live="polite"
          aria-atomic="true"
          className="sr-only"
        >
          {listAnnouncement}
        </div>

        {/* Page Header */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Triaje Pendiente</h2>
          <p className="mt-1 text-sm text-gray-600">
            Lista de citas activas pendientes de triaje
          </p>
        </div>

        {/* Global Alerts */}
        {appointmentsError && (
          <ErrorAlert
            message={appointmentsError}
            onDismiss={() => setAppointmentsError(null)}
            onRetry={handleManualRefresh}
          />
        )}

        {/* Main Content */}
        <div className="bg-white rounded-lg shadow p-6" aria-busy={appointmentsLoading}>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-medin-navy">
              Citas Pendientes
              {appointments.length > 0 && (
                <span className="ml-2 text-sm font-normal text-gray-500">
                  ({appointments.length})
                </span>
              )}
            </h3>
            <button
              onClick={handleManualRefresh}
              disabled={appointmentsLoading}
              className="text-sm text-medin-navy hover:text-medin-navy/80 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              aria-label="Actualizar lista de citas pendientes"
            >
              {appointmentsLoading ? (
                <span className="flex items-center gap-1">
                  <span
                    className="inline-block h-3 w-3 animate-spin rounded-full border-2 border-medin-cyan border-t-transparent"
                    aria-hidden="true"
                  />
                  Actualizando...
                </span>
              ) : (
                'Actualizar'
              )}
            </button>
          </div>

          <PendingAppointmentsList
            appointments={appointments}
            onCallPatient={handleCallPatient}
            loading={appointmentsLoading}
          />
        </div>
      </div>
    </MainLayout>
  );
};

export default TriagePendingPage;
