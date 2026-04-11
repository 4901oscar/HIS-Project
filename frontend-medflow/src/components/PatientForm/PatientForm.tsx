/**
 * Componente PatientForm
 * Formulario para el registro de pacientes con datos biográficos
 */

import { useState } from 'react';
import type { FC, FormEvent } from 'react';
import patientService from '../../services/patientService';
import type { PatientBiographicData } from '../../types/patient';

interface PatientFormProps {
  onSuccess?: (message: string) => void;
  onError?: (message: string) => void;
}

const PatientForm: FC<PatientFormProps> = ({ onSuccess, onError }) => {
  const [formData, setFormData] = useState<PatientBiographicData>({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    dateOfBirth: '',
    address: '',
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Limpiar error de validación al escribir
    if (validationErrors[name]) {
      setValidationErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.firstName.trim()) {
      errors.firstName = 'El nombre es obligatorio';
    }

    if (!formData.lastName.trim()) {
      errors.lastName = 'El apellido es obligatorio';
    }

    if (!formData.email.trim()) {
      errors.email = 'El correo electrónico es obligatorio';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = 'Ingrese un correo electrónico válido';
    }

    if (!formData.phoneNumber.trim()) {
      errors.phoneNumber = 'El teléfono es obligatorio';
    }

    if (!formData.dateOfBirth) {
      errors.dateOfBirth = 'La fecha de nacimiento es obligatoria';
    }

    if (!formData.address.trim()) {
      errors.address = 'La dirección es obligatoria';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!validateForm()) {
      onError?.('Por favor, corrija los errores en el formulario');
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await patientService.registerPatient({
        biographicData: formData,
      });

      if (response.success) {
        onSuccess?.(response.message || 'Paciente registrado exitosamente');
        // Resetear formulario
        setFormData({
          firstName: '',
          lastName: '',
          email: '',
          phoneNumber: '',
          dateOfBirth: '',
          address: '',
        });
        setValidationErrors({});
      } else {
        onError?.(response.message || 'Error al registrar el paciente');
        // Manejar errores de validación del backend
        if (response.errors) {
          const backendErrors: Record<string, string> = {};
          Object.entries(response.errors).forEach(([field, messages]) => {
            backendErrors[field] = messages[0];
          });
          setValidationErrors(backendErrors);
        }
      }
    } catch (error) {
      onError?.('Error de conexión. Por favor, intente nuevamente.');
      console.error('Error al registrar paciente:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="w-full space-y-6">
      {/* Nombre */}
      <div>
        <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-1">
          Nombre *
        </label>
        <input
          type="text"
          id="firstName"
          name="firstName"
          value={formData.firstName}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            validationErrors.firstName ? 'border-red-500' : 'border-gray-300'
          }`}
          disabled={isSubmitting}
        />
        {validationErrors.firstName && (
          <p className="mt-1 text-sm text-red-600">{validationErrors.firstName}</p>
        )}
      </div>

      {/* Apellido */}
      <div>
        <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-1">
          Apellido *
        </label>
        <input
          type="text"
          id="lastName"
          name="lastName"
          value={formData.lastName}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            validationErrors.lastName ? 'border-red-500' : 'border-gray-300'
          }`}
          disabled={isSubmitting}
        />
        {validationErrors.lastName && (
          <p className="mt-1 text-sm text-red-600">{validationErrors.lastName}</p>
        )}
      </div>

      {/* Correo Electrónico */}
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
          Correo Electrónico *
        </label>
        <input
          type="email"
          id="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            validationErrors.email ? 'border-red-500' : 'border-gray-300'
          }`}
          disabled={isSubmitting}
        />
        {validationErrors.email && (
          <p className="mt-1 text-sm text-red-600">{validationErrors.email}</p>
        )}
      </div>

      {/* Teléfono */}
      <div>
        <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-700 mb-1">
          Teléfono *
        </label>
        <input
          type="tel"
          id="phoneNumber"
          name="phoneNumber"
          value={formData.phoneNumber}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            validationErrors.phoneNumber ? 'border-red-500' : 'border-gray-300'
          }`}
          disabled={isSubmitting}
        />
        {validationErrors.phoneNumber && (
          <p className="mt-1 text-sm text-red-600">{validationErrors.phoneNumber}</p>
        )}
      </div>

      {/* Fecha de Nacimiento */}
      <div>
        <label htmlFor="dateOfBirth" className="block text-sm font-medium text-gray-700 mb-1">
          Fecha de Nacimiento *
        </label>
        <input
          type="date"
          id="dateOfBirth"
          name="dateOfBirth"
          value={formData.dateOfBirth}
          onChange={handleChange}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            validationErrors.dateOfBirth ? 'border-red-500' : 'border-gray-300'
          }`}
          disabled={isSubmitting}
        />
        {validationErrors.dateOfBirth && (
          <p className="mt-1 text-sm text-red-600">{validationErrors.dateOfBirth}</p>
        )}
      </div>

      {/* Dirección */}
      <div>
        <label htmlFor="address" className="block text-sm font-medium text-gray-700 mb-1">
          Dirección *
        </label>
        <textarea
          id="address"
          name="address"
          value={formData.address}
          onChange={handleChange}
          rows={3}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            validationErrors.address ? 'border-red-500' : 'border-gray-300'
          }`}
          disabled={isSubmitting}
        />
        {validationErrors.address && (
          <p className="mt-1 text-sm text-red-600">{validationErrors.address}</p>
        )}
      </div>

      {/* Botón de Envío */}
      <div className="pt-4">
        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isSubmitting ? 'Registrando...' : 'Registrar Paciente'}
        </button>
      </div>
    </form>
  );
};

export default PatientForm;
