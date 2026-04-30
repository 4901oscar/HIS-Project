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
  birthDate: '',
  gender: '',
  department: '',
  municipality: '',
  zone: '',
  address: '',
  password: '',
  confirmPassword: '',
};

type FieldErrors = Partial<Record<keyof FormState, string>>;

const RegisterPage: FC = () => {
  const today = new Date();
const minDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
const maxDateString = minDate.toISOString().split('T')[0]; // Formato YYYY-MM-DD
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
    if (!form.birthDate.trim())
      next.birthDate = 'La fecha de nacimiento es requerida.';
    else if (!/^\d{4}-\d{2}-\d{2}$/.test(form.birthDate))
      next.birthDate = 'Formato de fecha inválido (YYYY-MM-DD).';
    if (!form.gender.trim())
      next.gender = 'El género es requerido.';
    else if (form.gender !== 'M' && form.gender !== 'F')
      next.gender = 'El género debe ser M o F.';
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
        birthDate: form.birthDate,
        gender: form.gender,
        department: form.department || undefined,
        municipality: form.municipality || undefined,
        zone: form.zone || undefined,
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
    `block w-full px-6 py-3 border rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent transition-colors text-base font-medium ${
      errors[field] ? 'border-red-400 bg-red-50' : 'border-gray-300'
    }`;

  // --- Éxito ---
  if (success) {
    return (
      <>
      <Navbar />
      <div 
        className="min-h-screen flex items-center justify-center p-4 overflow-hidden relative"
        style={{
          backgroundColor: `#1F2B6C`,
        }}
      >
        {/* Brick-pattern medical cross overlay */}
        <div className="absolute inset-0 pointer-events-none" style={{
          backgroundImage: `
            url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><defs><linearGradient id="crossGrad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:rgba(21,158,236,0.2)"/><stop offset="100%" style="stop-color:rgba(21,158,236,0.05)"/></linearGradient></defs><g opacity="0.4"><rect x="40" y="5" width="20" height="90" fill="url(%23crossGrad)" stroke="rgba(21,158,236,0.15)" stroke-width="1" rx="2"/><rect x="5" y="40" width="90" height="20" fill="url(%23crossGrad)" stroke="rgba(21,158,236,0.15)" stroke-width="1" rx="2"/><rect x="140" y="105" width="20" height="90" fill="url(%23crossGrad)" stroke="rgba(21,158,236,0.15)" stroke-width="1" rx="2"/><rect x="105" y="140" width="90" height="20" fill="url(%23crossGrad)" stroke="rgba(21,158,236,0.15)" stroke-width="1" rx="2"/></g></svg>')`
          ,
          backgroundRepeat: 'repeat',
          backgroundSize: '200px 200px',
          opacity: 0.6,
        }}></div>
        <div className="relative bg-white rounded-3xl shadow-2xl p-12 w-full max-w-lg text-center">
          <div className="h-24 w-24 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <svg className="h-12 w-12 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 className="text-3xl font-bold text-gray-900 mb-4">¡Cuenta creada!</h2>
          <p className="text-gray-600 mb-10 text-lg">
            Tu cuenta ha sido creada exitosamente. Ya puedes iniciar sesión con tu DPI y contraseña.
          </p>
          <Link
            to="/login"
            className="inline-block w-full py-4 px-6 bg-medin-cyan text-medin-navy font-bold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-center text-base"
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
    <div 
      className="min-h-screen flex items-center justify-center p-4 py-10 overflow-hidden relative"
      style={{
        backgroundColor: `#1F2B6C`,
      }}
    >
      {/* Brick-pattern medical cross overlay */}
      <div className="absolute inset-0 pointer-events-none" style={{
        backgroundImage: `
          url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><defs><linearGradient id="crossGrad" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:rgba(21,158,236,0.2)"/><stop offset="100%" style="stop-color:rgba(21,158,236,0.05)"/></linearGradient></defs><g opacity="0.4"><rect x="40" y="5" width="20" height="90" fill="url(%23crossGrad)" stroke="rgba(21,158,236,0.15)" stroke-width="1" rx="2"/><rect x="5" y="40" width="90" height="20" fill="url(%23crossGrad)" stroke="rgba(21,158,236,0.15)" stroke-width="1" rx="2"/><rect x="140" y="105" width="20" height="90" fill="url(%23crossGrad)" stroke="rgba(21,158,236,0.15)" stroke-width="1" rx="2"/><rect x="105" y="140" width="90" height="20" fill="url(%23crossGrad)" stroke="rgba(21,158,236,0.15)" stroke-width="1" rx="2"/></g></svg>')`
        ,
        backgroundRepeat: 'repeat',
        backgroundSize: '200px 200px',
        opacity: 0.6,
      }}></div>

    

      <div className="relative w-full max-w-4xl">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center space-x-3 mb-4">
            <img src="/icono.svg" alt="MedFlow" className="h-14 w-auto" />
            <h1 className="text-4xl font-bold">
              <span className="text-white">Med</span>
              <span className="text-medin-cyan">Flow</span>
            </h1>
          </div>
          <p className="text-gray-200 text-base">Sistema de Información Hospitalaria</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-3xl shadow-2xl p-12 lg:p-16">
          <div className="mb-8">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Crear cuenta de paciente</h2>
            <p className="text-gray-500 text-base">Los campos marcados con <span className="text-red-500">*</span> son obligatorios</p>
          </div>

          {serverError && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start space-x-3">
              <svg className="h-6 w-6 text-red-600 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-red-800 text-base">{serverError}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6" noValidate>

            {/* DPI y NIT */}
            <div className="grid grid-cols-2 gap-5">
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">
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
                  maxLength={15}
                />
                {errors.nit && <p className="mt-1 text-xs text-red-600">{errors.nit}</p>}
              </div>
            </div>

            {/* Primer y segundo nombre */}
            <div className="grid grid-cols-2 gap-5">
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">
                  Primer nombre <span className="text-red-500">*</span>
                </label>
                <input
                  type="text" name="firstName" value={form.firstName}
                  onChange={handleChange} placeholder="Juan"
                  className={inputClass('firstName')}
                  maxLength={25}
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
                  maxLength={25}
                />
              </div>
            </div>

            {/* Primer y segundo apellido */}
            <div className="grid grid-cols-2 gap-5">
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">
                  Primer apellido <span className="text-red-500">*</span>
                </label>
                <input
                  type="text" name="firstLastName" value={form.firstLastName}
                  onChange={handleChange} placeholder="García"
                  className={inputClass('firstLastName')}
                  maxLength={25}
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
                  maxLength={25}
                />
              </div>
            </div>

            {/* Correo y teléfono */}
            <div className="grid grid-cols-2 gap-5">
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2 " >
                  Correo electrónico <span className="text-red-500">*</span>
                </label>
                <input
                  maxLength={60}
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

            {/* Fecha de nacimiento y género */}
            <div className="grid grid-cols-2 gap-5">
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">
                  Fecha de nacimiento <span className="text-red-500">*</span>
                </label>
                <input
                  type="date" name="birthDate" value={form.birthDate}
                  onChange={handleChange}
                  max={maxDateString}
                  className={inputClass('birthDate')}
                />
                {errors.birthDate && <p className="mt-1 text-xs text-red-600">{errors.birthDate}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Género <span className="text-red-500">*</span>
                </label>
                <select
                  name="gender" value={form.gender}
                  onChange={handleChange as any}
                  className={inputClass('gender')}
                >
                  <option value="">Selecciona...</option>
                  <option value="M">Masculino</option>
                  <option value="F">Femenino</option>
                  
                </select>
                {errors.gender && <p className="mt-1 text-xs text-red-600">{errors.gender}</p>}
              </div>
            </div>

            {/* Departamento, Municipio y Zona */}
            <div className="grid grid-cols-3 gap-5">
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">
                  Departamento
                </label>
                <input
                  type="text" name="department" value={form.department}
                  onChange={handleChange} placeholder="Guatemala"
                  className={inputClass('department')}
                />
              </div>
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">
                  Municipio
                </label>
                <input
                  type="text" name="municipality" value={form.municipality}
                  onChange={handleChange} placeholder="Guatemala"
                  className={inputClass('municipality')}
                />
              </div>
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">
                  Zona
                </label>
                <input
                  type="text" name="zone" value={form.zone}
                  onChange={handleChange} placeholder="1"
                  className={inputClass('zone')}
                />
              </div>
            </div>

            {/* Dirección */}
            <div>
              <label className="block text-base font-semibold text-gray-700 mb-2">
                Dirección
              </label>
              <textarea
                name="address" value={form.address}
                onChange={handleChange}
                placeholder="Zona 1, Ciudad de Guatemala..."
                rows={3}
                className="block w-full px-6 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent transition-colors text-base font-medium resize-none"
              />
            </div>

            {/* Contraseña */}
            <div className="grid grid-cols-2 gap-5">
              <div>
                <label className="block text-base font-semibold text-gray-700 mb-2">
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
                <label className="block text-base font-semibold text-gray-700 mb-2">
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
              className="w-full py-4 px-6 bg-medin-cyan text-medin-navy font-bold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2 mt-4 text-base"
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

          <p className="mt-8 text-center text-base text-gray-600">
            ¿Ya tienes cuenta?{' '}
            <Link to="/login" className="text-medin-cyan hover:text-medin-blue font-bold">
              Inicia sesión
            </Link>
          </p>
        </div>

        <div className="text-center mt-6">
          <p className="text-gray-400 text-sm">© 2026 MedFlow. Todos los derechos reservados.</p>
        </div>
      </div>
    </div>
    </>
  );
};

export default RegisterPage;
