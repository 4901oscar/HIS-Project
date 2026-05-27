import { useState, useEffect, useCallback, type FC } from 'react';
import { getManchesterCatalog } from '../../services/manchesterService';
import type { ManchesterMotif, ManchesterDiscriminator } from '../../types/triage';

// ─── Type Definitions ─────────────────────────────────────────────────────────

interface TriageFormProps {
  patientId: string;
  appointmentId: string;
  onSubmit: (data: TriageFormData) => Promise<void>;
  onCancel: () => void;
  submitting: boolean;
  error: string | null;
}

export interface TriageFormData {
  motifId: string;
  discriminatorIds: string[];
}

// ─── Helper Functions ─────────────────────────────────────────────────────────

const getPriorityColorClass = (priority: string): string => {
  const colorMap: Record<string, string> = {
    RED: 'bg-red-100 text-red-800',
    ORANGE: 'bg-orange-100 text-orange-800',
    YELLOW: 'bg-yellow-100 text-yellow-800',
    GREEN: 'bg-green-100 text-green-800',
    BLUE: 'bg-blue-100 text-blue-800',
  };
  return colorMap[priority] || 'bg-gray-100 text-gray-800';
};

// ─── Component ────────────────────────────────────────────────────────────────

const TriageForm: FC<TriageFormProps> = ({
  patientId: _patientId,
  appointmentId: _appointmentId,
  onSubmit,
  onCancel,
  submitting,
  error,
}) => {
  // Manchester catalog state
  const [motifs, setMotifs] = useState<ManchesterMotif[]>([]);
  const [discriminators, setDiscriminators] = useState<ManchesterDiscriminator[]>([]);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogError, setCatalogError] = useState<string | null>(null);

  // Form state
  const [selectedMotifId, setSelectedMotifId] = useState('');
  const [selectedDiscriminatorIds, setSelectedDiscriminatorIds] = useState<string[]>([]);
  const [validationError, setValidationError] = useState<string | null>(null);

  // ─── Load Manchester Catalog ──────────────────────────────────────────────

  useEffect(() => {
    const loadCatalog = async () => {
      setCatalogLoading(true);
      setCatalogError(null);
      
      try {
        const catalog = await getManchesterCatalog();
        setMotifs(catalog.motifs);
        setDiscriminators(catalog.discriminators);
      } catch (err) {
  
        setCatalogError('Error al cargar el catálogo Manchester');
      } finally {
        setCatalogLoading(false);
      }
    };
    
    loadCatalog();
  }, []);

  // ─── Event Handlers ───────────────────────────────────────────────────────

  const handleMotifChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedMotifId(e.target.value);
    setValidationError(null);
  }, []);

  const handleDiscriminatorToggle = useCallback((discriminatorId: string) => {
    setSelectedDiscriminatorIds((prev) => {
      if (prev.includes(discriminatorId)) {
        return prev.filter((id) => id !== discriminatorId);
      } else {
        return [...prev, discriminatorId];
      }
    });
    setValidationError(null);
  }, []);

  const handleSubmit = useCallback(async (e: React.FormEvent) => {
    e.preventDefault();

    // Validation
    if (!selectedMotifId) {
      setValidationError('Debe seleccionar un motivo de consulta');
      return;
    }

    if (selectedDiscriminatorIds.length === 0) {
      setValidationError('Debe seleccionar al menos un discriminador');
      return;
    }

    // Submit form data
    await onSubmit({
      motifId: selectedMotifId,
      discriminatorIds: selectedDiscriminatorIds,
    });
  }, [selectedMotifId, selectedDiscriminatorIds, onSubmit]);

  // ─── Render Loading State ─────────────────────────────────────────────────

  if (catalogLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex justify-center items-center py-8">
          <div 
            className="animate-spin rounded-full h-6 w-6 border-4 border-medin-cyan border-t-transparent"
            role="status"
            aria-live="polite"
            aria-busy="true"
            aria-label="Cargando catálogo Manchester"
          />
          <span className="ml-3 text-gray-600">Cargando catálogo Manchester...</span>
        </div>
      </div>
    );
  }

  // ─── Render Error State ───────────────────────────────────────────────────

  if (catalogError) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm" role="alert">
          {catalogError}
        </div>
        <button
          onClick={onCancel}
          className="mt-4 w-full px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium"
          aria-label="Volver a la lista de citas"
        >
          Volver
        </button>
      </div>
    );
  }

  // ─── Render Form ──────────────────────────────────────────────────────────

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-medin-navy">Realizar Triaje Manchester</h3>
        <button
          onClick={onCancel}
          className="text-sm text-gray-500 hover:text-gray-700 transition-colors"
          disabled={submitting}
          aria-label="Cancelar triaje"
        >
          Cancelar
        </button>
      </div>

      {/* API Error Message */}
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm" role="alert">
          {error}
        </div>
      )}

      {/* Validation Error Message */}
      {validationError && (
        <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg text-yellow-800 text-sm" role="alert">
          {validationError}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Motif Selection */}
        <div>
          <label htmlFor="motif" className="block text-sm font-medium text-gray-700 mb-2">
            Motivo de Consulta <span className="text-red-500">*</span>
          </label>
          <select
            id="motif"
            value={selectedMotifId}
            onChange={handleMotifChange}
            required
            disabled={submitting}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm"
            aria-required="true"
          >
            <option value="">Seleccione un motivo...</option>
            {motifs.filter((m) => m.active).map((motif) => (
              <option key={motif.id} value={motif.id}>
                {motif.description || motif.code}
              </option>
            ))}
          </select>
        </div>

        {/* Discriminator Selection */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Discriminadores <span className="text-red-500">*</span>
          </label>
          <p className="text-xs text-gray-500 mb-3">
            Seleccione todos los discriminadores que apliquen al paciente
          </p>
          <div className="space-y-2 max-h-96 overflow-y-auto border border-gray-200 rounded-lg p-4">
            {discriminators.filter((d) => d.active).map((discriminator) => (
              <label
                key={discriminator.id}
                className="flex items-start gap-3 p-2 hover:bg-gray-50 rounded cursor-pointer transition-colors"
              >
                <input
                  type="checkbox"
                  checked={selectedDiscriminatorIds.includes(discriminator.id)}
                  onChange={() => handleDiscriminatorToggle(discriminator.id)}
                  disabled={submitting}
                  className="mt-1 h-4 w-4 text-medin-cyan focus:ring-medin-cyan border-gray-300 rounded"
                  aria-label={`Discriminador: ${discriminator.description || discriminator.code}`}
                />
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">
                    {discriminator.description || discriminator.code}
                  </p>
                  <span
                    className={`inline-block mt-1 px-2 py-0.5 rounded-full text-xs font-medium ${getPriorityColorClass(
                      discriminator.priorityLevel
                    )}`}
                  >
                    {discriminator.priorityLevel}
                  </span>
                </div>
              </label>
            ))}
          </div>
          {selectedDiscriminatorIds.length > 0 && (
            <p className="text-xs text-gray-600 mt-2" role="status">
              {selectedDiscriminatorIds.length} discriminador(es) seleccionado(s)
            </p>
          )}
        </div>

        {/* Form Actions */}
        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onCancel}
            disabled={submitting}
            className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label="Cancelar registro de triaje"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="px-6 py-2 bg-medin-cyan text-white font-semibold rounded-lg hover:bg-medin-navy hover:text-white transition-colors text-sm disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label="Registrar triaje Manchester del paciente"
          >
            {submitting ? 'Registrando...' : 'Registrar Triaje'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default TriageForm;
