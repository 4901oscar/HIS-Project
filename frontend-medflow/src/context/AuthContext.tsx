/**
 * AuthContext - Contexto global de autenticación
 * Gestiona el estado de autenticación del usuario en toda la aplicación
 */

import { createContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { FC } from 'react';
import { getCurrentUser, isAuthenticated, logout as logoutService } from '../services/authService';

interface User {
  id: string;
  username: string;
  name: string;
  role: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  logout: () => void;
  setUser: (user: User | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay sesión activa al cargar
    const checkAuth = () => {
      if (isAuthenticated()) {
        const userData = getCurrentUser();
        setUser(userData);
      }
      setIsLoading(false);
    };

    checkAuth();
  }, []);

  const logout = () => {
    logoutService();
    setUser(null);
  };

  const value = {
    user,
    isAuthenticated: !!user,
    isLoading,
    logout,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthContext;
