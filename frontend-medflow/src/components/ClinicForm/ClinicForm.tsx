import { useState, useEffect } from 'react';
import type { FC, FormEvent, ChangeEvent } from 'react';
import { createClinic, updateClinic } from '../../services/clinicService';
import type { Clinic, ClinicStatus } from '../../types/clinic';
import { validateForm, clearFieldError, getBlockingError } from '../../utils/formValidation';
import type { Schema, FormErrors } from '../../utils/formValidation';

interface ClinicFormProps {
  clinic?: Clinic | null;
  onSuccess?: () => void;
  onCancel?: () => void;
}

const ClinicForm: FC<ClinicFormProps> = ({ clinic, onSuccess, onCancel }) => {
  const isEditMode = !!clinic;

  interface ClinicFormState { codigo: string; nombre: string; descripcion: string; }

  const [form, setForm] = useState<ClinicFormState>({ codigo: '', nombre: '', descripcion: '' });
  const [estado, setEstado] = useState<ClinicStatus>('ACTIVE');
  const [errors, setErrors] = useState<FormErrors<ClinicFormState>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const clinicSchema: Schema<ClinicFormState> = {
    codigo:     [{ type: 'required', message: 'El código es obligatorio.' }, { type: 'digits' }, { type: 'maxLength', max: 6, message: 'Máximo 6 dígitos.' }],
    nombre:     [{ type: 'required', message: 'El nombre es obligatorio.' }, { type: 'alphaName' }, { type: 'maxLength', max: 32, message: 'Máximo 32 caracteres.' }],
    descripcion:[{ type: 'required', message: 'La descripción es obligatoria.' }, { type: 'maxLength', max: 256, message: 'Máximo 256 caracteres.' }],
  };

  useEffect(() => {
    if (isEditMode && clinic) {
      setForm({ codigo: clinic.codigo, nombre: clinic.nombre, descripcion: clinic.descripcion });
      setEstado(clinic.estado);
    }
  }, [clinic, isEditMode]);

  const validate = (): boolean => {
    const errs = validateForm(clinicSchema, form);
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    const rules = clinicSchema[name as keyof ClinicFormState] ?? [];
    const blockErr = getBlockingError(rules, value);
    if (blockErr) {
      setErrors(prev => ({ ...prev, [name]: blockErr }));
      return;
    }
    setForm(prev => ({ ...prev, [name]: value }));
    setErrors(prev => clearFieldError(prev, name as keyof ClinicFormState));
    if (submitError) setSubmitError(null);
  };

  const handleCodigoChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (/[^\d]/.test(value)) {
      setErrors(prev => ({ ...prev, codigo: 'Dato inválido.' }));
      return;
    }
    setForm(prev => ({ ...prev, codigo: value }));
    setErrors(prev => clearFieldError(prev, 'codigo'));
    if (submitError) setSubmitError(null);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setSubmitError(null);
    try {
      if (isEditMode && clinic) {
        await updateClinic(clinic.id, {
          codigo: form.codigo.trim(),
          nombre: form.nombre.trim(),
          descripcion: form.descripcion.trim(),
          estado,
        });
      } else {
        await createClinic({
          codigo: form.codigo.trim(),
          nombre: form.nombre.trim(),
          descripcion: form.descripcion.trim(),
        });
      }
      onSuccess?.();
    } catch (err: unknown) {
      const { isAxiosError } = await import('axios');
      const msg = isAxiosError(err)
        ? (err.response?.data as { message?: string })?.message ?? 'Error al guardar la clínica'
        : 'Error al guardar la clínica';
      setSubmitError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const fieldClass = (field: keyof ClinicFormState) =>
    `w-full px-4 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
      errors[field] ? 'border-red-500 bg-red-50' : 'border-gray-300'
    }`;

  return (
    <form onSubmit={handleSubmit} className="space-y-4 max-w-2xl">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">
        {isEditMode ? 'Editar Clínica' : 'Crear Nueva Clínica'}
      </h3>

      {/* Codigo */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Código <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          name="codigo"
          value={form.codigo}
          onChange={handleCodigoChange}
          placeholder="101"
          maxLength={6}
          className={fieldClass('codigo')}
        />
        {errors.codigo && <p className="text-red-500 text-xs mt-1">{errors.codigo}</p>}
        <p className="text-xs text-gray-500 mt-1">Solo números, máx. 6 dígitos (ej: 101 para nivel 1, oficina 01)</p>
      </div>

      {/* Nombre */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Nombre <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          name="nombre"
          value={form.nombre}
          onChange={handleChange}
          placeholder="Clínica Principal"
          maxLength={32}
          className={fieldClass('nombre')}
        />
        {errors.nombre && <p className="text-red-500 text-xs mt-1">{errors.nombre}</p>}
        <p className="text-xs text-gray-500 mt-1">Letras, números y guiones, máx. 32 caracteres</p>
      </div>

      {/* Descripcion */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Descripción <span className="text-red-500">*</span>
        </label>
        <textarea
          name="descripcion"
          value={form.descripcion}
          onChange={handleChange}
          placeholder="Descripción de la clínica"
          rows={3}
          maxLength={256}
          className={fieldClass('descripcion')}
        />
        {errors.descripcion && <p className="text-red-500 text-xs mt-1">{errors.descripcion}</p>}
        <p className="text-xs text-gray-500 mt-1">Máx. 256 caracteres</p>
      </div>

      {/* Estado (edit mode only) */}
      {isEditMode && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Estado <span className="text-red-500">*</span>
          </label>
          <select
            value={estado}
            onChange={(e) => setEstado(e.target.value as ClinicStatus)}
            className={fieldClass('estado')}
          >
            <option value="ACTIVE">Activa</option>
            <option value="INACTIVE">Inactiva</option>
            <option value="DELETED">Eliminada</option>
          </select>
        </div>
      )}

      {submitError && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {submitError}
        </div>
      )}

      <div className="flex space-x-3">
        <button
          type="submit"
          disabled={isLoading}
          className="flex-1 py-2 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoading ? 'GUARDANDO...' : isEditMode ? 'ACTUALIZAR' : 'CREAR'}
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

export default ClinicForm;
