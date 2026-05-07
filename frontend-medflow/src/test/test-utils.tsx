import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import type { ReactElement } from 'react';

// Mock admin user for testing
export const mockAdminUser = {
  id: 'admin-1',
  username: 'admin@test.com',
  fullName: 'Admin Test',
  role: 'ADMIN' as const,
};

// Mock doctor user for testing
export const mockDoctorUser = {
  id: 'doctor-1',
  username: 'doctor@test.com',
  fullName: 'Dr. Test',
  role: 'DOCTOR' as const,
};

interface RenderOptions {
  user?: typeof mockAdminUser | typeof mockDoctorUser;
}

/**
 * Render a component with all necessary providers (Router, Auth, etc.)
 */
export const renderWithProviders = (
  ui: ReactElement,
  options: RenderOptions = {}
) => {
  const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <BrowserRouter>
      <AuthProvider>
        {children}
      </AuthProvider>
    </BrowserRouter>
  );

  return render(ui, { wrapper: Wrapper, ...options });
};
