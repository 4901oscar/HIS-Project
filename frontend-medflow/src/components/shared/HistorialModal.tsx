import { useEffect, useState } from 'react';
import type { FC } from 'react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { getMedicalHistory } from '../../services/clinicalService';
import type { MedicalHistoryResponse } from '../../services/clinicalService';
import { listMyAppointments } from '../../services/appointmentService';
import type { AppointmentResponse } from '../../services/appointmentService';
import AppointmentDetailModal from '../patient/AppointmentDetailModal';
import { useAuth } from '../../hooks/useAuth';

type Tab = 'citas' | 'consultas' | 'vitales' | 'recetas' | 'labs';

interface Props {
  patientId: string;
  patientName?: string | null;
  onClose: () => void;
}

const STATUS_COLOR: Record<string, string> = {
  SCHEDULED: 'bg-blue-100 text-blue-800',
  ACTIVE: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Agendada',
  ACTIVE: 'Activa',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
};

const HistorialModal: FC<Props> = ({ patientId, patientName, onClose }) => {
  const { user } = useAuth();
  const isPatient = user?.roles?.includes('PATIENT');

  const [history, setHistory] = useState<MedicalHistoryResponse | null>(null);
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<Tab>('citas');
  const [selectedAppt, setSelectedAppt] = useState<AppointmentResponse | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const [hist, appts] = await Promise.allSettled([
          getMedicalHistory(patientId),
          isPatient ? listMyAppointments() : Promise.resolve([]),
        ]);
        if (hist.status === 'fulfilled') setHistory(hist.value);
        if (appts.status === 'fulfilled') setAppointments(appts.value as AppointmentResponse[]);
      } catch {
        setError('No se pudo cargar el historial clinico.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [patientId, isPatient]);

  const tabs: { key: Tab; label: string }[] = [
    { key: 'citas', label: 'Citas' },
    { key: 'consultas', label: 'Consultas' },
    { key: 'vitales', label: 'Signos Vitales' },
    { key: 'recetas', label: 'Recetas' },
    { key: 'labs', label: 'Laboratorio' },
  ];

  return (
    <>
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col">
          <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between rounded-t-xl">
            <div>
              <h3 className="text-lg font-semibold text-gray-900">Historial Clinico</h3>
              {patientName && <p className="text-sm text-gray-500">{patientName}</p>}
            </div>
            <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
              <XMarkIcon className="h-6 w-6" />
            </button>
          </div>

          {/* Tabs */}
          <div className="border-b border-gray-200 px-6">
            <nav className="-mb-px flex space-x-4 overflow-x-auto">
              {tabs.map(({ key, label }) => (
                <button
                  key={key}
                  onClick={() => setActiveTab(key)}
                  className={`py-3 px-1 border-b-2 font-medium text-sm whitespace-nowrap transition-colors ${
                    activeTab === key
                      ? 'border-medin-cyan text-medin-cyan'
                      : 'border-transparent text-gray-500 hover:text-gray-700'
                  }`}
                >
                  {label}
                </button>
              ))}
            </nav>
          </div>

          <div className="overflow-y-auto flex-1 p-6">
            {loading && (
              <div className="text-center py-12">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
                <p className="mt-3 text-gray-500 text-sm">Cargando historial...</p>
              </div>
            )}

            {!loading && error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>
            )}

            {!loading && !error && (
              <>
                {activeTab === 'citas' && (
                  <div className="space-y-3">
                    {appointments.length === 0 ? (
                      <p className="text-gray-400 text-sm italic">Sin citas registradas.</p>
                    ) : (
                      appointments
                        .slice()
                        .sort((a, b) => b.appointmentDate.localeCompare(a.appointmentDate) || b.appointmentTime.localeCompare(a.appointmentTime))
                        .map((appt) => (
                          <div key={appt.id} className="bg-white border border-gray-200 rounded-lg p-4 flex items-start justify-between gap-4 hover:border-medin-cyan transition-colors">
                            <div>
                              <p className="font-semibold text-gray-900 text-sm">
                                {appt.appointmentDate} — {appt.appointmentTime.substring(0, 5)}
                              </p>
                              {appt.notes && <p className="text-xs text-gray-500 mt-0.5">{appt.notes}</p>}
                            </div>
                            <div className="flex items-center gap-2 shrink-0">
                              <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLOR[appt.status] ?? 'bg-gray-100 text-gray-700'}`}>
                                {STATUS_LABEL[appt.status] ?? appt.status}
                              </span>
                              <button
                                onClick={() => setSelectedAppt(appt)}
                                className="text-xs text-medin-cyan hover:text-medin-blue font-medium transition-colors"
                              >
                                Ver
                              </button>
                            </div>
                          </div>
                        ))
                    )}
                  </div>
                )}

                {activeTab === 'consultas' && (
                  <div className="space-y-3">
                    {!history || history.consultations.length === 0 ? (
                      <p className="text-gray-400 text-sm italic">Sin consultas registradas.</p>
                    ) : history.consultations.map((c) => (
                      <div key={c.id} className="bg-white border border-gray-200 rounded-lg p-4">
                        <div className="flex justify-between items-start mb-2">
                          <p className="font-semibold text-gray-900 text-sm">{c.primaryDiagnosis}</p>
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

                {activeTab === 'vitales' && (
                  <div className="space-y-3">
                    {!history || history.vitalSigns.length === 0 ? (
                      <p className="text-gray-400 text-sm italic">Sin registros de signos vitales.</p>
                    ) : history.vitalSigns.map((v) => (
                      <div key={v.id} className="bg-white border border-gray-200 rounded-lg p-4">
                        <p className="text-xs text-gray-400 mb-3">{new Date(v.recordedAt).toLocaleString('es-GT')}</p>
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-sm">
                          {[
                            { label: 'T.A.', value: `${v.systolicPressure}/${v.diastolicPressure}`, unit: 'mmHg' },
                            { label: 'F.C.', value: v.heartRate, unit: 'lpm' },
                            { label: 'Temp.', value: v.temperature, unit: '°C' },
                            { label: 'SpO2', value: `${v.oxygenSaturation}%`, unit: '' },
                          ].map(({ label, value, unit }) => (
                            <div key={label} className="bg-gray-50 rounded p-2 text-center">
                              <p className="text-xs text-gray-500">{label}</p>
                              <p className="font-semibold text-gray-900">{value}</p>
                              {unit && <p className="text-xs text-gray-400">{unit}</p>}
                            </div>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {activeTab === 'recetas' && (
                  <div className="space-y-3">
                    {!history || history.prescriptions.length === 0 ? (
                      <p className="text-gray-400 text-sm italic">Sin recetas registradas.</p>
                    ) : history.prescriptions.map((rx) => (
                      <div key={rx.id} className="bg-white border border-gray-200 rounded-lg p-4">
                        <div className="flex justify-between items-start mb-2">
                          <span className="font-mono text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded">{rx.prescriptionCode}</span>
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${rx.status === 'DISPENSED' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                            {rx.status === 'DISPENSED' ? 'Dispensada' : 'Pendiente'}
                          </span>
                        </div>
                        <div className="space-y-1">
                          {rx.medications.map((m, i) => (
                            <p key={i} className="text-sm text-gray-700">
                              <span className="font-medium">{m.name}</span> — {m.dosage}, {m.frequency}, {m.durationDays} dias
                            </p>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {activeTab === 'labs' && (
                  <div className="space-y-3">
                    {!history || history.labOrders.length === 0 ? (
                      <p className="text-gray-400 text-sm italic">Sin ordenes de laboratorio.</p>
                    ) : history.labOrders.map((lab) => (
                      <div key={lab.id} className="bg-white border border-gray-200 rounded-lg p-4">
                        <div className="flex justify-between items-start mb-2">
                          <span className="font-mono text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded">{lab.orderCode}</span>
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                            lab.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                            lab.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                            'bg-yellow-100 text-yellow-800'
                          }`}>
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
          </div>
        </div>
      </div>

      {selectedAppt && (
        <AppointmentDetailModal
          appointmentId={selectedAppt.id}
          appointmentDate={selectedAppt.appointmentDate}
          appointmentTime={selectedAppt.appointmentTime}
          status={selectedAppt.status}
          notes={selectedAppt.notes}
          onClose={() => setSelectedAppt(null)}
        />
      )}
    </>
  );
};

export default HistorialModal;
