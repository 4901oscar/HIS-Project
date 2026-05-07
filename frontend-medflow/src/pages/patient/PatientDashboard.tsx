import { useState, useEffect } from 'react';
import type { FC } from 'react';
import axios from 'axios';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { getPatientByDpi, updatePatient } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import { getMedicalHistory } from '../../services/clinicalService';
import type { MedicalHistoryResponse } from '../../services/clinicalService';
import { listMyAppointments } from '../../services/appointmentService';
import type { AppointmentResponse } from '../../services/appointmentService';
import PatientHeader from '../../components/PatientHeader/PatientHeader';

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Agendada',
  ACTIVE: 'Activa',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
};
const STATUS_COLOR: Record<string, string> = {
  SCHEDULED: 'bg-blue-100 text-blue-800',
  ACTIVE: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

const PatientDashboard: FC = () => {
  const { user } = useAuth();
  const location = useLocation();
  const [history, setHistory] = useState<MedicalHistoryResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sinHistorial, setSinHistorial] = useState(false);
  const [activeTab, setActiveTab] = useState<'appointments' | 'consultations' | 'vitals' | 'prescriptions' | 'labs'>('appointments');

  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [apptLoading, setApptLoading] = useState(true);

  // Profile modal state
  const [showProfile, setShowProfile] = useState(
    (location.state as { openProfile?: boolean })?.openProfile === true
  );
  const [patient, setPatient] = useState<PatientResponse | null>(null);
  const [profileForm, setProfileForm] = useState({
    firstName: '', secondName: '', firstLastName: '', secondLastName: '',
    phone: '', address: '', department: '', municipality: '',
  });
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileSuccess, setProfileSuccess] = useState(false);

  useEffect(() => {
    listMyAppointments()
      .then(setAppointments)
      .catch(() => {})
      .finally(() => setApptLoading(false));
  }, []);

  useEffect(() => {
    if (user?.username) {
      loadHistory(user.username);
    }
  }, [user]);

  const loadHistory = async (username: string) => {
    setLoading(true);
    setError(null);
    setSinHistorial(false);
    try {
      const pat = await getPatientByDpi(username);
      setPatient(pat);
      setProfileForm({
        firstName: pat.firstName ?? '',
        secondName: pat.secondName ?? '',
        firstLastName: pat.firstLastName ?? '',
        secondLastName: pat.secondLastName ?? '',
        phone: pat.phone ?? '',
        address: pat.address ?? '',
        department: pat.department ?? '',
        municipality: pat.municipality ?? '',
      });
      const hist = await getMedicalHistory(pat.id);
      setHistory(hist);
    } catch (err) {
      if (axios.isAxiosError(err) && err.response) {
        setSinHistorial(true);
      } else {
        setError('No se pudo conectar con el servidor. Intenta de nuevo más tarde.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleProfileSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!patient) return;
    setProfileSaving(true);
    setProfileError(null);
    setProfileSuccess(false);
    try {
      const updated = await updatePatient(patient.id, profileForm);
      setPatient(updated);
      setProfileSuccess(true);
      setTimeout(() => setProfileSuccess(false), 3000);
    } catch {
      setProfileError('No se pudo guardar. Intenta de nuevo.');
    } finally {
      setProfileSaving(false);
    }
  };

  const tabs = [
    { key: 'appointments', label: 'Mis Citas' },
    { key: 'consultations', label: 'Consultas' },
    { key: 'vitals', label: 'Signos Vitales' },
    { key: 'prescriptions', label: 'Recetas' },
    { key: 'labs', label: 'Laboratorio' },
  ] as const;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <PatientHeader onProfileClick={() => setShowProfile(true)} />

      <main className="flex-1 max-w-4xl mx-auto w-full p-6">
        <h2 className="text-2xl font-semibold text-gray-800 mb-6">Mi Panel de Salud</h2>

        {/* Tabs */}
        <div className="border-b border-gray-200 mb-6">
          <nav className="-mb-px flex space-x-6 overflow-x-auto">
            {tabs.map(({ key, label }) => (
              <button
                key={key}
                onClick={() => setActiveTab(key)}
                className={`py-3 px-1 border-b-2 font-medium text-sm whitespace-nowrap ${
                  activeTab === key ? 'border-medin-cyan text-medin-cyan' : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                {label}
              </button>
            ))}
          </nav>
        </div>

        {/* Mis Citas */}
        {activeTab === 'appointments' && (
          <div className="space-y-3">
            {apptLoading ? (
              <div className="text-center py-8">
                <div className="inline-block animate-spin rounded-full h-7 w-7 border-4 border-medin-cyan border-t-transparent"></div>
              </div>
            ) : appointments.length === 0 ? (
              <p className="text-gray-500 text-sm">No tienes citas registradas.</p>
            ) : (
              appointments
                .slice()
                .sort((a, b) => a.appointmentDate.localeCompare(b.appointmentDate) || a.appointmentTime.localeCompare(b.appointmentTime))
                .map((appt) => (
                  <div key={appt.id} className="bg-white rounded-lg shadow p-4 flex items-start justify-between gap-4">
                    <div>
                      <p className="font-semibold text-gray-900 text-sm">
                        {appt.appointmentDate} — {appt.appointmentTime.substring(0, 5)}
                      </p>
                      {appt.notes && <p className="text-sm text-gray-600 mt-1">{appt.notes}</p>}
                    </div>
                    <span className={`shrink-0 px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLOR[appt.status] ?? 'bg-gray-100 text-gray-700'}`}>
                      {STATUS_LABEL[appt.status] ?? appt.status}
                    </span>
                  </div>
                ))
            )}
          </div>
        )}

        {/* Historial clínico */}
        {activeTab !== 'appointments' && (
          <>
            {loading && (
              <div className="text-center py-12">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
                <p className="mt-3 text-gray-500 text-sm">Cargando historial...</p>
              </div>
            )}
            {error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm mb-4">{error}</div>
            )}
            {sinHistorial && !loading && (
              <div className="text-center py-16">
                <p className="text-gray-500 text-sm">Aún no tienes consultas, signos vitales ni resultados registrados.</p>
              </div>
            )}
            {history && (
              <>
                {activeTab === 'consultations' && (
                  <div className="space-y-3">
                    {history.consultations.length === 0 ? (
                      <p className="text-gray-500 text-sm">Sin consultas registradas</p>
                    ) : history.consultations.map((c) => (
                      <div key={c.id} className="bg-white rounded-lg shadow p-4">
                        <div className="flex justify-between items-start mb-2">
                          <p className="font-semibold text-gray-900">{c.primaryDiagnosis}</p>
                          <span className="text-xs text-gray-400">{new Date(c.consultationDate).toLocaleDateString('es-GT')}</span>
                        </div>
                        <p className="text-sm text-gray-700"><span className="font-medium">Motivo:</span> {c.chiefComplaint}</p>
                        {c.secondaryDiagnoses?.length > 0 && (
                          <p className="text-xs text-gray-500 mt-1">Dx secundarios: {c.secondaryDiagnoses.join(', ')}</p>
                        )}
                      </div>
                    ))}
                  </div>
                )}

                {activeTab === 'vitals' && (
                  <div className="space-y-3">
                    {history.vitalSigns.length === 0 ? (
                      <p className="text-gray-500 text-sm">Sin registros de signos vitales</p>
                    ) : history.vitalSigns.map((v) => (
                      <div key={v.id} className="bg-white rounded-lg shadow p-4">
                        <div className="flex justify-between items-start mb-2">
                          <p className="font-semibold text-gray-900 text-sm">Registro de signos vitales</p>
                          <span className="text-xs text-gray-400">{new Date(v.recordedAt).toLocaleString('es-GT')}</span>
                        </div>
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-sm">
                          <div className="bg-gray-50 rounded p-2 text-center">
                            <p className="text-xs text-gray-500">TA</p>
                            <p className="font-semibold">{v.systolicPressure}/{v.diastolicPressure}</p>
                            <p className="text-xs text-gray-400">mmHg</p>
                          </div>
                          <div className="bg-gray-50 rounded p-2 text-center">
                            <p className="text-xs text-gray-500">FC</p>
                            <p className="font-semibold">{v.heartRate}</p>
                            <p className="text-xs text-gray-400">lpm</p>
                          </div>
                          <div className="bg-gray-50 rounded p-2 text-center">
                            <p className="text-xs text-gray-500">Temp</p>
                            <p className="font-semibold">{v.temperature}</p>
                            <p className="text-xs text-gray-400">°C</p>
                          </div>
                          <div className="bg-gray-50 rounded p-2 text-center">
                            <p className="text-xs text-gray-500">SpO2</p>
                            <p className="font-semibold">{v.oxygenSaturation}%</p>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {activeTab === 'prescriptions' && (
                  <div className="space-y-3">
                    {history.prescriptions.length === 0 ? (
                      <p className="text-gray-500 text-sm">Sin recetas registradas</p>
                    ) : history.prescriptions.map((rx) => (
                      <div key={rx.id} className="bg-white rounded-lg shadow p-4">
                        <div className="flex justify-between items-start mb-2">
                          <span className="font-mono text-sm font-semibold text-gray-900">{rx.prescriptionCode}</span>
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${rx.status === 'DISPENSED' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                            {rx.status === 'DISPENSED' ? 'Dispensada' : 'Pendiente'}
                          </span>
                        </div>
                        <div className="space-y-1">
                          {rx.medications.map((m, i) => (
                            <p key={i} className="text-sm text-gray-700">
                              <span className="font-medium">{m.name}</span> — {m.dosage}, {m.frequency}, {m.durationDays} días
                            </p>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {activeTab === 'labs' && (
                  <div className="space-y-3">
                    {history.labOrders.length === 0 ? (
                      <p className="text-gray-500 text-sm">Sin órdenes de laboratorio</p>
                    ) : history.labOrders.map((lab) => (
                      <div key={lab.id} className="bg-white rounded-lg shadow p-4">
                        <div className="flex justify-between items-start mb-2">
                          <span className="font-mono text-sm font-semibold text-gray-900">{lab.orderCode}</span>
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${lab.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : lab.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' : 'bg-yellow-100 text-yellow-800'}`}>
                            {lab.status === 'COMPLETED' ? 'Completado' : lab.status === 'IN_PROGRESS' ? 'En proceso' : 'Pendiente'}
                          </span>
                        </div>
                        <div className="flex flex-wrap gap-1">
                          {lab.testNames.map((t, i) => (
                            <span key={i} className="px-2 py-0.5 bg-blue-50 text-blue-700 rounded text-xs">{t}</span>
                          ))}
                        </div>
                        <p className="text-xs text-gray-400 mt-1">{new Date(lab.orderedAt).toLocaleDateString('es-GT')}</p>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </>
        )}
      </main>

      {/* Profile Modal */}
      {showProfile && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between">
              <h3 className="text-xl font-semibold text-gray-900">Mi Perfil</h3>
              <button onClick={() => setShowProfile(false)} className="text-gray-400 hover:text-gray-600">
                <XMarkIcon className="h-6 w-6" />
              </button>
            </div>
            <form onSubmit={handleProfileSave} className="p-6 space-y-4">
              {profileError && (
                <div className="p-3 bg-red-50 border border-red-200 rounded text-red-800 text-sm">{profileError}</div>
              )}
              {profileSuccess && (
                <div className="p-3 bg-green-50 border border-green-200 rounded text-green-800 text-sm">Perfil actualizado correctamente</div>
              )}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">DPI</label>
                  <input type="text" value={patient?.dpi ?? ''} disabled className="w-full px-3 py-2 border border-gray-300 rounded bg-gray-50 text-gray-500 cursor-not-allowed" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Correo electrónico</label>
                  <input type="text" value={patient?.email ?? ''} disabled className="w-full px-3 py-2 border border-gray-300 rounded bg-gray-50 text-gray-500 cursor-not-allowed" />
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Primer nombre <span className="text-red-500">*</span></label>
                  <input type="text" required value={profileForm.firstName}
                    onChange={(e) => setProfileForm({ ...profileForm, firstName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Segundo nombre</label>
                  <input type="text" value={profileForm.secondName}
                    onChange={(e) => setProfileForm({ ...profileForm, secondName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Primer apellido <span className="text-red-500">*</span></label>
                  <input type="text" required value={profileForm.firstLastName}
                    onChange={(e) => setProfileForm({ ...profileForm, firstLastName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Segundo apellido</label>
                  <input type="text" value={profileForm.secondLastName}
                    onChange={(e) => setProfileForm({ ...profileForm, secondLastName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono <span className="text-red-500">*</span></label>
                <input type="tel" required value={profileForm.phone}
                  onChange={(e) => setProfileForm({ ...profileForm, phone: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Dirección</label>
                <input type="text" value={profileForm.address}
                  onChange={(e) => setProfileForm({ ...profileForm, address: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Departamento</label>
                  <input type="text" value={profileForm.department}
                    onChange={(e) => setProfileForm({ ...profileForm, department: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Municipio</label>
                  <input type="text" value={profileForm.municipality}
                    onChange={(e) => setProfileForm({ ...profileForm, municipality: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-medin-cyan focus:border-transparent" />
                </div>
              </div>
              <div className="flex justify-end space-x-3 pt-4">
                <button type="button" onClick={() => setShowProfile(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors">
                  Cancelar
                </button>
                <button type="submit" disabled={profileSaving}
                  className="px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50">
                  {profileSaving ? 'Guardando...' : 'Guardar cambios'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default PatientDashboard;
