import api from '../api';

export interface MotifResponse {
  id: string;
  code: string;
  description: string;
  category: string;
  active: boolean;
}

export interface MotifRequest {
  code: string;
  description: string;
  category?: string;
  active?: boolean;
}

export interface DiscriminatorResponse {
  id: string;
  code: string;
  description: string;
  priorityLevel: string;
  motifId: string;
  active: boolean;
}

export interface DiscriminatorRequest {
  code: string;
  description: string;
  priorityLevel: string;
  motifId?: string;
  active?: boolean;
}

export const PRIORITY_LEVELS: Record<string, string> = {
  RED: 'Inmediato (Rojo)',
  ORANGE: 'Muy urgente (Naranja)',
  YELLOW: 'Urgente (Amarillo)',
  GREEN: 'Poco urgente (Verde)',
  BLUE: 'No urgente (Azul)',
};

export const getMotifs = async (): Promise<MotifResponse[]> => {
  const r = await api.get<MotifResponse[]>('/api/clinical/catalog/motifs');
  return r.data;
};

export const createMotif = async (data: MotifRequest): Promise<MotifResponse> => {
  const r = await api.post<MotifResponse>('/api/clinical/catalog/motifs', data);
  return r.data;
};

export const updateMotif = async (id: string, data: MotifRequest): Promise<MotifResponse> => {
  const r = await api.put<MotifResponse>(`/api/clinical/catalog/motifs/${id}`, data);
  return r.data;
};

export const toggleMotif = async (id: string): Promise<MotifResponse> => {
  const r = await api.patch<MotifResponse>(`/api/clinical/catalog/motifs/${id}/toggle`);
  return r.data;
};

export const getDiscriminators = async (motifId?: string): Promise<DiscriminatorResponse[]> => {
  const r = await api.get<DiscriminatorResponse[]>('/api/clinical/catalog/discriminators', {
    params: motifId ? { motifId } : {},
  });
  return r.data;
};

export const createDiscriminator = async (data: DiscriminatorRequest): Promise<DiscriminatorResponse> => {
  const r = await api.post<DiscriminatorResponse>('/api/clinical/catalog/discriminators', data);
  return r.data;
};

export const updateDiscriminator = async (id: string, data: DiscriminatorRequest): Promise<DiscriminatorResponse> => {
  const r = await api.put<DiscriminatorResponse>(`/api/clinical/catalog/discriminators/${id}`, data);
  return r.data;
};

export const toggleDiscriminator = async (id: string): Promise<DiscriminatorResponse> => {
  const r = await api.patch<DiscriminatorResponse>(`/api/clinical/catalog/discriminators/${id}/toggle`);
  return r.data;
};
