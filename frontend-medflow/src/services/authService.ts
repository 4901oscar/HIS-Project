import axios from 'axios';

const API_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080';

const api = axios.create({ baseURL: API_URL });

// Adjunta el token en cada request si existe
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthUser {
  id: string;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
  active: boolean;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
  user: AuthUser;
}

export interface RegisterData {
  dpi: string;
  nit: string;
  firstName: string;
  secondName?: string;
  firstLastName: string;
  secondLastName?: string;
  email: string;
  phone: string;
  address?: string;
  password: string;
}

export interface RegisterResponse {
  message: string;
  email: string;
}

export const register = async (data: RegisterData): Promise<RegisterResponse> => {
  const response = await api.post<RegisterResponse>('/api/auth/register', data);
  return response.data;
};

export const activateAccount = async (token: string): Promise<void> => {
  await api.get('/api/auth/activate', { params: { token } });
};

export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/api/auth/login', credentials);
  localStorage.setItem('auth_token', response.data.token);
  localStorage.setItem('user_data', JSON.stringify(response.data.user));
  return response.data;
};

export const logout = async (): Promise<void> => {
  try {
    await api.post('/api/auth/logout');
  } finally {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_data');
  }
};

export const isAuthenticated = (): boolean => {
  return !!localStorage.getItem('auth_token');
};

export const getCurrentUser = (): AuthUser | null => {
  const data = localStorage.getItem('user_data');
  return data ? JSON.parse(data) : null;
};

export default { login, logout, isAuthenticated, getCurrentUser };
