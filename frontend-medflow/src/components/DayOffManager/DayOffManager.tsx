import { useState } from 'react';
import type { FC, FormEvent, ChangeEvent } from 'react';
import { swalAlert } from '../../utils/swal';
import { markDaysOff, type Doctor } from '../../services/doctorService';
import { validateForm, clearFieldError } from '../../utils/formValidation';
import type { Schema, FormErrors } from '../../utils/formValidation';

interface DayOffFormState { startDate: string; endDate: string; reason: string; }

interface DayOffManagerProps {
  doctor: Doctor;
  onSuccess?: () => void;
  onCancel?: () => void;
}

const DayOffManager: FC<DayOffManagerProps> = ({ doctor, onSuccess, onCancel }) => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [reason, setReason] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FormErrors<DayOffFormState>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const dayOffSchema: Schema<DayOffFormState> = {
    startDate: [{ type: 'required', message: 'La fecha de inicio es obligatoria.' }],
    endDate:   [{ type: 'required', message: 'La fecha de fin es obligatoria.' }],
    reason:    [{ type: 'required', message: 'El motivo es obligatorio.' }],
  };

  const today = new Date().toISOString().split('T')[0];

  const validate = (): boolean => {
    const errs = validateForm(dayOffSchema, { startDate, endDate, reason }) as FormErrors<DayOffFormState>;

    if (!errs.endDate && startDate && endDate && startDate > endDate)
      errs.endDate = 'La fecha de fin debe ser posterior a la fecha de inicio.';

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    try {
      await markDaysOff(doctor.id, {
        startDate,
        endDate,
        reason: reason.trim(),
      });

      swalAlert.fire({ title: '¡Listo!', text: 'Días libres marcados exitosamente', icon: 'success', timer: 2000, showConfirmButton: false });
      setStartDate('');
      setEndDate('');
      setReason('');
      setFieldErrors({});
      onSuccess?.();    } catch (err: unknown) {
      const { isAxiosError } = await import('axios');
      const errorMessage = isAxiosError(err)
        ? (err.response?.data as { message?: string })?.message ?? 'Error al marcar días libres'
        : 'Error al marcar días libres';
      setSubmitError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const fieldClass = (field: keyof DayOffFormState) =>
    `w-full px-4 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
      fieldErrors[field] ? 'border-red-500 bg-red-50' : 'border-gray-300'
    }`;

  return (
    <div className="space-y-4">
      <div className="bg-blue-50 border border-blue-200 p-4 rounded">
        <h2 className="text-xl font-bold text-gray-800">
          Gestión de Días Libres
        </h2>
        <p className="text-sm text-gray-600 mt-1">
          Dr. {doctor.name}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Start Date */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Fecha de Inicio *
          </label>
          <input
            type="date"
            value={startDate}
            min={today}
            onChange={(e: ChangeEvent<HTMLInputElement>) => {
              setStartDate(e.target.value);
              setFieldErrors(prev => clearFieldError(prev, 'startDate'));
            }}
            className={fieldClass('startDate')}
          />
          {fieldErrors.startDate && <p className="text-red-500 text-xs mt-1">{fieldErrors.startDate}</p>}
        </div>

        {/* End Date */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Fecha de Fin *
          </label>
          <input
            type="date"
            value={endDate}
            min={startDate || today}
            onChange={(e: ChangeEvent<HTMLInputElement>) => {
              setEndDate(e.target.value);
              setFieldErrors(prev => clearFieldError(prev, 'endDate'));
            }}
            className={fieldClass('endDate')}
          />
          {fieldErrors.endDate && <p className="text-red-500 text-xs mt-1">{fieldErrors.endDate}</p>}
        </div>

        {/* Reason */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Motivo *
          </label>
          <textarea
            value={reason}
            onChange={(e: ChangeEvent<HTMLTextAreaElement>) => {
              setReason(e.target.value);
              setFieldErrors(prev => clearFieldError(prev, 'reason'));
            }}
            placeholder="Vacaciones, Capacitación, etc."
            rows={3}
            className={`${fieldClass('reason')} resize-none`}
          />
          {fieldErrors.reason && <p className="text-red-500 text-xs mt-1">{fieldErrors.reason}</p>}
        </div>

        {submitError && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
            {submitError}
          </div>
        )}

        {/* Actions */}
        <div className="flex space-x-3">
          <button
            type="submit"
            disabled={isLoading}
            className="flex-1 py-2 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? 'GUARDANDO...' : 'MARCAR DÍAS LIBRES'}
          </button>
          <button
            type="button"
            onClick={onCancel}
            className="flex-1 py-2 bg-gray-300 text-gray-700 font-semibold hover:bg-gray-400 transition-colors"
          >
            CANCELAR
          </button>
        </div>
      </form>

      <div className="mt-6 p-4 bg-gray-50 border border-gray-200 rounded">
        <h3 className="text-sm font-semibold text-gray-700 mb-2">Información</h3>
        <ul className="text-xs text-gray-600 space-y-1">
          <li>• Los días libres se marcan para un rango de fechas (inclusive)</li>
          <li>• El doctor no será asignado a citas durante estos días</li>
          <li>• Puedes eliminar días libres individuales si es necesario</li>
        </ul>
      </div>
    </div>
  );
};

export default DayOffManager;
