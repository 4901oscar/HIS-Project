/**
 * Mock Data para pruebas del Frontend
 * Solo para desarrollo - NO usar en producción
 * 
 * Usuarios disponibles para testing:
 * - Admin: admin / Admin1234
 * - Doctor: doctor / Doctor1234
 * - Admisión: admision / Admision1234
 * - Farmacéutico: pharmacy / Pharmacy1234
 * - Laboratorista: laboratory / Laboratory1234
 * - Cajero: cashier / Cashier1234
 * - Signos Vitales: vital_signs / VitalSigns1234
 * - Paciente: patient / Patient1234
 */

interface AuthUser {
  id: string;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
  active: boolean;
}

interface AuthResponse {
  token: string;
  expiresIn: number;
  user: AuthUser;
}

export const MOCK_USERS = {
  admin: {
    id: '00000000-0000-0000-0000-000000000001',
    username: 'admin',
    password: 'Admin1234',
    email: 'admin@medflow.com',
    fullName: 'Administrador Sistema',
    roles: ['ADMIN'],
  },
  doctor: {
    id: '00000000-0000-0000-0000-000000000002',
    username: 'doctor',
    password: 'Doctor1234',
    email: 'doctor@medflow.com',
    fullName: 'Dr. Juan Pérez',
    roles: ['DOCTOR'],
  },
  admision: {
    id: '00000000-0000-0000-0000-000000000003',
    username: 'admision',
    password: 'Admision1234',
    email: 'admision@medflow.com',
    fullName: 'María López - Admisión',
    roles: ['ADMISSION'],
  },
  pharmacy: {
    id: '00000000-0000-0000-0000-000000000004',
    username: 'pharmacy',
    password: 'Pharmacy1234',
    email: 'pharmacy@medflow.com',
    fullName: 'Carlos González - Farmacéutico',
    roles: ['PHARMACY'],
  },
  laboratory: {
    id: '00000000-0000-0000-0000-000000000005',
    username: 'laboratory',
    password: 'Laboratory1234',
    email: 'laboratory@medflow.com',
    fullName: 'Ana Rodríguez - Laboratorio',
    roles: ['LABORATORY'],
  },
  cashier: {
    id: '00000000-0000-0000-0000-000000000006',
    username: 'cashier',
    password: 'Cashier1234',
    email: 'cashier@medflow.com',
    fullName: 'Roberto Martínez - Cajero',
    roles: ['CASHIER'],
  },
  vital_signs: {
    id: '00000000-0000-0000-0000-000000000007',
    username: 'vital_signs',
    password: 'VitalSigns1234',
    email: 'vital_signs@medflow.com',
    fullName: 'Patricia Hernández - Signos Vitales',
    roles: ['VITAL_SIGNS'],
  },
  patient: {
    id: '00000000-0000-0000-0000-000000000008',
    username: 'patient',
    password: 'Patient1234',
    email: 'patient@medflow.com',
    fullName: 'Juan Pérez García',
    roles: ['PATIENT'],
  },
};

export const getMockAuthResponse = (username: string): AuthResponse | null => {
  const user = MOCK_USERS[username as keyof typeof MOCK_USERS];
  
  if (!user) {
    return null;
  }

  return {
    token: `mock-token-${username}-${Date.now()}`,
    expiresIn: 3600,
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      roles: user.roles,
      active: true,
    },
  };
};

