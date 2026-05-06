import { useState, useEffect, useCallback } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import { listAppointments } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import PaymentModal from '../../components/Cashier/PaymentModal';
import type { PaymentType } from '../../components/Cashier/PaymentModal';

// ─── Tabla de citas ───────────────────────────────────────────────────────────

interface AppointmentTableProps {
  appointments: AppointmentListItem[];
  loading: boolean;
  onPay: (appt: AppointmentListItem) => void;
  emptyText: string;
}

const AppointmentTable: FC<AppointmentTableProps> = ({ appointments, loading, onPay, emptyText }) => {
  if (loading) {
    return (
      <div className="text-center py-12">
        <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" />
      </div>
    );
  }

  if (appointments.length === 0) {
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
            <th className="pb-2 pr-4 pl-6">Fecha</th>
            <th className="pb-2 pr-4">Hora</th>
            <th className="pb-2 pr-4">Paciente</th>
            <th className="pb-2 pr-4">DPI</th>
            <th className="pb-2 pr-4">Nº Factura</th>
            <th className="pb-2 pr-4">Estado pago</th>
            <th className="pb-2 pr-4">Total</th>
            <th className="pb-2 pr-6">Acción</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {appointments
            .slice()
            .sort((a, b) => {
              const d = a.appointmentDate.toString().localeCompare(b.appointmentDate.toString());
              return d !== 0 ? d : a.appointmentTime.toString().localeCompare(b.appointmentTime.toString());
            })
            .map(appt => (
              <tr key={appt.id} className="hover:bg-gray-50">
                <td className="py-3 pr-4 pl-6 whitespace-nowrap text-xs">
                  {new Date(appt.appointmentDate + 'T00:00:00').toLocaleDateString('es-GT', { day: '2-digit', month: '2-digit', year: 'numeric' })}
                </td>
                <td className="py-3 pr-4 whitespace-nowrap font-medium">
                  {appt.appointmentTime.toString().substring(0, 5)}
                </td>
                <td className="py-3 pr-4">{appt.patient.fullName}</td>
                <td className="py-3 pr-4 font-mono text-xs">{appt.patient.dpi ?? '—'}</td>
                <td className="py-3 pr-4 font-mono text-xs font-medium">
                  {appt.payment.invoiceNumber ?? '—'}
                </td>
                <td className="py-3 pr-4">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                    appt.payment.statusColor === 'green'  ? 'bg-green-100 text-green-800' :
                    appt.payment.statusColor === 'orange' ? 'bg-orange-100 text-orange-800' :
                    appt.payment.statusColor === 'red'    ? 'bg-red-100 text-red-700' :
                    'bg-gray-100 text-gray-600'
                  }`}>
                    {appt.payment.statusLabel}
                  </span>
                </td>
                <td className="py-3 pr-4 whitespace-nowrap">
                  <span className="font-bold text-medin-navy">
                    Q {appt.payment.amount?.toFixed(2) ?? '0.00'}
                  </span>
                </td>
                <td className="py-3 pr-6 whitespace-nowrap text-right">
                  <button
                    onClick={() => onPay(appt)}
                    disabled={!appt.payment.invoiceId}
                    className="px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm disabled:opacity-40 disabled:cursor-not-allowed"
                    title={!appt.payment.invoiceId ? 'Sin factura registrada' : 'Procesar pago'}
                  >
                    Cobrar
                  </button>
                </td>
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
};

// ─── Página principal ─────────────────────────────────────────────────────────

const CashierPage: FC = () => {
  // Cola de consultas (PENDING_PAYMENT)
  const [consultations, setConsultations] = useState<AppointmentListItem[]>([]);
  const [loadingConsultations, setLoadingConsultations] = useState(true);

  // Cola de laboratorio (PENDING_LAB_PAYMENT)
  const [labPayments, setLabPayments] = useState<AppointmentListItem[]>([]);
  const [loadingLab, setLoadingLab] = useState(true);

  // Modal
  const [selectedAppointment, setSelectedAppointment] = useState<AppointmentListItem | null>(null);
  const [selectedPaymentType, setSelectedPaymentType] = useState<PaymentType>('CONSULTATION');

  const loadConsultations = useCallback(async () => {
    setLoadingConsultations(true);
    try {
      const data = await listAppointments({ queue: 'payment' });
      setConsultations(data.filter(a => a.payment.status !== 'PAID' && a.payment.status !== 'CANCELLED'));
    } catch (err) {
      console.error('Error cargando cola de consultas:', err);
    } finally {
      setLoadingConsultations(false);
    }
  }, []);

  const loadLabPayments = useCallback(async () => {
    setLoadingLab(true);
    try {
      const data = await listAppointments({ status: ['PENDING_LAB_PAYMENT'] });
      setLabPayments(data);
    } catch (err) {
      console.error('Error cargando cola de laboratorio:', err);
    } finally {
      setLoadingLab(false);
    }
  }, []);

  useEffect(() => {
    loadConsultations();
    loadLabPayments();
  }, [loadConsultations, loadLabPayments]);

  const handlePay = (appt: AppointmentListItem, type: PaymentType) => {
    setSelectedAppointment(appt);
    setSelectedPaymentType(type);
  };

  const handlePaymentSuccess = () => {
    setSelectedAppointment(null);
    // Recarga ambas listas
    loadConsultations();
    loadLabPayments();
  };

  return (
    <MainLayout>
      <div className="space-y-8">
        {/* Header */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Módulo de Caja</h2>
          <p className="text-gray-500 text-sm mt-1">Gestiona los cobros y pagos de pacientes</p>
        </div>

        {/* ── Lista 1: Cobros de Consulta ── */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gradient-to-r from-medin-navy/5 to-transparent">
            <div className="flex items-center gap-3">
              <span className="text-xl">🏥</span>
              <div>
                <h3 className="text-sm font-semibold text-gray-900">Cobros de Consulta</h3>
                <p className="text-xs text-gray-500">Pacientes pendientes de pago inicial</p>
              </div>
              <span className="ml-2 px-2 py-0.5 bg-medin-navy/10 text-medin-navy rounded-full text-xs font-semibold">
                {loadingConsultations ? '…' : consultations.length}
              </span>
            </div>
            <button
              onClick={loadConsultations}
              disabled={loadingConsultations}
              className="text-xs text-medin-navy hover:text-medin-navy/70 disabled:opacity-50"
            >
              Actualizar
            </button>
          </div>
          <AppointmentTable
            appointments={consultations}
            loading={loadingConsultations}
            onPay={appt => handlePay(appt, 'CONSULTATION')}
            emptyText="No hay cobros de consulta pendientes"
          />
        </div>

        {/* ── Lista 2: Cobros de Laboratorio ── */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gradient-to-r from-purple-500/5 to-transparent">
            <div className="flex items-center gap-3">
              <span className="text-xl">🔬</span>
              <div>
                <h3 className="text-sm font-semibold text-gray-900">Cobros de Laboratorio</h3>
                <p className="text-xs text-gray-500">Pacientes con exámenes pendientes de pago</p>
              </div>
              <span className="ml-2 px-2 py-0.5 bg-purple-100 text-purple-700 rounded-full text-xs font-semibold">
                {loadingLab ? '…' : labPayments.length}
              </span>
            </div>
            <button
              onClick={loadLabPayments}
              disabled={loadingLab}
              className="text-xs text-purple-700 hover:text-purple-500 disabled:opacity-50"
            >
              Actualizar
            </button>
          </div>
          <AppointmentTable
            appointments={labPayments}
            loading={loadingLab}
            onPay={appt => handlePay(appt, 'LAB')}
            emptyText="No hay cobros de laboratorio pendientes"
          />
        </div>
      </div>

      {/* Modal de pago */}
      {selectedAppointment && (
        selectedAppointment.payment.invoiceId ? (
          <PaymentModal
            invoice={{
              id: selectedAppointment.payment.invoiceId,
              invoiceNumber: selectedAppointment.payment.invoiceNumber || '',
              patientId: selectedAppointment.patient.id,
              total: selectedAppointment.payment.amount || 0,
              subtotal: selectedAppointment.payment.amount || 0,
              discountAmount: 0,
              status: selectedAppointment.payment.status as any,
              createdAt: selectedAppointment.createdAt,
              createdBy: '',
              charges: [],
            }}
            paymentType={selectedPaymentType}
            appointmentId={selectedAppointment.id}
            onSuccess={handlePaymentSuccess}
            onClose={() => setSelectedAppointment(null)}
          />
        ) : (
          <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6 text-center">
              <p className="text-gray-700 mb-4">
                No se encontró información de factura para esta cita.<br />
                Consulta al administrador o recarga la página.
              </p>
              <button
                onClick={() => setSelectedAppointment(null)}
                className="px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg"
              >
                Cerrar
              </button>
            </div>
          </div>
        )
      )}
    </MainLayout>
  );
};

export default CashierPage;
