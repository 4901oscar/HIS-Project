import { useEffect, useState } from 'react';
import type { FC } from 'react';
import { XMarkIcon } from '@heroicons/react/24/outline';
import { getVitalSignsByAppointment, getPrescriptionByAppointment, getAppointmentTriage } from '../../services/clinicalService';
import type { VitalSignsResponse, PrescriptionDetailResponse, TriageResponse } from '../../services/clinicalService';
import { getLabOrderByAppointmentId, getLabResultsByAppointmentId, viewLabResult } from '../../api/labApi';
import type { LabOrderWithTestsResponse, LabResultResponse } from '../../api/labApi';

interface Props {
  appointmentId: string;
  appointmentDate: string;
  appointmentTime: string;
  doctorName?: string;
  status: string;
  notes?: string;
  onClose: () => void;
}

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Agendada',
  ACTIVE: 'Activa',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
  IN_PROGRESS: 'En progreso',
};

const STATUS_COLOR: Record<string, string> = {
  SCHEDULED: 'bg-blue-100 text-blue-800',
  ACTIVE: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

const AppointmentDetailModal: FC<Props> = ({
  appointmentId,
  appointmentDate,
  appointmentTime,
  doctorName,
  status,
  notes,
  onClose,
}) => {
  const [vitals, setVitals] = useState<VitalSignsResponse | null>(null);
  const [triage, setTriage] = useState<TriageResponse | null>(null);
  const [prescription, setPrescription] = useState<PrescriptionDetailResponse | null>(null);
  const [labOrder, setLabOrder] = useState<LabOrderWithTestsResponse | null>(null);
  const [labResults, setLabResults] = useState<LabResultResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      const [v, tri, rx, lab, results] = await Promise.allSettled([
        getVitalSignsByAppointment(appointmentId),
        getAppointmentTriage(appointmentId),
        getPrescriptionByAppointment(appointmentId),
        getLabOrderByAppointmentId(appointmentId),
        getLabResultsByAppointmentId(appointmentId),
      ]);
      if (v.status === 'fulfilled') setVitals(v.value);
      if (tri.status === 'fulfilled') setTriage(tri.value);
      if (rx.status === 'fulfilled') setPrescription(rx.value);
      if (lab.status === 'fulfilled') setLabOrder(lab.value);
      if (results.status === 'fulfilled') setLabResults(results.value);
      setLoading(false);
    };
    load();
  }, [appointmentId]);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col">
        <div className="sticky top-0 bg-white border-b px-6 py-4 flex items-center justify-between rounded-t-xl">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">Detalle de Cita</h3>
            <p className="text-sm text-gray-500">{appointmentDate} a las {appointmentTime.substring(0, 5)}</p>
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <XMarkIcon className="h-6 w-6" />
          </button>
        </div>

        <div className="overflow-y-auto flex-1 p-6 space-y-6">
          {/* Header info */}
          <div className="flex items-start justify-between gap-4">
            <div className="space-y-1">
              {doctorName && (
                <p className="text-sm text-gray-700"><span className="font-medium">Doctor:</span> {doctorName}</p>
              )}
              {notes && (
                <p className="text-sm text-gray-600"><span className="font-medium">Motivo:</span> {notes}</p>
              )}
            </div>
            <span className={`shrink-0 px-3 py-1 rounded-full text-xs font-medium ${STATUS_COLOR[status] ?? 'bg-gray-100 text-gray-700'}`}>
              {STATUS_LABEL[status] ?? status}
            </span>
          </div>

          {loading ? (
            <div className="text-center py-10">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
              <p className="mt-3 text-gray-500 text-sm">Cargando informacion clinica...</p>
            </div>
          ) : (
            <>
              {/* Signos Vitales */}
              <section>
                <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wide mb-3 flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-red-400 inline-block"></span>
                  Signos Vitales
                </h4>
                {vitals ? (
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                    {[
                      { label: 'T.A.', value: `${vitals.systolicPressure}/${vitals.diastolicPressure}`, unit: 'mmHg' },
                      { label: 'F.C.', value: vitals.heartRate, unit: 'lpm' },
                      { label: 'Temp.', value: vitals.temperature, unit: '°C' },
                      { label: 'SpO2', value: `${vitals.oxygenSaturation}%`, unit: '' },
                      { label: 'F.R.', value: vitals.respiratoryRate, unit: 'rpm' },
                      ...(vitals.weight ? [{ label: 'Peso', value: vitals.weight, unit: 'kg' }] : []),
                      ...(vitals.height ? [{ label: 'Talla', value: vitals.height, unit: 'cm' }] : []),
                      ...(vitals.bmi ? [{ label: 'IMC', value: vitals.bmi.toFixed(1), unit: '' }] : []),
                    ].map(({ label, value, unit }) => (
                      <div key={label} className="bg-gray-50 rounded-lg p-3 text-center">
                        <p className="text-xs text-gray-500 mb-1">{label}</p>
                        <p className="font-bold text-gray-900">{value}</p>
                        {unit && <p className="text-xs text-gray-400">{unit}</p>}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-gray-400 italic">No se registraron signos vitales para esta cita.</p>
                )}
              </section>

              <hr className="border-gray-100" />

              {/* Triaje Manchester */}
              <section>
                <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wide mb-3 flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-orange-400 inline-block"></span>
                  Triaje Manchester
                </h4>
                {triage ? (() => {
                  const colorMap: Record<string, string> = {
                    RED: 'bg-red-100 text-red-800 border-red-300',
                    ORANGE: 'bg-orange-100 text-orange-800 border-orange-300',
                    YELLOW: 'bg-yellow-100 text-yellow-800 border-yellow-300',
                    GREEN: 'bg-green-100 text-green-800 border-green-300',
                    BLUE: 'bg-blue-100 text-blue-800 border-blue-300',
                  };
                  const labelMap: Record<string, string> = {
                    RED: 'Inmediato',
                    ORANGE: 'Muy urgente',
                    YELLOW: 'Urgente',
                    GREEN: 'Poco urgente',
                    BLUE: 'No urgente',
                  };
                  const cls = colorMap[triage.priorityLevel] ?? 'bg-gray-100 text-gray-700 border-gray-300';
                  return (
                    <div className={`border rounded-lg p-4 ${cls}`}>
                      <div className="flex items-center justify-between flex-wrap gap-2">
                        <div>
                          <p className="font-bold text-base">{labelMap[triage.priorityLevel] ?? triage.priorityLevel}</p>
                          <p className="text-sm mt-0.5">{triage.priorityDescription}</p>
                        </div>
                        <div className="text-right text-sm">
                          <p className="font-medium">Espera max.</p>
                          <p className="font-bold text-lg">{triage.maxWaitTimeMinutes} min</p>
                        </div>
                      </div>
                      <p className="text-xs mt-2 opacity-70">
                        Realizado: {new Date(triage.performedAt).toLocaleString('es-GT')}
                      </p>
                    </div>
                  );
                })() : (
                  <p className="text-sm text-gray-400 italic">No se realizo triaje para esta cita.</p>
                )}
              </section>

              <hr className="border-gray-100" />

              {/* Consulta / Receta */}
              <section>
                <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wide mb-3 flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-blue-400 inline-block"></span>
                  Medicamentos Recetados
                </h4>
                {prescription ? (
                  <div className="space-y-3">
                    <div className="flex items-center gap-3 mb-2">
                      <span className="font-mono text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded">
                        {prescription.prescriptionCode}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                        prescription.status === 'DISPENSED' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                      }`}>
                        {prescription.status === 'DISPENSED' ? 'Dispensada' : 'Pendiente'}
                      </span>
                    </div>
                    <div className="space-y-2">
                      {prescription.medications.map((m, i) => (
                        <div key={i} className="bg-blue-50 rounded-lg p-3">
                          <p className="font-medium text-gray-900 text-sm">{m.name}</p>
                          <p className="text-xs text-gray-600 mt-0.5">
                            {m.dosage} — {m.frequency} — {m.durationDays} dias
                          </p>
                          {m.specialInstructions && (
                            <p className="text-xs text-gray-500 mt-1">{m.specialInstructions}</p>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                ) : (
                  <p className="text-sm text-gray-400 italic">No se emitio receta para esta cita.</p>
                )}
              </section>

              <hr className="border-gray-100" />

              {/* Laboratorio */}
              <section>
                <h4 className="text-sm font-semibold text-gray-700 uppercase tracking-wide mb-3 flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full bg-purple-400 inline-block"></span>
                  Examenes de Laboratorio
                </h4>
                {labOrder ? (
                  <div className="space-y-3">
                    <div className="flex items-center gap-3 flex-wrap">
                      <span className="font-mono text-xs text-gray-500 bg-gray-100 px-2 py-0.5 rounded">
                        {labOrder.orderCode}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                        labOrder.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                        labOrder.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                        'bg-yellow-100 text-yellow-800'
                      }`}>
                        {labOrder.status === 'COMPLETED' ? 'Completado' :
                         labOrder.status === 'IN_PROGRESS' ? 'En proceso' : 'Pendiente'}
                      </span>
                    </div>

                    <div className="flex flex-wrap gap-2">
                      {labOrder.tests.map((t) => (
                        <span key={t.testName} className={`px-2 py-1 rounded text-xs font-medium ${
                          t.hasResult ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-600'
                        }`}>
                          {t.testName} {t.hasResult ? '✓' : ''}
                        </span>
                      ))}
                    </div>

                    {labResults.length > 0 && (
                      <div className="space-y-2 mt-2">
                        <p className="text-xs font-medium text-gray-600">Archivos de resultados:</p>
                        {labResults.map((r) => (
                          <button
                            key={r.id}
                            onClick={() => viewLabResult(r.id)}
                            className="flex items-center gap-2 text-sm text-medin-cyan hover:text-medin-blue transition-colors w-full text-left"
                          >
                            <svg className="h-4 w-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                            </svg>
                            <span className="truncate">{r.testName} — {r.originalFilename}</span>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                ) : (
                  <p className="text-sm text-gray-400 italic">No se solicitaron examenes de laboratorio para esta cita.</p>
                )}
              </section>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default AppointmentDetailModal;
