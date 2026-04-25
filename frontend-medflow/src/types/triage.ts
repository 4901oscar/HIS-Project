// Manchester catalog structures — fields match MotifCatalogResponse / DiscriminatorCatalogResponse
export interface ManchesterMotif {
  id: string;
  code: string;
  description: string;
  category: string;
  active: boolean;
}

export interface ManchesterDiscriminator {
  id: string;
  code: string;
  description: string;
  priorityLevel: 'RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE';
  motifId: string | null;
  active: boolean;
}

export interface ManchesterCatalog {
  motifs: ManchesterMotif[];
  discriminators: ManchesterDiscriminator[];
}

// Triage request (to backend)
export interface TriageRequest {
  appointmentId: string;
  patientId: string;
  motifId: string;
  discriminatorIds: string[];
}

// Triage response (from backend)
export interface TriageResponse {
  id: string;
  patientId: string;
  priorityLevel: 'RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE';
  priorityDescription: string;
  maxWaitTimeMinutes: number;
  performedAt: string;
}

// Pending triage appointment
export interface PendingTriageAppointment {
  id: string;
  patientId: string;
  doctorId: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  notes: string | null;
  createdAt: string;
}
