import { useState, useEffect, useCallback, useRef } from 'react';
import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import { listAppointments } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import { getClinics } from '../../services/clinicService';
import type { Clinic } from '../../types/clinic';
import { listActiveDoctors } from '../../services/doctorService';
import { useAuth } from '../../hooks/useAuth';
import { usePatientHistory } from '../../context/PatientHistoryContext';

const AUTO_REFRESH_INTERVAL_MS = 30_000;
const REFRESH_DEBOUNCE_MS = 500;

const DoctorConsultation: FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { clearPatient } = usePatientHistory();

  useEffect(() => {
    clearPatient();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  const [doctorClinic, setDoctorClinic] = useState<Clinic | null>(null);

  // ── Mis citas asignadas ───────────────────────────────────────────────────
  const [myAppointments, setMyAppointments] = useState<AppointmentListItem[]>([]);
  const [apptLoading, setApptLoading] = useState(true);
  const [search, setSearch] = useState('');
  const refreshDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Load doctor's clinic information
  useEffect(() => {
    const loadDoctorClinic = async () => {
      try {
        if (!user?.id) return;
        
        // Get all doctors and find the one matching current user (doctor.id === user.id)
        const doctors = await listActiveDoctors();
        const currentDoctor = doctors.find(d => d.id === user.id);
        
        if (currentDoctor?.clinicId) {
          // Get clinic information
          const clinics = await getClinics();
          const clinic = clinics.find(c => c.id === currentDoctor.clinicId);
          if (clinic) {
            setDoctorClinic(clinic);
          }
        }
      } catch {
        // Clinic info is non-critical — silently ignore
      }
    };
    loadDoctorClinic();
  }, [user]);

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
    } catch {
      // Appointments error is shown via empty state — no console needed
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

  const MANCHESTER_LABEL: Record<string, string> = {
    RED: 'ROJO', ORANGE: 'NARANJA', YELLOW: 'AMARILLO', GREEN: 'VERDE', BLUE: 'AZUL',
  };

  const filtered = myAppointments
    .filter(a => !search || a.patient.dpi?.includes(search.trim()))
    .slice()
    .sort((a, b) => {
      const priorityOrder = { RED: 0, ORANGE: 1, YELLOW: 2, GREEN: 3, BLUE: 4 };
      const ap = a.clinical?.manchesterLevel ? priorityOrder[a.clinical.manchesterLevel as keyof typeof priorityOrder] ?? 999 : 999;
      const bp = b.clinical?.manchesterLevel ? priorityOrder[b.clinical.manchesterLevel as keyof typeof priorityOrder] ?? 999 : 999;
      if (ap !== bp) return ap - bp;
      const d = a.appointmentDate.toString().localeCompare(b.appointmentDate.toString());
      if (d !== 0) return d;
      return a.appointmentTime.toString().localeCompare(b.appointmentTime.toString());
    });

  const handleAtender = (appt: AppointmentListItem) => {
    // Call patient to clinic
    const clinicName = doctorClinic?.codigo || 'consultorio';
    const utterance = new SpeechSynthesisUtterance(
      `${appt.patient.fullName}, por favor pasar a Clínica ${clinicName}`
    );
    utterance.lang = 'es-GT';
    utterance.rate = 0.9;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);

    // Navigate to consultation form
    navigate(`/doctor/consulta/${appt.id}`);
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Citas Asignadas</h2>
          <p className="mt-1 text-sm text-gray-600">Listado de pacientes en espera de consulta</p>
        </div>

        {/* ── Mis citas ── */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">Citas Asignadas ({filtered.length})</h3>
            <div className="flex items-center gap-3">
              <input
                type="text"
                inputMode="numeric"
                value={search}
                onChange={e => { if (/^\d*$/.test(e.target.value)) setSearch(e.target.value); }}
                placeholder="Buscar por DPI..."
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-medin-cyan focus:border-transparent w-44"
              />
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
                  {filtered.map((appt) => (
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
                              {MANCHESTER_LABEL[appt.clinical.manchesterLevel] ?? appt.clinical.manchesterLevel}
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
                            onClick={() => handleAtender(appt)}
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
      </div>
    </MainLayout>
  );
};

export default DoctorConsultation;
