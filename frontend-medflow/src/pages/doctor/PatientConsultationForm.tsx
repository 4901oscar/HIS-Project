import { useState, useEffect, useRef, useMemo } from 'react';
import type { FC } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';
import { MainLayout } from '../../components/Layout';
import { ArrowLeftIcon, PlusIcon, TrashIcon } from '@heroicons/react/24/outline';
import { getAppointmentById } from '../../services/appointmentService';
import { getAvailableSlotsForDate } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import {
  getVitalSignsByAppointment,
  getAppointmentTriage,
  registerConsultation,
  getConsultationByAppointment,
  generateLabOrder,
  generatePrescription,
} from '../../services/clinicalService';
import type { VitalSignsResponse, TriageResponse, MedicationItem, ServiceCharge } from '../../services/clinicalService';
import { getServiceItems } from '../../services/billingCatalogService';
import type { ServiceItemResponse } from '../../services/billingCatalogService';
import { listActiveDoctors, getDoctorDaysOff, type Doctor, type DayOff } from '../../services/doctorService';
import { Calendar, shiftSlots, fmt } from '../../components/Calendar';
import CIE10 from '../../data/cie10';
import type { Cie10Item } from '../../data/cie10';
import axios from 'axios';
import LabResultsSection from '../../components/doctor/LabResultsSection';
import { usePatientHistory } from '../../context/PatientHistoryContext';
import { validateForm, clearFieldError } from '../../utils/formValidation';
import type { Schema, FormErrors } from '../../utils/formValidation';

interface ConsultationForm {
  chiefComplaint: string;
  symptomsText: string;
  primaryDiagnosis: string;
}

// ─── Tipos ────────────────────────────────────────────────────────────────────

type Destination = 'LAB' | 'PHARMACY' | 'EXTERNAL_RX' | 'DISCHARGE';

// ─── CIE-10 Combobox ──────────────────────────────────────────────────────────

interface Cie10InputProps {
  value: string;
  onChange: (val: string) => void;
  required?: boolean;
  className?: string;
}

