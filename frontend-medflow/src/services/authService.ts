/**
 * authService - Servicio de autenticación para empleados
 * PLACEHOLDER MODE: Mock data hasta que el backend esté disponible
 */

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import axios from 'axios';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const API_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: {
    id: string;
    username: string;
    name: string;
    role: string;
    email: string;
  };
}

/**
 * Login de empleado
 * @param credentials - Username y password
 * @returns Promise con token JWT y datos del usuario
 */
export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
  // BACKEND READY: Descomentar cuando el endpoint esté disponible
  // try {
  //   const response = await axios.post(`${API_URL}/auth/login`, credentials);
  //   const { token, user } = response.data;
  //   
  //   // Guardar token en localStorage
  //   localStorage.setItem('auth_token', token);
  //   localStorage.setItem('user_data', JSON.stringify(user));
  //   
  //   return { token, user };
  // } catch (error) {
  //   console.error('Error during login:', error);
  //   throw error;
  // }

  // Mock: Simular autenticación
  console.log('Mock: Authenticating user...', credentials.username);

  // Validación básica de mock
  const mockUsers: Record<string, AuthResponse> = {
    'admin': {
      token: 'mock_jwt_token_admin_123456789',
      user: {
        id: 'USR-001',
        username: 'admin',
        name: 'Administrator User',
        role: 'ADMINISTRATOR',
        email: 'admin@medflow.com',
      },
    },
    'admission': {
      token: 'mock_jwt_token_admission_123456789',
      user: {
        id: 'USR-002',
        username: 'admission',
        name: 'Admission Staff',
        role: 'ADMISSION',
        email: 'admission@medflow.com',
      },
    },
    'doctor': {
      token: 'mock_jwt_token_doctor_123456789',
      user: {
        id: 'USR-003',
        username: 'doctor',
        name: 'Dr. María López',
        role: 'DOCTOR',
        email: 'doctor@medflow.com',
      },
    },
    'nurse': {
      token: 'mock_jwt_token_nurse_123456789',
      user: {
        id: 'USR-004',
        username: 'nurse',
        name: 'Nurse Ana García',
        role: 'VITAL_SIGNS',
        email: 'nurse@medflow.com',
      },
    },
  };

  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const user = mockUsers[credentials.username.toLowerCase()];
      
      if (user && credentials.password === 'password') {
        // Guardar en localStorage (mock)
        localStorage.setItem('auth_token', user.token);
        localStorage.setItem('user_data', JSON.stringify(user.user));
        
        resolve(user);
      } else {
        reject(new Error('Invalid credentials'));
      }
    }, 1000);
  });
};

/**
 * Logout del usuario
 */
export const logout = async (): Promise<void> => {
  // BACKEND READY: Descomentar cuando el endpoint esté disponible
  // try {
  //   const token = localStorage.getItem('auth_token');
  //   await axios.post(`${API_URL}/auth/logout`, {}, {
  //     headers: { Authorization: `Bearer ${token}` }
  //   });
  // } catch (error) {
  //   console.error('Error during logout:', error);
  // }

  // Limpiar localStorage
  localStorage.removeItem('auth_token');
  localStorage.removeItem('user_data');
  
  console.log('Mock: User logged out');
};

/**
 * Verificar si el usuario está autenticado
 * @returns boolean
 */
export const isAuthenticated = (): boolean => {
  const token = localStorage.getItem('auth_token');
  return !!token;
};

/**
 * Obtener datos del usuario actual
 * @returns User data o null
 */
export const getCurrentUser = () => {
  const userData = localStorage.getItem('user_data');
  return userData ? JSON.parse(userData) : null;
};

/**
 * Verificar token JWT (validación con backend)
 */
export const verifyToken = async (): Promise<boolean> => {
  // BACKEND READY: Descomentar cuando el endpoint esté disponible
  // try {
  //   const token = localStorage.getItem('auth_token');
  //   if (!token) return false;
  //   
  //   const response = await axios.post(`${API_URL}/auth/verify`, {}, {
  //     headers: { Authorization: `Bearer ${token}` }
  //   });
  //   
  //   return response.data.valid;
  // } catch (error) {
  //   console.error('Token verification failed:', error);
  //   return false;
  // }

  // Mock: Simular verificación
  const token = localStorage.getItem('auth_token');
  return Promise.resolve(!!token);
};

export default {
  login,
  logout,
  isAuthenticated,
  getCurrentUser,
  verifyToken,
};
