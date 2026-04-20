import { useState, useEffect, useRef } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import { ArrowPathIcon } from '@heroicons/react/24/outline';
import { getLabOrders, collectSample, uploadResult } from '../../services/labService';
import type { LabOrderResponse, OrderStatus } from '../../services/labService';
import axios from 'axios';

const statusLabel: Record<OrderStatus, string> = {
  PENDING: 'Pendiente',
  IN_PROGRESS: 'En proceso',
  COMPLETED: 'Completado',
};

const statusColor: Record<OrderStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  IN_PROGRESS: 'bg-blue-100 text-blue-800',
  COMPLETED: 'bg-green-100 text-green-800',
};

const LabSampleManagement: FC = () => {
  const [filter, setFilter] = useState<OrderStatus | ''>('');
  const [orders, setOrders] = useState<LabOrderResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [pendingUploadId, setPendingUploadId] = useState<string | null>(null);

  const loadOrders = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getLabOrders(filter || undefined);
      setOrders(data);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al cargar órdenes');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, [filter]);

  const flash = (msg: string) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(null), 3000);
  };

  const handleCollect = async (id: string) => {
    setActionLoading(id);
    setError(null);
    try {
      const updated = await collectSample(id);
      setOrders((prev) => prev.map((o) => (o.id === id ? updated : o)));
      flash('Muestra recolectada exitosamente');
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al recolectar muestra');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setActionLoading(null);
    }
  };

  const handleUploadClick = (id: string) => {
    setPendingUploadId(id);
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !pendingUploadId) return;
    setActionLoading(pendingUploadId);
    setError(null);
    try {
      const updated = await uploadResult(pendingUploadId, file);
      setOrders((prev) => prev.map((o) => (o.id === pendingUploadId ? updated : o)));
      flash('Resultado subido exitosamente');
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al subir resultado');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setActionLoading(null);
      setPendingUploadId(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Gestión de Muestras</h2>
            <p className="mt-1 text-sm text-gray-600">Administrar órdenes de laboratorio y resultados</p>
          </div>
          <button onClick={loadOrders} className="flex items-center gap-2 px-4 py-2 bg-medin-navy text-white rounded-lg text-sm hover:bg-medin-navy/90">
            <ArrowPathIcon className="h-4 w-4" />
            Actualizar
          </button>
        </div>

        {successMsg && <div className="p-3 bg-green-50 border border-green-200 rounded-lg text-green-800 text-sm">{successMsg}</div>}
        {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>}

        {/* Filtros */}
        <div className="flex gap-2 flex-wrap">
          {(['', 'PENDING', 'IN_PROGRESS', 'COMPLETED'] as const).map((s) => (
            <button
              key={s}
              onClick={() => setFilter(s)}
              className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                filter === s ? 'bg-medin-navy text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {s === '' ? 'Todas' : statusLabel[s as OrderStatus]}
            </button>
          ))}
        </div>

        {/* Tabla */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          {loading ? (
            <div className="p-12 text-center">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
              <p className="mt-3 text-gray-500 text-sm">Cargando órdenes...</p>
            </div>
          ) : orders.length === 0 ? (
            <div className="p-12 text-center text-gray-500 text-sm">No hay órdenes con ese filtro</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    {['Código', 'Paciente', 'Exámenes', 'Estado', 'Fecha', 'Acciones'].map((h) => (
                      <th key={h} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {orders.map((order) => (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm font-mono text-gray-900">{order.orderCode}</td>
                      <td className="px-4 py-3 text-sm text-gray-600 font-mono text-xs">{order.patientId}</td>
                      <td className="px-4 py-3 text-sm text-gray-700">
                        <div className="flex flex-wrap gap-1">
                          {order.testNames.map((t, i) => (
                            <span key={i} className="px-1.5 py-0.5 bg-blue-50 text-blue-700 rounded text-xs">{t}</span>
                          ))}
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColor[order.status as OrderStatus]}`}>
                          {statusLabel[order.status as OrderStatus] || order.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-xs text-gray-500">
                        {new Date(order.orderedAt).toLocaleDateString('es-GT')}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex gap-2">
                          {order.status === 'PENDING' && (
                            <button
                              onClick={() => handleCollect(order.id)}
                              disabled={actionLoading === order.id}
                              className="px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs font-medium hover:bg-blue-200 disabled:opacity-50"
                            >
                              {actionLoading === order.id ? '...' : 'Recolectar'}
                            </button>
                          )}
                          {order.status === 'IN_PROGRESS' && (
                            <button
                              onClick={() => handleUploadClick(order.id)}
                              disabled={actionLoading === order.id}
                              className="px-2 py-1 bg-green-100 text-green-800 rounded text-xs font-medium hover:bg-green-200 disabled:opacity-50"
                            >
                              {actionLoading === order.id ? '...' : 'Subir PDF'}
                            </button>
                          )}
                          {order.status === 'COMPLETED' && (
                            <span className="text-xs text-green-600">✓ Completo</span>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Input oculto para subir archivo */}
        <input
          ref={fileInputRef}
          type="file"
          accept=".pdf"
          className="hidden"
          onChange={handleFileChange}
        />
      </div>
    </MainLayout>
  );
};

export default LabSampleManagement;
