import { useState, useEffect } from 'react';
import type { FC, ChangeEvent, FormEvent } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';
import { useAuth } from '../hooks/useAuth';
import { createInvoice, getInvoiceById, processPayment, type Invoice } from '../services/billingService';
import { createAppointment, releaseHold } from '../services/appointmentService';
import type { AppointmentResponse } from '../services/appointmentService';

interface PaymentLocationState {
  date: string;
  time: string;
  notes: string;
  sessionId: string;
}

const fmt12 = (t: string) => {
  const [h, m] = t.split(':').map(Number);
  const period = h < 12 ? 'AM' : 'PM';
  const dh = h === 0 ? 12 : h > 12 ? h - 12 : h;
  return `${dh}:${String(m).padStart(2, '0')} ${period}`;
};

/**
 * Calculates the time window for QR code validity.
 * @param appointmentTime Time in HH:mm:ss format
 * @returns Object with validFrom and validUntil in 12-hour format
 */
const calculateTimeWindow = (appointmentTime: string): { validFrom: string; validUntil: string } => {
  const [hours, minutes] = appointmentTime.split(':').map(Number);
  
  // Calculate validFrom (15 minutes before)
  let fromMinutes = hours * 60 + minutes - 15;
  if (fromMinutes < 0) fromMinutes += 24 * 60;
  const fromHours = Math.floor(fromMinutes / 60) % 24;
  const fromMins = fromMinutes % 60;
  
  // Calculate validUntil (60 minutes after)
  let untilMinutes = hours * 60 + minutes + 60;
  const untilHours = Math.floor(untilMinutes / 60) % 24;
  const untilMins = untilMinutes % 60;
  
  // Format to 12-hour
  const formatTime = (h: number, m: number) => {
    const period = h < 12 ? 'AM' : 'PM';
    const displayHour = h === 0 ? 12 : h > 12 ? h - 12 : h;
    return `${displayHour}:${String(m).padStart(2, '0')} ${period}`;
  };
  
  return {
    validFrom: formatTime(fromHours, fromMins),
    validUntil: formatTime(untilHours, untilMins),
  };
};

/**
 * Downloads the QR code as a PNG file.
 * @param qrCodeBase64 Base64-encoded QR code image
 * @param appointmentId Appointment ID for filename
 */
