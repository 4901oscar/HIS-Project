import { useState, useEffect } from 'react';
import type { FC, FormEvent, ChangeEvent } from 'react';
import {
  createDoctor,
  updateDoctor,
  getDoctorEmployees,
  type Doctor,
  type CreateDoctorRequest,
  type DoctorEmployee,
} from '../../services/doctorService';
import { getClinics } from '../../services/clinicService';
import type { Clinic } from '../../types/clinic';

interface DoctorFormProps {
  doctor?: Doctor | null;
  onSuccess?: () => void;
  onCancel?: () => void;
}

const DoctorForm: FC<DoctorFormProps> = ({ doctor, onSuccess, onCancel }) => {
  const isEditMode = !!doctor;

  // Create-mode state
  const [employees, setEmployees] = useState<DoctorEmployee[]>([]);
  const [selectedEmployee, setSelectedEmployee] = useState<DoctorEmployee | null>(null);
  const [loadingEmployees, setLoadingEmployees] = useState(false);
  const [employeesError, setEmployeesError] = useState<string | null>(null);

  // Shared state
  const [shiftStart, setShiftStart] = useState('');
  const [shiftEnd, setShiftEnd] = useState('');
  const [clinicId, setClinicId] = useState('');
  const [clinics, setClinics] = useState<Clinic[]>([]);
  const [loadingClinics, setLoadingClinics] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    loadClinics();
    if (isEditMode && doctor) {
      setShiftStart(doctor.shiftStart);
      setShiftEnd(doctor.shiftEnd);
      setClinicId(doctor.clinicId ?? '');
    } else {
      loadEmployees();
    }
  }, [doctor]);

  const loadClinics = async () => {
    setLoadingClinics(true);
    try {
      const data = await getClinics('ACTIVE');
      setClinics(data);
    } catch {
      // silently fail; validation will catch if no clinic selected
    } finally {
      setLoadingClinics(false);
    }
  };

  const loadEmployees = async () => {
    setLoadingEmployees(true);
    setEmployeesError(null);
    try {
      const data = await getDoctorEmployees();
      setEmployees(data);
    } catch {
      setEmployeesError('No se pudo cargar la lista de doctores disponibles');
    } finally {
      setLoadingEmployees(false);
    }
  };

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!isEditMode && !selectedEmployee) {
      newErrors.employee = 'Debe seleccionar un doctor';
    }

    if (!clinicId) {
      newErrors.clinicId = 'Debe seleccionar una clínica';
    }

    if (!shiftStart) {
      newErrors.shiftStart = 'La hora de inicio es obligatoria';
    }

    if (!shiftEnd) {
      newErrors.shiftEnd = 'La hora de fin es obligatoria';
    }

    if (shiftStart && shiftEnd) {
      const [startHour, startMin] = shiftStart.split(':').map(Number);
      const [endHour, endMin] = shiftEnd.split(':').map(Number);
      const startTotal = startHour * 60 + startMin;
      let endTotal = endHour * 60 + endMin;
      if (endTotal === 0) endTotal = 24 * 60;         // 00:00 = fin de día
      if (endTotal <= startTotal) endTotal += 24 * 60; // turno nocturno
      if (endTotal - startTotal !== 480) {
        newErrors.shiftEnd = 'El turno debe ser de exactamente 8 horas';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    try {
      if (isEditMode && doctor) {
        await updateDoctor(doctor.id, { name: doctor.name, clinicId, shiftStart, shiftEnd });
      } else {
        const data: CreateDoctorRequest = {
          userId: selectedEmployee!.id,
          clinicId,
          name: selectedEmployee!.fullName,
          shiftStart,
          shiftEnd,
        };
        await createDoctor(data);
      }
      onSuccess?.();
    } catch (err: unknown) {
      const { isAxiosError } = await import('axios');
      const msg = isAxiosError(err)
        ? (err.response?.data as { message?: string })?.message ?? 'Error al guardar el doctor'
        : 'Error al guardar el doctor';
      setErrors({ submit: msg });
    } finally {
      setIsLoading(false);
    }
  };

  const fieldClass = (field: string) =>
    `w-full px-4 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
      errors[field] ? 'border-red-500' : 'border-gray-300'
    }`;

  return (
    <form onSubmit={handleSubmit} className="space-y-4 max-w-lg">
      <h2 className="text-2xl font-bold text-gray-800 mb-4">
        {isEditMode ? 'Editar Doctor' : 'Vincular Doctor'}
      </h2>

      {/* Doctor selector (create mode only) */}
      {!isEditMode && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Seleccionar Doctor <span className="text-red-500">*</span>
          </label>
          {loadingEmployees ? (
            <p className="text-sm text-gray-500">Cargando doctores disponibles...</p>
          ) : employeesError ? (
            <p className="text-sm text-red-500">{employeesError}</p>
          ) : employees.length === 0 ? (
            <p className="text-sm text-gray-500">
              No hay usuarios con rol DOCTOR disponibles para vincular.
            </p>
          ) : (
            <select
              value={selectedEmployee?.id ?? ''}
              onChange={(e: ChangeEvent<HTMLSelectElement>) => {
                const emp = employees.find((em) => em.id === e.target.value) ?? null;
                setSelectedEmployee(emp);
                if (errors.employee) setErrors((prev) => ({ ...prev, employee: '' }));
              }}
              className={fieldClass('employee')}
            >
              <option value="">-- Seleccione un doctor --</option>
              {employees.map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.fullName} ({emp.username})
                </option>
              ))}
            </select>
          )}
          {errors.employee && <p className="text-red-500 text-xs mt-1">{errors.employee}</p>}
        </div>
      )}

      {/* Name (edit mode: readonly display) */}
      {isEditMode && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
          <input
            type="text"
            value={doctor?.name ?? ''}
            disabled
            className="w-full px-4 py-2 border border-gray-200 rounded bg-gray-50 text-gray-600"
          />
        </div>
      )}

      {/* Clinic selector */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Clínica <span className="text-red-500">*</span>
        </label>
        {loadingClinics ? (
          <p className="text-sm text-gray-500">Cargando clínicas...</p>
        ) : (
          <select
            value={clinicId}
            onChange={(e: ChangeEvent<HTMLSelectElement>) => {
              setClinicId(e.target.value);
              if (errors.clinicId) setErrors((prev) => ({ ...prev, clinicId: '' }));
            }}
            className={fieldClass('clinicId')}
          >
            <option value="">-- Seleccione una clínica --</option>
            {clinics.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nombre} ({c.codigo})
              </option>
            ))}
          </select>
        )}
        {errors.clinicId && <p className="text-red-500 text-xs mt-1">{errors.clinicId}</p>}
      </div>

      {/* Shift Start */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Hora de Inicio del Turno <span className="text-red-500">*</span>
        </label>
        <input
          type="time"
          value={shiftStart}
          onChange={(e: ChangeEvent<HTMLInputElement>) => {
            setShiftStart(e.target.value);
            if (errors.shiftStart) setErrors((prev) => ({ ...prev, shiftStart: '' }));
          }}
          className={fieldClass('shiftStart')}
        />
        {errors.shiftStart && <p className="text-red-500 text-xs mt-1">{errors.shiftStart}</p>}
      </div>

      {/* Shift End */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Hora de Fin del Turno <span className="text-red-500">*</span>
        </label>
        <input
          type="time"
          value={shiftEnd}
          onChange={(e: ChangeEvent<HTMLInputElement>) => {
            setShiftEnd(e.target.value);
            if (errors.shiftEnd) setErrors((prev) => ({ ...prev, shiftEnd: '' }));
          }}
          className={fieldClass('shiftEnd')}
        />
        {errors.shiftEnd && <p className="text-red-500 text-xs mt-1">{errors.shiftEnd}</p>}
        <p className="text-xs text-gray-500 mt-1">El turno debe ser de exactamente 8 horas (ej: 08:00 - 16:00)</p>
      </div>

      {errors.submit && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {errors.submit}
        </div>
      )}

      <div className="flex space-x-3">
        <button
          type="submit"
          disabled={isLoading}
          className="flex-1 py-2 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoading ? 'GUARDANDO...' : isEditMode ? 'ACTUALIZAR' : 'VINCULAR'}
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
  );
};

export default DoctorForm;
