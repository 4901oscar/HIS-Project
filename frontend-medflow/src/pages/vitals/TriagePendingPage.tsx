import { useState, useEffect, useCallback, useRef } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import PendingAppointmentsList from '../../components/triage/PendingAppointmentsList';
import PatientDetailsCard from '../../components/triage/PatientDetailsCard';
import VitalSignsForm from '../../components/triage/VitalSignsForm';
import TriageForm from '../../components/triage/TriageForm';
import type { TriageFormData } from '../../components/triage/TriageForm';
import ErrorAlert from '../../components/common/ErrorAlert';
import SuccessAlert from '../../components/common/SuccessAlert';
import {
  getPendingTriageAppointments,
  recordVitalSigns,
  performTriage,
} from '../../services/clinicalService';
import type { AppointmentResponse, VitalSignsRequest } from '../../services/clinicalService';
import { getPatientById } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import { extractErrorMessage, isHttpError } from '../../utils/errorHandler';

// ─── Constants ────────────────────────────────────────────────────────────────

const PRIORITY_LABELS: Record<string, string> = {
  RED: 'Rojo — Inmediato',
  ORANGE: 'Naranja — Muy Urgente',
  YELLOW: 'Amarillo — Urgente',
  GREEN: 'Verde — Poco Urgente',
  BLUE: 'Azul — No Urgente',
};

const TRIAGE_ERROR_MAP: Record<number, string> = {
  409: 'Esta cita ya tiene triaje registrado',
  400: 'El paciente no tiene signos vitales registrados',
  404: 'Cita no encontrada',
};

const AUTO_DISMISS_MS = 5000;
const REFRESH_DEBOUNCE_MS = 500;
const AUTO_REFRESH_INTERVAL_MS = 30_000;

// ─── Helpers ──────────────────────────────────────────────────────────────────

function triageErrorMessage(error: unknown): string {
  for (const [status, msg] of Object.entries(TRIAGE_ERROR_MAP)) {
    if (isHttpError(error, Number(status))) return msg;
  }
  return extractErrorMessage(error);
}

// ─── Component ────────────────────────────────────────────────────────────────

