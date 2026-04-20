import { useState, useEffect } from 'react';
import type { FC } from 'react';
import axios from 'axios';
import { useAuth } from '../../hooks/useAuth';
import { getPatientByDpi } from '../../services/patientService';
import { getMedicalHistory } from '../../services/clinicalService';
import type { MedicalHistoryResponse } from '../../services/clinicalService';

const PatientDashboard: FC = () => {
  const { user, logout } = useAuth();
  const [history, setHistory] = useState<MedicalHistoryResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sinHistorial, setSinHistorial] = useState(false);
  const [activeTab, setActiveTab] = useState<'consultations' | 'vitals' | 'prescriptions' | 'labs'>('consultations');

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
      const patient = await getPatientByDpi(username);
      const hist = await getMedicalHistory(patient.id);
      setHistory(hist);
    } catch (err) {
      if (axios.isAxiosError(err) && err.response) {
        // El servidor respondió (404 paciente no encontrado, 404 sin historial, etc.)
        setSinHistorial(true);
      } else {
        // Sin respuesta = red caída
        setError('No se pudo conectar con el servidor. Intenta de nuevo más tarde.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  const tabs = [
    { key: 'consultations', label: 'Consultas' },
    { key: 'vitals', label: 'Signos Vitales' },
    { key: 'prescriptions', label: 'Recetas' },
    { key: 'labs', label: 'Laboratorio' },
  ] as const;

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-medin-navy px-6 py-4 flex items-center justify-between shadow">
        <div className="flex items-center space-x-2">
          <img src="/icono.svg" alt="MedFlow" className="h-8 w-auto" />
          <span className="text-xl font-bold">
            <span className="text-white">Med</span>
            <span className="text-medin-cyan">Flow</span>
          </span>
        </div>
        <div className="flex items-center space-x-4">
          <span className="text-gray-300 text-sm hidden sm:block">{user?.fullName}</span>
          <button onClick={handleLogout} className="text-sm text-gray-300 hover:text-white transition-colors">
            Cerrar sesión
          </button>
        </div>
      </header>

      <main className="flex-1 max-w-4xl mx-auto w-full p-6">
        <h2 className="text-2xl font-semibold text-gray-800 mb-6">
          Mi Historial Clínico
        </h2>

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
            <div className="h-16 w-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <h3 className="text-gray-700 font-medium mb-1">Sin historial clínico</h3>
            <p className="text-gray-500 text-sm">Aún no tienes consultas, signos vitales ni resultados registrados.</p>
          </div>
        )}

        {history && (
          <>
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

            {/* Consultas */}
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

            {/* Signos Vitales */}
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

            {/* Recetas */}
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

            {/* Laboratorio */}
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
      </main>
    </div>
  );
};

export default PatientDashboard;
