import api from '../api';

export interface Employee {
  id: string;
  username: string;
  email: string;
  firstName: string;
  secondName?: string;
  firstLastName: string;
  secondLastName?: string;
  fullName: string;
  phone?: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface CreateEmployeeData {
  firstName: string;
  secondName?: string;
  firstLastName: string;
  secondLastName?: string;
  email: string;
  phone?: string;
  roleName: string;
}

export interface UpdateEmployeeData {
  firstName?: string;
  secondName?: string;
  firstLastName?: string;
  secondLastName?: string;
  email?: string;
  phone?: string;
  roleName?: string;
}

export interface CreateEmployeeResponse {
  empleado: Employee;
  contrasenaTemporalParaEntregar: string;
  mensaje: string;
}

export const EMPLOYEE_ROLES: Record<string, string> = {
  ADMISSION: 'Admisión',
  VITAL_SIGNS: 'Signos Vitales',
  DOCTOR: 'Doctor',
  LABORATORY: 'Laboratorio',
  PHARMACY: 'Farmacia',
  CASHIER: 'Cajero',
};

export const createEmployee = async (data: CreateEmployeeData): Promise<CreateEmployeeResponse> => {
  const response = await api.post<CreateEmployeeResponse>('/api/users/empleados', data);
  return response.data;
};

export const listEmployees = async (rol?: string, activo?: boolean): Promise<Employee[]> => {
  const params: Record<string, string | boolean> = {};
  if (rol) params.rol = rol;
  if (activo !== undefined) params.activo = activo;
  const response = await api.get<Employee[]>('/api/users/empleados', { params });
  return response.data;
};

export const getEmployee = async (id: string): Promise<Employee> => {
  const response = await api.get<Employee>(`/api/users/empleados/${id}`);
  return response.data;
};

export const updateEmployee = async (id: string, data: UpdateEmployeeData): Promise<Employee> => {
  const response = await api.put<Employee>(`/api/users/empleados/${id}`, data);
  return response.data;
};

export const toggleEmployeeActive = async (id: string): Promise<Employee> => {
  const response = await api.patch<Employee>(`/api/users/empleados/${id}/estado`);
  return response.data;
};
