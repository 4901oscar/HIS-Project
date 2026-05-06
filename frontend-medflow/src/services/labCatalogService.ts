import api from '../api';

export type ExamTypeStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED';

export interface ExamTypeResponse {
  id: string;
  code: string;
  name: string;
  description: string;
  testType?: string;
  sampleType?: string;
  status: ExamTypeStatus;
}

export interface ExamTypeRequest {
  code: string;
  name: string;
  description?: string;
  testType?: string;
  sampleType?: string;
  status: ExamTypeStatus;
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
