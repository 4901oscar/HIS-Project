import { useState, useEffect, useCallback } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import { getLabOrders } from '../../services/labService';
import type { LabOrderResponse, OrderStatus } from '../../services/labService';
import { getAppointmentById } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';

// ─── Tipos combinados ─────────────────────────────────────────────────────────

interface LabOrderWithAppointment {
  order: LabOrderResponse;
  appointment: AppointmentListItem | null;
}

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

// ─── Tabla de órdenes con citas ───────────────────────────────────────────────

interface LabOrderTableProps {
  orders: LabOrderWithAppointment[];
  loading: boolean;
  emptyText: string;
}

const LabOrderTable: FC<LabOrderTableProps> = ({ orders, loading, emptyText }) => {
  if (loading) {
    return (
      <div className="text-center py-12">
        <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" />
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="text-center py-12 text-gray-400">
        <svg className="mx-auto h-10 w-10 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p className="text-sm">{emptyText}</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-sm">
        <thead>
          <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
            <th className="pb-2 pr-4 pl-6">Código Orden</th>
            <th className="pb-2 pr-4">Fecha Cita</th>
            <th className="pb-2 pr-4">Hora Cita</th>
            <th className="pb-2 pr-4">Paciente</th>
            <th className="pb-2 pr-4">DPI</th>
            <th className="pb-2 pr-4">Doctor</th>
            <th className="pb-2 pr-4">Exámenes</th>
            <th className="pb-2 pr-4">Estado</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {orders
            .slice()
            .sort((a, b) => {
              // Ordenar por fecha de orden
              return new Date(a.order.orderedAt).getTime() - new Date(b.order.orderedAt).getTime();
            })
            .map(({ order, appointment }) => (
              <tr key={order.id} className="hover:bg-gray-50">
                <td className="py-3 pr-4 pl-6 whitespace-nowrap font-mono text-xs font-medium text-purple-700">
                  {order.orderCode}
                </td>
                <td className="py-3 pr-4 whitespace-nowrap text-xs">
                  {appointment 
                    ? new Date(appointment.appointmentDate + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: '2-digit', year: 'numeric' })
                    : '—'
                  }
                </td>
                <td className="py-3 pr-4 whitespace-nowrap font-medium">
                  {appointment 
                    ? appointment.appointmentTime.toString().substring(0, 5)
                    : '—'
                  }
                </td>
                <td className="py-3 pr-4">
                  {appointment ? appointment.patient.fullName : order.patientId}
                </td>
                <td className="py-3 pr-4 font-mono text-xs">
                  {appointment?.patient.dpi ?? '—'}
                </td>
                <td className="py-3 pr-4">
                  {appointment ? appointment.doctor.name : order.doctorId}
                </td>
                <td className="py-3 pr-4">
                  <div className="flex flex-wrap gap-1">
                    {order.testNames.map((test, i) => (
                      <span key={i} className="px-1.5 py-0.5 bg-blue-50 text-blue-700 rounded text-xs">
                        {test}
                      </span>
                    ))}
                  </div>
                </td>
                <td className="py-3 pr-4">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColor[order.status]}`}>
                    {statusLabel[order.status]}
                  </span>
                </td>
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
};

// ─── Página principal ─────────────────────────────────────────────────────────

const LabSampleManagement: FC = () => {
  const [ordersWithAppointments, setOrdersWithAppointments] = useState<LabOrderWithAppointment[]>([]);
  const [loading, setLoading] = useState(true);

  const loadLabOrders = useCallback(async () => {
    setLoading(true);
    try {
      // 1. Obtener todas las órdenes de laboratorio
      const orders = await getLabOrders();
      
      // 2. Para cada orden, obtener la cita asociada usando appointmentId
      const ordersWithAppts = await Promise.all(
        orders.map(async (order) => {
          try {
            // Si la orden tiene appointmentId, obtener la cita
            if (order.appointmentId) {
              const appointment = await getAppointmentById(order.appointmentId);
              return { order, appointment };
            }
            // Si no tiene appointmentId, devolver solo la orden
            return { order, appointment: null };
          } catch (err) {
            console.warn(`No se pudo obtener la cita para la orden ${order.orderCode}:`, err);
            return { order, appointment: null };
          }
        })
      );
      
      setOrdersWithAppointments(ordersWithAppts);
    } catch (err) {
      console.error('Error cargando órdenes de laboratorio:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLabOrders();
  }, [loadLabOrders]);

  return (
    <MainLayout>
      <div className="space-y-8">
        {/* Header */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Gestión de Laboratorio</h2>
          <p className="text-gray-500 text-sm mt-1">Órdenes de laboratorio con información de citas</p>
        </div>

        {/* ── Lista: Órdenes de Laboratorio ── */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gradient-to-r from-purple-500/5 to-transparent">
            <div className="flex items-center gap-3">
              <span className="text-xl">🔬</span>
              <div>
                <h3 className="text-sm font-semibold text-gray-900">Órdenes de Laboratorio</h3>
                <p className="text-xs text-gray-500">Pacientes con exámenes pendientes, en proceso o completados</p>
              </div>
              <span className="ml-2 px-2 py-0.5 bg-purple-100 text-purple-700 rounded-full text-xs font-semibold">
                {loading ? '…' : ordersWithAppointments.length}
              </span>
            </div>
            <button
              onClick={loadLabOrders}
              disabled={loading}
              className="text-xs text-purple-700 hover:text-purple-500 disabled:opacity-50"
            >
              Actualizar
            </button>
          </div>
          <LabOrderTable
            orders={ordersWithAppointments}
            loading={loading}
            emptyText="No hay órdenes de laboratorio"
          />
        </div>
      </div>
    </MainLayout>
  );
};

export default LabSampleManagement;
