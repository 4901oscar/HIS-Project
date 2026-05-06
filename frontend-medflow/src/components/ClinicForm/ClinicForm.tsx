import { useState, useEffect } from 'react';
import type { FC, FormEvent, ChangeEvent } from 'react';
import { createClinic, updateClinic } from '../../services/clinicService';
import type { Clinic, ClinicStatus } from '../../types/clinic';

interface ClinicFormProps {
  clinic?: Clinic | null;
  onSuccess?: () => void;
  onCancel?: () => void;
}

const ClinicForm: FC<ClinicFormProps> = ({ clinic, onSuccess, onCancel }) => {
  const isEditMode = !!clinic;

  const [codigo, setCodigo] = useState('');
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [estado, setEstado] = useState<ClinicStatus>('ACTIVE');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (isEditMode && clinic) {
      setCodigo(clinic.codigo);
      setNombre(clinic.nombre);
      setDescripcion(clinic.descripcion);
      setEstado(clinic.estado);
    }
  }, [clinic, isEditMode]);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Codigo validation
    if (!codigo.trim()) {
      newErrors.codigo = 'El código es obligatorio';
    } else if (!/^[0-9]+$/.test(codigo)) {
      newErrors.codigo = 'El código debe contener solo caracteres numéricos';
    }

    // Nombre validation
    if (!nombre.trim()) {
      newErrors.nombre = 'El nombre es obligatorio';
    } else if (!/^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\s]+$/.test(nombre)) {
      newErrors.nombre = 'El nombre debe contener solo caracteres alfanuméricos';
    }

    // Descripcion validation
    if (!descripcion.trim()) {
      newErrors.descripcion = 'La descripción es obligatoria';
    } else if (!/^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\s]+$/.test(descripcion)) {
      newErrors.descripcion = 'La descripción debe contener solo caracteres alfanuméricos';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setSubmitError(null);
    try {
      if (isEditMode && clinic) {
        await updateClinic(clinic.id, {
          codigo: codigo.trim(),
          nombre: nombre.trim(),
          descripcion: descripcion.trim(),
          estado,
        });
      } else {
        await createClinic({
          codigo: codigo.trim(),
          nombre: nombre.trim(),
          descripcion: descripcion.trim(),
        });
      }
      onSuccess?.();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Error al guardar la clínica';
      setSubmitError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCodigoChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setCodigo(value);
    if (errors.codigo) {
      // Real-time validation
      if (!value.trim()) {
        setErrors((prev) => ({ ...prev, codigo: 'El código es obligatorio' }));
      } else if (!/^[0-9]+$/.test(value)) {
        setErrors((prev) => ({ ...prev, codigo: 'El código debe contener solo caracteres numéricos' }));
      } else {
        setErrors((prev) => ({ ...prev, codigo: '' }));
      }
    }
  };

  const handleNombreChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setNombre(value);
    if (errors.nombre) {
      // Real-time validation
      if (!value.trim()) {
        setErrors((prev) => ({ ...prev, nombre: 'El nombre es obligatorio' }));
      } else if (!/^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\s]+$/.test(value)) {
        setErrors((prev) => ({ ...prev, nombre: 'El nombre debe contener solo caracteres alfanuméricos' }));
      } else {
        setErrors((prev) => ({ ...prev, nombre: '' }));
      }
    }
  };

  const handleDescripcionChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value;
    setDescripcion(value);
    if (errors.descripcion) {
      // Real-time validation
      if (!value.trim()) {
        setErrors((prev) => ({ ...prev, descripcion: 'La descripción es obligatoria' }));
      } else if (!/^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\s]+$/.test(value)) {
        setErrors((prev) => ({ ...prev, descripcion: 'La descripción debe contener solo caracteres alfanuméricos' }));
      } else {
        setErrors((prev) => ({ ...prev, descripcion: '' }));
      }
    }
  };

  const fieldClass = (field: string) =>
    `w-full px-4 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-medin-cyan ${
      errors[field] ? 'border-red-500' : 'border-gray-300'
    }`;

  const isFormValid = () => {
    return (
      codigo.trim() &&
      /^[0-9]+$/.test(codigo) &&
      nombre.trim() &&
      /^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\s]+$/.test(nombre) &&
      descripcion.trim() &&
      /^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\s]+$/.test(descripcion) &&
      !Object.values(errors).some(Boolean)
    );
  };

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
          value={codigo}
          onChange={handleCodigoChange}
          placeholder="101"
          className={fieldClass('codigo')}
        />
        {errors.codigo && <p className="text-red-500 text-xs mt-1">{errors.codigo}</p>}
        <p className="text-xs text-gray-500 mt-1">Solo caracteres numéricos (ej: 101 para nivel 1 oficina 01)</p>
      </div>

      {/* Nombre */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Nombre <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          value={nombre}
          onChange={handleNombreChange}
          placeholder="Clínica Principal"
          className={fieldClass('nombre')}
        />
        {errors.nombre && <p className="text-red-500 text-xs mt-1">{errors.nombre}</p>}
        <p className="text-xs text-gray-500 mt-1">Solo caracteres alfanuméricos</p>
      </div>

      {/* Descripcion */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Descripción <span className="text-red-500">*</span>
        </label>
        <textarea
          value={descripcion}
          onChange={handleDescripcionChange}
          placeholder="Descripción de la clínica"
          rows={3}
          className={fieldClass('descripcion')}
        />
        {errors.descripcion && <p className="text-red-500 text-xs mt-1">{errors.descripcion}</p>}
        <p className="text-xs text-gray-500 mt-1">Solo caracteres alfanuméricos</p>
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
          disabled={isLoading || !isFormValid()}
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
