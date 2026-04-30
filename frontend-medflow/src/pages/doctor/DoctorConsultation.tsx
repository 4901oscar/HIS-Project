import { useState, useEffect, useCallback, useRef } from 'react';
import type { FC, FormEvent } from 'react';
import { MainLayout } from '../../components/Layout';
import { MagnifyingGlassIcon, PlusIcon, TrashIcon } from '@heroicons/react/24/outline';
import { listAppointments } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';

import { searchPatients } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import {
  registerConsultation,
  generatePrescription,
  generateLabOrder,
} from '../../services/clinicalService';
import type { ConsultationResponse, PrescriptionResponse, LabOrderResponse, MedicationItem } from '../../services/clinicalService';
import axios from 'axios';

type Step = 'patient' | 'consultation' | 'orders';

const AUTO_REFRESH_INTERVAL_MS = 30_000;
const REFRESH_DEBOUNCE_MS = 500;

const DoctorConsultation: FC = () => {
  const [step, setStep] = useState<Step>('patient');

  // ── Mis citas asignadas ───────────────────────────────────────────────────
  const [myAppointments, setMyAppointments] = useState<AppointmentListItem[]>([]);
  const [apptLoading, setApptLoading] = useState(true);
  const refreshDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (refreshDebounceRef.current) clearTimeout(refreshDebounceRef.current);
    };
  }, []);

  const fetchAppointments = useCallback(async (showLoading = true) => {
    if (showLoading) setApptLoading(true);
    try {
      // Fetch appointments with status CONSULTATION and include clinical data
      const data = await listAppointments({ 
        status: ['CONSULTATION'],
        includeClinical: true 
      });
      setMyAppointments(data);
    } catch (err) {
      console.error('Error loading appointments:', err);
    } finally {
      if (showLoading) setApptLoading(false);
    }
  }, []);

  const handleManualRefresh = useCallback(() => {
    if (refreshDebounceRef.current) clearTimeout(refreshDebounceRef.current);
    refreshDebounceRef.current = setTimeout(() => fetchAppointments(true), REFRESH_DEBOUNCE_MS);
  }, [fetchAppointments]);

  useEffect(() => {
    fetchAppointments(true);
    const interval = setInterval(() => fetchAppointments(false), AUTO_REFRESH_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [fetchAppointments]);

  // ── Búsqueda de paciente ──────────────────────────────────────────────────
  const [searchQuery, setSearchQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [searchResults, setSearchResults] = useState<PatientResponse[]>([]);
  const [patient, setPatient] = useState<PatientResponse | null>(null);

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;
    setSearching(true);
    setSearchResults([]);
    try {
      const results = await searchPatients(searchQuery.trim());
      setSearchResults(results);
    } catch {
      setError('Error al buscar paciente');
    } finally {
      setSearching(false);
    }
  };

  // ── Consulta ─────────────────────────────────────────────────────────────
  const [chiefComplaint, setChiefComplaint] = useState('');
  const [symptomsText, setSymptomsText] = useState('');
  const [primaryDiagnosis, setPrimaryDiagnosis] = useState('');
  const [secondaryDiagnosesText, setSecondaryDiagnosesText] = useState('');
  const [medicalNotes, setMedicalNotes] = useState('');
  const [treatmentPlan, setTreatmentPlan] = useState('');
  const [savingConsult, setSavingConsult] = useState(false);
  const [consultation, setConsultation] = useState<ConsultationResponse | null>(null);

  // ── Receta ────────────────────────────────────────────────────────────────
  const [medications, setMedications] = useState<MedicationItem[]>([]);
  const [newMed, setNewMed] = useState<MedicationItem>({
    name: '', dosage: '', frequency: '', durationDays: 1, route: '', specialInstructions: '',
  });
  const [savingRx, setSavingRx] = useState(false);
  const [prescription, setPrescription] = useState<PrescriptionResponse | null>(null);

  // ── Órdenes de laboratorio ────────────────────────────────────────────────
  const [testInput, setTestInput] = useState('');
  const [testNames, setTestNames] = useState<string[]>([]);
  const [savingLab, setSavingLab] = useState(false);
  const [labOrder, setLabOrder] = useState<LabOrderResponse | null>(null);

  const [error, setError] = useState<string | null>(null);

  const handleRegisterConsultation = async (e: FormEvent) => {
    e.preventDefault();
    if (!patient) return;
    setSavingConsult(true);
    setError(null);
    try {
      const res = await registerConsultation({
        patientId: patient.id,
        chiefComplaint,
        symptoms: symptomsText.split(',').map((s) => s.trim()).filter(Boolean),
        primaryDiagnosis,
        secondaryDiagnoses: secondaryDiagnosesText ? secondaryDiagnosesText.split(',').map((s) => s.trim()).filter(Boolean) : [],
        medicalNotes,
        treatmentPlan,
      });
      setConsultation(res);
      setStep('orders');
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al registrar consulta');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setSavingConsult(false);
    }
  };

  const addMedication = () => {
    if (!newMed.name.trim()) return;
    setMedications((prev) => [...prev, { ...newMed }]);
    setNewMed({ name: '', dosage: '', frequency: '', durationDays: 1, route: '', specialInstructions: '' });
  };

  const removeMedication = (idx: number) => {
    setMedications((prev) => prev.filter((_, i) => i !== idx));
  };

  const handleGeneratePrescription = async () => {
    if (!consultation || medications.length === 0) return;
    setSavingRx(true);
    setError(null);
    try {
      const res = await generatePrescription({
        consultationId: consultation.id,
        patientId: consultation.patientId,
        medications,
      });
      setPrescription(res);
      setMedications([]);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al generar receta');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setSavingRx(false);
    }
  };

  const addTest = () => {
    if (!testInput.trim()) return;
    setTestNames((prev) => [...prev, testInput.trim()]);
    setTestInput('');
  };

  const removeTest = (idx: number) => {
    setTestNames((prev) => prev.filter((_, i) => i !== idx));
  };

  const handleGenerateLabOrder = async () => {
    if (!consultation || testNames.length === 0) return;
    setSavingLab(true);
    setError(null);
    try {
      const res = await generateLabOrder({
        consultationId: consultation.id,
        patientId: consultation.patientId,
        testNames,
      });
      setLabOrder(res);
      setTestNames([]);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al generar orden de laboratorio');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setSavingLab(false);
    }
  };

  const inputClass = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Consulta Médica</h2>
          <p className="mt-1 text-sm text-gray-600">Registro de consultas, recetas y órdenes de laboratorio</p>
        </div>

        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>
        )}

        {/* ── Mis citas ── */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">Mis Citas Asignadas ({myAppointments.length})</h3>
            <button
              onClick={handleManualRefresh}
              disabled={apptLoading}
              className="text-sm text-medin-navy hover:text-medin-navy/80 transition-colors disabled:opacity-50"
            >
              {apptLoading ? (
                <span className="flex items-center gap-1">
                  <span className="inline-block h-3 w-3 animate-spin rounded-full border-2 border-medin-cyan border-t-transparent" />
                  Actualizando...
                </span>
              ) : 'Actualizar'}
            </button>
          </div>
          
          {apptLoading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
            </div>
          ) : myAppointments.length === 0 ? (
            <div className="text-center py-16 text-gray-500">
              <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <p className="text-lg font-medium">No hay pacientes en espera</p>
              <p className="text-sm text-gray-400 mt-1">Todos los pacientes han sido atendidos</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
                    <th className="pb-2 pr-4 pl-6">Fecha</th>
                    <th className="pb-2 pr-4">Hora</th>
                    <th className="pb-2 pr-4">Paciente</th>
                    <th className="pb-2 pr-4">DPI</th>
                    <th className="pb-2 pr-4">Estado</th>
                    <th className="pb-2 pr-4">Prioridad</th>
                    <th className="pb-2 pr-4">Triaje</th>
                    <th className="pb-2 pr-4">Motivo</th>
                    <th className="pb-2 pr-6">Acción</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {myAppointments
                    .slice()
                    .sort((a, b) => {
                      // Sort by Manchester priority first (RED > ORANGE > YELLOW > GREEN > BLUE)
                      const priorityOrder = { RED: 0, ORANGE: 1, YELLOW: 2, GREEN: 3, BLUE: 4 };
                      const aPriority = a.clinical?.manchesterLevel ? priorityOrder[a.clinical.manchesterLevel as keyof typeof priorityOrder] ?? 999 : 999;
                      const bPriority = b.clinical?.manchesterLevel ? priorityOrder[b.clinical.manchesterLevel as keyof typeof priorityOrder] ?? 999 : 999;
                      if (aPriority !== bPriority) return aPriority - bPriority;
                      
                      // Then by date and time
                      const d = a.appointmentDate.toString().localeCompare(b.appointmentDate.toString());
                      if (d !== 0) return d;
                      return a.appointmentTime.toString().localeCompare(b.appointmentTime.toString());
                    })
                    .map((appt) => (
                      <tr key={appt.id} className="hover:bg-gray-50">
                        <td className="py-3 pr-4 pl-6 whitespace-nowrap text-xs">
                          {new Date(appt.appointmentDate + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: '2-digit', year: 'numeric' })}
                        </td>
                        <td className="py-3 pr-4 whitespace-nowrap font-medium">
                          {appt.appointmentTime.toString().substring(0, 5)}
                        </td>
                        <td className="py-3 pr-4">{appt.patient.fullName}</td>
                        <td className="py-3 pr-4 font-mono text-xs">{appt.patient.dpi ?? '—'}</td>
                        <td className="py-3 pr-4">
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                            appt.statusColor === 'green' ? 'bg-green-100 text-green-800' :
                            appt.statusColor === 'orange' ? 'bg-orange-100 text-orange-800' :
                            appt.statusColor === 'blue' ? 'bg-blue-100 text-blue-800' :
                            'bg-gray-100 text-gray-600'
                          }`}>
                            {appt.statusLabel}
                          </span>
                        </td>
                        <td className="py-3 pr-4">
                          {appt.clinical?.manchesterLevel ? (
                            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                              appt.clinical.manchesterLevel === 'RED' ? 'bg-red-100 text-red-800' :
                              appt.clinical.manchesterLevel === 'ORANGE' ? 'bg-orange-100 text-orange-800' :
                              appt.clinical.manchesterLevel === 'YELLOW' ? 'bg-yellow-100 text-yellow-800' :
                              appt.clinical.manchesterLevel === 'GREEN' ? 'bg-green-100 text-green-800' :
                              appt.clinical.manchesterLevel === 'BLUE' ? 'bg-blue-100 text-blue-800' :
                              'bg-gray-100 text-gray-600'
                            }`}>
                              {appt.clinical.manchesterLevel}
                            </span>
                          ) : (
                            <span className="text-gray-400 text-xs">—</span>
                          )}
                        </td>
                        <td className="py-3 pr-4">
                          {appt.clinical?.hasTriage ? (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                              <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                              </svg>
                              Completo
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600">
                              <svg className="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                              </svg>
                              Pendiente
                            </span>
                          )}
                        </td>
                        <td className="py-3 pr-4 max-w-xs truncate text-gray-500 text-xs">
                          {appt.notes ?? '—'}
                        </td>
                        <td className="py-3 pr-6 whitespace-nowrap text-right">
                          <button
                            onClick={() => {
                              // TODO: Implement consultation flow with this appointment
                              console.log('Atender cita:', appt.id);
                            }}
                            className="px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm"
                          >
                            Atender
                          </button>
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* ── Step 1: Seleccionar paciente ── */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Paciente</h3>

          {!patient ? (
            <>
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <MagnifyingGlassIcon className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                  <input
                    type="text"
                    placeholder="Buscar por nombre, DPI o correo..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm"
                  />
                </div>
                <button onClick={handleSearch} disabled={searching} className="px-4 py-2 bg-medin-navy text-white rounded-lg text-sm disabled:opacity-50">
                  {searching ? 'Buscando...' : 'Buscar'}
                </button>
              </div>
              {searchResults.length > 0 && (
                <div className="mt-2 divide-y border border-gray-200 rounded-lg overflow-hidden">
                  {searchResults.map((p) => (
                    <button key={p.id} onClick={() => { setPatient(p); setSearchResults([]); setStep('consultation'); }} className="w-full text-left px-4 py-3 hover:bg-gray-50">
                      <p className="font-medium text-sm text-gray-900">{p.fullName}</p>
                      <p className="text-xs text-gray-500">DPI: {p.dpi}</p>
                    </button>
                  ))}
                </div>
              )}
            </>
          ) : (
            <div className="flex items-start justify-between p-3 bg-medin-cyan/10 border border-medin-cyan/30 rounded-lg">
              <div>
                <p className="font-semibold text-gray-900">{patient.fullName}</p>
                <p className="text-xs text-gray-600">DPI: {patient.dpi} — {patient.email}</p>
              </div>
              {step === 'patient' && (
                <button onClick={() => setPatient(null)} className="text-xs text-gray-400 hover:text-gray-600">Cambiar</button>
              )}
            </div>
          )}
        </div>

        {/* ── Step 2: Consulta ── */}
        {step === 'consultation' && patient && !consultation && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Datos de la Consulta</h3>
            <form onSubmit={handleRegisterConsultation} className="space-y-4">
              <div>
                <label className={labelClass}>Motivo de consulta <span className="text-red-500">*</span></label>
                <input value={chiefComplaint} onChange={(e) => setChiefComplaint(e.target.value)} required className={inputClass} placeholder="Ej: Dolor de cabeza persistente" />
              </div>
              <div>
                <label className={labelClass}>Síntomas <span className="text-xs text-gray-400">(separados por coma)</span> <span className="text-red-500">*</span></label>
                <input value={symptomsText} onChange={(e) => setSymptomsText(e.target.value)} required className={inputClass} placeholder="Ej: cefalea, náuseas, fotofobia" />
              </div>
              <div>
                <label className={labelClass}>Diagnóstico principal <span className="text-red-500">*</span></label>
                <input value={primaryDiagnosis} onChange={(e) => setPrimaryDiagnosis(e.target.value)} required className={inputClass} placeholder="Ej: Migraña sin aura (G43.0)" />
              </div>
              <div>
                <label className={labelClass}>Diagnósticos secundarios <span className="text-xs text-gray-400">(separados por coma)</span></label>
                <input value={secondaryDiagnosesText} onChange={(e) => setSecondaryDiagnosesText(e.target.value)} className={inputClass} />
              </div>
              <div>
                <label className={labelClass}>Notas médicas</label>
                <textarea value={medicalNotes} onChange={(e) => setMedicalNotes(e.target.value)} rows={3} className={inputClass} />
              </div>
              <div>
                <label className={labelClass}>Plan de tratamiento</label>
                <textarea value={treatmentPlan} onChange={(e) => setTreatmentPlan(e.target.value)} rows={3} className={inputClass} />
              </div>
              <div className="flex justify-end">
                <button type="submit" disabled={savingConsult} className="px-6 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50">
                  {savingConsult ? 'Registrando...' : 'Registrar Consulta'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* ── Step 3: Órdenes post-consulta ── */}
        {step === 'orders' && consultation && (
          <>
            {/* Consulta registrada */}
            <div className="bg-green-50 border border-green-200 rounded-lg p-4">
              <p className="font-semibold text-green-800">Consulta registrada</p>
              <p className="text-sm text-green-700">Dx: {consultation.primaryDiagnosis}</p>
              <p className="text-xs text-gray-500">ID: {consultation.id}</p>
            </div>

            {/* Receta médica */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Receta Médica</h3>

              {prescription && (
                <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg text-sm">
                  <p className="font-medium text-green-800">Receta generada: <span className="font-mono">{prescription.prescriptionCode}</span></p>
                </div>
              )}

              {/* Agregar medicamento */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 mb-3">
                <div>
                  <label className={labelClass}>Medicamento</label>
                  <input value={newMed.name} onChange={(e) => setNewMed(p => ({ ...p, name: e.target.value }))} className={inputClass} placeholder="Ej: Ibuprofeno" />
                </div>
                <div>
                  <label className={labelClass}>Dosis</label>
                  <input value={newMed.dosage} onChange={(e) => setNewMed(p => ({ ...p, dosage: e.target.value }))} className={inputClass} placeholder="Ej: 400mg" />
                </div>
                <div>
                  <label className={labelClass}>Frecuencia</label>
                  <input value={newMed.frequency} onChange={(e) => setNewMed(p => ({ ...p, frequency: e.target.value }))} className={inputClass} placeholder="Ej: cada 8 horas" />
                </div>
                <div>
                  <label className={labelClass}>Días</label>
                  <input type="number" value={newMed.durationDays} onChange={(e) => setNewMed(p => ({ ...p, durationDays: Number(e.target.value) }))} min={1} className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Vía</label>
                  <input value={newMed.route} onChange={(e) => setNewMed(p => ({ ...p, route: e.target.value }))} className={inputClass} placeholder="Ej: oral" />
                </div>
                <div>
                  <label className={labelClass}>Indicaciones</label>
                  <input value={newMed.specialInstructions} onChange={(e) => setNewMed(p => ({ ...p, specialInstructions: e.target.value }))} className={inputClass} placeholder="Ej: con alimentos" />
                </div>
              </div>

              <button onClick={addMedication} className="flex items-center gap-1 text-sm text-medin-cyan hover:text-medin-blue mb-3">
                <PlusIcon className="h-4 w-4" /> Agregar medicamento
              </button>

              {medications.length > 0 && (
                <div className="mb-3 space-y-1">
                  {medications.map((m, i) => (
                    <div key={i} className="flex items-center justify-between p-2 bg-gray-50 rounded text-sm">
                      <span>{m.name} — {m.dosage}, {m.frequency}, {m.durationDays} días ({m.route})</span>
                      <button onClick={() => removeMedication(i)}><TrashIcon className="h-4 w-4 text-red-400 hover:text-red-600" /></button>
                    </div>
                  ))}
                </div>
              )}

              {medications.length > 0 && (
                <div className="flex justify-end">
                  <button onClick={handleGeneratePrescription} disabled={savingRx} className="px-5 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white disabled:opacity-50">
                    {savingRx ? 'Generando...' : 'Generar Receta'}
                  </button>
                </div>
              )}
            </div>

            {/* Orden de laboratorio */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Orden de Laboratorio</h3>

              {labOrder && (
                <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg text-sm">
                  <p className="font-medium text-green-800">Orden generada: <span className="font-mono">{labOrder.orderCode}</span></p>
                  <p className="text-green-700">Exámenes: {labOrder.testNames.join(', ')}</p>
                </div>
              )}

              <div className="flex gap-2 mb-3">
                <input
                  value={testInput}
                  onChange={(e) => setTestInput(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addTest(); } }}
                  placeholder="Nombre del examen (Enter para agregar)"
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-medin-cyan focus:border-transparent"
                />
                <button onClick={addTest} className="px-3 py-2 bg-medin-navy text-white rounded-lg text-sm">
                  <PlusIcon className="h-4 w-4" />
                </button>
              </div>

              {testNames.length > 0 && (
                <div className="mb-3 flex flex-wrap gap-2">
                  {testNames.map((t, i) => (
                    <span key={i} className="flex items-center gap-1 px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs">
                      {t}
                      <button onClick={() => removeTest(i)}>×</button>
                    </span>
                  ))}
                </div>
              )}

              {testNames.length > 0 && (
                <div className="flex justify-end">
                  <button onClick={handleGenerateLabOrder} disabled={savingLab} className="px-5 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white disabled:opacity-50">
                    {savingLab ? 'Generando...' : 'Generar Orden'}
                  </button>
                </div>
              )}
            </div>

            {/* Nueva consulta */}
            <div className="flex justify-end">
              <button
                onClick={() => {
                  setStep('patient');
                  setPatient(null);
                  setConsultation(null);
                  setPrescription(null);
                  setLabOrder(null);
                  setChiefComplaint('');
                  setSymptomsText('');
                  setPrimaryDiagnosis('');
                  setSecondaryDiagnosesText('');
                  setMedicalNotes('');
                  setTreatmentPlan('');
                  setMedications([]);
                  setTestNames([]);
                  setError(null);
                }}
                className="px-5 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm hover:bg-gray-50"
              >
                Nueva Consulta
              </button>
            </div>
          </>
        )}
      </div>
    </MainLayout>
  );
};

export default DoctorConsultation;