const Cie10Input: FC<Cie10InputProps> = ({ value, onChange, required, className }) => {
  // `value` es el valor seleccionado real (ej: "G43.0 — Migraña sin aura")
  // `query` es lo que el usuario escribe para buscar
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [results, setResults] = useState<Cie10Item[]>([]);
  const inputRef = useRef<HTMLInputElement>(null);
  const wrapperRef = useRef<HTMLDivElement>(null);

  // Cuando el usuario escribe, filtra el catálogo
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
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setOpen(false);
        setQuery(''); // descarta texto libre sin seleccionar
      }
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const handleSelect = (item: Cie10Item) => {
    onChange(`${item.code} — ${item.description}`);
    setQuery('');
    setOpen(false);
  };

  const handleClear = () => {
    onChange('');
    setQuery('');
    setTimeout(() => inputRef.current?.focus(), 0);
  };

  // Si hay un valor seleccionado, muestra el chip; si no, muestra el input de búsqueda
  if (value) {
    const [code, ...rest] = value.split(' — ');
    return (
      <div className="flex items-center gap-2 px-3 py-2 border border-medin-cyan rounded-lg bg-cyan-50">
        <span className="font-mono text-xs font-bold text-medin-navy shrink-0">{code}</span>
        <span className="text-sm text-gray-800 flex-1 truncate">{rest.join(' — ')}</span>
        <button
          type="button"
          onClick={handleClear}
          className="shrink-0 text-gray-400 hover:text-red-500 transition-colors text-lg leading-none"
          title="Cambiar diagnóstico"
        >
          ×
        </button>
        {/* Hidden input para validación HTML5 */}
        <input type="text" value={value} required={required} readOnly className="sr-only" tabIndex={-1} />
      </div>
    );
  }

  return (
    <div ref={wrapperRef} className="relative">
      <input
        ref={inputRef}
        value={query}
        onChange={e => setQuery(e.target.value)}
        placeholder="Escribe enfermedad o código CIE-10… (ej: migraña, G43)"
        className={className}
        autoComplete="off"
      />
      {/* Hidden input para validación HTML5 cuando no hay selección */}
      {required && (
        <input
          type="text"
          value={value}
          required
          readOnly
          className="sr-only"
          tabIndex={-1}
          aria-hidden="true"
        />
      )}
      {open && (
        <ul className="absolute z-50 mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg max-h-64 overflow-y-auto text-sm">
          {results.map(item => (
            <li
              key={item.code}
              onMouseDown={() => handleSelect(item)}
              className="flex gap-2 px-3 py-2.5 hover:bg-cyan-50 cursor-pointer border-b border-gray-50 last:border-0"
            >
              <span className="font-mono text-xs text-medin-navy font-bold shrink-0 pt-0.5 w-14">{item.code}</span>
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
  catalog: ServiceItemResponse[];
  onChange: (index: number, field: keyof MedicationItem, value: string | number) => void;
  onRemove: (index: number) => void;
  isInternal: boolean; // true = farmacia interna, false = receta externa
  errors?: Record<string, string>;
  onClearError?: (field: string) => void;
}

const MedRow: FC<MedRowProps> = ({ med, index, catalog, onChange, onRemove, isInternal, errors = {}, onClearError }) => {
  const inp = (hasError = false) =>
    `w-full px-2 py-1.5 border ${hasError ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-md text-xs focus:ring-1 focus:ring-medin-cyan focus:border-transparent`;
  
  // Actualizar cantidad total cuando cambian los valores
  const handleDosageAmountChange = (value: number) => {
    onChange(index, 'dosageAmount', value);
    // Recalcular total con los nuevos valores
    if (isInternal && med.frequencyHours && med.durationDays) {
      const timesPerDay = Math.floor(24 / med.frequencyHours);
      const total = value * timesPerDay * med.durationDays;
      onChange(index, 'totalQuantity', total);
    }
  };

  const handleFrequencyHoursChange = (value: number) => {
    onChange(index, 'frequencyHours', value);
    // Actualizar también el campo frequency con formato legible
    const timesPerDay = Math.floor(24 / value);
    onChange(index, 'frequency', `Cada ${value} horas (${timesPerDay}x/día)`);
    // Recalcular total con los nuevos valores
    if (isInternal && med.dosageAmount && med.durationDays) {
      const total = med.dosageAmount * timesPerDay * med.durationDays;
      onChange(index, 'totalQuantity', total);
    }
  };

  const handleDurationChange = (value: number) => {
    onChange(index, 'durationDays', value);
    // Recalcular total con los nuevos valores
    if (isInternal && med.dosageAmount && med.frequencyHours) {
      const timesPerDay = Math.floor(24 / med.frequencyHours);
      const total = med.dosageAmount * timesPerDay * value;
      onChange(index, 'totalQuantity', total);
    }
  };

  // Obtener precio del medicamento del catálogo
  const getMedicationPrice = () => {
    if (!isInternal) return 0;
    const catalogItem = catalog.find(m => m.id === med.name); // med.name contiene el ID
    return catalogItem ? Number(catalogItem.price) : 0;
  };

  const totalQuantity = med.totalQuantity || 0;  // Usar el valor del estado directamente
  const unitPrice = getMedicationPrice();
  const totalCost = totalQuantity * unitPrice;

  if (isInternal) {
    // Modo Farmacia Interna - Con cálculos automáticos
    return (
      <div className="border border-gray-200 rounded-lg p-3 bg-gray-50">
        <div className="grid grid-cols-12 gap-2 items-start mb-2">
          {/* Medicamento del catálogo */}
          <div className="col-span-4">
            <label className="block text-xs font-medium text-gray-700 mb-1">Medicamento *</label>
            <select value={med.name} onChange={e => { onChange(index, 'name', e.target.value); onClearError?.('name'); }} className={inp(!!errors.name)}>
              <option value="">— Seleccionar —</option>
              {catalog.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
            </select>
            {errors.name && <p className="text-xs text-red-600 mt-0.5">{errors.name}</p>}
          </div>

          {/* Dosis por toma */}
          <div className="col-span-2">
            <label className="block text-xs font-medium text-gray-700 mb-1">Dosis/toma *</label>
            <input
              type="number"
              step="0.5"
              min="0.5"
              value={med.dosageAmount || ''}
              onChange={e => { handleDosageAmountChange(parseFloat(e.target.value) || 0); onClearError?.('dosageAmount'); }}
              placeholder="1"
              className={inp(!!errors.dosageAmount)}
            />
            {errors.dosageAmount && <p className="text-xs text-red-600 mt-0.5">{errors.dosageAmount}</p>}
          </div>

          {/* Unidad */}
          <div className="col-span-2">
            <label className="block text-xs font-medium text-gray-700 mb-1">Unidad *</label>
            <select
              value={med.dosageUnit || ''}
              onChange={e => {
                onChange(index, 'dosageUnit', e.target.value);
                onChange(index, 'dosage', `${med.dosageAmount || ''} ${e.target.value}`);
                onClearError?.('dosageUnit');
              }}
              className={inp(!!errors.dosageUnit)}
            >
              <option value="">—</option>
              <option value="pastilla(s)">pastilla(s)</option>
              <option value="cápsula(s)">cápsula(s)</option>
              <option value="tableta(s)">tableta(s)</option>
              <option value="ml">ml</option>
              <option value="cucharada(s)">cucharada(s)</option>
              <option value="gota(s)">gota(s)</option>
              <option value="aplicación(es)">aplicación(es)</option>
            </select>
            {errors.dosageUnit && <p className="text-xs text-red-600 mt-0.5">{errors.dosageUnit}</p>}
          </div>

          {/* Frecuencia en horas */}
          <div className="col-span-2">
            <label className="block text-xs font-medium text-gray-700 mb-1">Cada (hrs) *</label>
            <select
              value={med.frequencyHours || ''}
              onChange={e => { handleFrequencyHoursChange(parseInt(e.target.value)); onClearError?.('frequencyHours'); }}
              className={inp(!!errors.frequencyHours)}
            >
              <option value="">—</option>
              <option value="4">4 hrs (6x/día)</option>
              <option value="6">6 hrs (4x/día)</option>
              <option value="8">8 hrs (3x/día)</option>
              <option value="12">12 hrs (2x/día)</option>
              <option value="24">24 hrs (1x/día)</option>
            </select>
            {errors.frequencyHours && <p className="text-xs text-red-600 mt-0.5">{errors.frequencyHours}</p>}
          </div>

          {/* Duración */}
          <div className="col-span-1">
            <label className="block text-xs font-medium text-gray-700 mb-1">Días *</label>
            <input
              type="number"
              value={med.durationDays}
              min={1}
              onChange={e => handleDurationChange(parseInt(e.target.value) || 1)}
              className={inp()}
            />
          </div>

          {/* Botón eliminar */}
          <div className="col-span-1 flex justify-center pt-6">
            <button type="button" onClick={() => onRemove(index)}
              className="p-1 text-red-400 hover:text-red-600 transition-colors">
              <TrashIcon className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Segunda fila: Vía e Instrucciones */}
        <div className="grid grid-cols-12 gap-2 mb-2">
          <div className="col-span-2">
            <label className="block text-xs font-medium text-gray-700 mb-1">Vía *</label>
            <select value={med.route} onChange={e => { onChange(index, 'route', e.target.value); onClearError?.('route'); }} className={inp(!!errors.route)}>
              <option value="">—</option>
              <option value="Oral">Oral</option>
              <option value="Sublingual">Sublingual</option>
              <option value="Tópica">Tópica</option>
              <option value="Intravenosa">Intravenosa</option>
              <option value="Intramuscular">Intramuscular</option>
              <option value="Subcutánea">Subcutánea</option>
              <option value="Inhalatoria">Inhalatoria</option>
              <option value="Oftálmica">Oftálmica</option>
              <option value="Ótica">Ótica</option>
              <option value="Nasal">Nasal</option>
              <option value="Rectal">Rectal</option>
            </select>
            {errors.route && <p className="text-xs text-red-600 mt-0.5">{errors.route}</p>}
          </div>
          <div className="col-span-10">
            <label className="block text-xs font-medium text-gray-700 mb-1">Instrucciones especiales</label>
            <input
              value={med.specialInstructions || ''}
              onChange={e => onChange(index, 'specialInstructions', e.target.value)}
              placeholder="Ej: Tomar con alimentos, evitar alcohol, etc."
              className={inp()}
            />
          </div>
        </div>

        {/* Resumen de cálculos */}
        {totalQuantity > 0 && (
          <div className="bg-medin-navy/5 rounded-md p-2 border border-medin-navy/20">
            <div className="flex items-center justify-between text-xs">
              <div className="flex items-center gap-4">
                <span className="text-gray-600">
                  📦 <strong>Cantidad total:</strong> {totalQuantity} {med.dosageUnit || 'unidad(es)'}
                </span>
                {unitPrice > 0 && (
                  <span className="text-gray-600">
                    💰 <strong>Costo total:</strong> Q {totalCost.toFixed(2)}
                  </span>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    );
  } else {
    // Modo Receta Externa - Campos de texto libre
    return (
      <div className="border border-gray-200 rounded-lg p-3 bg-amber-50/30">
        <div className="grid grid-cols-12 gap-2 items-start mb-2">
          {/* Medicamento - texto libre */}
          <div className="col-span-4">
            <label className="block text-xs font-medium text-gray-700 mb-1">Medicamento *</label>
            <input
              value={med.name}
              onChange={e => { onChange(index, 'name', e.target.value); onClearError?.('name'); }}
              placeholder="Ej: Amoxicilina 500mg"
              className={inp(!!errors.name)}
            />
            {errors.name && <p className="text-xs text-red-600 mt-0.5">{errors.name}</p>}
          </div>

          {/* Dosis - texto libre */}
          <div className="col-span-3">
            <label className="block text-xs font-medium text-gray-700 mb-1">Dosis *</label>
            <input
              value={med.dosage}
              onChange={e => { onChange(index, 'dosage', e.target.value); onClearError?.('dosage'); }}
              placeholder="Ej: 1 cápsula"
              className={inp(!!errors.dosage)}
            />
            {errors.dosage && <p className="text-xs text-red-600 mt-0.5">{errors.dosage}</p>}
          </div>

          {/* Frecuencia - texto libre */}
          <div className="col-span-3">
            <label className="block text-xs font-medium text-gray-700 mb-1">Frecuencia *</label>
            <input
              value={med.frequency}
              onChange={e => { onChange(index, 'frequency', e.target.value); onClearError?.('frequency'); }}
              placeholder="Ej: Cada 8 horas"
              className={inp(!!errors.frequency)}
            />
            {errors.frequency && <p className="text-xs text-red-600 mt-0.5">{errors.frequency}</p>}
          </div>

          {/* Duración */}
          <div className="col-span-1">
            <label className="block text-xs font-medium text-gray-700 mb-1">Días *</label>
            <input
              type="number"
              value={med.durationDays}
              min={1}
              onChange={e => onChange(index, 'durationDays', parseInt(e.target.value) || 1)}
              className={inp()}
            />
          </div>

          {/* Botón eliminar */}
          <div className="col-span-1 flex justify-center pt-6">
            <button type="button" onClick={() => onRemove(index)}
              className="p-1 text-red-400 hover:text-red-600 transition-colors">
              <TrashIcon className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Segunda fila: Vía e Instrucciones */}
        <div className="grid grid-cols-12 gap-2">
          <div className="col-span-2">
            <label className="block text-xs font-medium text-gray-700 mb-1">Vía *</label>
            <select value={med.route} onChange={e => { onChange(index, 'route', e.target.value); onClearError?.('route'); }} className={inp(!!errors.route)}>
              <option value="">—</option>
              <option value="Oral">Oral</option>
              <option value="Sublingual">Sublingual</option>
              <option value="Tópica">Tópica</option>
              <option value="Intravenosa">Intravenosa</option>
              <option value="Intramuscular">Intramuscular</option>
              <option value="Subcutánea">Subcutánea</option>
              <option value="Inhalatoria">Inhalatoria</option>
              <option value="Oftálmica">Oftálmica</option>
              <option value="Ótica">Ótica</option>
              <option value="Nasal">Nasal</option>
              <option value="Rectal">Rectal</option>
            </select>
            {errors.route && <p className="text-xs text-red-600 mt-0.5">{errors.route}</p>}
          </div>
          <div className="col-span-10">
            <label className="block text-xs font-medium text-gray-700 mb-1">Instrucciones especiales</label>
            <input
              value={med.specialInstructions || ''}
              onChange={e => onChange(index, 'specialInstructions', e.target.value)}
              placeholder="Ej: Tomar con alimentos, evitar alcohol, etc."
              className={inp()}
            />
          </div>
        </div>
      </div>
    );
  }
};

// ─── Main component ───────────────────────────────────────────────────────────

const PatientConsultationForm: FC = () => {
  const { appointmentId } = useParams<{ appointmentId: string }>();
  const navigate = useNavigate();
  const { setPatient } = usePatientHistory();

  const [appointment, setAppointment] = useState<AppointmentListItem | null>(null);
  const [vitalSigns, setVitalSigns] = useState<VitalSignsResponse | null>(null);
  const [triage, setTriage] = useState<TriageResponse | null>(null);
  const [labCatalog, setLabCatalog] = useState<ServiceItemResponse[]>([]);
  const [medCatalog, setMedCatalog] = useState<ServiceItemResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Doctor data for follow-up scheduling
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [daysOffMap, setDaysOffMap] = useState<Record<string, string[]>>({});
  const [loadingDoctorData, setLoadingDoctorData] = useState(false);

  // Bloque 2 — Evaluación clínica
  const [chiefComplaint, setChiefComplaint] = useState('');
  const [symptomsText, setSymptomsText] = useState('');
  const [primaryDiagnosis, setPrimaryDiagnosis] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FormErrors<ConsultationForm>>({});
  const [secondaryDiagnosesText, setSecondaryDiagnosesText] = useState('');
  const [medicalNotes, setMedicalNotes] = useState('');
  const [treatmentPlan, setTreatmentPlan] = useState('');

  // Bloque 3 — Decisiones de cierre
  const [destination, setDestination] = useState<Destination | null>(null);
  const [selectedTests, setSelectedTests] = useState<string[]>([]);
  const [medications, setMedications] = useState<MedicationItem[]>([]);
  const [medErrors, setMedErrors] = useState<Record<string, string>[]>([]);
  const [followUp, setFollowUp] = useState(false);
  const [followUpDate, setFollowUpDate] = useState('');
  const [followUpTime, setFollowUpTime] = useState('');
  const [followUpNotes, setFollowUpNotes] = useState('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(false);

  useEffect(() => {
    if (!appointmentId) { setError('ID de cita no proporcionado'); setLoading(false); return; }
    const load = async () => {
      try {
        const [appt, vitals, tri, labItems, medItems, existingConsult] = await Promise.allSettled([
          getAppointmentById(appointmentId),
          getVitalSignsByAppointment(appointmentId),
          getAppointmentTriage(appointmentId),
          getServiceItems('LABORATORY'),
          getServiceItems('MEDICATION'),
          getConsultationByAppointment(appointmentId),
        ]);
        if (appt.status === 'fulfilled') {
          setAppointment(appt.value);
          setPatient(appt.value.patient.id, appt.value.patient.fullName);
        } else setError('Error al cargar la cita');
        if (vitals.status === 'fulfilled') setVitalSigns(vitals.value);
        if (tri.status === 'fulfilled') setTriage(tri.value);
        if (labItems.status === 'fulfilled') setLabCatalog(labItems.value.filter(t => t.status === 'ACTIVE'));
        if (medItems.status === 'fulfilled') setMedCatalog(medItems.value.filter(m => m.status === 'ACTIVE'));
        if (existingConsult.status === 'fulfilled' && existingConsult.value) {
          const c = existingConsult.value;
          if (c.chiefComplaint) setChiefComplaint(c.chiefComplaint);
          if (c.symptoms) setSymptomsText(c.symptoms);
          if (c.primaryDiagnosis) setPrimaryDiagnosis(c.primaryDiagnosis);
          if (c.secondaryDiagnoses?.length) setSecondaryDiagnosesText(c.secondaryDiagnoses.join(', '));
          if (c.medicalNotes) setMedicalNotes(c.medicalNotes);
          if (c.treatmentPlan) setTreatmentPlan(c.treatmentPlan);
        }
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [appointmentId]);

  // Load doctor data for follow-up scheduling
  useEffect(() => {
    const loadDoctorData = async () => {
      setLoadingDoctorData(true);
      try {
        const docs = await listActiveDoctors();
        setDoctors(docs);
        const offResults = await Promise.all(docs.map(d => getDoctorDaysOff(d.id)));
        const map: Record<string, string[]> = {};
        docs.forEach((d, i) => {
          map[d.id] = offResults[i].map((o: DayOff) => o.date.substring(0, 10));
        });
        setDaysOffMap(map);
      } catch {
        // silently ignore — form will still render, just without blocking
      } finally {
        setLoadingDoctorData(false);
      }
    };
    loadDoctorData();
  }, []);

  // Calendar blocking logic: block dates when ALL doctors are on day-off
  const isBlocked = useMemo(() => (dateStr: string): boolean => {
    if (!doctors.length) return false;
    return doctors.every(d => (daysOffMap[d.id] ?? []).includes(dateStr));
  }, [doctors, daysOffMap]);

  // Generate time slots for a date (doctors not on day-off that day)
  const shiftSlotsForDate = useMemo(() => (dateStr: string): string[] => {
    const available = doctors.filter(d => !(daysOffMap[d.id] ?? []).includes(dateStr));
    const set = new Set<string>();
    available.forEach(d => shiftSlots(d.shiftStart, d.shiftEnd).forEach(s => set.add(s)));
    return Array.from(set).sort();
  }, [doctors, daysOffMap]);

  // Filter past time slots when today is selected
  const displaySlots = useMemo(() => {
    if (!followUpDate) return [];
    const slots = shiftSlotsForDate(followUpDate);
    
    // If selected date is today, filter out past time slots
    const today = new Date();
    const selectedDate = new Date(followUpDate + 'T00:00:00');
    const isToday = 
      selectedDate.getFullYear() === today.getFullYear() &&
      selectedDate.getMonth() === today.getMonth() &&
      selectedDate.getDate() === today.getDate();
    
    if (!isToday) return slots;
    
    // Filter out slots that have already passed
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();
    const currentTimeInMinutes = currentHour * 60 + currentMinute;
    
    return slots.filter(slot => {
      const [slotHour, slotMinute] = slot.split(':').map(Number);
      const slotTimeInMinutes = slotHour * 60 + slotMinute;
      // Keep slots that are at least 30 minutes in the future
      return slotTimeInMinutes >= currentTimeInMinutes + 30;
    });
  }, [followUpDate, shiftSlotsForDate]);

  // Load available slots when follow-up date changes
  useEffect(() => {
    if (!followUpDate) {
      setAvailableSlots([]);
      setFollowUpTime('');
      return;
    }
    
    setFollowUpTime(''); // Clear time selection when date changes
    setLoadingSlots(true);
    
    getAvailableSlotsForDate(followUpDate)
      .then(slots => setAvailableSlots(slots))
      .catch(() => setAvailableSlots([]))
      .finally(() => setLoadingSlots(false));
  }, [followUpDate]);

  const addMedication = () => {
    const isInternal = destination === 'PHARMACY';
    if (isInternal) {
      // Farmacia interna - con campos de cálculo
      // Calcular totalQuantity inicial: dosageAmount × (24 / frequencyHours) × durationDays
      const initialDosageAmount = 1;
      const initialFrequencyHours = 8;
      const initialDurationDays = 7;
      const initialTotalQuantity = initialDosageAmount * Math.floor(24 / initialFrequencyHours) * initialDurationDays;
      
      setMedications(prev => [...prev, { 
        name: '', 
        dosage: '', 
        frequency: '', 
        durationDays: initialDurationDays, 
        route: 'Oral', 
        specialInstructions: '',
        dosageAmount: initialDosageAmount,
        dosageUnit: 'pastilla(s)',
        frequencyHours: initialFrequencyHours,
        totalQuantity: initialTotalQuantity  // 1 × 3 × 7 = 21
      }]);
    } else {
      // Receta externa - campos de texto libre
      setMedications(prev => [...prev, { 
        name: '', 
        dosage: '', 
        frequency: '', 
        durationDays: 7, 
        route: 'Oral', 
        specialInstructions: '' 
      }]);
    }
  };

  const updateMedication = (index: number, field: keyof MedicationItem, value: string | number) => {
    setMedications(prev => prev.map((m, i) => i === index ? { ...m, [field]: value } : m));
  };

  const clearMedError = (index: number, field: string) => {
    setMedErrors(prev => {
      const next = [...prev];
      if (next[index]) {
        next[index] = { ...next[index] };
        delete next[index][field];
      }
      return next;
    });
  };

  const removeMedication = (index: number) => {
    setMedications(prev => prev.filter((_, i) => i !== index));
    setMedErrors(prev => prev.filter((_, i) => i !== index));
  };

  const toggleTest = (test: string) => {
    setSelectedTests(prev => prev.includes(test) ? prev.filter(t => t !== test) : [...prev, test]);
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
    if (requiresMedications && medications.length > 0) {
      const isInternal = destination === 'PHARMACY';
      const newMedErrors: Record<string, string>[] = medications.map(med => {
        const errs: Record<string, string> = {};
        if (!med.name) errs.name = 'Este campo es requerido.';
        if (isInternal) {
          if (!med.dosageAmount) errs.dosageAmount = 'Este campo es requerido.';
          if (!med.dosageUnit) errs.dosageUnit = 'Este campo es requerido.';
          if (!med.frequencyHours) errs.frequencyHours = 'Este campo es requerido.';
        } else {
          if (!med.dosage) errs.dosage = 'Este campo es requerido.';
          if (!med.frequency) errs.frequency = 'Este campo es requerido.';
        }
        if (!med.route) errs.route = 'Este campo es requerido.';
        return errs;
      });
      const hasMedErrors = newMedErrors.some(e => Object.keys(e).length > 0);
      if (hasMedErrors) { setMedErrors(newMedErrors); return; }
      setMedErrors([]);
    }
    if (followUp && !followUpDate) {
      setError('Selecciona la fecha de la cita de seguimiento.');
      return;
    }
    if (followUp && !followUpTime) {
      setError('Selecciona la hora de la cita de seguimiento.');
      return;
    }

    const consultationSchema: Schema<ConsultationForm> = {
      chiefComplaint:   [{ type: 'required', message: 'El motivo de consulta es requerido.' }],
      symptomsText:     [{ type: 'required', message: 'Los síntomas son requeridos.' }],
      primaryDiagnosis: requiresDiagnosis
        ? [{ type: 'required', message: 'El diagnóstico principal es requerido.' }]
        : [],
    };
    const errs = validateForm(consultationSchema, { chiefComplaint, symptomsText, primaryDiagnosis });
    if (Object.keys(errs).length > 0) { setFieldErrors(errs); return; }
    setFieldErrors({});

    setSaving(true);
    setError(null);

    try {
      // Extrae solo el código CIE-10 del formato "G43.0 — Migraña sin aura"
      const cieCode = primaryDiagnosis.includes(' — ')
        ? primaryDiagnosis.split(' — ')[0].trim()
        : primaryDiagnosis.trim();

      const hasLabOrders = destination === 'LAB';
      // Solo farmacia interna activa el flujo de pago — receta externa cierra en COMPLETED
      const hasPrescription = destination === 'PHARMACY';

      const labCharges: ServiceCharge[] = hasLabOrders
        ? labCatalog
            .filter(t => selectedTests.includes(t.name))
            .map(t => ({ name: t.name, price: t.price }))
        : [];

      const pharmacyCharges: ServiceCharge[] = hasPrescription && destination === 'PHARMACY'
        ? medications
            .map(med => {
              const catalogItem = medCatalog.find(m => m.id === med.name); // med.name contiene el ID
              if (!catalogItem) return null;
              
              // Calcular precio total = precio unitario × cantidad total
              const totalQuantity = med.totalQuantity || 0;
              const unitPrice = Number(catalogItem.price);
              const totalPrice = unitPrice * totalQuantity;
              
              // Crear descripción detallada: "Amoxicilina 500mg (21 pastillas × Q1.50)"
              const description = `${catalogItem.name} (${totalQuantity} ${med.dosageUnit || 'unidad(es)'} × Q${unitPrice.toFixed(2)})`;
              
              return {
                name: description,
                price: totalPrice
              };
            })
            .filter((charge): charge is ServiceCharge => charge !== null)
        : [];

      // 1. Registrar consulta
      const consultation = await registerConsultation({
        patientId: appointment.patient.id,
        appointmentId: appointment.id,
        chiefComplaint,
        symptoms: symptomsText,
        primaryDiagnosis: cieCode || '',
        secondaryDiagnoses: secondaryDiagnosesText
          ? secondaryDiagnosesText.split(',').map(s => s.trim()).filter(Boolean)
          : [],
        medicalNotes,
        treatmentPlan,
        hasLabOrders,
        hasPrescription,
        labCharges,
        pharmacyCharges,
        followUpDate: followUp ? followUpDate : undefined,
        followUpTime: followUp ? followUpTime : undefined,
      });

      // 2. Generar orden de lab si aplica
      if (requiresLabTests && selectedTests.length > 0) {
        await generateLabOrder({
          consultationId: consultation.id,
          patientId: appointment.patient.id,
          appointmentId: appointment.id,
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
  const inputCls = (hasError: boolean) =>
    `w-full px-3 py-2 border ${hasError ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm`;
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
                  {manchesterMeta?.label ?? `${triage.priorityLevel} — ${triage.priorityDescription}`}
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

        {/* ── Lab Results Section ── */}
        {appointment && <LabResultsSection appointmentId={appointment.id} />}

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
                    onChange={e => { setChiefComplaint(e.target.value); setFieldErrors(prev => clearFieldError(prev, 'chiefComplaint')); }}
                    className={inputCls(!!fieldErrors.chiefComplaint)}
                    placeholder="Ej: Dolor de cabeza desde hace 3 días"
                  />
                  {fieldErrors.chiefComplaint && <p className="text-xs text-red-600">{fieldErrors.chiefComplaint}</p>}
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                    Síntomas <span className="text-red-500">*</span>
                    <span className="ml-1 normal-case font-normal text-gray-400">(separados por coma)</span>
                  </label>
                  <input
                    value={symptomsText}
                    onChange={e => { setSymptomsText(e.target.value); setFieldErrors(prev => clearFieldError(prev, 'symptomsText')); }}
                    className={inputCls(!!fieldErrors.symptomsText)}
                    placeholder="cefalea, náuseas, fotofobia"
                  />
                  {fieldErrors.symptomsText && <p className="text-xs text-red-600">{fieldErrors.symptomsText}</p>}
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
                <Cie10Input
                  value={primaryDiagnosis}
                  onChange={val => { setPrimaryDiagnosis(val); setFieldErrors(prev => clearFieldError(prev, 'primaryDiagnosis')); }}
                  className={inputCls(!!fieldErrors.primaryDiagnosis)}
                />
                {fieldErrors.primaryDiagnosis && <p className="text-xs text-red-600">{fieldErrors.primaryDiagnosis}</p>}
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
                  {labCatalog.length === 0 ? (
                    <p className="text-sm text-gray-400 italic col-span-2">No hay exámenes disponibles. Agrega exámenes en Servicios y Precios.</p>
                  ) : labCatalog.map(test => (
                    <label key={test.id} className="flex items-center gap-2 text-sm cursor-pointer">
                      <input
                        type="checkbox"
                        checked={selectedTests.includes(test.name)}
                        onChange={() => toggleTest(test.name)}
                        className="accent-medin-cyan"
                      />
                      <span className="flex-1">{test.name}</span>
                      <span className="text-xs text-medin-navy font-semibold shrink-0">Q {Number(test.price).toFixed(2)}</span>
                    </label>
                  ))}
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
                      <MedRow
                        key={i}
                        med={med}
                        index={i}
                        catalog={medCatalog}
                        onChange={updateMedication}
                        onRemove={removeMedication}
                        isInternal={destination === 'PHARMACY'}
                        errors={medErrors[i] ?? {}}
                        onClearError={(field) => clearMedError(i, field)}
                      />
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
                  <div className="mt-4 space-y-4">
                    {/* Calendar */}
                    <div>
                      <label className={labelClass}>Fecha de seguimiento <span className="text-red-500">*</span></label>
                      {loadingDoctorData ? (
                        <p className="text-sm text-gray-400">Cargando disponibilidad...</p>
                      ) : (
                        <>
                          <Calendar 
                            selected={followUpDate} 
                            onSelect={setFollowUpDate} 
                            isBlocked={isBlocked} 
                          />
                          {followUpDate && (
                            <p className="text-medin-cyan text-xs mt-2">
                              Fecha seleccionada: {followUpDate}
                            </p>
                          )}
                        </>
                      )}
                    </div>

                    {/* Time slot selection */}
                    {followUpDate && (
                      <div>
                        <label className={labelClass}>Hora de la cita <span className="text-red-500">*</span></label>
                        {loadingSlots ? (
                          <p className="text-sm text-gray-400">Verificando disponibilidad...</p>
                        ) : displaySlots.length === 0 ? (
                          <p className="text-sm text-amber-600">No hay horarios disponibles para esta fecha.</p>
                        ) : (
                          <div className="grid grid-cols-3 gap-2">
                            {displaySlots.map(slot => {
                              const isAvailable = availableSlots.includes(slot) || followUpTime === slot;
                              const isSelected = followUpTime === slot;
                              return (
                                <button
                                  key={slot}
                                  type="button"
                                  disabled={!isAvailable}
                                  onClick={() => setFollowUpTime(slot)}
                                  className={`py-2 text-sm font-medium border transition-colors ${
                                    isSelected
                                      ? 'bg-medin-cyan text-medin-navy border-medin-cyan'
                                      : isAvailable
                                        ? 'bg-medin-navy text-white border-gray-600 hover:border-medin-cyan'
                                        : 'bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed line-through'
                                  }`}
                                >
                                  {fmt(slot)}
                                </button>
                              );
                            })}
                          </div>
                        )}
                      </div>
                    )}

                    {/* Notes */}
                    <div>
                      <label className={labelClass}>Notas para la próxima cita</label>
                      <input 
                        value={followUpNotes} 
                        onChange={e => setFollowUpNotes(e.target.value)} 
                        className={inputClass} 
                        placeholder="Ej: Traer resultados de lab" 
                      />
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
