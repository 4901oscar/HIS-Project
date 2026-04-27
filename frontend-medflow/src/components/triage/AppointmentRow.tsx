import { useCallback, memo } from 'react';
import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckIcon } from '@heroicons/react/24/solid';
import type { AppointmentResponse } from '../../services/clinicalService';

// ─── Props Interface ──────────────────────────────────────────────────────────

interface AppointmentRowProps {
  appointment: AppointmentResponse;
  onCallPatient: (appointment: AppointmentResponse) => void;
}

// ─── Helper Functions ─────────────────────────────────────────────────────────

/**
 * Convert date from YYYY-MM-DD to DD/MM/YYYY
 */
function formatDate(dateStr: string): string {
  const [year, month, day] = dateStr.split('-');
  return `${day}/${month}/${year}`;
}

// ─── Component ────────────────────────────────────────────────────────────────

const AppointmentRow: FC<AppointmentRowProps> = ({
  appointment,
  onCallPatient,
}) => {
  const navigate = useNavigate();

  const handleCallPatient = useCallback((e: React.MouseEvent) => {
    e.stopPropagation(); // Prevent row selection
    
    // Call the audio announcement
    onCallPatient(appointment);
    
    // Navigate to vital signs capture page with appointment data
    navigate('/vitals/triage/capture', {
      state: {
        appointmentId: appointment.id,
        patientId: appointment.patientId,
      }
    });
  }, [appointment, onCallPatient, navigate]);

  return (
    <tr
      className="hover:bg-gray-50 transition-colors"
    >
      <td className="py-3 pr-4 whitespace-nowrap">{formatDate(appointment.appointmentDate)}</td>
      <td className="py-3 pr-4 whitespace-nowrap">
        {appointment.appointmentTime.substring(0, 5)}
      </td>
      <td className="py-3 pr-4">{appointment.patientName || 'Cargando...'}</td>
      <td className="py-3 pr-4 font-mono text-xs">{appointment.patientDpi || 'N/A'}</td>
      <td className="py-3 pr-4 max-w-xs truncate">{appointment.notes || '—'}</td>
      <td className="py-3 pr-4 font-mono text-xs text-gray-500">{appointment.id}</td>
      <td className="py-3">
        <button
          onClick={handleCallPatient}
          className="inline-flex items-center justify-center w-8 h-8 rounded-full bg-green-500 hover:bg-green-600 text-white transition-colors focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
          aria-label="Llamar a paciente y registrar signos vitales"
          title="Llamar a sala de triaje"
        >
          <CheckIcon className="h-5 w-5" />
        </button>
      </td>
    </tr>
  );
};

// Memoize component to prevent unnecessary re-renders
export default memo(AppointmentRow, (prevProps, nextProps) => {
  return prevProps.appointment.id === nextProps.appointment.id;
});
