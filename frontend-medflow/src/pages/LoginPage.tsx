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
        'DOCTOR': '/doctor/consultas',
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
    <div className="min-h-screen bg-gradient-to-br from-medin-navy via-medin-navy to-medin-blue flex items-center justify-center p-4">
      {/* Decorative circles */}
      <div className="absolute top-20 left-20 w-64 h-64 bg-medin-cyan opacity-10 rounded-full blur-3xl"></div>
      <div className="absolute bottom-20 right-20 w-96 h-96 bg-medin-blue opacity-10 rounded-full blur-3xl"></div>

      <div className="relative w-full max-w-md">      
        {/* Login Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <div className="mb-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Inicio de sesión</h2>
            <p className="text-gray-600 text-sm">Ingresa tus credenciales para acceder al sistema</p>
          </div>

          {/* Error Message */}
          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start space-x-3">
              <svg className="h-5 w-5 text-red-600 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-red-800 text-sm">{error}</p>
            </div>
          )}

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Username */}
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
                Usuario <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <input
                  type="text"
                  id="username"
                  name="username"
                  value={credentials.username}
                  onChange={handleChange}
                  required
                  autoComplete="username"
                  className="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent transition-colors"
                  placeholder="Usuario o correo electrónico"
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
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
                  className="block w-full pl-10 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent transition-colors"
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

            {/* Remember Me */}
            <div className="flex items-center justify-between">
              <label className="flex items-center">
                <input
                  type="checkbox"
                  className="h-4 w-4 text-medin-cyan border-gray-300 rounded focus:ring-medin-cyan"
                />
                <span className="ml-2 text-sm text-gray-600">Recordarme</span>
              </label>
              <span className="text-sm text-gray-400">¿Olvidaste tu contraseña?</span>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3 px-4 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
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

          <p className="mt-6 text-center text-sm text-gray-600">
            ¿Eres paciente y no tienes cuenta?{' '}
            <Link to="/register" className="text-medin-cyan hover:text-medin-blue font-medium">
              Regístrate aquí
            </Link>
          </p>
        </div>

        <div className="text-center mt-6">
          <p className="text-gray-400 text-xs">© 2026 MedFlow. Todos los derechos reservados.</p>
        </div>
      </div>
    </div>
    </>
  );
};

export default LoginPage;
