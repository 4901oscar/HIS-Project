import api from '../api';

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
  birthDate: string; // YYYY-MM-DD
  gender: string; // M o F
  department?: string;
  municipality?: string;
  zone?: string;
  address?: string;
  password: string;
}

export interface RegisterResponse {
  message: string;
  email: string;
}

export interface CreatePatientAccountRequest {
  dpi: string;
  nit?: string;
  firstName: string;
  secondName?: string;
  firstLastName: string;
  secondLastName?: string;
  email: string;
  phone: string;
  birthDate: string; // YYYY-MM-DD
  gender: string; // M o F
  department?: string;
  municipality?: string;
  zone?: string;
  address?: string;
}

export interface CreatePatientAccountResponse {
  userId: string;
  patientId: string;
  username: string;
  temporaryPassword: string;
  message: string;
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
  localStorage.removeItem('auth_token');
  localStorage.removeItem('user_data');
  try {
    await api.post('/api/auth/logout');
  } catch {
    // ignore — token already cleared locally
  }
};

export const isAuthenticated = (): boolean => !!localStorage.getItem('auth_token');

export const getCurrentUser = (): AuthUser | null => {
  const data = localStorage.getItem('user_data');
  return data ? JSON.parse(data) : null;
};

export const createPatientAccount = async (data: CreatePatientAccountRequest): Promise<CreatePatientAccountResponse> => {
  const response = await api.post<CreatePatientAccountResponse>('/api/auth/internal/create-patient', data);
  return response.data;
};

export const changePassword = async (currentPassword: string, newPassword: string): Promise<void> => {
  await api.patch('/api/auth/change-password', { currentPassword, newPassword });
};

export default { login, logout, isAuthenticated, getCurrentUser, createPatientAccount, changePassword };
