import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import type { FC, FormEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import { SpeakerWaveIcon } from '@heroicons/react/24/outline';
import { getPatientById } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import { recordVitalSigns, getVitalSignsByAppointment, performTriage } from '../../services/clinicalService';
import { getManchesterCatalog } from '../../services/manchesterService';
import type { ManchesterMotif, ManchesterDiscriminator } from '../../types/triage';
import { extractErrorMessage } from '../../utils/errorHandler';
import { validateForm, clearFieldError } from '../../utils/formValidation';
import type { Schema, FormErrors } from '../../utils/formValidation';
import { usePatientHistory } from '../../context/PatientHistoryContext';

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

const vitalSignsSchema: Schema<VitalForm> = {
  systolicPressure:  [{ type: 'required', message: 'Requerida.' }, { type: 'range', min: 50,  max: 250, message: 'Debe estar entre 50 y 250 mmHg.' }],
  diastolicPressure: [{ type: 'required', message: 'Requerida.' }, { type: 'range', min: 30,  max: 150, message: 'Debe estar entre 30 y 150 mmHg.' }],
  heartRate:         [{ type: 'required', message: 'Requerida.' }, { type: 'range', min: 20,  max: 300, message: 'Debe estar entre 20 y 300 lpm.' }],
  respiratoryRate:   [{ type: 'required', message: 'Requerida.' }, { type: 'range', min: 5,   max: 60,  message: 'Debe estar entre 5 y 60 rpm.' }],
  temperature:       [{ type: 'required', message: 'Requerida.' }, { type: 'range', min: 30,  max: 45,  message: 'Debe estar entre 30 y 45 °C.' }],
  oxygenSaturation:  [{ type: 'required', message: 'Requerida.' }, { type: 'range', min: 50,  max: 100, message: 'Debe estar entre 50 y 100%.' }],
  weight:            [{ type: 'range', min: 1,   max: 300, message: 'Debe estar entre 1 y 300 kg.' }],
  height:            [{ type: 'range', min: 30,  max: 250, message: 'Debe estar entre 30 y 250 cm.' }],
};

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
const PRIORITY_MAP: Record<string, { level: string; label: string; description: string; color: string; bgColor: string }> = {
  RED: {
    level: 'RED',
    label: 'ROJO',
    description: 'Inmediato',
    color: 'text-red-800',
    bgColor: 'bg-red-100 border-red-300'
  },
  ORANGE: {
    level: 'ORANGE',
    label: 'NARANJA',
    description: 'Muy urgente',
    color: 'text-orange-800',
    bgColor: 'bg-orange-100 border-orange-300'
  },
  YELLOW: {
    level: 'YELLOW',
    label: 'AMARILLO',
    description: 'Urgente',
    color: 'text-yellow-800',
    bgColor: 'bg-yellow-100 border-yellow-300'
  },
  GREEN: {
    level: 'GREEN',
    label: 'VERDE',
    description: 'Poco urgente',
    color: 'text-green-800',
    bgColor: 'bg-green-100 border-green-300'
  },
  BLUE: {
    level: 'BLUE',
    label: 'AZUL',
    description: 'No urgente',
    color: 'text-blue-800',
    bgColor: 'bg-blue-100 border-blue-300'
  }
};

const TriageVitalSignsCapture: FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { setPatient: setHistorialPatient } = usePatientHistory();

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
  const [fieldErrors, setFieldErrors] = useState<FormErrors<VitalForm>>({});
  const [vitalSignsSuccess, setVitalSignsSuccess] = useState(false);
  const [vitalSignsLoadWarning, setVitalSignsLoadWarning] = useState<string | null>(null);
  const [isEditingVitalSigns, setIsEditingVitalSigns] = useState(false);
  const savedFormValuesRef = useRef<VitalForm>(emptyForm);
  
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
          const p = patientData.value;
          const fullName = [p.firstName, p.firstLastName].filter(Boolean).join(' ');
          setHistorialPatient(p.id, fullName);
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
          const loadedForm: VitalForm = {
            systolicPressure: String(vitalSigns.systolicPressure),
            diastolicPressure: String(vitalSigns.diastolicPressure),
            heartRate: String(vitalSigns.heartRate),
            respiratoryRate: String(vitalSigns.respiratoryRate),
            temperature: String(vitalSigns.temperature),
            oxygenSaturation: String(vitalSigns.oxygenSaturation),
            weight: vitalSigns.weight ? String(vitalSigns.weight) : '',
            height: vitalSigns.height ? String(vitalSigns.height) : '',
          };
          setForm(loadedForm);
          savedFormValuesRef.current = loadedForm;
          setVitalSignsSaved(true);
          setVitalSignsLocked(true);
        } else {
          // 404 = no vital signs yet (expected on first entry)
          const reason = existingVitalSigns.reason as { response?: { status?: number } };
          if (reason?.response?.status !== 404) {
            setVitalSignsLoadWarning('No se pudieron cargar los signos vitales guardados. Verifique la conexión o ingrese los valores nuevamente.');
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

  const DECIMAL_FIELDS = new Set(['temperature', 'weight']);

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (DECIMAL_FIELDS.has(name)) {
      if (!/^[\d.]*$/.test(value) || (value.match(/\./g) ?? []).length > 1) return;
    } else {
      if (!/^\d*$/.test(value)) return;
    }
    setForm((prev) => ({ ...prev, [name]: value }));
    setFieldErrors((prev) => clearFieldError(prev, name as keyof VitalForm));
  }, []);

  // Step 1: Submit vital signs
  const handleSaveVitalSigns = useCallback(async () => {
    if (!patient || !appointmentId) return;

    const errs = validateForm(vitalSignsSchema, form);
    if (Object.keys(errs).length > 0) {
      setFieldErrors(errs);
      return;
    }
    setFieldErrors({});
    
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
      savedFormValuesRef.current = { ...form };
      setVitalSignsSaved(true);
      setVitalSignsLocked(true);
      setVitalSignsSuccess(true);
      setIsEditingVitalSigns(false);
      
    } catch (err) {

      setVitalSignsError(extractErrorMessage(err));
    } finally {
      setSavingVitalSigns(false);
    }
  }, [patient, appointmentId, form]);

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
      }, 500);
      
    } catch (err) {

      setTriageError(extractErrorMessage(err));
    } finally {
      setSavingTriage(false);
    }
  }, [patient, appointmentId, selectedMotifId, selectedDiscriminatorIds, validateManchesterSelection, navigate]);

  const inputCls = (hasError: boolean) =>
    `w-full px-3 py-2 border ${hasError ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm`;
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
        <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Signos Vitales</h3>

            {vitalSignsLoadWarning && (
              <div className="mb-4 bg-yellow-50 border border-yellow-200 rounded-lg p-3 text-sm text-yellow-800">
                {vitalSignsLoadWarning}
              </div>
            )}

            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div>
                  <label className={labelClass}>Sist. (mmHg) <span className="text-red-500">*</span></label>
                  <input
                    type="text"
                    inputMode="numeric"
                    name="systolicPressure"
                    value={form.systolicPressure}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    maxLength={3}
                    className={inputCls(!!fieldErrors.systolicPressure)}
                    placeholder="120"
                  />
                  {fieldErrors.systolicPressure && <p className="mt-1 text-xs text-red-600">{fieldErrors.systolicPressure}</p>}
                </div>
                <div>
                  <label className={labelClass}>Diast. (mmHg) <span className="text-red-500">*</span></label>
                  <input
                    type="text"
                    inputMode="numeric"
                    name="diastolicPressure"
                    value={form.diastolicPressure}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    maxLength={3}
                    className={inputCls(!!fieldErrors.diastolicPressure)}
                    placeholder="80"
                  />
                  {fieldErrors.diastolicPressure && <p className="mt-1 text-xs text-red-600">{fieldErrors.diastolicPressure}</p>}
                </div>
                <div>
                  <label className={labelClass}>FC (lpm) <span className="text-red-500">*</span></label>
                  <input
                    type="text"
                    inputMode="numeric"
                    name="heartRate"
                    value={form.heartRate}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    maxLength={3}
                    className={inputCls(!!fieldErrors.heartRate)}
                    placeholder="72"
                  />
                  {fieldErrors.heartRate && <p className="mt-1 text-xs text-red-600">{fieldErrors.heartRate}</p>}
                </div>
                <div>
                  <label className={labelClass}>FR (rpm) <span className="text-red-500">*</span></label>
                  <input
                    type="text"
                    inputMode="numeric"
                    name="respiratoryRate"
                    value={form.respiratoryRate}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    maxLength={2}
                    className={inputCls(!!fieldErrors.respiratoryRate)}
                    placeholder="16"
                  />
                  {fieldErrors.respiratoryRate && <p className="mt-1 text-xs text-red-600">{fieldErrors.respiratoryRate}</p>}
                </div>
                <div>
                  <label className={labelClass}>Temperatura (°C) <span className="text-red-500">*</span></label>
                  <input
                    type="text"
                    inputMode="decimal"
                    name="temperature"
                    value={form.temperature}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    maxLength={4}
                    className={inputCls(!!fieldErrors.temperature)}
                    placeholder="36.5"
                  />
                  {fieldErrors.temperature && <p className="mt-1 text-xs text-red-600">{fieldErrors.temperature}</p>}
                </div>
                <div>
                  <label className={labelClass}>SpO2 (%) <span className="text-red-500">*</span></label>
                  <input
                    type="text"
                    inputMode="numeric"
                    name="oxygenSaturation"
                    value={form.oxygenSaturation}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    maxLength={3}
                    className={inputCls(!!fieldErrors.oxygenSaturation)}
                    placeholder="98"
                  />
                  {fieldErrors.oxygenSaturation && <p className="mt-1 text-xs text-red-600">{fieldErrors.oxygenSaturation}</p>}
                </div>
                <div>
                  <label className={labelClass}>Peso (kg)</label>
                  <input
                    type="text"
                    inputMode="decimal"
                    name="weight"
                    value={form.weight}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    maxLength={5}
                    className={inputCls(!!fieldErrors.weight)}
                    placeholder="70.0"
                  />
                  {fieldErrors.weight && <p className="mt-1 text-xs text-red-600">{fieldErrors.weight}</p>}
                </div>
                <div>
                  <label className={labelClass}>Talla (cm)</label>
                  <input
                    type="text"
                    inputMode="numeric"
                    name="height"
                    value={form.height}
                    onChange={handleChange}
                    disabled={vitalSignsLocked || savingVitalSigns}
                    maxLength={3}
                    className={inputCls(!!fieldErrors.height)}
                    placeholder="170"
                  />
                  {fieldErrors.height && <p className="mt-1 text-xs text-red-600">{fieldErrors.height}</p>}
                </div>
              </div>

              {vitalSignsError && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                  <p className="text-red-700 text-sm">{vitalSignsError}</p>
                </div>
              )}

              <div className="flex justify-end gap-3 pt-4">
                {vitalSignsSaved ? (
                  <button
                    type="button"
                    onClick={() => { setVitalSignsLocked(false); setVitalSignsSaved(false); setVitalSignsSuccess(false); setIsEditingVitalSigns(true); setFieldErrors({}); setVitalSignsError(null); }}
                    disabled={savingTriage}
                    className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 text-sm"
                  >
                    Editar signos vitales
                  </button>
                ) : (
                  <>
                    <button
                      type="button"
                      onClick={() => {
                        if (isEditingVitalSigns) {
                          setForm(savedFormValuesRef.current);
                          setVitalSignsLocked(true);
                          setVitalSignsSaved(true);
                          setVitalSignsSuccess(false);
                          setIsEditingVitalSigns(false);
                          setVitalSignsError(null);
                          setFieldErrors({});
                        } else {
                          handleCancel();
                        }
                      }}
                      disabled={savingVitalSigns}
                      className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 text-sm"
                    >
                      Cancelar
                    </button>
                    <button
                      type="button"
                      onClick={handleSaveVitalSigns}
                      disabled={savingVitalSigns}
                      className="px-6 py-2 bg-medin-cyan text-white font-semibold rounded-lg hover:bg-medin-blue transition-colors disabled:opacity-50"
                    >
                      {savingVitalSigns ? 'Guardando...' : 'Guardar Signos Vitales'}
                    </button>
                  </>
                )}
              </div>
            </div>
          </div>

        {/* Mensaje entre secciones */}
        {vitalSignsSuccess && !triageSuccess && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="font-semibold text-green-800">✓ Signos vitales guardados exitosamente</p>
            <p className="text-sm text-green-700 mt-1">Ahora puede completar la clasificación Manchester</p>
          </div>
        )}

        {/* Manchester Classification Section (Step 2) - Show only after vital signs saved */}
        {vitalSignsSaved && !triageSuccess && (
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
                  className={inputCls(false)}
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
                                {priorityInfo.label}
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
                    <span className="font-bold text-lg">{PRIORITY_MAP[calculatedPriority.level!].label}</span>
                    <span className="text-sm">— {calculatedPriority.description}</span>
                  </div>
                </div>
              )}

              {/* Step 2 Submit Button */}
              <div className="flex justify-end gap-3 pt-4 border-t">
                <button
                  type="button"
                  onClick={handleCancel}
                  disabled={savingTriage}
                  className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50 text-sm"
                >
                  Cancelar
                </button>
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
