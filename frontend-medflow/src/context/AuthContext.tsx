import { createContext, useState, useEffect } from 'react';
import type { ReactNode, FC } from 'react';
import type { AuthUser } from '../services/authService';
import { getCurrentUser, isAuthenticated, logout as logoutService } from '../services/authService';

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  hasRole: (role: string) => boolean;
  logout: () => void;
  setUser: (user: AuthUser | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (isAuthenticated()) {
      setUser(getCurrentUser());
    }
    setIsLoading(false);
  }, []);

  const logout = () => {
    logoutService();
    setUser(null);
  };

  const hasRole = (role: string) =>
    !!user?.roles?.includes(role) || !!user?.roles?.includes('ADMIN');

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, hasRole, logout, setUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;
