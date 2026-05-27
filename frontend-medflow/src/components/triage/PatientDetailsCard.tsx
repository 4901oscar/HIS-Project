import type { FC } from 'react';
import type { AppointmentResponse } from '../../services/clinicalService';

// ─── Type Definitions ─────────────────────────────────────────────────────────

interface PatientResponse {
  id: string;
  fullName: string;
  dpi: string;
  email: string;
}

interface PatientDetailsCardProps {
  patient: PatientResponse;
  appointment: AppointmentResponse;
  vitalSignsRecorded: boolean;
  onRecordVitalSigns: () => void;
  onPerformTriage: () => void;
  onCancel: () => void;
  loading: boolean;
}

// ─── Component ────────────────────────────────────────────────────────────────

const PatientDetailsCard: FC<PatientDetailsCardProps> = ({
  patient,
  appointment,
  vitalSignsRecorded,
  onRecordVitalSigns,
  onPerformTriage,
  onCancel,
  loading,
}) => {
  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
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
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6" tabIndex={-1}>
      <div className="flex justify-between items-start mb-4">
        <h3 className="text-lg font-semibold text-medin-navy">
          Detalles del Paciente
        </h3>
        <button
          onClick={onCancel}
          className="text-sm text-gray-500 hover:text-gray-700 transition-colors"
          aria-label="Cancelar selección"
        >
          Cancelar
        </button>
      </div>

      <div className="space-y-3 mb-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
          <div>
            <span className="text-gray-500 block mb-1">Nombre:</span>
            <p className="font-medium text-gray-900">{patient.fullName}</p>
          </div>
          <div>
            <span className="text-gray-500 block mb-1">DPI:</span>
            <p className="font-medium text-gray-900">{patient.dpi}</p>
          </div>
          <div>
            <span className="text-gray-500 block mb-1">Fecha de Cita:</span>
            <p className="font-medium text-gray-900">{appointment.appointmentDate}</p>
          </div>
          <div>
            <span className="text-gray-500 block mb-1">Hora:</span>
            <p className="font-medium text-gray-900">
              {appointment.appointmentTime.substring(0, 5)}
            </p>
          </div>
          <div className="col-span-1 sm:col-span-2">
            <span className="text-gray-500 block mb-1">ID Cita:</span>
            <p className="font-mono text-xs text-gray-700">{appointment.id}</p>
          </div>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <button
          onClick={onRecordVitalSigns}
          className="flex-1 px-4 py-2 bg-medin-navy text-white rounded-lg hover:bg-medin-navy/90 transition-colors text-sm font-medium"
          aria-label="Registrar signos vitales del paciente"
        >
          Registrar Signos Vitales
        </button>
        <button
          onClick={onPerformTriage}
          disabled={!vitalSignsRecorded}
          className="flex-1 px-4 py-2 bg-medin-cyan text-white rounded-lg hover:bg-medin-navy hover:text-white transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
          title={!vitalSignsRecorded ? 'Primero registre los signos vitales' : 'Realizar triaje Manchester'}
          aria-label={!vitalSignsRecorded ? 'Realizar triaje (deshabilitado: primero registre los signos vitales)' : 'Realizar triaje Manchester'}
          aria-disabled={!vitalSignsRecorded}
        >
          Realizar Triaje
        </button>
      </div>

      {!vitalSignsRecorded && (
        <p className="text-xs text-gray-500 mt-2 text-center">
          Primero registre los signos vitales
        </p>
      )}
    </div>
  );
};

export default PatientDetailsCard;
