import api from '../api';
import type { ManchesterCatalog, ManchesterMotif, ManchesterDiscriminator } from '../types/triage';

/**
 * Get the complete Manchester triage catalog (motifs and discriminators)
 * Fetches both motifs and discriminators in parallel for efficiency
 * @returns Manchester catalog containing motifs and discriminators
 */
export const getManchesterCatalog = async (): Promise<ManchesterCatalog> => {
  // Fetch motifs and discriminators in parallel
  const [motifsResponse, discriminatorsResponse] = await Promise.all([
    api.get<ManchesterMotif[]>('/api/clinical/catalog/motifs'),
    api.get<ManchesterDiscriminator[]>('/api/clinical/catalog/discriminators'),
  ]);

  return {
    motifs: motifsResponse.data,
    discriminators: discriminatorsResponse.data,
  };
};

/**
 * Get all Manchester motifs
 * @returns List of motifs
 */
export const getManchesterMotifs = async (): Promise<ManchesterMotif[]> => {
  const response = await api.get<ManchesterMotif[]>('/api/clinical/catalog/motifs');
  return response.data;
};

/**
 * Get all Manchester discriminators
 * @returns List of discriminators
 */
export const getManchesterDiscriminators = async (): Promise<ManchesterDiscriminator[]> => {
  const response = await api.get<ManchesterDiscriminator[]>(
    '/api/clinical/catalog/discriminators'
  );
  return response.data;
};

export default {
  getManchesterCatalog,
  getManchesterMotifs,
  getManchesterDiscriminators,
};
