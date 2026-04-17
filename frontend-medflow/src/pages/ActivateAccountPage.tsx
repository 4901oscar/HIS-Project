import { useEffect, useState } from 'react';
import type { FC, ReactElement } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { activateAccount } from '../services/authService';
import axios from 'axios';

type Status = 'loading' | 'success' | 'invalid_token' | 'already_active' | 'error';

const ActivateAccountPage: FC = () => {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState<Status>('loading');

  useEffect(() => {
    const token = searchParams.get('token');

    if (!token) {
      setStatus('invalid_token');
      return;
    }

    activateAccount(token)
      .then(() => setStatus('success'))
      .catch((err) => {
        if (axios.isAxiosError(err)) {
          const serverMsg = (err.response?.data?.message as string | undefined)?.toLowerCase() ?? '';
          if (err.response?.status === 400 && serverMsg.includes('already')) {
            setStatus('already_active');
          } else if (err.response?.status === 400 || err.response?.status === 404) {
            setStatus('invalid_token');
          } else {
            setStatus('error');
          }
        } else {
          setStatus('error');
        }
      });
  }, [searchParams]);

  const content: Record<Status, { icon: ReactElement; title: string; body: string; cta: ReactElement }> = {
    loading: {
      icon: (
        <div className="h-20 w-20 rounded-full bg-medin-cyan/20 flex items-center justify-center mx-auto mb-6">
          <svg className="animate-spin h-10 w-10 text-medin-cyan" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
        </div>
      ),
      title: 'Activando tu cuenta…',
      body: 'Por favor espera un momento.',
      cta: <></>,
    },
    success: {
      icon: (
        <div className="h-20 w-20 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-6">
          <svg className="h-10 w-10 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>
      ),
      title: '¡Cuenta activada!',
      body: 'Tu cuenta ha sido verificada exitosamente. Ya puedes iniciar sesión.',
      cta: (
        <Link
          to="/login"
          className="inline-block w-full py-3 px-4 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-center"
        >
          Iniciar sesión
        </Link>
      ),
    },
    already_active: {
      icon: (
        <div className="h-20 w-20 rounded-full bg-blue-100 flex items-center justify-center mx-auto mb-6">
          <svg className="h-10 w-10 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M12 2a10 10 0 100 20A10 10 0 0012 2z" />
          </svg>
        </div>
      ),
      title: 'Cuenta ya activa',
      body: 'Esta cuenta ya fue activada anteriormente. Puedes iniciar sesión directamente.',
      cta: (
        <Link
          to="/login"
          className="inline-block w-full py-3 px-4 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-center"
        >
          Iniciar sesión
        </Link>
      ),
    },
    invalid_token: {
      icon: (
        <div className="h-20 w-20 rounded-full bg-yellow-100 flex items-center justify-center mx-auto mb-6">
          <svg className="h-10 w-10 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
          </svg>
        </div>
      ),
      title: 'Enlace inválido o expirado',
      body: 'El enlace de activación no es válido o ya expiró. Vuelve a registrarte para recibir un nuevo correo.',
      cta: (
        <Link
          to="/register"
          className="inline-block w-full py-3 px-4 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-center"
        >
          Volver al registro
        </Link>
      ),
    },
    error: {
      icon: (
        <div className="h-20 w-20 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-6">
          <svg className="h-10 w-10 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
      ),
      title: 'Error al activar',
      body: 'Ocurrió un error al conectar con el servidor. Intenta hacer clic en el enlace nuevamente.',
      cta: (
        <button
          onClick={() => window.location.reload()}
          className="w-full py-3 px-4 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors"
        >
          Reintentar
        </button>
      ),
    },
  };

  const { icon, title, body, cta } = content[status];

  return (
    <div className="min-h-screen bg-gradient-to-br from-medin-navy via-medin-navy to-medin-blue flex items-center justify-center p-4">
      <div className="absolute top-20 left-20 w-64 h-64 bg-medin-cyan opacity-10 rounded-full blur-3xl" />
      <div className="absolute bottom-20 right-20 w-96 h-96 bg-medin-blue opacity-10 rounded-full blur-3xl" />

      <div className="relative w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center space-x-3 mb-4">
            <img src="/icono.svg" alt="MedFlow" className="h-16 w-auto" />
            <h1 className="text-4xl font-bold">
              <span className="text-white">Med</span>
              <span className="text-medin-cyan">Flow</span>
            </h1>
          </div>
          <p className="text-gray-300 text-sm">Hospital Information System</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-2xl p-10 text-center">
          {icon}
          <h2 className="text-2xl font-bold text-gray-900 mb-3">{title}</h2>
          <p className="text-gray-600 mb-8">{body}</p>
          {cta}
        </div>

        <div className="text-center mt-6">
          <p className="text-gray-400 text-xs">© 2026 MedFlow. Todos los derechos reservados.</p>
        </div>
      </div>
    </div>
  );
};

export default ActivateAccountPage;
