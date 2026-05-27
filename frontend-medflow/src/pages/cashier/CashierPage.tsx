import { useState, useEffect, useCallback } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import { listAppointments } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import PaymentModal from '../../components/Cashier/PaymentModal';
import type { PaymentType } from '../../components/Cashier/PaymentModal';
import { getInvoiceById } from '../../services/billingService';
import type { Invoice } from '../../services/billingService';

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
  const [consultationSearch, setConsultationSearch] = useState('');

  // Cola de laboratorio (PENDING_LAB_PAYMENT)
  const [labPayments, setLabPayments] = useState<AppointmentListItem[]>([]);
  const [loadingLab, setLoadingLab] = useState(true);
  const [labSearch, setLabSearch] = useState('');

  // Cola de farmacia (PENDING_PHARMACY_PAYMENT)
  const [pharmacyPayments, setPharmacyPayments] = useState<AppointmentListItem[]>([]);
  const [loadingPharmacy, setLoadingPharmacy] = useState(true);
  const [pharmacySearch, setPharmacySearch] = useState('');

  // Modal
  const [selectedAppointment, setSelectedAppointment] = useState<AppointmentListItem | null>(null);
  const [selectedPaymentType, setSelectedPaymentType] = useState<PaymentType>('CONSULTATION');
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [loadingInvoice, setLoadingInvoice] = useState(false);

  const loadConsultations = useCallback(async () => {
    setLoadingConsultations(true);
    try {
      const data = await listAppointments({ queue: 'payment' });
      setConsultations(data.filter(a => a.payment.status !== 'PAID' && a.payment.status !== 'CANCELLED'));
    } catch {
      // Error shown via empty state
    } finally {
      setLoadingConsultations(false);
    }
  }, []);

  const loadLabPayments = useCallback(async () => {
    setLoadingLab(true);
    try {
      const data = await listAppointments({ status: ['PENDING_LAB_PAYMENT'] });
      setLabPayments(data);
    } catch {
      // Error shown via empty state
    } finally {
      setLoadingLab(false);
    }
  }, []);

  const loadPharmacyPayments = useCallback(async () => {
    setLoadingPharmacy(true);
    try {
      const data = await listAppointments({ status: ['PENDING_PHARMACY_PAYMENT'] });
      setPharmacyPayments(data);
    } catch {
      // Error shown via empty state
    } finally {
      setLoadingPharmacy(false);
    }
  }, []);

  useEffect(() => {
    loadConsultations();
    loadLabPayments();
    loadPharmacyPayments();
  }, [loadConsultations, loadLabPayments, loadPharmacyPayments]);

  const handlePay = async (appt: AppointmentListItem, type: PaymentType) => {
    if (!appt.payment.invoiceId) return;
    
    setSelectedAppointment(appt);
    setSelectedPaymentType(type);
    setLoadingInvoice(true);
    
    try {
      const invoice = await getInvoiceById(appt.payment.invoiceId);
      setSelectedInvoice(invoice);
    } catch {
      alert('Error al cargar los detalles de la factura');
      setSelectedAppointment(null);
    } finally {
      setLoadingInvoice(false);
    }
  };

  const handlePaymentSuccess = () => {
    setSelectedAppointment(null);
    setSelectedInvoice(null);
    // Recarga todas las listas
    loadConsultations();
    loadLabPayments();
    loadPharmacyPayments();
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
                {loadingConsultations ? '…' : consultations.filter(a => !consultationSearch || a.patient.dpi?.includes(consultationSearch.trim())).length}
              </span>
            </div>
            <div className="flex items-center gap-3">
              <input
                type="text"
                inputMode="numeric"
                value={consultationSearch}
                onChange={e => { if (/^\d*$/.test(e.target.value)) setConsultationSearch(e.target.value); }}
                placeholder="Buscar por DPI..."
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-medin-cyan focus:border-transparent w-44"
              />
              <button
                onClick={loadConsultations}
                disabled={loadingConsultations}
                className="text-xs text-medin-navy hover:text-medin-navy/70 disabled:opacity-50"
              >
                Actualizar
              </button>
            </div>
          </div>
          <AppointmentTable
            appointments={consultations.filter(a => !consultationSearch || a.patient.dpi?.includes(consultationSearch.trim()))}
            loading={loadingConsultations}
            onPay={appt => handlePay(appt, 'CONSULTATION')}
            emptyText={consultationSearch ? 'No se encontraron cobros para ese DPI' : 'No hay cobros de consulta pendientes'}
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
                {loadingLab ? '…' : labPayments.filter(a => !labSearch || a.patient.dpi?.includes(labSearch.trim())).length}
              </span>
            </div>
            <div className="flex items-center gap-3">
              <input
                type="text"
                inputMode="numeric"
                value={labSearch}
                onChange={e => { if (/^\d*$/.test(e.target.value)) setLabSearch(e.target.value); }}
                placeholder="Buscar por DPI..."
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-purple-500 focus:border-transparent w-44"
              />
              <button
                onClick={loadLabPayments}
                disabled={loadingLab}
                className="text-xs text-purple-700 hover:text-purple-500 disabled:opacity-50"
              >
                Actualizar
              </button>
            </div>
          </div>
          <AppointmentTable
            appointments={labPayments.filter(a => !labSearch || a.patient.dpi?.includes(labSearch.trim()))}
            loading={loadingLab}
            onPay={appt => handlePay(appt, 'LAB')}
            emptyText={labSearch ? 'No se encontraron cobros para ese DPI' : 'No hay cobros de laboratorio pendientes'}
          />
        </div>

        {/* ── Lista 3: Cobros de Farmacia ── */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gradient-to-r from-green-500/5 to-transparent">
            <div className="flex items-center gap-3">
              <span className="text-xl">💊</span>
              <div>
                <h3 className="text-sm font-semibold text-gray-900">Cobros de Farmacia</h3>
                <p className="text-xs text-gray-500">Pacientes con medicamentos pendientes de pago</p>
              </div>
              <span className="ml-2 px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-semibold">
                {loadingPharmacy ? '…' : pharmacyPayments.filter(a => !pharmacySearch || a.patient.dpi?.includes(pharmacySearch.trim())).length}
              </span>
            </div>
            <div className="flex items-center gap-3">
              <input
                type="text"
                inputMode="numeric"
                value={pharmacySearch}
                onChange={e => { if (/^\d*$/.test(e.target.value)) setPharmacySearch(e.target.value); }}
                placeholder="Buscar por DPI..."
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-green-500 focus:border-transparent w-44"
              />
              <button
                onClick={loadPharmacyPayments}
                disabled={loadingPharmacy}
                className="text-xs text-green-700 hover:text-green-500 disabled:opacity-50"
              >
                Actualizar
              </button>
            </div>
          </div>
          <AppointmentTable
            appointments={pharmacyPayments.filter(a => !pharmacySearch || a.patient.dpi?.includes(pharmacySearch.trim()))}
            loading={loadingPharmacy}
            onPay={appt => handlePay(appt, 'PHARMACY')}
            emptyText={pharmacySearch ? 'No se encontraron cobros para ese DPI' : 'No hay cobros de farmacia pendientes'}
          />
        </div>
      </div>

      {/* Modal de pago */}
      {selectedAppointment && selectedInvoice && !loadingInvoice && (
        <PaymentModal
          invoice={selectedInvoice}
          paymentType={selectedPaymentType}
          appointmentId={selectedAppointment.id}
          onSuccess={handlePaymentSuccess}
          onClose={() => {
            setSelectedAppointment(null);
            setSelectedInvoice(null);
          }}
        />
      )}

      {/* Loading invoice */}
      {selectedAppointment && loadingInvoice && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6 text-center">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent mb-4" />
            <p className="text-gray-700">Cargando detalles de la factura...</p>
          </div>
        </div>
      )}
    </MainLayout>
  );
};

export default CashierPage;
