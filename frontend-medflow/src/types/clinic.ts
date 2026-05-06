/**
 * Tipos e interfaces para el dominio de Clínicas
 * Módulo: Gestión de Clínicas
 */

export type ClinicStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED';

export interface Clinic {
  id: string;
  codigo: string;
  nombre: string;
  descripcion: string;
  estado: ClinicStatus;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

export interface CreateClinicRequest {
  codigo: string;
  nombre: string;
  descripcion: string;
}

export interface UpdateClinicRequest {
  codigo?: string;
  nombre?: string;
  descripcion?: string;
  estado?: ClinicStatus;
}
