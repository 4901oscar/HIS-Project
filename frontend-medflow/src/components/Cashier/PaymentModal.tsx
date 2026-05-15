import { useState, type FC, type FormEvent } from 'react';
import { processPayment, type Invoice, type PaymentRequest } from '../../services/billingService';
import { confirmLabPayment, confirmPharmacyPayment } from '../../services/appointmentService';

export type PaymentType = 'CONSULTATION' | 'LAB' | 'PHARMACY';

interface PaymentModalProps {
  invoice: Invoice;
  onSuccess: () => void;
  onClose: () => void;
  paymentType?: PaymentType;
  appointmentId?: string;
}

const PaymentModal: FC<PaymentModalProps> = ({ invoice, onSuccess, onClose, paymentType = 'CONSULTATION', appointmentId }) => {
  const [nit, setNit] = useState('CF');
  const [customerName, setCustomerName] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD' | 'TRANSFER'>('CASH');
  const [amountReceived, setAmountReceived] = useState<string>(invoice.total.toFixed(2));
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [paymentSuccess, setPaymentSuccess] = useState(false);
  const [changeAmount, setChangeAmount] = useState<number>(0);

  const inputCls = (field: string) =>
    `w-full px-3 py-2 border ${fieldErrors[field] ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan text-sm`;

  const calculateChange = (): number => {
    const received = parseFloat(amountReceived) || 0;
    return Math.max(0, received - invoice.total);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    const errors: Record<string, string> = {};

    if (!nit.trim()) errors.nit = 'El NIT es obligatorio';
    if (!customerName.trim()) errors.customerName = 'El nombre del cliente es obligatorio';

    const received = parseFloat(amountReceived) || 0;
    if (paymentMethod === 'CASH' && received < invoice.total) {
      errors.amountReceived = `El monto recibido debe ser al menos Q ${invoice.total.toFixed(2)}`;
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors({});

    setProcessing(true);

    try {
      const paymentData: PaymentRequest = {
        amount: received,
        method: paymentMethod,
        nit: nit.trim(),
        customerName: customerName.trim(),
      };

      const response = await processPayment(invoice.id, paymentData);
      setChangeAmount(response.change);

      // Para lab: notificar al clinical-service que el pago fue confirmado
      if (paymentType === 'LAB' && appointmentId) {
        await confirmLabPayment(appointmentId, invoice.id);
      }

      // Para pharmacy: notificar al clinical-service que el pago fue confirmado
      if (paymentType === 'PHARMACY' && appointmentId) {
        await confirmPharmacyPayment(appointmentId, invoice.id);
      }

      setPaymentSuccess(true);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Error al procesar el pago';
      setError(message);
    } finally {
      setProcessing(false);
    }
  };

  if (paymentSuccess) {
    return (
      <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100 mb-4">
              <svg className="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">¡Pago Procesado!</h3>
            <p className="text-gray-600 mb-4">La factura ha sido pagada exitosamente</p>

            <div className="bg-gray-50 rounded-lg p-4 mb-4">
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm text-gray-600">Factura:</span>
                <span className="font-mono font-semibold text-gray-900">{invoice.invoiceNumber}</span>
              </div>
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm text-gray-600">Total:</span>
                <span className="font-bold text-lg text-medin-navy">Q {invoice.total.toFixed(2)}</span>
              </div>
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm text-gray-600">Recibido:</span>
                <span className="font-semibold text-gray-900">Q {parseFloat(amountReceived).toFixed(2)}</span>
              </div>
              {changeAmount > 0 && (
                <div className="flex justify-between items-center pt-2 border-t border-gray-200">
                  <span className="text-sm font-medium text-gray-700">Cambio:</span>
                  <span className="font-bold text-lg text-green-600">Q {changeAmount.toFixed(2)}</span>
                </div>
              )}
            </div>

            <button
              onClick={onSuccess}
              className="w-full py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors"
            >
              Continuar
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h3 className="text-xl font-bold text-gray-900">
            {paymentType === 'LAB' ? '🔬 Pago de Laboratorio' : paymentType === 'PHARMACY' ? '💊 Pago de Farmacia' : '🏥 Pago de Consulta'}
          </h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
            disabled={processing}
          >
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* Invoice Details */}
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex justify-between items-center mb-3">
              <span className="text-sm font-medium text-gray-600">Factura:</span>
              <span className="font-mono font-semibold text-gray-900">{invoice.invoiceNumber}</span>
            </div>

            <div className="border-t border-gray-200 pt-3 space-y-2">
              <h4 className="text-sm font-semibold text-gray-700 mb-2">Servicios:</h4>
              {invoice.charges.map((charge, index) => (
                <div key={index} className="flex justify-between text-sm">
                  <span className="text-gray-600">
                    {charge.description}
                  </span>
                  <span className="font-medium text-gray-900">
                    Q {charge.subtotal.toFixed(2)}
                  </span>
                </div>
              ))}
            </div>

            <div className="border-t border-gray-200 mt-3 pt-3 space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Subtotal:</span>
                <span className="font-medium text-gray-900">Q {invoice.subtotal.toFixed(2)}</span>
              </div>
              {invoice.discountAmount > 0 && (
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">Descuento:</span>
                  <span className="font-medium text-red-600">-Q {invoice.discountAmount.toFixed(2)}</span>
                </div>
              )}
              <div className="flex justify-between items-center pt-2 border-t border-gray-300">
                <span className="text-base font-bold text-gray-900">Total a Pagar:</span>
                <span className="text-2xl font-bold text-medin-navy">Q {invoice.total.toFixed(2)}</span>
              </div>
            </div>
          </div>

          {/* Payment Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* NIT */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                NIT <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={nit}
                onChange={(e) => { setNit(e.target.value); setFieldErrors(fe => ({ ...fe, nit: '' })); }}
                placeholder="CF o NIT del cliente"
                className={inputCls('nit')}
                disabled={processing}
              />
              {fieldErrors.nit && <p className="mt-1 text-xs text-red-600">{fieldErrors.nit}</p>}
            </div>

            {/* Customer Name */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nombre del Cliente <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={customerName}
                onChange={(e) => { setCustomerName(e.target.value); setFieldErrors(fe => ({ ...fe, customerName: '' })); }}
                placeholder="Nombre completo"
                className={inputCls('customerName')}
                disabled={processing}
              />
              {fieldErrors.customerName && <p className="mt-1 text-xs text-red-600">{fieldErrors.customerName}</p>}
            </div>

            {/* Payment Method */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Forma de Pago <span className="text-red-500">*</span>
              </label>
              <div className="grid grid-cols-3 gap-3">
                <button
                  type="button"
                  onClick={() => setPaymentMethod('CASH')}
                  disabled={processing}
                  className={`py-3 px-4 rounded-lg border-2 font-medium text-sm transition-colors ${
                    paymentMethod === 'CASH'
                      ? 'border-medin-cyan bg-medin-cyan/10 text-medin-navy'
                      : 'border-gray-300 text-gray-600 hover:border-gray-400'
                  }`}
                >
                  💵 Efectivo
                </button>
                <button
                  type="button"
                  onClick={() => setPaymentMethod('CARD')}
                  disabled={processing}
                  className={`py-3 px-4 rounded-lg border-2 font-medium text-sm transition-colors ${
                    paymentMethod === 'CARD'
                      ? 'border-medin-cyan bg-medin-cyan/10 text-medin-navy'
                      : 'border-gray-300 text-gray-600 hover:border-gray-400'
                  }`}
                >
                  💳 Tarjeta
                </button>
                <button
                  type="button"
                  onClick={() => setPaymentMethod('TRANSFER')}
                  disabled={processing}
                  className={`py-3 px-4 rounded-lg border-2 font-medium text-sm transition-colors ${
                    paymentMethod === 'TRANSFER'
                      ? 'border-medin-cyan bg-medin-cyan/10 text-medin-navy'
                      : 'border-gray-300 text-gray-600 hover:border-gray-400'
                  }`}
                >
                  🏦 Transferencia
                </button>
              </div>
            </div>

            {/* Amount Received (only for CASH) */}
            {paymentMethod === 'CASH' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Monto Recibido <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  inputMode="decimal"
                  value={amountReceived}
                  onChange={(e) => {
                    const val = e.target.value;
                    if (/^\d*\.?\d{0,2}$/.test(val)) setAmountReceived(val);
                    setFieldErrors(fe => ({ ...fe, amountReceived: '' }));
                  }}
                  className={inputCls('amountReceived')}
                  disabled={processing}
                />
                {fieldErrors.amountReceived
                  ? <p className="mt-1 text-xs text-red-600">{fieldErrors.amountReceived}</p>
                  : calculateChange() > 0 && (
                    <p className="mt-2 text-sm text-gray-600">
                      Cambio a devolver: <span className="font-bold text-green-600">Q {calculateChange().toFixed(2)}</span>
                    </p>
                  )
                }
              </div>
            )}

            {/* Error de servidor */}
            {error && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">
                {error}
              </div>
            )}

            {/* Actions */}
            <div className="flex gap-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                disabled={processing}
                className="flex-1 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50 disabled:opacity-50"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={processing}
                className="flex-1 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white disabled:opacity-50 transition-colors"
              >
                {processing ? 'Procesando...' : 'Confirmar Pago'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default PaymentModal;
