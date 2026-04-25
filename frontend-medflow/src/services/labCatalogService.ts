import api from '../api';

export interface ExamTypeResponse {
  id: string;
  code: string;
  name: string;
  description: string;
  active: boolean;
}

export interface ExamTypeRequest {
  code: string;
  name: string;
  description?: string;
}

export const getExamTypes = async (): Promise<ExamTypeResponse[]> => {
  const r = await api.get<ExamTypeResponse[]>('/api/lab/exam-types');
  return r.data;
};

export const createExamType = async (data: ExamTypeRequest): Promise<ExamTypeResponse> => {
  const r = await api.post<ExamTypeResponse>('/api/lab/exam-types', data);
  return r.data;
};

export const updateExamType = async (id: string, data: ExamTypeRequest): Promise<ExamTypeResponse> => {
  const r = await api.put<ExamTypeResponse>(`/api/lab/exam-types/${id}`, data);
  return r.data;
};

export const toggleExamType = async (id: string): Promise<ExamTypeResponse> => {
  const r = await api.patch<ExamTypeResponse>(`/api/lab/exam-types/${id}/toggle`);
  return r.data;
};
