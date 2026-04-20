import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import { ArrowPathIcon } from '@heroicons/react/24/outline';
import { getPrescriptions, dispensePrescription } from '../../services/pharmacyService';
import type { PrescriptionResponse, PrescriptionStatus } from '../../services/pharmacyService';
import axios from 'axios';

const statusLabel: Record<PrescriptionStatus, string> = {
  PENDING: 'Pendiente',
  DISPENSED: 'Dispensada',
  CANCELLED: 'Cancelada',
};

const statusColor: Record<PrescriptionStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  DISPENSED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
};

const PharmacyDispense: FC = () => {
  const [filter, setFilter] = useState<PrescriptionStatus | ''>('PENDING');
  const [prescriptions, setPrescriptions] = useState<PrescriptionResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [dispensing, setDispensing] = useState<string | null>(null);

  const loadPrescriptions = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getPrescriptions(filter || undefined);
      setPrescriptions(data);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al cargar prescripciones');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPrescriptions();
  }, [filter]);

  const flash = (msg: string) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(null), 3000);
  };

  const handleDispense = async (id: string, code: string) => {
    setDispensing(id);
    setError(null);
    try {
      const updated = await dispensePrescription(id);
      setPrescriptions((prev) => prev.map((p) => (p.id === id ? updated : p)));
      flash(`Receta ${code} dispensada exitosamente`);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al dispensar');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setDispensing(null);
    }
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Despacho de Farmacia</h2>
            <p className="mt-1 text-sm text-gray-600">Dispensar medicamentos a pacientes</p>
          </div>
          <button onClick={loadPrescriptions} className="flex items-center gap-2 px-4 py-2 bg-medin-navy text-white rounded-lg text-sm">
            <ArrowPathIcon className="h-4 w-4" />
            Actualizar
          </button>
        </div>

        {successMsg && <div className="p-3 bg-green-50 border border-green-200 rounded-lg text-green-800 text-sm">{successMsg}</div>}
        {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>}

        {/* Filtros */}
        <div className="flex gap-2 flex-wrap">
          {(['', 'PENDING', 'DISPENSED', 'CANCELLED'] as const).map((s) => (
            <button
              key={s}
              onClick={() => setFilter(s)}
              className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                filter === s ? 'bg-medin-navy text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {s === '' ? 'Todas' : statusLabel[s as PrescriptionStatus]}
            </button>
          ))}
        </div>

        {/* Listado */}
        {loading ? (
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
            <p className="mt-3 text-gray-500 text-sm">Cargando prescripciones...</p>
          </div>
        ) : prescriptions.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-12 text-center text-gray-500 text-sm">
            No hay prescripciones con ese filtro
          </div>
        ) : (
          <div className="space-y-3">
            {prescriptions.map((rx) => (
              <div key={rx.id} className="bg-white rounded-lg shadow p-5">
                <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <span className="font-mono text-sm font-semibold text-gray-900">{rx.prescriptionCode}</span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColor[rx.status]}`}>
                        {statusLabel[rx.status]}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500 mb-2">Paciente: <span className="font-mono">{rx.patientId}</span></p>
                    <div className="space-y-1">
                      {rx.medications.map((m, i) => (
                        <div key={i} className="text-sm text-gray-700">
                          <span className="font-medium">{m.name}</span> — {m.dosage}, {m.frequency}, {m.durationDays} días ({m.route})
                          {m.specialInstructions && <span className="text-gray-500"> · {m.specialInstructions}</span>}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="flex flex-col gap-2 sm:items-end">
                    <p className="text-xs text-gray-400">{new Date(rx.createdAt).toLocaleDateString('es-GT')}</p>
                    {rx.status === 'PENDING' && (
                      <button
                        onClick={() => handleDispense(rx.id, rx.prescriptionCode)}
                        disabled={dispensing === rx.id}
                        className="px-4 py-1.5 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50"
                      >
                        {dispensing === rx.id ? 'Despachando...' : 'Despachar'}
                      </button>
                    )}
                    {rx.status === 'DISPENSED' && rx.dispensedAt && (
                      <p className="text-xs text-green-600">Despachado: {new Date(rx.dispensedAt).toLocaleDateString('es-GT')}</p>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default PharmacyDispense;
