/**
 * LoginPage - Página de autenticación para empleados del hospital
 * Solo personal autorizado puede acceder al sistema
 */

import { useState } from 'react';
import type { FC, FormEvent } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { login } from '../services/authService';
import type { LoginCredentials } from '../services/authService';
import { useAuth } from '../hooks/useAuth';
import { Navbar } from '../components';
import axios from 'axios';

const LoginPage: FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { setUser } = useAuth();
  const [credentials, setCredentials] = useState<LoginCredentials>({
    username: '',
    password: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const response = await login(credentials);
      setUser(response.user);

      const roleRoutes: Record<string, string> = {
        'ADMIN': '/administrator',
        'ADMISSION': '/admission',
        'VITAL_SIGNS': '/vitals/triage',
        'DOCTOR': '/doctor',
        'LABORATORY': '/lab',
        'PHARMACY': '/pharmacy',
        'CASHIER': '/cashier',
        'PATIENT': '/',
      };

      const from = (location.state as { from?: string })?.from;
      const primaryRole = response.user.roles[0] ?? '';
      const redirectPath = from ?? roleRoutes[primaryRole] ?? '/dashboard';
      navigate(redirectPath);
    } catch (err) {
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        setError('Usuario o contraseña incorrectos.');
      } else if (axios.isAxiosError(err) && err.response?.status === 429) {
        setError('Demasiados intentos. Espera un momento e intenta de nuevo.');
      } else {
        setError('Error al conectar con el servidor. Intenta más tarde.');
      }
      setCredentials({ ...credentials, password: '' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCredentials((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Limpiar error al escribir
    if (error) setError(null);
  };

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
      

      <div className="relative w-full max-w-2xl">
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

        {/* Login Card */}
        <div className="bg-white rounded-3xl shadow-2xl p-12 lg:p-16">
          <div className="mb-8">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Inicio de sesión</h2>
            <p className="text-gray-600 text-base">Ingresa tus credenciales para acceder al sistema</p>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-8 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start space-x-3">
              <svg className="h-6 w-6 text-red-600 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-red-800 text-base">{error}</p>
            </div>
          )}

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Username */}
            <div>
              <label htmlFor="username" className="block text-base font-semibold text-gray-700 mb-2">
                Correo electrónico <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <input
                  type="email"
                  id="username"
                  name="username"
                  value={credentials.username}
                  onChange={handleChange}
                  required
                  autoComplete="username"
                  className="block w-full pl-12 pr-4 py-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent transition-colors text-base font-medium"
                  placeholder="Correo electrónico"
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="block text-base font-semibold text-gray-700 mb-2">
                Contraseña <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  name="password"
                  value={credentials.password}
                  onChange={handleChange}
                  required
                  autoComplete="current-password"
                  className="block w-full pl-12 pr-14 py-4 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent transition-colors text-base font-medium"
                  placeholder="Contraseña"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? (
                    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  ) : (
                    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

           

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-4 px-6 bg-medin-cyan text-medin-navy font-bold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2 text-base mt-4"
            >
              {isLoading ? (
                <>
                  <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span>Ingresando...</span>
                </>
              ) : (
                <span>Ingresar</span>
              )}
            </button>
          </form>

          <p className="mt-8 text-center text-base text-gray-600">
      
            <Link to="/register" className="text-medin-cyan hover:text-medin-blue font-bold">
              Regístrate aquí
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

export default LoginPage;