const TriagePendingPage: FC = () => {
  // Appointments list
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [appointmentsLoading, setAppointmentsLoading] = useState(false);
  const [appointmentsError, setAppointmentsError] = useState<string | null>(null);
  // Screen reader announcement for list changes (task 52)
  const [listAnnouncement, setListAnnouncement] = useState('');

  // Selected appointment & patient
  const [selectedAppointment, setSelectedAppointment] = useState<AppointmentResponse | null>(null);
  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null);
  const [patientLoading, setPatientLoading] = useState(false);
  const [patientError, setPatientError] = useState<string | null>(null);

  // Vital signs form
  const [showVitalSignsForm, setShowVitalSignsForm] = useState(false);
  const [vitalSignsSubmitting, setVitalSignsSubmitting] = useState(false);
  const [vitalSignsRecorded, setVitalSignsRecorded] = useState(false);
  const [vitalSignsSuccess, setVitalSignsSuccess] = useState<string | null>(null);
  const [vitalSignsError, setVitalSignsError] = useState<string | null>(null);

  // Triage form
  const [showTriageForm, setShowTriageForm] = useState(false);
  const [triageSubmitting, setTriageSubmitting] = useState(false);
  const [triageSuccess, setTriageSuccess] = useState<string | null>(null);
  const [triageError, setTriageError] = useState<string | null>(null);

  // Refs
  const patientDetailsRef = useRef<HTMLDivElement>(null);
  const autoDismissTimers = useRef<ReturnType<typeof setTimeout>[]>([]);
  const refreshDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  // ─── Auto-dismiss Helper ──────────────────────────────────────────────────

  const scheduleAutoDismiss = useCallback((setter: (v: null) => void) => {
    const id = setTimeout(() => setter(null), AUTO_DISMISS_MS);
    autoDismissTimers.current.push(id);
  }, []);

  // ─── Cleanup on unmount (task 63) ────────────────────────────────────────

  useEffect(() => {
    return () => {
      autoDismissTimers.current.forEach(clearTimeout);
      if (refreshDebounceRef.current) clearTimeout(refreshDebounceRef.current);
      abortControllerRef.current?.abort();
    };
  }, []);

  // ─── Escape key to close forms (task 50) ─────────────────────────────────

  const handleVitalSignsCancel = useCallback(() => {
    setShowVitalSignsForm(false);
    setVitalSignsError(null);
  }, []);

  const handleTriageCancel = useCallback(() => {
    setShowTriageForm(false);
    setTriageError(null);
  }, []);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key !== 'Escape') return;
      if (showTriageForm) handleTriageCancel();
      else if (showVitalSignsForm) handleVitalSignsCancel();
    };
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [showTriageForm, showVitalSignsForm, handleTriageCancel, handleVitalSignsCancel]);

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

      // Task 40: clear selection if appointment was removed from list
      setSelectedAppointment((prev) => {
        if (prev && !data.some((a) => a.id === prev.id)) {
          setSelectedPatient(null);
          setShowVitalSignsForm(false);
          setShowTriageForm(false);
          setVitalSignsRecorded(false);
          const msg = 'Esta cita ya fue procesada y ya no está pendiente';
          setTriageSuccess(msg);
          scheduleAutoDismiss(setTriageSuccess);
          return null;
        }
        return prev;
      });
    } catch (error) {
      if (showLoading) {
        setAppointmentsError(extractErrorMessage(error));
      } else {
        console.error('Auto-refresh error:', error);
      }
    } finally {
      if (showLoading) setAppointmentsLoading(false);
    }
  }, [scheduleAutoDismiss]);

  // ─── Appointment Selection ────────────────────────────────────────────────

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

  const handleSelectAppointment = useCallback(async (appointment: AppointmentResponse) => {
    setSelectedAppointment(appointment);
    setPatientLoading(true);
    setPatientError(null);
    setShowVitalSignsForm(false);
    setShowTriageForm(false);
    setVitalSignsRecorded(false);
    setVitalSignsSuccess(null);
    setVitalSignsError(null);
    setTriageSuccess(null);
    setTriageError(null);

    try {
      const patient = await getPatientById(appointment.patientId);
      setSelectedPatient(patient);
      setTimeout(() => patientDetailsRef.current?.focus(), 100);
    } catch (error) {
      console.error('Error fetching patient:', error);
      setPatientError(extractErrorMessage(error));
      setSelectedPatient(null);
    } finally {
      setPatientLoading(false);
    }
  }, []);

  const handleRetryPatient = useCallback(() => {
    if (selectedAppointment) handleSelectAppointment(selectedAppointment);
  }, [selectedAppointment, handleSelectAppointment]);

  const handleCancelSelection = useCallback(() => {
    setSelectedAppointment(null);
    setSelectedPatient(null);
    setPatientError(null);
    setShowVitalSignsForm(false);
    setShowTriageForm(false);
    setVitalSignsRecorded(false);
    setVitalSignsSuccess(null);
    setVitalSignsError(null);
    setTriageSuccess(null);
    setTriageError(null);
  }, []);

  // ─── Vital Signs ──────────────────────────────────────────────────────────

  const handleRecordVitalSigns = useCallback(() => {
    setShowVitalSignsForm(true);
    setShowTriageForm(false);
    setVitalSignsError(null);
  }, []);

  const handleVitalSignsSubmit = useCallback(async (data: VitalSignsRequest) => {
    setVitalSignsSubmitting(true);
    setVitalSignsError(null);

    try {
      await recordVitalSigns(data);
      setVitalSignsRecorded(true);
      setShowVitalSignsForm(false);
      const msg = 'Signos vitales registrados exitosamente';
      setVitalSignsSuccess(msg);
      scheduleAutoDismiss(setVitalSignsSuccess);
    } catch (error) {
      console.error('Error recording vital signs:', error);
      setVitalSignsError(extractErrorMessage(error));
    } finally {
      setVitalSignsSubmitting(false);
    }
  }, [scheduleAutoDismiss]);

  // ─── Triage ───────────────────────────────────────────────────────────────

  const handlePerformTriage = useCallback(() => {
    setShowTriageForm(true);
    setShowVitalSignsForm(false);
    setTriageError(null);
  }, []);

  const handleTriageSubmit = useCallback(async (data: TriageFormData) => {
    if (!selectedAppointment || !selectedPatient) return;

    setTriageSubmitting(true);
    setTriageError(null);

    try {
      const result = await performTriage({
        appointmentId: selectedAppointment.id,
        patientId: selectedPatient.id,
        motifId: data.motifId,
        discriminatorIds: data.discriminatorIds,
      });

      const priorityLabel = PRIORITY_LABELS[result.priorityLevel] ?? result.priorityLevel;
      const msg = `Triaje registrado exitosamente — Prioridad: ${priorityLabel}`;
      setTriageSuccess(msg);
      scheduleAutoDismiss(setTriageSuccess);

      setSelectedAppointment(null);
      setSelectedPatient(null);
      setShowTriageForm(false);
      setVitalSignsRecorded(false);

      await fetchPendingAppointments(false);
    } catch (error) {
      console.error('Error performing triage:', error);
      setTriageError(triageErrorMessage(error));
    } finally {
      setTriageSubmitting(false);
    }
  }, [selectedAppointment, selectedPatient, fetchPendingAppointments, scheduleAutoDismiss]);

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

        {triageSuccess && !selectedAppointment && (
          <SuccessAlert
            message={triageSuccess}
            onDismiss={() => setTriageSuccess(null)}
          />
        )}

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column: Pending List */}
          <div className="lg:col-span-2">
            <div
              className="bg-white rounded-lg shadow p-6"
              aria-busy={appointmentsLoading}
            >
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
                selectedAppointmentId={selectedAppointment?.id || null}
                onSelectAppointment={handleSelectAppointment}
                onCallPatient={handleCallPatient}
                loading={appointmentsLoading}
              />
            </div>
          </div>

          {/* Right Column: Patient Details */}
          <div className="lg:col-span-1">
            {selectedAppointment && selectedPatient ? (
              <div
                ref={patientDetailsRef}
                tabIndex={-1}
                className="space-y-4 outline-none"
              >
                <PatientDetailsCard
                  patient={selectedPatient}
                  appointment={selectedAppointment}
                  vitalSignsRecorded={vitalSignsRecorded}
                  onRecordVitalSigns={handleRecordVitalSigns}
                  onPerformTriage={handlePerformTriage}
                  onCancel={handleCancelSelection}
                  loading={patientLoading}
                />

                {vitalSignsSuccess && (
                  <SuccessAlert
                    message={vitalSignsSuccess}
                    onDismiss={() => setVitalSignsSuccess(null)}
                  />
                )}

                {triageError && (
                  <ErrorAlert
                    message={triageError}
                    onDismiss={() => setTriageError(null)}
                    onRetry={showTriageForm ? undefined : handlePerformTriage}
                  />
                )}
              </div>
            ) : selectedAppointment && patientLoading ? (
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-medin-navy mb-4">Detalles del Paciente</h3>
                <div className="flex justify-center items-center py-8">
                  <div
                    className="animate-spin rounded-full h-6 w-6 border-4 border-medin-cyan border-t-transparent"
                    role="status"
                    aria-live="polite"
                    aria-busy="true"
                    aria-label="Cargando detalles del paciente"
                  />
                </div>
              </div>
            ) : selectedAppointment && patientError ? (
              <div className="bg-white rounded-lg shadow p-6 space-y-4">
                <h3 className="text-lg font-semibold text-medin-navy">Detalles del Paciente</h3>
                <ErrorAlert
                  message={patientError}
                  onDismiss={handleCancelSelection}
                  onRetry={handleRetryPatient}
                />
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow p-6">
                <h3 className="text-lg font-semibold text-medin-navy mb-4">Detalles del Paciente</h3>
                <div className="text-center py-8 text-gray-500">
                  <p className="text-sm">Seleccione una cita para ver los detalles</p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Forms Section */}
        {showVitalSignsForm && selectedPatient && (
          <VitalSignsForm
            patientId={selectedPatient.id}
            onSubmit={handleVitalSignsSubmit}
            onCancel={handleVitalSignsCancel}
            submitting={vitalSignsSubmitting}
            error={vitalSignsError}
          />
        )}

        {showTriageForm && selectedPatient && selectedAppointment && (
          <TriageForm
            patientId={selectedPatient.id}
            appointmentId={selectedAppointment.id}
            onSubmit={handleTriageSubmit}
            onCancel={handleTriageCancel}
            submitting={triageSubmitting}
            error={triageError}
          />
        )}
      </div>
    </MainLayout>
  );
};

export default TriagePendingPage;
