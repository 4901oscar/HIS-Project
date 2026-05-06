import api from '../api';
import type { Clinic, CreateClinicRequest, UpdateClinicRequest, ClinicStatus } from '../types/clinic';

/**
 * Fetches all clinics with optional status filter.
 * Any authenticated user can access this endpoint.
 * 
 * @param estado Optional status filter (ACTIVE, INACTIVE, or DELETED)
 * @returns List of clinics sorted by createdAt descending
 */
export const getClinics = async (estado?: ClinicStatus): Promise<Clinic[]> => {
  const params = estado ? { estado } : {};
  const response = await api.get<Clinic[]>('/api/clinical/clinics', { params });
  return response.data;
};

/**
 * Creates a new clinic.
 * Requires ADMIN role.
 * 
 * @param data Clinic creation data
 * @returns The created clinic with generated UUID and audit fields
 */
export const createClinic = async (data: CreateClinicRequest): Promise<Clinic> => {
  const response = await api.post<Clinic>('/api/clinical/clinics', data);
  return response.data;
};

/**
 * Updates an existing clinic.
 * Requires ADMIN role.
 * 
 * @param id Clinic UUID
 * @param data Clinic update data (only provided fields will be updated)
 * @returns The updated clinic with updated audit fields
 */
export const updateClinic = async (id: string, data: UpdateClinicRequest): Promise<Clinic> => {
  const response = await api.put<Clinic>(`/api/clinical/clinics/${id}`, data);
  return response.data;
};

/**
 * Deletes a clinic (soft delete).
 * Requires ADMIN role.
 * 
 * The clinic record remains in the database with estado set to DELETED.
 * 
 * @param id Clinic UUID
 */
export const deleteClinic = async (id: string): Promise<void> => {
  await api.delete(`/api/clinical/clinics/${id}`);
};

export default {
  getClinics,
  createClinic,
  updateClinic,
  deleteClinic,
};
