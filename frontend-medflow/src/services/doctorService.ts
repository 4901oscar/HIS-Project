import api from '../api';

export interface Doctor {
  id: string;
  name: string;
  specialty: string;
  shiftStart: string;  // Format: HH:mm
  shiftEnd: string;    // Format: HH:mm
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
}

export interface DoctorEmployee {
  id: string;
  fullName: string;
  email: string;
  username: string;
  active: boolean;
}

export interface CreateDoctorRequest {
  userId: string;
  name: string;
  specialty: string;
  shiftStart: string;  // Format: HH:mm
  shiftEnd: string;    // Format: HH:mm
}

export interface UpdateDoctorRequest {
  name: string;
  specialty: string;
  shiftStart: string;  // Format: HH:mm
  shiftEnd: string;    // Format: HH:mm
}

export interface MarkDaysOffRequest {
  startDate: string;   // Format: YYYY-MM-DD
  endDate: string;     // Format: YYYY-MM-DD
  reason: string;
}

export interface DayOff {
  id: string;
  doctorId: string;
  date: string;        // Format: YYYY-MM-DD
  isAvailable: boolean;
  reason: string;
  createdAt: string;
  createdBy: string;
}

/**
 * Fetches all active doctors.
 * Requires ADMINISTRATOR role.
 */
export const listActiveDoctors = async (): Promise<Doctor[]> => {
  const response = await api.get<Doctor[]>('/api/clinical/doctors');
  return response.data;
};

/**
 * Creates a new doctor.
 * Requires ADMINISTRATOR role.
 */
export const createDoctor = async (data: CreateDoctorRequest): Promise<Doctor> => {
  const response = await api.post<Doctor>('/api/clinical/doctors', data);
  return response.data;
};

/**
 * Updates an existing doctor.
 * Requires ADMINISTRATOR role.
 */
export const updateDoctor = async (id: string, data: UpdateDoctorRequest): Promise<Doctor> => {
  const response = await api.put<Doctor>(`/api/clinical/doctors/${id}`, data);
  return response.data;
};

/**
 * Deactivates a doctor (soft delete).
 * Requires ADMINISTRATOR role.
 */
export const deactivateDoctor = async (id: string): Promise<void> => {
  await api.delete(`/api/clinical/doctors/${id}`);
};

/**
 * Marks days off for a doctor (vacation period).
 * Requires ADMINISTRATOR role.
 */
export const markDaysOff = async (id: string, data: MarkDaysOffRequest): Promise<void> => {
  await api.post(`/api/clinical/doctors/${id}/days-off`, data);
};

/**
 * Removes a day-off record for a doctor.
 * Requires ADMINISTRATOR role.
 */
export const removeDayOff = async (id: string, date: string): Promise<void> => {
  await api.delete(`/api/clinical/doctors/${id}/days-off/${date}`);
};

/**
 * Gets all day-off records for a doctor.
 * Requires ADMINISTRATOR role.
 */
export const getDoctorDaysOff = async (id: string): Promise<DayOff[]> => {
  const response = await api.get<DayOff[]>(`/api/clinical/doctors/${id}/days-off`);
  return response.data;
};

/**
 * Fetches active employees with DOCTOR role from auth-service.
 * Used to populate the doctor selector when linking a user as a doctor.
 */
export const getDoctorEmployees = async (): Promise<DoctorEmployee[]> => {
  const response = await api.get<any[]>('/api/users/empleados', {
    params: { rol: 'DOCTOR', activo: true },
  });
  return response.data.map((e) => ({
    id: e.id,
    fullName: e.fullName,
    email: e.email,
    username: e.username,
    active: e.active,
  }));
};

export default {
  listActiveDoctors,
  createDoctor,
  updateDoctor,
  deactivateDoctor,
  markDaysOff,
  removeDayOff,
  getDoctorDaysOff,
};
