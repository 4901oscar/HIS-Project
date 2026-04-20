import { useState, useEffect } from 'react';
import type { FC, ChangeEvent, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';
import { useAuth } from '../hooks/useAuth';

const APPOINTMENT_FEE = 'Q 150.00';

const PaymentGatewayPage: FC = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const [card, setCard] = useState({ number: '', name: '', expiry: '', cvv: '' });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [paid, setPaid] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/payment' } });
    }
  }, [isAuthenticated, navigate]);

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
    if (!validate()) return;
    setIsLoading(true);
    await new Promise(r => setTimeout(r, 2000));
    setIsLoading(false);
    setPaid(true);
  };

  const inputClass = (field: string) =>
    `w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan transition-colors${errors[field] ? ' border-red-400 bg-red-50' : ' border-gray-300'}`;

  if (paid) {
    return (
      <div className="min-h-screen bg-white flex flex-col">
        <Navbar />
        <main className="flex-1 flex items-center justify-center p-6">
          <div className="text-center max-w-md">
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">¡Pago confirmado!</h2>
            <p className="text-gray-600 mb-2">
              Se realizó un cargo de <span className="font-semibold text-medin-navy">{APPOINTMENT_FEE}</span> a tu tarjeta.
            </p>
            <p className="text-gray-500 text-sm mb-8">
              Bienvenido, <span className="font-medium">{user?.fullName}</span>. Ahora puedes agendar tu cita.
            </p>
            <button
              onClick={() => navigate('/appointment')}
              className="px-8 py-3 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors"
            >
              Agendar mi cita
            </button>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white flex flex-col">
      <Navbar />

      <section className="bg-gradient-to-r from-gray-100 to-blue-50 py-8 md:py-12">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          <h1 className="text-3xl md:text-4xl font-bold text-medin-navy">Pasarela de Pago</h1>
          <p className="text-gray-600 mt-2 text-sm">Completa el pago para continuar con tu cita</p>
        </div>
      </section>

      <main className="flex-1 py-10 px-4">
        <div className="max-w-md mx-auto">
          {/* Resumen */}
          <div className="bg-medin-navy text-white rounded-xl p-5 mb-6">
            <p className="text-gray-300 text-sm mb-1">Monto a pagar</p>
            <p className="text-3xl font-bold text-medin-cyan">{APPOINTMENT_FEE}</p>
            <p className="text-gray-400 text-xs mt-2">Consulta médica general — MedFlow Hospital</p>
          </div>

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
              disabled={isLoading}
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
                  Pagar {APPOINTMENT_FEE}
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
