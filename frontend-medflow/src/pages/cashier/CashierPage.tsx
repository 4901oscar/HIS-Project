import { useState, useEffect } from 'react';
import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import { listAppointments } from '../../services/appointmentService';
import type { AppointmentListItem } from '../../services/appointmentService';
import PaymentModal from '../../components/Cashier/PaymentModal';

const CashierPage: FC = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<AppointmentListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedAppointment, setSelectedAppointment] = useState<AppointmentListItem | null>(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  const loadPendingPayments = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await listAppointments({ queue: 'payment' });
      setAppointments(data.filter(a => a.payment.status !== 'PAID' && a.payment.status !== 'CANCELLED'));
    } catch (err) {
      setError('No se pudo cargar la lista de cobros pendientes.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPendingPayments();
  }, []);

  const handleProcessPayment = (appointment: AppointmentListItem) => {
    setSelectedAppointment(appointment);
    setShowPaymentModal(true);
  };

  const handlePaymentSuccess = () => {
    setShowPaymentModal(false);
    setSelectedAppointment(null);
    loadPendingPayments(); // Recargar lista
  };

  const handleCloseModal = () => {
    setShowPaymentModal(false);
    setSelectedAppointment(null);
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/dashboard')}
            className="text-gray-400 hover:text-gray-600"
          >
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Módulo de Caja</h2>
            <p className="text-gray-500 text-sm">Gestiona los cobros y pagos de pacientes</p>
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">
            {error}
          </div>
        )}

        {/* Loading */}
        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" />
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
              <h3 className="text-lg font-semibold text-gray-900">
                Pacientes con Cobros Pendientes ({appointments.length})
              </h3>
            </div>

            {appointments.length === 0 ? (
              <div className="text-center py-16 text-gray-500">
                <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <p className="text-lg font-medium">No hay cobros pendientes</p>
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
                      <th className="pb-2 pr-4">Nº Factura</th>
                      <th className="pb-2 pr-4">Estado</th>
                      <th className="pb-2 pr-4">Total</th>
                      <th className="pb-2 pr-6">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {appointments
                      .slice()
                      .sort((a, b) => {
                        const dateCompare = a.appointmentDate.toString().localeCompare(b.appointmentDate.toString());
                        if (dateCompare !== 0) return dateCompare;
                        return a.appointmentTime.toString().localeCompare(b.appointmentTime.toString());
                      })
                      .map((appt) => (
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
                            <span
                              className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                                appt.payment.statusColor === 'green' ? 'bg-green-100 text-green-800' :
                                appt.payment.statusColor === 'orange' ? 'bg-orange-100 text-orange-800' :
                                appt.payment.statusColor === 'red' ? 'bg-red-100 text-red-700' :
                                'bg-gray-100 text-gray-600'
                              }`}
                            >
                              {appt.payment.statusLabel}
                            </span>
                          </td>
                          <td className="py-3 pr-4 whitespace-nowrap">
                            <span className="text-base font-bold text-medin-navy">
                              Q {appt.payment.amount?.toFixed(2) ?? '0.00'}
                            </span>
                          </td>
                          <td className="py-3 pr-6 whitespace-nowrap text-right">
                            <button
                              onClick={() => handleProcessPayment(appt)}
                              className="px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm"
                            >
                              Procesar Pago
                            </button>
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Payment Modal */}
      {showPaymentModal && selectedAppointment && (
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
              charges: []
            }}
            onSuccess={handlePaymentSuccess}
            onClose={handleCloseModal}
          />
        ) : (
          <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6 text-center">
              <p className="text-gray-700 mb-4">
                No se encontró información de factura para esta cita.<br />
                Consulta al administrador o recarga la página.
              </p>
              <button
                onClick={handleCloseModal}
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
