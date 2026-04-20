import { useState } from 'react';
import type { FC, FormEvent, ChangeEvent } from 'react';
import { Link } from 'react-router-dom';
import { register } from '../services/authService';
import type { RegisterData } from '../services/authService';
import { validateDPI } from '../utils/validateDPI';
import { Navbar } from '../components';
import axios from 'axios';

interface FormState extends RegisterData {
  confirmPassword: string;
}

const initialForm: FormState = {
  dpi: '',
  nit: '',
  firstName: '',
  secondName: '',
  firstLastName: '',
  secondLastName: '',
  email: '',
  phone: '',
  address: '',
  password: '',
  confirmPassword: '',
};

type FieldErrors = Partial<Record<keyof FormState, string>>;

const RegisterPage: FC = () => {
  const [form, setForm] = useState<FormState>(initialForm);
  const [errors, setErrors] = useState<FieldErrors>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const validate = (): boolean => {
    const next: FieldErrors = {};

    const dpiResult = validateDPI(form.dpi);
    if (!dpiResult.valid)
      next.dpi = dpiResult.error!;
    if (!form.nit.trim())
      next.nit = 'El NIT es requerido.';
    if (!form.firstName.trim())
      next.firstName = 'El primer nombre es requerido.';
    if (!form.firstLastName.trim())
      next.firstLastName = 'El primer apellido es requerido.';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
      next.email = 'Ingresa un correo electrónico válido.';
    if (!/^\d{8}$/.test(form.phone))
      next.phone = 'El teléfono debe tener exactamente 8 dígitos.';
    if (form.password.length < 8)
      next.password = 'La contraseña debe tener al menos 8 caracteres.';
    if (form.password !== form.confirmPassword)
      next.confirmPassword = 'Las contraseñas no coinciden.';

    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (errors[name as keyof FormState])
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    if (serverError) setServerError(null);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setServerError(null);

    try {
      await register({
        dpi: form.dpi,
        nit: form.nit,
        firstName: form.firstName,
        secondName: form.secondName || undefined,
        firstLastName: form.firstLastName,
        secondLastName: form.secondLastName || undefined,
        email: form.email,
        phone: form.phone,
        address: form.address || undefined,
        password: form.password,
      });
      setSuccess(true);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const status = err.response?.status;
        const msg = err.response?.data?.message as string | undefined;
        if (status === 409)
          setServerError(msg ?? 'Ya existe una cuenta con ese DPI o correo electrónico.');
        else if (status === 400)
          setServerError(msg ?? 'Revisa los datos ingresados e intenta de nuevo.');
        else
          setServerError('Error al conectar con el servidor. Intenta más tarde.');
      } else {
        setServerError('Error inesperado. Intenta más tarde.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const inputClass = (field: keyof FormState) =>
    `block w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent transition-colors text-sm ${
      errors[field] ? 'border-red-400 bg-red-50' : 'border-gray-300'
    }`;

  // --- Éxito ---
  if (success) {
    return (
      <>
      <Navbar />
      <div className="min-h-screen bg-gradient-to-br from-medin-navy via-medin-navy to-medin-blue flex items-center justify-center p-4">
        <div className="bg-white rounded-2xl shadow-2xl p-10 w-full max-w-md text-center">
          <div className="h-20 w-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <svg className="h-10 w-10 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">¡Cuenta creada!</h2>
          <p className="text-gray-600 mb-8">
            Tu cuenta ha sido creada exitosamente. Ya puedes iniciar sesión con tu DPI y contraseña.
          </p>
          <Link
            to="/login"
            className="inline-block w-full py-3 px-4 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-center"
          >
            Iniciar sesión
          </Link>
        </div>
      </div>
      </>
    );
  }

  // --- Formulario ---
  return (
    <>
    <Navbar />
    <div className="min-h-screen bg-gradient-to-br from-medin-navy via-medin-navy to-medin-blue flex items-center justify-center p-4 py-10">
      <div className="absolute top-20 left-20 w-64 h-64 bg-medin-cyan opacity-10 rounded-full blur-3xl" />
      <div className="absolute bottom-20 right-20 w-96 h-96 bg-medin-blue opacity-10 rounded-full blur-3xl" />

      <div className="relative w-full max-w-lg">
        {/* Logo */}
        <div className="text-center mb-6">
          <div className="flex items-center justify-center space-x-3 mb-3">
            <img src="/icono.svg" alt="MedFlow" className="h-12 w-auto" />
            <h1 className="text-3xl font-bold">
              <span className="text-white">Med</span>
              <span className="text-medin-cyan">Flow</span>
            </h1>
          </div>
          <p className="text-gray-300 text-sm">Sistema de Información Hospitalaria</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <div className="mb-5">
            <h2 className="text-xl font-bold text-gray-900 mb-1">Crear cuenta de paciente</h2>
            <p className="text-gray-500 text-sm">Los campos marcados con <span className="text-red-500">*</span> son obligatorios</p>
          </div>

          {serverError && (
            <div className="mb-5 p-3 bg-red-50 border border-red-200 rounded-lg flex items-start space-x-2">
              <svg className="h-5 w-5 text-red-600 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-red-800 text-sm">{serverError}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4" noValidate>

            {/* DPI y NIT */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  DPI <span className="text-red-500">*</span>
                </label>
                <input
                  type="text" name="dpi" value={form.dpi}
                  onChange={handleChange} maxLength={13} inputMode="numeric"
                  placeholder="1234567890123"
                  className={inputClass('dpi')}
                />
                {errors.dpi && <p className="mt-1 text-xs text-red-600">{errors.dpi}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  NIT <span className="text-red-500">*</span>
                </label>
                <input
                  type="text" name="nit" value={form.nit}
                  onChange={handleChange} placeholder="12345678 o C/F"
                  className={inputClass('nit')}
                />
                {errors.nit && <p className="mt-1 text-xs text-red-600">{errors.nit}</p>}
              </div>
            </div>

            {/* Primer y segundo nombre */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Primer nombre <span className="text-red-500">*</span>
                </label>
                <input
                  type="text" name="firstName" value={form.firstName}
                  onChange={handleChange} placeholder="Juan"
                  className={inputClass('firstName')}
                />
                {errors.firstName && <p className="mt-1 text-xs text-red-600">{errors.firstName}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Segundo nombre
                </label>
                <input
                  type="text" name="secondName" value={form.secondName}
                  onChange={handleChange} placeholder="Carlos"
                  className={inputClass('secondName')}
                />
              </div>
            </div>

            {/* Primer y segundo apellido */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Primer apellido <span className="text-red-500">*</span>
                </label>
                <input
                  type="text" name="firstLastName" value={form.firstLastName}
                  onChange={handleChange} placeholder="García"
                  className={inputClass('firstLastName')}
                />
                {errors.firstLastName && <p className="mt-1 text-xs text-red-600">{errors.firstLastName}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Segundo apellido
                </label>
                <input
                  type="text" name="secondLastName" value={form.secondLastName}
                  onChange={handleChange} placeholder="López"
                  className={inputClass('secondLastName')}
                />
              </div>
            </div>

            {/* Correo y teléfono */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Correo electrónico <span className="text-red-500">*</span>
                </label>
                <input
                  type="email" name="email" value={form.email}
                  onChange={handleChange} placeholder="juan@ejemplo.com"
                  autoComplete="email"
                  className={inputClass('email')}
                />
                {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Teléfono <span className="text-red-500">*</span>
                </label>
                <input
                  type="text" name="phone" value={form.phone}
                  onChange={handleChange} maxLength={8} inputMode="numeric"
                  placeholder="55551234"
                  className={inputClass('phone')}
                />
                {errors.phone && <p className="mt-1 text-xs text-red-600">{errors.phone}</p>}
              </div>
            </div>

            {/* Dirección */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Dirección
              </label>
              <textarea
                name="address" value={form.address}
                onChange={handleChange}
                placeholder="Zona 1, Ciudad de Guatemala..."
                rows={2}
                className="block w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent transition-colors text-sm resize-none"
              />
            </div>

            {/* Contraseña */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Contraseña <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    name="password" value={form.password}
                    onChange={handleChange} placeholder="Mín. 8 caracteres"
                    autoComplete="new-password"
                    className={inputClass('password')}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                  >
                    {showPassword ? (
                      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                      </svg>
                    ) : (
                      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    )}
                  </button>
                </div>
                {errors.password && <p className="mt-1 text-xs text-red-600">{errors.password}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Confirmar contraseña <span className="text-red-500">*</span>
                </label>
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="confirmPassword" value={form.confirmPassword}
                  onChange={handleChange} placeholder="Repite tu contraseña"
                  autoComplete="new-password"
                  className={inputClass('confirmPassword')}
                />
                {errors.confirmPassword && <p className="mt-1 text-xs text-red-600">{errors.confirmPassword}</p>}
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3 px-4 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2 mt-2"
            >
              {isLoading ? (
                <>
                  <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  <span>Creando cuenta...</span>
                </>
              ) : (
                <span>Crear cuenta</span>
              )}
            </button>
          </form>

          <p className="mt-5 text-center text-sm text-gray-600">
            ¿Ya tienes cuenta?{' '}
            <Link to="/login" className="text-medin-cyan hover:text-medin-blue font-medium">
              Inicia sesión
            </Link>
          </p>
        </div>

        <div className="text-center mt-4">
          <p className="text-gray-400 text-xs">© 2026 MedFlow. Todos los derechos reservados.</p>
        </div>
      </div>
    </div>
    </>
  );
};

export default RegisterPage;
