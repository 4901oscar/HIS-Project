import { useState, useEffect, useRef } from 'react';
import type { FC } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';
import { MainLayout } from '../../components/Layout';
import { ArrowLeftIcon, PlusIcon, TrashIcon } from '@heroicons/react/24/outline';
import { getAppointmentById } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import {
  getVitalSignsByAppointment,
  getAppointmentTriage,
  registerConsultation,
  generateLabOrder,
  generatePrescription,
} from '../../services/clinicalService';
import type { VitalSignsResponse, TriageResponse, MedicationItem } from '../../services/clinicalService';
import CIE10 from '../../data/cie10';
import type { Cie10Item } from '../../data/cie10';
import axios from 'axios';

// ─── Tipos ────────────────────────────────────────────────────────────────────

type Destination = 'LAB' | 'PHARMACY' | 'EXTERNAL_RX' | 'DISCHARGE';

const COMMON_LAB_TESTS = [
  'Hemograma completo (BHC)',
  'Química sanguínea (QS)',
  'Glicemia en ayunas',
  'Hemoglobina glicosilada (HbA1c)',
  'Perfil lipídico',
  'Creatinina sérica',
  'Ácido úrico',
  'Uroanálisis',
  'Urocultivo',
  'TSH (Tiroides)',
  'Proteína C reactiva (PCR)',
  'Velocidad de sedimentación (VSG)',
  'Transaminasas (TGO/TGP)',
  'Bilirrubinas totales y fraccionadas',
  'Serología VDRL',
  'Prueba de embarazo (HCG)',
  'Cultivo de secreción',
  'Radiografía de tórax',
  'Electrocardiograma (EKG)',
];

// ─── CIE-10 Autocomplete ──────────────────────────────────────────────────────

interface Cie10InputProps {
  value: string;
  onChange: (val: string) => void;
  required?: boolean;
  className?: string;
}