const downloadQR = (qrCodeBase64: string, appointmentId: string) => {
  const link = document.createElement('a');
  link.href = `data:image/png;base64,${qrCodeBase64}`;
  link.download = `cita-${appointmentId}.png`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

const PaymentGatewayPage: FC = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as PaymentLocationState | null;

  const [feePrice, setFeePrice] = useState<number | null>(null);
  const [feeDescription, setFeeDescription] = useState('Consulta médica general');
  const [loadingFee, setLoadingFee] = useState(true);

  const [card, setCard] = useState({ number: '', name: '', expiry: '', cvv: '' });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [paymentError, setPaymentError] = useState<string | null>(null);
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [appointment, setAppointment] = useState<AppointmentResponse | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/payment' } });
    }
  }, [isAuthenticated, navigate]);

  // Fetch consultation fee - hardcoded for now
  useEffect(() => {
    setFeePrice(150);
    setFeeDescription('Consulta General');
    setLoadingFee(false);
  }, []);

  const formatCardNumber = (val: string) =>
    val.replace(/\D/g, '').slice(0, 16).replace(/(.{4})/g, '$1 ').trim();

  const formatExpiry = (val: string) => {
    const digits = val.replace(/\D/g, '').slice(0, 4);
    return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
  };

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    let formatted = value;
    if (name === 'number') formatted = formatCardNumber(value);
    if (name === 'expiry') formatted = formatExpiry(value);
    if (name === 'cvv') formatted = value.replace(/\D/g, '').slice(0, 4);
    setCard(prev => ({ ...prev, [name]: formatted }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const e: Record<string, string> = {};
    if (card.number.replace(/\s/g, '').length < 16) e.number = 'Número de tarjeta inválido';
    if (!card.name.trim()) e.name = 'Ingresa el nombre del titular';
    if (card.expiry.length < 5) e.expiry = 'Fecha inválida (MM/AA)';
    if (card.cvv.length < 3) e.cvv = 'CVV inválido';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate() || feePrice === null || !state) return;
    setIsLoading(true);
    setPaymentError(null);
    try {
      // 1. Create the appointment — el backend crea y vincula la factura automáticamente
      const appointmentResponse = await createAppointment({
        appointmentDate: state.date,
        appointmentTime: state.time,
        notes: state.notes,
        sessionId: state.sessionId,
      });
      setAppointment(appointmentResponse);

      // 2. Obtener la factura vinculada al appointment (creada por el backend)
      //    Si no viene invoiceId (billing-service falló), crear una nueva como fallback
      let inv: Invoice;
      if (appointmentResponse.invoiceId) {
        inv = await getInvoiceById(appointmentResponse.invoiceId);
      } else {
        inv = await createInvoice({
          patientId: user!.id,
          charges: [
            { type: 'CONSULTATION', description: feeDescription, quantity: 1, unitPrice: feePrice },
          ],
        });
      }

      // 3. Procesar pago sobre la factura vinculada al appointment
      await processPayment(inv.id, {
        amount: feePrice,
        method: 'CARD',
        nit: 'CF',
        customerName: user!.fullName,
      });

      releaseHold(state.sessionId).catch(() => {});
      setInvoice(inv);
    } catch {
      setPaymentError('No se pudo procesar el pago o reservar la cita. Intenta de nuevo.');
    } finally {
      setIsLoading(false);
    }
  };

  const inputClass = (field: string) =>
    `w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan transition-colors${errors[field] ? ' border-red-400 bg-red-50' : ' border-gray-300'}`;

  const feeLabel = feePrice !== null ? `Q ${feePrice.toFixed(2)}` : '...';

  // ── Success screen ──────────────────────────────────────────────────────────
  if (invoice) {
    const timeWindow = state ? calculateTimeWindow(state.time) : null;
    
    return (
      <div className="min-h-screen bg-white flex flex-col">
        <Navbar />
        <main className="flex-1 flex items-center justify-center p-6">
          <div className="text-center max-w-md w-full">
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">¡Pago confirmado!</h2>
            <p className="text-gray-500 text-sm mb-6">
              Hola, <span className="font-medium text-medin-navy">{user?.fullName}</span>. Tu cita ha sido registrada.
            </p>

            {/* Resumen de cita y pago */}
            <div className="bg-medin-navy text-white rounded-xl p-5 mb-6 text-left space-y-3">
              {state && (
                <>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-400">Fecha</span>
                    <span className="font-medium">{state.date}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-400">Hora</span>
                    <span className="font-medium">{fmt12(state.time)}</span>
                  </div>
                  <div className="border-t border-white/10 pt-2" />
                </>
              )}
              <div className="flex justify-between text-sm">
                <span className="text-gray-400">Factura</span>
                <span className="font-mono font-medium text-medin-cyan">{invoice.invoiceNumber}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-400">Concepto</span>
                <span className="font-medium">{feeDescription}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-400 text-sm">Total pagado</span>
                <span className="text-medin-cyan font-bold text-lg">{feeLabel}</span>
              </div>
            </div>

            {/* QR Code Section */}
            {appointment?.qrCodeBase64 && timeWindow && (
              <div className="bg-gray-50 border-2 border-medin-cyan rounded-xl p-5 mb-6">
                <h3 className="text-lg font-bold text-medin-navy mb-3">Tu código QR de confirmación</h3>
                <div className="flex justify-center mb-4">
                  <img
                    src={`data:image/png;base64,${appointment.qrCodeBase64}`}
                    alt="QR Code de Cita"
                    className="w-48 h-48 border-2 border-medin-cyan rounded-lg"
                  />
                </div>
                <div className="text-sm text-gray-700 space-y-2 text-left">
                  <p className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-medin-cyan flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span><strong>QR válido desde</strong> {timeWindow.validFrom} <strong>hasta</strong> {timeWindow.validUntil}</span>
                  </p>
                  <p className="flex items-start gap-2">
                    <svg className="w-5 h-5 text-medin-cyan flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span>Presenta este código en recepción el día de tu cita</span>
                  </p>
                </div>
                <button
                  onClick={() => downloadQR(appointment.qrCodeBase64!, appointment.id)}
                  className="w-full mt-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors flex items-center justify-center gap-2"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                  </svg>
                  Descargar QR
                </button>
              </div>
            )}

            <button
              onClick={() => navigate('/patient-dashboard')}
              className="w-full py-3 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors"
            >
              Ver mis citas
            </button>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  // ── Payment form ────────────────────────────────────────────────────────────
  return (
    <div className="min-h-screen bg-white flex flex-col">
      <Navbar />

      <section className="bg-gradient-to-r from-gray-100 to-blue-50 py-8 md:py-12">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          <h1 className="text-3xl md:text-4xl font-bold text-medin-navy">Pasarela de Pago</h1>
          <p className="text-gray-600 mt-2 text-sm">Completa el pago para confirmar tu cita</p>
        </div>
      </section>

      <main className="flex-1 py-10 px-4">
        <div className="max-w-md mx-auto space-y-6">

          {/* Resumen de la cita */}
          <div className="bg-medin-navy text-white rounded-xl p-5">
            {state && (
              <div className="flex justify-between text-sm mb-3">
                <span className="text-gray-400">Cita agendada</span>
                <span className="font-medium">{state.date} — {fmt12(state.time)}</span>
              </div>
            )}
            <p className="text-gray-300 text-sm mb-1">Monto a pagar</p>
            {loadingFee ? (
              <p className="text-2xl font-bold text-medin-cyan animate-pulse">Cargando...</p>
            ) : (
              <p className="text-3xl font-bold text-medin-cyan">{feeLabel}</p>
            )}
            <p className="text-gray-400 text-xs mt-2">{feeDescription} — MedFlow Hospital</p>
          </div>

          {/* Error de pago */}
          {paymentError && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
              <p className="text-red-800 text-sm">{paymentError}</p>
            </div>
          )}

          {/* Formulario de tarjeta */}
          <form onSubmit={handleSubmit} className="space-y-4 bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
            <h3 className="font-semibold text-gray-800 mb-2">Datos de la tarjeta</h3>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Número de tarjeta</label>
              <input
                name="number"
                value={card.number}
                onChange={handleChange}
                placeholder="0000 0000 0000 0000"
                className={inputClass('number')}
              />
              {errors.number && <p className="text-red-500 text-xs mt-1">{errors.number}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Titular de la tarjeta</label>
              <input
                name="name"
                value={card.name}
                onChange={handleChange}
                placeholder="Nombre como aparece en la tarjeta"
                className={inputClass('name')}
              />
              {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Vencimiento</label>
                <input
                  name="expiry"
                  value={card.expiry}
                  onChange={handleChange}
                  placeholder="MM/AA"
                  className={inputClass('expiry')}
                />
                {errors.expiry && <p className="text-red-500 text-xs mt-1">{errors.expiry}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">CVV</label>
                <input
                  name="cvv"
                  value={card.cvv}
                  onChange={handleChange}
                  placeholder="123"
                  className={inputClass('cvv')}
                />
                {errors.cvv && <p className="text-red-500 text-xs mt-1">{errors.cvv}</p>}
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading || loadingFee}
              className="w-full py-3 mt-2 bg-medin-cyan text-medin-navy font-bold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {isLoading ? (
                <>
                  <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  Procesando...
                </>
              ) : (
                <>
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                  Pagar {feeLabel}
                </>
              )}
            </button>

            <p className="text-center text-xs text-gray-400 mt-2">
              Transacción simulada — entorno de demostración
            </p>
          </form>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default PaymentGatewayPage;
