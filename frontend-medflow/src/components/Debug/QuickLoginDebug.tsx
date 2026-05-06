/**
 * QuickLoginDebug - Componente para testing rápido (SOLO DESARROLLO)
 * 
 * Muestra botones para loguear rápidamente con usuarios mock.
 * Remover este componente antes de producción.
 * 
 * Uso:
 * import QuickLoginDebug from './QuickLoginDebug';
 * <QuickLoginDebug /> // en LoginPage o App
 */

import React from 'react';
import { getMockAuthResponse } from '../../services/mockData';
import { MOCK_USERS } from '../../services/mockData';

const QuickLoginDebug: React.FC = () => {
  const handleQuickLogin = (username: string) => {
    const response = getMockAuthResponse(username);
    if (response) {
      localStorage.setItem('auth_token', response.token);
      localStorage.setItem('user_data', JSON.stringify(response.user));
      // Recargar o navegar a dashboard
      window.location.href = '/dashboard';
    }
  };

  // Solo mostrar en desarrollo
  if (import.meta.env.MODE !== 'development') {
    return null;
  }

  return (
    <div className="fixed bottom-4 right-4 bg-yellow-100 border-2 border-yellow-500 rounded-lg p-3 max-w-xs z-50">
      <details className="cursor-pointer">
        <summary className="font-bold text-sm text-yellow-900 hover:text-yellow-800">
          🧪 Login Rápido (DEV)
        </summary>
        <div className="mt-2 space-y-1">
          {Object.entries(MOCK_USERS).map(([key, user]) => (
            <button
              key={key}
              onClick={() => handleQuickLogin(user.username)}
              className="block w-full text-left px-2 py-1 text-xs bg-yellow-50 hover:bg-yellow-200 rounded border border-yellow-300 truncate"
              title={`${user.username} / ${user.password}`}
            >
              {user.username} ({user.roles[0]})
            </button>
          ))}
        </div>
        <p className="text-xs text-yellow-700 mt-2 italic">
          Solo visible en desarrollo
        </p>
      </details>
    </div>
  );
};

export default QuickLoginDebug;