const Cie10Input: FC<Cie10InputProps> = ({ value, onChange, required, className }) => {
  const [query, setQuery] = useState(value);
  const [open, setOpen] = useState(false);
  const [results, setResults] = useState<Cie10Item[]>([]);
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!query || query.length < 2) { setResults([]); setOpen(false); return; }
    const q = query.toLowerCase();
    const filtered = (CIE10 as Cie10Item[])
      .filter(i => i.code.toLowerCase().includes(q) || i.description.toLowerCase().includes(q))
      .slice(0, 8);
    setResults(filtered);
    setOpen(filtered.length > 0);
  }, [query]);

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const handleSelect = (item: Cie10Item) => {
    const formatted = `${item.code} — ${item.description}`;
    setQuery(formatted);
    onChange(formatted);
    setOpen(false);
  };

  return (
    <div ref={wrapperRef} className="relative">
      <input
        value={query}
        onChange={e => { setQuery(e.target.value); onChange(e.target.value); }}
        required={required}
        placeholder="Ej: migraña  o  G43.0"
        className={className}
        autoComplete="off"
      />
      {open && (
        <ul className="absolute z-50 mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg max-h-64 overflow-y-auto text-sm">
          {results.map(item => (
            <li
              key={item.code}
              onMouseDown={() => handleSelect(item)}
              className="flex gap-2 px-3 py-2 hover:bg-medin-cyan/10 cursor-pointer"
            >
              <span className="font-mono text-xs text-medin-navy font-semibold shrink-0 pt-0.5">{item.code}</span>
              <span className="text-gray-700">{item.description}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

// ─── Manchester metadata ──────────────────────────────────────────────────────

const MANCHESTER_META: Record<string, { label: string; bg: string; text: string }> = {
  RED:    { label: 'Rojo — Inmediato',      bg: 'bg-red-100',    text: 'text-red-800' },
  ORANGE: { label: 'Naranja — Muy urgente', bg: 'bg-orange-100', text: 'text-orange-800' },
  YELLOW: { label: 'Amarillo — Urgente',    bg: 'bg-yellow-100', text: 'text-yellow-800' },
  GREEN:  { label: 'Verde — Poco urgente',  bg: 'bg-green-100',  text: 'text-green-800' },
  BLUE:   { label: 'Azul — No urgente',     bg: 'bg-blue-100',   text: 'text-blue-800' },
};

// ─── Vital sign card ──────────────────────────────────────────────────────────

interface VitalCardProps { label: string; value: string; unit: string; icon: string; alert?: boolean; }

const VitalCard: FC<VitalCardProps> = ({ label, value, unit, icon, alert }) => (
  <div className={`rounded-lg border p-3 flex flex-col gap-1 ${alert ? 'border-red-300 bg-red-50' : 'border-gray-200 bg-gray-50'}`}>
    <span className="text-xs text-gray-500 flex items-center gap-1">{icon} {label}</span>
    <span className={`text-xl font-bold ${alert ? 'text-red-700' : 'text-gray-900'}`}>{value}</span>
    <span className="text-xs text-gray-400">{unit}</span>
  </div>
);

// ─── Medication row ───────────────────────────────────────────────────────────

interface MedRowProps {
  med: MedicationItem;
  index: number;
  onChange: (index: number, field: keyof MedicationItem, value: string | number) => void;
  onRemove: (index: number) => void;
}

const MedRow: FC<MedRowProps> = ({ med, index, onChange, onRemove }) => {
  const inp = 'w-full px-2 py-1.5 border border-gray-300 rounded-md text-xs focus:ring-1 focus:ring-medin-cyan focus:border-transparent';
  return (
    <div className="grid grid-cols-12 gap-2 items-start">
      <div className="col-span-3">
        <input value={med.name} onChange={e => onChange(index, 'name', e.target.value)}
          required placeholder="Medicamento" className={inp} />
      </div>
      <div className="col-span-2">
        <input value={med.dosage} onChange={e => onChange(index, 'dosage', e.target.value)}
          required placeholder="Dosis" className={inp} />
      </div>
      <div className="col-span-3">
        <input value={med.frequency} onChange={e => onChange(index, 'frequency', e.target.value)}
          required placeholder="Frecuencia" className={inp} />
      </div>
      <div className="col-span-2">
        <input type="number" value={med.durationDays} min={1}
          onChange={e => onChange(index, 'durationDays', parseInt(e.target.value) || 1)}
          required placeholder="Días" className={inp} />
      </div>
      <div className="col-span-1">
        <input value={med.route} onChange={e => onChange(index, 'route', e.target.value)}
          required placeholder="Vía" className={inp} />
      </div>
      <div className="col-span-1 flex justify-center pt-1">
        <button type="button" onClick={() => onRemove(index)}
          className="p-1 text-red-400 hover:text-red-600 transition-colors">
          <TrashIcon className="h-4 w-4" />
        </button>
      </div>
    </div>
  );
};

// ─── Main component ───────────────────────────────────────────────────────────

const PatientConsultationForm: FC = () => {
  const { appointmentId } = useParams<{ appointmentId: string }>();
  const navigate = useNavigate();

  const [appointment, setAppointment] = useState<AppointmentListItem | null>(null);
  const [vitalSigns, setVitalSigns] = useState<VitalSignsResponse | null>(null);
  const [triage, setTriage] = useState<TriageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Bloque 2 — Evaluación clínica
  const [chiefComplaint, setChiefComplaint] = useState('');
  const [symptomsText, setSymptomsText] = useState('');
  const [primaryDiagnosis, setPrimaryDiagnosis] = useState('');
  const [secondaryDiagnosesText, setSecondaryDiagnosesText] = useState('');
  const [medicalNotes, setMedicalNotes] = useState('');
  const [treatmentPlan, setTreatmentPlan] = useState('');

  // Bloque 3 — Decisiones de cierre
  const [destination, setDestination] = useState<Destination | null>(null);
  const [selectedTests, setSelectedTests] = useState<string[]>([]);
  const [customTest, setCustomTest] = useState('');
  const [medications, setMedications] = useState<MedicationItem[]>([]);
  const [followUp, setFollowUp] = useState(false);
  const [followUpDate, setFollowUpDate] = useState('');
  const [followUpNotes, setFollowUpNotes] = useState('');

  useEffect(() => {
    if (!appointmentId) { setError('ID de cita no proporcionado'); setLoading(false); return; }
    const load = async () => {
      try {
        const [appt, vitals, tri] = await Promise.allSettled([
          getAppointmentById(appointmentId),
          getVitalSignsByAppointment(appointmentId),
          getAppointmentTriage(appointmentId),
        ]);
        if (appt.status === 'fulfilled') setAppointment(appt.value);
        else setError('Error al cargar la cita');
        if (vitals.status === 'fulfilled') setVitalSigns(vitals.value);
        if (tri.status === 'fulfilled') setTriage(tri.value);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [appointmentId]);

  const addMedication = () => {
    setMedications(prev => [...prev, { name: '', dosage: '', frequency: '', durationDays: 7, route: 'Oral', specialInstructions: '' }]);
  };

  const updateMedication = (index: number, field: keyof MedicationItem, value: string | number) => {
    setMedications(prev => prev.map((m, i) => i === index ? { ...m, [field]: value } : m));
  };

  const removeMedication = (index: number) => {
    setMedications(prev => prev.filter((_, i) => i !== index));
  };

  const toggleTest = (test: string) => {
    setSelectedTests(prev => prev.includes(test) ? prev.filter(t => t !== test) : [...prev, test]);
  };

  const addCustomTest = () => {
    const t = customTest.trim();
    if (t && !selectedTests.includes(t)) { setSelectedTests(prev => [...prev, t]); setCustomTest(''); }
  };

  const handleDestinationChange = async (dest: Destination) => {
    if (dest === 'PHARMACY') {
      const result = await Swal.fire({
        title: '¿El paciente puede pagar en farmacia?',
        html: `
          <p class="text-gray-500 text-sm mt-1">
            Si no cuenta con el dinero, se generará una <strong>receta externa</strong>
            que se enviará al correo del paciente.
          </p>
        `,
        icon: 'question',
        showConfirmButton: true,
        showDenyButton: true,
        showCancelButton: true,
        confirmButtonText: '💊 Sí, pasa a farmacia',
        denyButtonText: '📄 No, generar receta externa',
        cancelButtonText: 'Cancelar',
        buttonsStyling: false,
        customClass: {
          confirmButton: 'swal-btn-confirm',
          denyButton: 'swal-btn-deny',
          cancelButton: 'swal-btn-cancel',
          actions: 'swal-actions',
        },
      });
      if (result.isDismissed) return;
      if (result.isDenied) {
        setDestination('EXTERNAL_RX');
        if (medications.length === 0) addMedication();
        return;
      }
      if (result.isConfirmed) {
        setDestination('PHARMACY');
        if (medications.length === 0) addMedication();
      }
      return;
    }
    setDestination(dest);
    if (dest === 'EXTERNAL_RX' && medications.length === 0) {
      addMedication();
    }
  };

  const requiresDiagnosis = destination !== 'LAB';
  const requiresMedications = destination === 'PHARMACY' || destination === 'EXTERNAL_RX';
  const requiresLabTests = destination === 'LAB';

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!appointment || !destination) {
      setError('Selecciona el destino del paciente antes de continuar.');
      return;
    }
    if (requiresLabTests && selectedTests.length === 0) {
      setError('Agrega al menos un examen de laboratorio.');
      return;
    }
    if (requiresMedications && medications.length === 0) {
      setError('Agrega al menos un medicamento a la receta.');
      return;
    }
    if (followUp && !followUpDate) {
      setError('Selecciona la fecha de la cita de seguimiento.');
      return;
    }

    setSaving(true);
    setError(null);

    try {
      // 1. Registrar consulta
      const consultation = await registerConsultation({
        patientId: appointment.patient.id,
        appointmentId: appointment.id,
        chiefComplaint,
        symptoms: symptomsText.split(',').map(s => s.trim()).filter(Boolean),
        primaryDiagnosis: requiresDiagnosis ? primaryDiagnosis : 'Pendiente — resultado de laboratorio',
        secondaryDiagnoses: secondaryDiagnosesText
          ? secondaryDiagnosesText.split(',').map(s => s.trim()).filter(Boolean)
          : [],
        medicalNotes,
        treatmentPlan,
      });

      // 2. Generar orden de lab si aplica
      if (requiresLabTests && selectedTests.length > 0) {
        await generateLabOrder({
          consultationId: consultation.id,
          patientId: appointment.patient.id,
          testNames: selectedTests,
        });
      }

      // 3. Generar receta si aplica
      if (requiresMedications && medications.length > 0) {
        await generatePrescription({
          consultationId: consultation.id,
          patientId: appointment.patient.id,
          medications,
        });
      }

      await Swal.fire({
        title: destination === 'LAB'
          ? '¡Orden de laboratorio generada!'
          : destination === 'PHARMACY'
          ? '¡Consulta cerrada!'
          : destination === 'EXTERNAL_RX'
          ? '¡Receta externa generada!'
          : '¡Consulta cerrada!',
        text: destination === 'LAB'
          ? 'El paciente pasa a caja para pagar los exámenes.'
          : destination === 'PHARMACY'
          ? 'El paciente pasa a farmacia.'
          : destination === 'EXTERNAL_RX'
          ? 'La receta será enviada al correo del paciente.'
          : 'El paciente ha sido dado de alta.',
        icon: 'success',
        confirmButtonColor: '#0891b2',
        timer: 2500,
        showConfirmButton: false,
      });

      navigate('/doctor/consultas');
    } catch (err) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Error al registrar la consulta');
      else setError('Error al conectar con el servidor');
    } finally {
      setSaving(false);
    }
  };

  const inputClass = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';

  if (loading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center min-h-screen">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-medin-cyan border-t-transparent" />
        </div>
      </MainLayout>
    );
  }

  if (error && !appointment) {
    return (
      <MainLayout>
        <div className="max-w-4xl mx-auto">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">{error}</div>
          <button onClick={() => navigate('/doctor/consultas')} className="mt-4 px-4 py-2 text-medin-navy hover:text-medin-navy/80">
            Volver al listado
          </button>
        </div>
      </MainLayout>
    );
  }

  const manchesterMeta = appointment?.clinical?.manchesterLevel
    ? MANCHESTER_META[appointment.clinical.manchesterLevel]
    : null;

  const bpAlert  = vitalSigns ? vitalSigns.systolicPressure > 140 || vitalSigns.diastolicPressure > 90 : false;
  const hrAlert  = vitalSigns ? vitalSigns.heartRate > 100 || vitalSigns.heartRate < 50 : false;
  const tempAlert = vitalSigns ? vitalSigns.temperature >= 38.5 : false;
  const o2Alert  = vitalSigns ? vitalSigns.oxygenSaturation < 94 : false;
  const hasAlert = bpAlert || hrAlert || tempAlert || o2Alert;

  return (
    <MainLayout>
      <div className="max-w-4xl mx-auto space-y-6 pb-12">

        {/* Header */}
        <div className="flex items-center gap-4">
          <button onClick={() => navigate('/doctor/consultas')} className="p-2 hover:bg-gray-100 rounded-lg transition-colors">
            <ArrowLeftIcon className="h-5 w-5 text-gray-600" />
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Consulta Médica</h2>
            <p className="mt-1 text-sm text-gray-600">
              {appointment?.patient.fullName}
              {manchesterMeta && (
                <span className={`ml-2 inline-block px-2 py-0.5 rounded-full text-xs font-semibold ${manchesterMeta.bg} ${manchesterMeta.text}`}>
                  {manchesterMeta.label}
                </span>
              )}
            </p>
          </div>
        </div>

        {/* ── BLOQUE 1: Información del paciente ── */}
        <div className="bg-white rounded-lg shadow p-6 space-y-5">
          <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wide border-b border-gray-100 pb-2">
            Bloque 1 — Información del paciente
          </h3>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <p className="text-gray-500">Nombre</p>
              <p className="font-medium text-gray-900">{appointment?.patient.fullName}</p>
            </div>
            <div>
              <p className="text-gray-500">DPI</p>
              <p className="font-medium font-mono text-gray-900">{appointment?.patient.dpi || '—'}</p>
            </div>
            <div>
              <p className="text-gray-500">Fecha</p>
              <p className="font-medium text-gray-900">
                {appointment && new Date(appointment.appointmentDate + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: '2-digit', year: 'numeric' })}
              </p>
            </div>
            <div>
              <p className="text-gray-500">Hora</p>
              <p className="font-medium text-gray-900">{appointment?.appointmentTime.toString().substring(0, 5)}</p>
            </div>
            {appointment?.notes && (
              <div className="col-span-2 md:col-span-4">
                <p className="text-gray-500">Motivo de la cita</p>
                <p className="font-medium text-gray-900">{appointment.notes}</p>
              </div>
            )}
          </div>

          {triage && (
            <div>
              <p className="text-sm font-medium text-gray-700 mb-2">Triaje Manchester</p>
              <div className="flex flex-wrap gap-2 text-sm">
                <span className={`px-3 py-1.5 rounded-full font-semibold ${manchesterMeta?.bg} ${manchesterMeta?.text}`}>
                  {triage.priorityLevel} — {triage.priorityDescription}
                </span>
                <span className="px-3 py-1.5 rounded-full bg-gray-100 text-gray-700">
                  Tiempo máx. espera: <strong>{triage.maxWaitTimeMinutes} min</strong>
                </span>
                <span className="px-3 py-1.5 rounded-full bg-gray-100 text-gray-600 text-xs">
                  Realizado: {new Date(triage.performedAt).toLocaleTimeString('es-GT', { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
            </div>
          )}

          {vitalSigns ? (
            <div>
              <p className="text-sm font-medium text-gray-700 mb-3">Signos Vitales (triaje)</p>
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
                <VitalCard label="Presión Arterial" value={`${vitalSigns.systolicPressure}/${vitalSigns.diastolicPressure}`} unit="mmHg" icon="🩺" alert={bpAlert} />
                <VitalCard label="Frec. Cardíaca"   value={String(vitalSigns.heartRate)}                                    unit="lpm"  icon="❤️"  alert={hrAlert} />
                <VitalCard label="Frec. Respiratoria" value={String(vitalSigns.respiratoryRate)}                            unit="rpm"  icon="🫁" />
                <VitalCard label="Temperatura"      value={`${vitalSigns.temperature.toFixed(1)}`}                          unit="°C"   icon="🌡️" alert={tempAlert} />
                <VitalCard label="Saturación O₂"   value={`${vitalSigns.oxygenSaturation}%`}                               unit="SpO₂" icon="💨" alert={o2Alert} />
                {vitalSigns.weight && <VitalCard label="Peso"  value={String(vitalSigns.weight)}       unit="kg"    icon="⚖️" />}
                {vitalSigns.height && <VitalCard label="Talla" value={String(vitalSigns.height)}       unit="cm"    icon="📏" />}
                {vitalSigns.bmi    && <VitalCard label="IMC"   value={vitalSigns.bmi.toFixed(1)}       unit="kg/m²" icon="📊" />}
              </div>
              {hasAlert && (
                <p className="mt-2 text-xs text-red-600 font-medium">
                  ⚠️ Valores en rojo están fuera de rango normal.
                </p>
              )}
            </div>
          ) : (
            <p className="text-sm text-gray-400 italic">No se encontraron signos vitales registrados para esta cita.</p>
          )}
        </div>

        {/* ── BLOQUE 2 + 3 en un mismo form ── */}
        <form onSubmit={handleSubmit} className="space-y-6">

          {/* BLOQUE 2: Evaluación clínica */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            {/* Header del bloque */}
            <div className="flex items-center gap-3 px-6 py-4 bg-gradient-to-r from-medin-navy/5 to-transparent border-b border-gray-100">
              <span className="flex items-center justify-center w-7 h-7 rounded-full bg-medin-navy text-white text-xs font-bold shrink-0">2</span>
              <div>
                <h3 className="text-sm font-semibold text-gray-900">Evaluación clínica</h3>
                <p className="text-xs text-gray-500">Registra el diagnóstico y hallazgos de la consulta</p>
              </div>
            </div>

            <div className="p-6 space-y-6">
              {/* Motivo + síntomas en 2 columnas */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Motivo de consulta <span className="text-red-500">*</span>
                  </label>
                  <input
                    value={chiefComplaint}
                    onChange={e => setChiefComplaint(e.target.value)}
                    required
                    className={inputClass}
                    placeholder="Ej: Dolor de cabeza desde hace 3 días"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Síntomas <span className="text-red-500">*</span>
                    <span className="ml-1 normal-case font-normal text-gray-400">(separados por coma)</span>
                  </label>
                  <input
                    value={symptomsText}
                    onChange={e => setSymptomsText(e.target.value)}
                    required
                    className={inputClass}
                    placeholder="cefalea, náuseas, fotofobia"
                  />
                </div>
              </div>

              {/* Diagnóstico CIE-10 */}
              <div className="space-y-1.5">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Diagnóstico principal (CIE-10)
                    {requiresDiagnosis && <span className="text-red-500"> *</span>}
                  </label>
                  {!requiresDiagnosis && (
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 text-xs font-medium">
                      ⏳ Se completará con resultados de lab
                    </span>
                  )}
                </div>
                <Cie10Input value={primaryDiagnosis} onChange={setPrimaryDiagnosis} required={requiresDiagnosis} className={inputClass} />
                <p className="text-xs text-gray-400">Escribe el nombre de la enfermedad o el código CIE-10 para buscar en el catálogo.</p>
              </div>

              {/* Diagnósticos secundarios */}
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                  Diagnósticos secundarios
                  <span className="ml-1 normal-case font-normal text-gray-400">(separados por coma)</span>
                </label>
                <input
                  value={secondaryDiagnosesText}
                  onChange={e => setSecondaryDiagnosesText(e.target.value)}
                  className={inputClass}
                  placeholder="Ej: Hipertensión arterial (I10)"
                />
              </div>

              {/* Notas + Plan en 2 columnas */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Notas de evolución</label>
                  <textarea
                    value={medicalNotes}
                    onChange={e => setMedicalNotes(e.target.value)}
                    rows={5}
                    className={inputClass + ' resize-none'}
                    placeholder="Observaciones clínicas, hallazgos al examen físico, evolución del cuadro..."
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Plan de tratamiento</label>
                  <textarea
                    value={treatmentPlan}
                    onChange={e => setTreatmentPlan(e.target.value)}
                    rows={5}
                    className={inputClass + ' resize-none'}
                    placeholder="Indicaciones médicas, medidas generales, reposo, dieta..."
                  />
                </div>
              </div>
            </div>
          </div>

          {/* BLOQUE 3: Decisiones de cierre */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <div className="flex items-center gap-3 px-6 py-4 bg-gradient-to-r from-medin-navy/5 to-transparent border-b border-gray-100">
              <span className="flex items-center justify-center w-7 h-7 rounded-full bg-medin-navy text-white text-xs font-bold shrink-0">3</span>
              <div>
                <h3 className="text-sm font-semibold text-gray-900">¿A dónde va el paciente?</h3>
                <p className="text-xs text-gray-500">Selecciona el siguiente paso al cerrar esta consulta</p>
              </div>
            </div>
          <div className="p-6 space-y-6">

            {/* Radio buttons de destino */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {([
                { value: 'LAB',         label: 'Laboratorio',         desc: 'Requiere exámenes antes de diagnosticar', icon: '🔬', color: 'border-purple-300 bg-purple-50' },
                { value: 'PHARMACY',    label: 'Farmacia interna',    desc: 'Necesita medicamentos con pago interno',   icon: '💊', color: 'border-blue-300 bg-blue-50' },
                { value: 'EXTERNAL_RX', label: 'Receta externa',      desc: 'Receta enviada al correo del paciente',    icon: '📄', color: 'border-amber-300 bg-amber-50' },
                { value: 'DISCHARGE',   label: 'Alta sin receta',     desc: 'Consulta finalizada, no requiere más',     icon: '✅', color: 'border-green-300 bg-green-50' },
              ] as { value: Destination; label: string; desc: string; icon: string; color: string }[]).map(opt => (
                <label
                  key={opt.value}
                  className={`flex items-start gap-3 p-4 rounded-xl border-2 cursor-pointer transition-all ${
                    destination === opt.value ? opt.color + ' ring-2 ring-offset-1 ring-medin-cyan' : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <input
                    type="radio"
                    name="destination"
                    value={opt.value}
                    checked={destination === opt.value}
                    onChange={() => handleDestinationChange(opt.value as Destination)}
                    className="mt-0.5 accent-medin-cyan"
                  />
                  <div>
                    <p className="text-sm font-semibold text-gray-900">{opt.icon} {opt.label}</p>
                    <p className="text-xs text-gray-500 mt-0.5">{opt.desc}</p>
                  </div>
                </label>
              ))}
            </div>

            {/* ── Laboratorio: selección de exámenes ── */}
            {destination === 'LAB' && (
              <div className="space-y-3 pt-2">
                <p className="text-sm font-medium text-gray-700">Selecciona los exámenes a solicitar:</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-56 overflow-y-auto pr-1">
                  {COMMON_LAB_TESTS.map(test => (
                    <label key={test} className="flex items-center gap-2 text-sm cursor-pointer">
                      <input
                        type="checkbox"
                        checked={selectedTests.includes(test)}
                        onChange={() => toggleTest(test)}
                        className="accent-medin-cyan"
                      />
                      {test}
                    </label>
                  ))}
                </div>
                <div className="flex gap-2">
                  <input
                    value={customTest}
                    onChange={e => setCustomTest(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && (e.preventDefault(), addCustomTest())}
                    placeholder="Otro examen..."
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-medin-cyan focus:border-transparent"
                  />
                  <button type="button" onClick={addCustomTest} className="px-3 py-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                    <PlusIcon className="h-4 w-4 text-gray-600" />
                  </button>
                </div>
                {selectedTests.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {selectedTests.map(t => (
                      <span key={t} className="flex items-center gap-1 px-2 py-1 bg-purple-100 text-purple-800 rounded-full text-xs font-medium">
                        {t}
                        <button type="button" onClick={() => toggleTest(t)} className="ml-1 hover:text-purple-600">×</button>
                      </span>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* ── Farmacia / Receta externa: medicamentos ── */}
            {(destination === 'PHARMACY' || destination === 'EXTERNAL_RX') && (
              <div className="space-y-3 pt-2">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-gray-700">Medicamentos recetados:</p>
                  <button type="button" onClick={addMedication} className="flex items-center gap-1 text-xs text-medin-navy hover:text-medin-navy/70 font-medium">
                    <PlusIcon className="h-4 w-4" /> Agregar
                  </button>
                </div>
                {medications.length > 0 && (
                  <div className="space-y-1">
                    <div className="grid grid-cols-12 gap-2 text-xs text-gray-400 font-medium px-0.5">
                      <span className="col-span-3">Medicamento</span>
                      <span className="col-span-2">Dosis</span>
                      <span className="col-span-3">Frecuencia</span>
                      <span className="col-span-2">Días</span>
                      <span className="col-span-1">Vía</span>
                      <span className="col-span-1" />
                    </div>
                    {medications.map((med, i) => (
                      <MedRow key={i} med={med} index={i} onChange={updateMedication} onRemove={removeMedication} />
                    ))}
                  </div>
                )}
                {medications.length === 0 && (
                  <p className="text-sm text-gray-400 italic">Sin medicamentos — agrega al menos uno.</p>
                )}
                {destination === 'EXTERNAL_RX' && (
                  <div className="mt-2 p-3 bg-amber-50 border border-amber-200 rounded-lg text-xs text-amber-800">
                    📧 La receta se enviará al correo registrado del paciente: <strong>{appointment?.patient.email || 'no registrado'}</strong>
                  </div>
                )}
              </div>
            )}

            {/* ── Cita de seguimiento (combinable) ── */}
            {destination && (
              <div className="border-t border-gray-100 pt-4">
                <label className="flex items-center gap-2 text-sm font-medium text-gray-700 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={followUp}
                    onChange={e => setFollowUp(e.target.checked)}
                    className="accent-medin-cyan"
                  />
                  Agendar cita de seguimiento
                </label>
                {followUp && (
                  <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label className={labelClass}>Fecha de seguimiento <span className="text-red-500">*</span></label>
                      <input type="date" value={followUpDate} onChange={e => setFollowUpDate(e.target.value)} min={new Date().toISOString().split('T')[0]} className={inputClass} />
                    </div>
                    <div>
                      <label className={labelClass}>Notas para la próxima cita</label>
                      <input value={followUpNotes} onChange={e => setFollowUpNotes(e.target.value)} className={inputClass} placeholder="Ej: Traer resultados de lab" />
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
          </div>

          {/* Error global */}
          {error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>
          )}

          {/* Acciones */}
          <div className="flex justify-end gap-3">
            <button type="button" onClick={() => navigate('/doctor/consultas')} className="px-5 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm hover:bg-gray-50 transition-colors">
              Cancelar
            </button>
            <button
              type="submit"
              disabled={saving || !destination}
              className="px-6 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-40 text-sm"
            >
              {saving ? 'Guardando...' : !destination ? 'Selecciona destino' : 'Cerrar consulta'}
            </button>
          </div>
        </form>

      </div>
    </MainLayout>
  );
};

export default PatientConsultationForm;
