import { useState } from 'react';
import type { ChangeEvent, FormEvent, FC } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { createAppointment } from '../../services/appointmentService';

interface BookAppointmentFormProps {
  onSuccess?: (message: string) => void;
  onError?: (message: string) => void;
}

const TIME_SLOTS = [
  '09:00', '10:00', '11:00', '12:00',
  '13:00', '14:00', '15:00', '16:00',
];

const formatTime = (t: string) => {
  const [h] = t.split(':').map(Number);
  return h < 12 ? `${t} AM` : `${h === 12 ? 12 : h - 12}:00 PM`;
};

const BookAppointmentForm: FC<BookAppointmentFormProps> = ({ onSuccess, onError }) => {
  const { user } = useAuth();

  const today = new Date().toISOString().split('T')[0];

  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [motivo, setMotivo] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (!date) newErrors.date = 'Selecciona una fecha';
    if (!time) newErrors.time = 'Selecciona una hora';
    if (!motivo.trim()) newErrors.motivo = 'Describe el motivo de la consulta';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    try {
      await createAppointment({ patientId: user!.id, doctorId: '', appointmentDate: `${date}T${time}:00`, appointmentTime: time, notes: motivo });
      onSuccess?.('¡Cita agendada exitosamente! Pronto recibirás confirmación.');
      setDate('');
      setTime('');
      setMotivo('');
      setErrors({});
    } catch {
      onError?.('No se pudo agendar la cita. Intenta de nuevo más tarde.');
    } finally {
      setIsLoading(false);
    }
  };

  const fieldClass = (field: string) =>
    `w-full px-4 py-3 bg-medin-navy border-0 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-medin-cyan${errors[field] ? ' ring-2 ring-red-500' : ''}`;

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Fecha */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Fecha de la cita</label>
        <input
          type="date"
          value={date}
          min={today}
          onChange={(e: ChangeEvent<HTMLInputElement>) => {
            setDate(e.target.value);
            if (errors.date) setErrors(prev => ({ ...prev, date: '' }));
          }}
          className={fieldClass('date')}
        />
        {errors.date && <p className="text-red-500 text-xs mt-1">{errors.date}</p>}
      </div>

      {/* Hora */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Hora</label>
        <select
          value={time}
          onChange={(e: ChangeEvent<HTMLSelectElement>) => {
            setTime(e.target.value);
            if (errors.time) setErrors(prev => ({ ...prev, time: '' }));
          }}
          className={`${fieldClass('time')}${!time ? ' text-gray-400' : ''}`}
        >
          <option value="">Selecciona una hora</option>
          {TIME_SLOTS.map(t => (
            <option key={t} value={t}>{formatTime(t)}</option>
          ))}
        </select>
        {errors.time && <p className="text-red-500 text-xs mt-1">{errors.time}</p>}
      </div>

      {/* Motivo */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Motivo de consulta</label>
        <textarea
          value={motivo}
          onChange={(e: ChangeEvent<HTMLTextAreaElement>) => {
            setMotivo(e.target.value);
            if (errors.motivo) setErrors(prev => ({ ...prev, motivo: '' }));
          }}
          placeholder="Describe brevemente tus síntomas o el motivo de tu cita..."
          rows={5}
          className={`${fieldClass('motivo')} resize-none`}
        />
        {errors.motivo && <p className="text-red-500 text-xs mt-1">{errors.motivo}</p>}
      </div>

      <button
        type="submit"
        disabled={isLoading}
        className="w-full py-3 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isLoading ? 'ENVIANDO...' : 'AGENDAR CITA'}
      </button>
    </form>
  );
};

export default BookAppointmentForm;
