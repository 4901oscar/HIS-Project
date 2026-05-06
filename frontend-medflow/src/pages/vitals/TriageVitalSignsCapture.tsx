import { useState, useEffect, useCallback, useMemo } from 'react';
import type { FC, FormEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import { SpeakerWaveIcon } from '@heroicons/react/24/outline';
import { getPatientById } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import { recordVitalSigns, getVitalSignsByAppointment, performTriage } from '../../services/clinicalService';
import type { VitalSignsResponse } from '../../services/clinicalService';
import { getManchesterCatalog } from '../../services/manchesterService';
import type { ManchesterMotif, ManchesterDiscriminator } from '../../types/triage';
import { extractErrorMessage } from '../../utils/errorHandler';

interface VitalForm {
  systolicPressure: string;
  diastolicPressure: string;
  heartRate: string;
  respiratoryRate: string;
  temperature: string;
  oxygenSaturation: string;
  weight: string;
  height: string;
}

const emptyForm: VitalForm = {
  systolicPressure: '',
  diastolicPressure: '',
  heartRate: '',
  respiratoryRate: '',
  temperature: '',
  oxygenSaturation: '',
  weight: '',
  height: '',
};

// Priority level mapping for Manchester classification
const PRIORITY_MAP: Record<string, { level: string; description: string; color: string; bgColor: string }> = {
  RED: {
    level: 'RED',
    description: 'Inmediato',
    color: 'text-red-800',
    bgColor: 'bg-red-100 border-red-300'
  },
  ORANGE: {
    level: 'ORANGE',
    description: 'Muy urgente',
    color: 'text-orange-800',
    bgColor: 'bg-orange-100 border-orange-300'
  },
  YELLOW: {
    level: 'YELLOW',
    description: 'Urgente',
    color: 'text-yellow-800',
    bgColor: 'bg-yellow-100 border-yellow-300'
  },
  GREEN: {
    level: 'GREEN',
    description: 'Poco urgente',
    color: 'text-green-800',
    bgColor: 'bg-green-100 border-green-300'
  },
  BLUE: {
    level: 'BLUE',
    description: 'No urgente',
    color: 'text-blue-800',
    bgColor: 'bg-blue-100 border-blue-300'
  }
};

const TriageVitalSignsCapture: FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  const { appointmentId, patientId } = location.state || {};
  
  const [patient, setPatient] = useState<PatientResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [form, setForm] = useState<VitalForm>(emptyForm);
  
  // Two-step workflow control state
  const [vitalSignsSaved, setVitalSignsSaved] = useState(false);
  const [vitalSignsLocked, setVitalSignsLocked] = useState(false);
  const [savingVitalSigns, setSavingVitalSigns] = useState(false);
  const [vitalSignsError, setVitalSignsError] = useState<string | null>(null);
  const [vitalSignsSuccess, setVitalSignsSuccess] = useState(false);
  
  // Manchester catalog state
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogError, setCatalogError] = useState<string | null>(null);
  const [motifs, setMotifs] = useState<ManchesterMotif[]>([]);
  const [discriminators, setDiscriminators] = useState<ManchesterDiscriminator[]>([]);
  
  // Manchester selection state
  const [selectedMotifId, setSelectedMotifId] = useState<string>('');
  const [selectedDiscriminatorIds, setSelectedDiscriminatorIds] = useState<string[]>([]);
  const [calculatedPriority, setCalculatedPriority] = useState<{
    level: 'RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE' | null;
    description: string;
  } | null>(null);
  
  // Step 2 submission state
  const [savingTriage, setSavingTriage] = useState(false);
  const [triageError, setTriageError] = useState<string | null>(null);
  const [triageSuccess, setTriageSuccess] = useState(false);
  
  // Legacy state (will be replaced by two-step workflow)
  const [saving, setSaving] = useState(false);
  const [result, setResult] = useState<VitalSignsResponse | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);

  // Derived data: Filtered discriminators based on selected motif
  const filteredDiscriminators = useMemo(() => {
    if (!selectedMotifId) return [];
    return discriminators.filter(d => d.motifId === selectedMotifId && d.active);
  }, [discriminators, selectedMotifId]);

  // Derived data: Sorted motifs for dropdown
  const sortedMotifs = useMemo(() => {
    return [...motifs]
      .filter(m => m.active)
      .sort((a, b) => a.description.localeCompare(b.description, 'es'));
  }, [motifs]);

  // Component initialization: Load patient, Manchester catalog, and check existing vital signs
  useEffect(() => {
    if (!patientId || !appointmentId) {
      setError('No se proporcionó información de la cita');
      setLoading(false);
      return;
    }

    const initializeComponent = async () => {
      try {
        setLoading(true);
        setError(null);
        
        // Load patient data, Manchester catalog, and check existing vital signs in parallel
        const [patientData, catalog, existingVitalSigns] = await Promise.allSettled([
          getPatientById(patientId),
          getManchesterCatalog(),
          getVitalSignsByAppointment(appointmentId)
        ]);
        
        if (patientData.status === 'fulfilled') {
          setPatient(patientData.value);
        } else {
          setError(extractErrorMessage(patientData.reason));
          setLoading(false);
          return;
        }
        
        if (catalog.status === 'fulfilled') {
          setMotifs(catalog.value.motifs);
          setDiscriminators(catalog.value.discriminators);
          setCatalogLoading(false);
        } else {
          setCatalogError(extractErrorMessage(catalog.reason));
          setCatalogLoading(false);
        }
        
        if (existingVitalSigns.status === 'fulfilled') {
          const vitalSigns = existingVitalSigns.value;
          setForm({
            systolicPressure: String(vitalSigns.systolicPressure),
            diastolicPressure: String(vitalSigns.diastolicPressure),
            heartRate: String(vitalSigns.heartRate),
            respiratoryRate: String(vitalSigns.respiratoryRate),
            temperature: String(vitalSigns.temperature),
            oxygenSaturation: String(vitalSigns.oxygenSaturation),
            weight: vitalSigns.weight ? String(vitalSigns.weight) : '',
            height: vitalSigns.height ? String(vitalSigns.height) : '',
          });
          setVitalSignsSaved(true);
          setVitalSignsLocked(true);
        } else {
          // 404 is expected when no vital signs exist yet — ignore silently
          const reason = existingVitalSigns.reason as { response?: { status?: number } };
          if (reason?.response?.status !== 404) {
            // Non-404 error: non-blocking, UI continues normally
          }
        }
        
      } catch (err) {
        setError(extractErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    initializeComponent();
  }, [patientId, appointmentId]);

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }, []);

  // Step 1: Validate vital signs only
  const validateVitalSigns = useCallback((): { valid: boolean; errors: string[] } => {
    const errors: string[] = [];
    
    if (!form.systolicPressure || Number(form.systolicPressure) < 50 || Number(form.systolicPressure) > 250) {
      errors.push('Presión sistólica debe estar entre 50 y 250 mmHg');
    }
    if (!form.diastolicPressure || Number(form.diastolicPressure) < 30 || Number(form.diastolicPressure) > 150) {
      errors.push('Presión diastólica debe estar entre 30 y 150 mmHg');
    }
    if (!form.heartRate || Number(form.heartRate) < 20 || Number(form.heartRate) > 300) {
      errors.push('Frecuencia cardíaca debe estar entre 20 y 300 lpm');
    }
    if (!form.respiratoryRate || Number(form.respiratoryRate) < 5 || Number(form.respiratoryRate) > 60) {
      errors.push('Frecuencia respiratoria debe estar entre 5 y 60 rpm');
    }
    if (!form.temperature || Number(form.temperature) < 30 || Number(form.temperature) > 45) {
      errors.push('Temperatura debe estar entre 30 y 45 °C');
    }
    if (!form.oxygenSaturation || Number(form.oxygenSaturation) < 50 || Number(form.oxygenSaturation) > 100) {
      errors.push('Saturación de oxígeno debe estar entre 50 y 100%');
    }
    
    return { valid: errors.length === 0, errors };
  }, [form]);

  // Step 1: Submit vital signs
  const handleSaveVitalSigns = useCallback(async (e: FormEvent) => {
    e.preventDefault();
    if (!patient || !appointmentId) return;
    
    // Validate vital signs only
    const validation = validateVitalSigns();
    if (!validation.valid) {
      setVitalSignsError(validation.errors.join('. '));
      return;
    }
    
    setSavingVitalSigns(true);
    setVitalSignsError(null);
    setVitalSignsSuccess(false);
    
    try {
      await recordVitalSigns({
        appointmentId,
        patientId: patient.id,
        systolicPressure: Number(form.systolicPressure),
        diastolicPressure: Number(form.diastolicPressure),
        heartRate: Number(form.heartRate),
        respiratoryRate: Number(form.respiratoryRate),
        temperature: Number(form.temperature),
        oxygenSaturation: Number(form.oxygenSaturation),
        weight: form.weight ? Number(form.weight) : undefined,
        height: form.height ? Number(form.height) : undefined,
      });
      
      // Success: Lock fields, show Manchester section
      setVitalSignsSaved(true);
      setVitalSignsLocked(true);
      setVitalSignsSuccess(true);
      
    } catch (err) {

      setVitalSignsError(extractErrorMessage(err));
    } finally {
      setSavingVitalSigns(false);
    }
  }, [patient, appointmentId, form, validateVitalSigns]);

  const handleSubmit = useCallback(async (e: FormEvent) => {
    e.preventDefault();
    if (!patient || !appointmentId) return;

    setSaving(true);
    setSaveError(null);
    setResult(null);

    try {
      const res = await recordVitalSigns({
        appointmentId,
        patientId: patient.id,
        systolicPressure: Number(form.systolicPressure),
        diastolicPressure: Number(form.diastolicPressure),
        heartRate: Number(form.heartRate),
        respiratoryRate: Number(form.respiratoryRate),
        temperature: Number(form.temperature),
        oxygenSaturation: Number(form.oxygenSaturation),
        weight: form.weight ? Number(form.weight) : undefined,
        height: form.height ? Number(form.height) : undefined,
      });
      setResult(res);
      
      // Redirect back to triage pending page after 2 seconds
      setTimeout(() => {
        navigate('/vitals/triage');
      }, 2000);
    } catch (err) {

      setSaveError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  }, [patient, appointmentId, form, navigate]);

  const handleCancel = useCallback(() => {
    // Simply navigate back - appointment stays in VITAL_SIGNS for next staff member
    navigate('/vitals/triage');
  }, [navigate]);

  const handleCallPatient = useCallback(() => {
    if (!patient) return;
    const utterance = new SpeechSynthesisUtterance(
      `${patient.fullName}, por favor pasar a sala de triaje`
    );
    utterance.lang = 'es-GT';
    utterance.rate = 0.9;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  }, [patient]);

  // Handle motif selection change - clear discriminators when motif changes
  const handleMotifChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedMotifId(e.target.value);
    setSelectedDiscriminatorIds([]); // Clear discriminator selections
  }, []);

  // Handle discriminator checkbox change
  const handleDiscriminatorChange = useCallback((discriminatorId: string, checked: boolean) => {
    setSelectedDiscriminatorIds(prev => {
      if (checked) {
        return [...prev, discriminatorId];
      } else {
        return prev.filter(id => id !== discriminatorId);
      }
    });
  }, []);

  // Calculate maximum priority from selected discriminators
  const calculateMaxPriority = useCallback((selectedIds: string[]): {
    level: 'RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE' | null;
    description: string;
  } | null => {
    if (selectedIds.length === 0) return null;
    
    // Priority ordering: RED > ORANGE > YELLOW > GREEN > BLUE
    const priorityOrder: ('RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE')[] = ['RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE'];
    
    // Get all selected discriminators
    const selectedDiscriminators = discriminators.filter(d => selectedIds.includes(d.id));
    
    // Find highest priority
    for (const priority of priorityOrder) {
      const hasThisPriority = selectedDiscriminators.some(d => d.priorityLevel === priority);
      if (hasThisPriority) {
        return {
          level: priority,
          description: PRIORITY_MAP[priority].description
        };
      }
    }
    
    return null;
  }, [discriminators]);

  // Update calculated priority when discriminator selection changes
  useEffect(() => {
    const priority = calculateMaxPriority(selectedDiscriminatorIds);
    setCalculatedPriority(priority);
  }, [selectedDiscriminatorIds, calculateMaxPriority]);

  // Step 2: Validate Manchester selection only
  const validateManchesterSelection = useCallback((): { valid: boolean; errors: string[] } => {
    const errors: string[] = [];
    
    if (!selectedMotifId) {
      errors.push('Debe seleccionar un motivo de consulta');
    }
    if (selectedDiscriminatorIds.length === 0) {
      errors.push('Debe seleccionar al menos un discriminador');
    }
    
    return { valid: errors.length === 0, errors };
  }, [selectedMotifId, selectedDiscriminatorIds]);

  // Step 2: Submit Manchester classification
  const handleSaveTriage = useCallback(async (e: FormEvent) => {
    e.preventDefault();
    if (!patient || !appointmentId) return;
    
    // Validate Manchester selection only
    const validation = validateManchesterSelection();
    if (!validation.valid) {
      setTriageError(validation.errors.join('. '));
      return;
    }
    
    setSavingTriage(true);
    setTriageError(null);
    setTriageSuccess(false);
    
    try {
      await performTriage({
        appointmentId,
        patientId: patient.id,
        motifId: selectedMotifId,
        discriminatorIds: selectedDiscriminatorIds,
      });
      
      // Success: Show success message and navigate after 2 seconds
      setTriageSuccess(true);
      
      setTimeout(() => {
        navigate('/vitals/triage');
      }, 2000);
      
    } catch (err) {

      setTriageError(extractErrorMessage(err));
    } finally {
      setSavingTriage(false);
    }
  }, [patient, appointmentId, selectedMotifId, selectedDiscriminatorIds, validateManchesterSelection, navigate]);

  const inputClass = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';

  if (loading) {
    return (
      <MainLayout>
        <div className="flex flex-col justify-center items-center min-h-[60vh] gap-4">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-medin-cyan border-t-transparent" />
          <p className="text-gray-600">Cargando...</p>
        </div>
      </MainLayout>
    );
  }

  if (error || !patient) {
    return (
      <MainLayout>
        <div className="space-y-6">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Registrar Signos Vitales</h2>
            <p className="mt-1 text-sm text-gray-600">Triaje</p>
          </div>
          
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">{error || 'No se pudo cargar la información del paciente'}</p>
            <button
              onClick={handleCancel}
              className="mt-4 px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
            >
              Volver a Triaje Pendiente
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Registrar Signos Vitales</h2>
            <p className="mt-1 text-sm text-gray-600">Triaje - {patient.fullName}</p>
          </div>
          <button
            onClick={handleCallPatient}
            className="flex items-center gap-2 px-4 py-2 text-medin-cyan bg-medin-cyan/10 rounded-lg hover:bg-medin-cyan/20 transition-colors font-medium"
            title="Llamar a sala de triaje"
            aria-label="Llamar a paciente"
          >
            <SpeakerWaveIcon className="h-5 w-5" />
            Llamar Paciente
          </button>
        </div>

        {/* Patient Info Card */}
        <div className="bg-medin-cyan/10 border border-medin-cyan/30 rounded-lg p-4">
          <h3 className="font-semibold text-medin-navy mb-2">Información del Paciente</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
            <div>
              <span className="text-gray-600">Nombre:</span>
              <span className="ml-2 font-medium">{patient.fullName}</span>
            </div>
            <div>
              <span className="text-gray-600">DPI:</span>
              <span className="ml-2 font-medium font-mono">{patient.dpi}</span>
            </div>
            <div>
              <span className="text-gray-600">Email:</span>
              <span className="ml-2 font-medium">{patient.email}</span>
            </div>
          </div>
        </div>

        {/* Success Message */}
        {result && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="font-semibold text-green-800 mb-2">✓ Signos vitales registrados exitosamente</p>
            <p className="text-sm text-green-700">Redirigiendo a Triaje Pendiente...</p>
          </div>
        )}
        
        {/* Step 1 Success Message */}
        {vitalSignsSuccess && !triageSuccess && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="font-semibold text-green-800 mb-2">✓ Signos vitales guardados exitosamente</p>
            <p className="text-sm text-green-700">Ahora puede completar la clasificación Manchester</p>
          </div>
        )}

        {/* Error Message */}
        {saveError && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">{saveError}</p>
          </div>
        )}
        
        {/* Step 1 Error Message */}
        {vitalSignsError && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800 font-semibold">Error al guardar signos vitales</p>
            <p className="text-red-700 text-sm mt-1">{vitalSignsError}</p>
          </div>
        )}
        
        {/* Step 2 Error Message */}
        {triageError && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800 font-semibold">Error al guardar clasificación Manchester</p>
            <p className="text-red-700 text-sm mt-1">{triageError}</p>
          </div>
        )}
        
        {/* Step 2 Success Message */}
        {triageSuccess && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="font-semibold text-green-800 mb-2">✓ Clasificación Manchester registrada exitosamente</p>
            <p className="text-sm text-green-700">Redirigiendo a Triaje Pendiente...</p>
          </div>
        )}

        {/* Catalog Loading State */}
        {catalogLoading && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 flex items-center gap-3">
            <div className="animate-spin rounded-full h-5 w-5 border-2 border-blue-600 border-t-transparent" />
            <p className="text-blue-800">Cargando catálogo Manchester...</p>
          </div>
        )}
        
        {/* Catalog Error State */}
        {catalogError && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800 font-semibold mb-2">Error al cargar catálogo Manchester</p>
            <p className="text-red-700 text-sm mb-3">{catalogError}</p>
            <button
              onClick={() => window.location.reload()}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm"
            >
              Reintentar
            </button>
          </div>
        )}

        {/* Form */}
        {!result && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Signos Vitales</h3>

            <form onSubmit={!vitalSignsSaved ? handleSaveVitalSigns : handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div>
                  <label className={labelClass}>Sist. (mmHg) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="systolicPressure"
                    value={form.systolicPressure}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    required
                    min={50}
                    max={250}
                    className={inputClass}
                    placeholder="120"
                  />
                </div>
                <div>
                  <label className={labelClass}>Diast. (mmHg) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="diastolicPressure"
                    value={form.diastolicPressure}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    required
                    min={30}
                    max={150}
                    className={inputClass}
                    placeholder="80"
                  />
                </div>
                <div>
                  <label className={labelClass}>FC (lpm) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="heartRate"
                    value={form.heartRate}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    required
                    min={20}
                    max={300}
                    className={inputClass}
                    placeholder="72"
                  />
                </div>
                <div>
                  <label className={labelClass}>FR (rpm) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="respiratoryRate"
                    value={form.respiratoryRate}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    required
                    min={5}
                    max={60}
                    className={inputClass}
                    placeholder="16"
                  />
                </div>
                <div>
                  <label className={labelClass}>Temperatura (°C) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    step="0.1"
                    name="temperature"
                    value={form.temperature}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    required
                    min={30}
                    max={45}
                    className={inputClass}
                    placeholder="36.5"
                  />
                </div>
                <div>
                  <label className={labelClass}>SpO2 (%) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="oxygenSaturation"
                    value={form.oxygenSaturation}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    required
                    min={50}
                    max={100}
                    className={inputClass}
                    placeholder="98"
                  />
                </div>
                <div>
                  <label className={labelClass}>Peso (kg)</label>
                  <input
                    type="number"
                    step="0.1"
                    name="weight"
                    value={form.weight}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    min={1}
                    max={300}
                    className={inputClass}
                    placeholder="70"
                  />
                </div>
                <div>
                  <label className={labelClass}>Talla (cm)</label>
                  <input
                    type="number"
                    name="height"
                    value={form.height}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    min={30}
                    max={250}
                    className={inputClass}
                    placeholder="170"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  onClick={handleCancel}
                  disabled={saving || savingVitalSigns || savingTriage}
                  className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
                >
                  Cancelar
                </button>
                
                {/* Step 1 Button: Show only if vital signs not saved */}
                {!vitalSignsSaved && !triageSuccess && (
                  <button
                    type="submit"
                    disabled={savingVitalSigns}
                    className="px-6 py-2 bg-medin-cyan text-white font-semibold rounded-lg hover:bg-medin-blue transition-colors disabled:opacity-50"
                  >
                    {savingVitalSigns ? 'Guardando...' : 'Guardar Signos Vitales'}
                  </button>
                )}
              </div>
            </form>
          </div>
        )}

        {/* Manchester Classification Section (Step 2) - Show only after vital signs saved */}
        {vitalSignsSaved && !result && !triageSuccess && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Clasificación Manchester</h3>
            
            <form onSubmit={handleSaveTriage} className="space-y-6">
              {/* Motif Selection */}
              <div>
                <label className={labelClass}>
                  Motivo de Consulta <span className="text-red-500">*</span>
                </label>
                <select
                  value={selectedMotifId}
                  onChange={handleMotifChange}
                  disabled={catalogLoading || savingTriage}
                  required
                  className={inputClass}
                >
                  <option value="">Seleccione motivo de consulta</option>
                  {sortedMotifs.map(motif => (
                    <option key={motif.id} value={motif.id}>
                      {motif.description}
                    </option>
                  ))}
                </select>
              </div>

              {/* Discriminator Selection */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Discriminadores <span className="text-red-500">*</span>
                </label>
                
                {!selectedMotifId && (
                  <p className="text-sm text-gray-500 italic">
                    Seleccione un motivo para ver discriminadores
                  </p>
                )}
                
                {selectedMotifId && filteredDiscriminators.length === 0 && (
                  <p className="text-sm text-gray-500 italic">
                    No hay discriminadores disponibles para este motivo
                  </p>
                )}
                
                {selectedMotifId && filteredDiscriminators.length > 0 && (
                  <div className="space-y-2 max-h-64 overflow-y-auto border border-gray-200 rounded-lg p-3">
                    {filteredDiscriminators.map(discriminator => {
                      const priorityInfo = PRIORITY_MAP[discriminator.priorityLevel];
                      return (
                        <label
                          key={discriminator.id}
                          className="flex items-start gap-3 p-2 hover:bg-gray-50 rounded cursor-pointer"
                        >
                          <input
                            type="checkbox"
                            checked={selectedDiscriminatorIds.includes(discriminator.id)}
                            onChange={(e) => handleDiscriminatorChange(discriminator.id, e.target.checked)}
                            disabled={savingTriage}
                            className="mt-1 h-4 w-4 text-medin-cyan focus:ring-medin-cyan border-gray-300 rounded"
                          />
                          <div className="flex-1">
                            <div className="flex items-center gap-2">
                              <span className="text-sm font-medium text-gray-900">
                                {discriminator.description}
                              </span>
                              <span className={`text-xs px-2 py-0.5 rounded border ${priorityInfo.bgColor} ${priorityInfo.color}`}>
                                {priorityInfo.level}
                              </span>
                            </div>
                          </div>
                        </label>
                      );
                    })}
                  </div>
                )}
              </div>

              {/* Priority Preview - Show when discriminators are selected */}
              {calculatedPriority && (
                <div className="border-t pt-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Prioridad Calculada
                  </label>
                  <div className={`inline-flex items-center gap-2 px-4 py-2 rounded-lg border-2 ${PRIORITY_MAP[calculatedPriority.level!].bgColor} ${PRIORITY_MAP[calculatedPriority.level!].color}`}>
                    <span className="font-bold text-lg">{calculatedPriority.level}</span>
                    <span className="text-sm">- {calculatedPriority.description}</span>
                  </div>
                </div>
              )}

              {/* Step 2 Submit Button */}
              <div className="flex justify-end gap-3 pt-4 border-t">
                <button
                  type="submit"
                  disabled={savingTriage || !selectedMotifId || selectedDiscriminatorIds.length === 0}
                  className="px-6 py-2 bg-medin-cyan text-white font-semibold rounded-lg hover:bg-medin-blue transition-colors disabled:opacity-50"
                >
                  {savingTriage ? 'Guardando...' : 'Guardar Clasificación Manchester'}
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default TriageVitalSignsCapture;
