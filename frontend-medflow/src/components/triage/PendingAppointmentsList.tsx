import { useMemo } from 'react';
import type { FC } from 'react';
import AppointmentRow from './AppointmentRow';
import type { AppointmentResponse } from '../../services/clinicalService';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface PendingAppointmentsListProps {
  appointments: AppointmentResponse[];
  onCallPatient: (appointment: AppointmentResponse) => void;
  loading: boolean;
}

// ─── Component ────────────────────────────────────────────────────────────────

const PendingAppointmentsList: FC<PendingAppointmentsListProps> = ({
  appointments,
  onCallPatient,
  loading,
}) => {
  // Sort appointments by date ascending, then time ascending
  const sortedAppointments = useMemo(() => {
    return [...appointments].sort((a, b) => {
      const dateCompare = a.appointmentDate.localeCompare(b.appointmentDate);
      if (dateCompare !== 0) return dateCompare;
      return a.appointmentTime.localeCompare(b.appointmentTime);
    });
  }, [appointments]);

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12" role="status" aria-live="polite">
        <div className="animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" />
        <span className="sr-only">Cargando citas pendientes...</span>
      </div>
    );
  }

  if (appointments.length === 0) {
    return (
      <div className="text-center py-12 text-gray-500">
        <p className="text-sm">No hay citas pendientes de triaje</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-sm">
        <thead>
          <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
            <th scope="col" className="pb-3 pr-4">Fecha</th>
            <th scope="col" className="pb-3 pr-4">Hora</th>
            <th scope="col" className="pb-3 pr-4">Paciente</th>
            <th scope="col" className="pb-3 pr-4">DPI</th>
            <th scope="col" className="pb-3 pr-4">Motivo</th>
            <th scope="col" className="pb-3 pr-4">ID Cita</th>
            <th scope="col" className="pb-3">Acción</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {sortedAppointments.map((appointment) => (
            <AppointmentRow
              key={appointment.id}
              appointment={appointment}
              onCallPatient={onCallPatient}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default PendingAppointmentsList;
