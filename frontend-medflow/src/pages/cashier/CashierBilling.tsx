import { useState, useEffect } from 'react';
import type { FC, FormEvent } from 'react';
import { MainLayout } from '../../components/Layout';
import { ArrowPathIcon, PlusIcon, TrashIcon } from '@heroicons/react/24/outline';
import {
  getInvoices,
  createInvoice,
  processPayment,
  cancelInvoice,
} from '../../services/billingService';
import type { InvoiceResponse, InvoiceStatus, ChargeRequest, ChargeType, PaymentMethod } from '../../services/billingService';
import { searchPatients } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import axios from 'axios';

const statusLabel: Record<InvoiceStatus, string> = {
  PENDING: 'Pendiente',
  PAID: 'Pagada',
  CANCELLED: 'Cancelada',
};

const statusColor: Record<InvoiceStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  PAID: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
};

const chargeTypes: ChargeType[] = ['CONSULTATION', 'LABORATORY', 'MEDICATION', 'OTHER'];
const chargeTypeLabel: Record<ChargeType, string> = {
  CONSULTATION: 'Consulta',
  LABORATORY: 'Laboratorio',
  MEDICATION: 'Medicamento',
  OTHER: 'Otro',
};
const paymentMethods: PaymentMethod[] = ['CASH', 'CARD', 'TRANSFER'];
const paymentMethodLabel: Record<PaymentMethod, string> = {
  CASH: 'Efectivo',
  CARD: 'Tarjeta',
  TRANSFER: 'Transferencia',
};

const CashierBilling: FC = () => {
  const [tab, setTab] = useState<'list' | 'create'>('list');
  const [filter, setFilter] = useState<InvoiceStatus | ''>('');
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // ── Crear factura ─────────────��───────────────────────────────────────────
  const [patientSearch, setPatientSearch] = useState('');
  const [searchResults, setSearchResults] = useState<PatientResponse[]>([]);
  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null);
  const [charges, setCharges] = useState<ChargeRequest[]>([]);
  const [newCharge, setNewCharge] = useState<ChargeRequest>({ type: 'CONSULTATION', description: '', quantity: 1, unitPrice: 0 });
  const [creating, setCreating] = useState(false);

  // ── Pago ────────────────────���────────────────────────────────────────────
  const [payingId, setPayingId] = useState<string | null>(null);
  const [payAmount, setPayAmount] = useState('');
  const [payMethod, setPayMethod] = useState<PaymentMethod>('CASH');
  const [payLoading, setPayLoading] = useState(false);
  const [payResult, setPayResult] = useState<{ change: number; method: string } | null>(null);

  const flash = (msg: string) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(null), 4000);
  };

  const loadInvoices = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getInvoices(filter || undefined);
      setInvoices(data);
    } catch (err) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Error al cargar facturas');
      else setError('Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (tab === 'list') loadInvoices();
  }, [filter, tab]);

  const handleSearchPatient = async () => {
    if (!patientSearch.trim()) return;
    try {
      const res = await searchPatients(patientSearch);
      setSearchResults(res);
    } catch {
      setError('Error al buscar paciente');
    }
  };

  const addCharge = () => {
    if (!newCharge.description.trim() || newCharge.unitPrice <= 0) return;
    setCharges((prev) => [...prev, { ...newCharge }]);
    setNewCharge({ type: 'CONSULTATION', description: '', quantity: 1, unitPrice: 0 });
  };

  const removeCharge = (idx: number) => setCharges((prev) => prev.filter((_, i) => i !== idx));

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    if (!selectedPatient || charges.length === 0) return;
    setCreating(true);
    setError(null);
    try {
      await createInvoice(selectedPatient.id, charges);
      flash('Factura creada exitosamente');
      setTab('list');
      setSelectedPatient(null);
      setCharges([]);
      setPatientSearch('');
    } catch (err) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Error al crear factura');
      else setError('Error al conectar con el servidor');
    } finally {
      setCreating(false);
    }
  };

  const handlePay = async (e: FormEvent) => {
    e.preventDefault();
    if (!payingId) return;
    setPayLoading(true);
    setError(null);
    setPayResult(null);
    try {
      const res = await processPayment(payingId, Number(payAmount), payMethod);
      setPayResult({ change: res.change, method: res.method });
      setInvoices((prev) => prev.map((inv) => (inv.id === payingId ? { ...inv, status: 'PAID' } : inv)));
      flash('Pago procesado exitosamente');
      setPayingId(null);
      setPayAmount('');
    } catch (err) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Error al procesar pago');
      else setError('Error al conectar con el servidor');
    } finally {
      setPayLoading(false);
    }
  };

  const handleCancel = async (id: string, num: string) => {
    if (!confirm(`¿Cancelar factura ${num}?`)) return;
    setError(null);
    try {
      const updated = await cancelInvoice(id);
      setInvoices((prev) => prev.map((inv) => (inv.id === id ? updated : inv)));
      flash(`Factura ${num} cancelada`);
    } catch (err) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Error al cancelar factura');
      else setError('Error al conectar con el servidor');
    }
  };

  const inputClass = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';

  const total = charges.reduce((sum, c) => sum + c.quantity * c.unitPrice, 0);

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Caja y Facturación</h2>
          <p className="mt-1 text-sm text-gray-600">Procesar pagos y generar facturas</p>
        </div>

        {successMsg && <div className="p-3 bg-green-50 border border-green-200 rounded-lg text-green-800 text-sm">{successMsg}</div>}
        {error && <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>}

        {payResult && (
          <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-800">
            Pago recibido — Método: {paymentMethodLabel[payResult.method as PaymentMethod] || payResult.method} — Vuelto: Q{payResult.change.toFixed(2)}
          </div>
        )}

        {/* Tabs */}
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            {(['list', 'create'] as const).map((t) => (
              <button
                key={t}
                onClick={() => { setTab(t); setError(null); }}
                className={`py-3 px-1 border-b-2 font-medium text-sm ${
                  tab === t ? 'border-medin-cyan text-medin-cyan' : 'border-transparent text-gray-500 hover:text-gray-700'
                }`}
              >
                {t === 'list' ? 'Facturas' : 'Nueva Factura'}
              </button>
            ))}
          </nav>
        </div>

        {/* ── Tab: Lista de facturas ── */}
        {tab === 'list' && (
          <>
            <div className="flex flex-wrap gap-2 items-center">
              {(['', 'PENDING', 'PAID', 'CANCELLED'] as const).map((s) => (
                <button
                  key={s}
                  onClick={() => setFilter(s)}
                  className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                    filter === s ? 'bg-medin-navy text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  {s === '' ? 'Todas' : statusLabel[s as InvoiceStatus]}
                </button>
              ))}
              <button onClick={loadInvoices} className="ml-auto flex items-center gap-1 px-3 py-1.5 bg-medin-navy text-white rounded-lg text-sm">
                <ArrowPathIcon className="h-4 w-4" /> Actualizar
              </button>
            </div>

            {loading ? (
              <div className="bg-white rounded-lg shadow p-12 text-center">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent"></div>
              </div>
            ) : invoices.length === 0 ? (
              <div className="bg-white rounded-lg shadow p-12 text-center text-gray-500 text-sm">No hay facturas</div>
            ) : (
              <div className="space-y-3">
                {invoices.map((inv) => (
                  <div key={inv.id} className="bg-white rounded-lg shadow p-5">
                    <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-1">
                          <span className="font-mono font-semibold text-gray-900">{inv.invoiceNumber}</span>
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColor[inv.status]}`}>
                            {statusLabel[inv.status]}
                          </span>
                        </div>
                        <p className="text-xs text-gray-500 mb-2">Paciente: <span className="font-mono">{inv.patientId}</span></p>
                        <div className="space-y-0.5">
                          {inv.charges.map((c, i) => (
                            <div key={i} className="text-xs text-gray-600">
                              {chargeTypeLabel[c.type as ChargeType] || c.type} — {c.description} × {c.quantity} = Q{c.subtotal.toFixed(2)}
                            </div>
                          ))}
                        </div>
                        <div className="mt-2 text-sm font-semibold text-gray-900">
                          Total: Q{inv.total.toFixed(2)}
                          {inv.discount > 0 && <span className="ml-2 text-xs text-green-600">(Descuento: Q{inv.discount.toFixed(2)})</span>}
                        </div>
                      </div>

                      <div className="flex flex-col gap-2 sm:items-end min-w-[140px]">
                        <p className="text-xs text-gray-400">{new Date(inv.createdAt).toLocaleDateString('es-GT')}</p>
                        {inv.status === 'PENDING' && (
                          <>
                            <button
                              onClick={() => { setPayingId(inv.id); setPayAmount(inv.total.toString()); setPayResult(null); }}
                              className="px-3 py-1.5 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-xs hover:bg-medin-blue hover:text-white transition-colors"
                            >
                              Registrar Pago
                            </button>
                            <button
                              onClick={() => handleCancel(inv.id, inv.invoiceNumber)}
                              className="px-3 py-1.5 bg-red-100 text-red-700 rounded-lg text-xs hover:bg-red-200"
                            >
                              Cancelar
                            </button>
                          </>
                        )}
                      </div>
                    </div>

                    {/* Modal de pago inline */}
                    {payingId === inv.id && (
                      <form onSubmit={handlePay} className="mt-4 pt-4 border-t border-gray-100 grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <div>
                          <label className={labelClass}>Monto recibido (Q) <span className="text-red-500">*</span></label>
                          <input type="number" step="0.01" value={payAmount} onChange={(e) => setPayAmount(e.target.value)} required min={inv.total} className={inputClass} />
                        </div>
                        <div>
                          <label className={labelClass}>Método de pago <span className="text-red-500">*</span></label>
                          <select value={payMethod} onChange={(e) => setPayMethod(e.target.value as PaymentMethod)} className={inputClass}>
                            {paymentMethods.map((m) => <option key={m} value={m}>{paymentMethodLabel[m]}</option>)}
                          </select>
                        </div>
                        <div className="flex items-end gap-2">
                          <button type="submit" disabled={payLoading} className="flex-1 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white disabled:opacity-50">
                            {payLoading ? 'Procesando...' : 'Confirmar'}
                          </button>
                          <button type="button" onClick={() => setPayingId(null)} className="py-2 px-3 border border-gray-300 rounded-lg text-sm text-gray-600">
                            Cancelar
                          </button>
                        </div>
                      </form>
                    )}
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {/* ── Tab: Crear factura ── */}
        {tab === 'create' && (
          <form onSubmit={handleCreate} className="space-y-4">
            {/* Buscar paciente */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Paciente</h3>
              {!selectedPatient ? (
                <>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      placeholder="Buscar por nombre, DPI o correo..."
                      value={patientSearch}
                      onChange={(e) => setPatientSearch(e.target.value)}
                      onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); handleSearchPatient(); } }}
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-medin-cyan focus:border-transparent"
                    />
                    <button type="button" onClick={handleSearchPatient} className="px-4 py-2 bg-medin-navy text-white rounded-lg text-sm">
                      Buscar
                    </button>
                  </div>
                  {searchResults.length > 0 && (
                    <div className="mt-2 divide-y border border-gray-200 rounded-lg overflow-hidden">
                      {searchResults.map((p) => (
                        <button key={p.id} type="button" onClick={() => { setSelectedPatient(p); setSearchResults([]); }} className="w-full text-left px-4 py-3 hover:bg-gray-50">
                          <p className="font-medium text-sm text-gray-900">{p.fullName}</p>
                          <p className="text-xs text-gray-500">DPI: {p.dpi}</p>
                        </button>
                      ))}
                    </div>
                  )}
                </>
              ) : (
                <div className="flex items-center justify-between p-3 bg-medin-cyan/10 border border-medin-cyan/30 rounded-lg">
                  <div>
                    <p className="font-semibold text-sm text-gray-900">{selectedPatient.fullName}</p>
                    <p className="text-xs text-gray-600">DPI: {selectedPatient.dpi}</p>
                  </div>
                  <button type="button" onClick={() => setSelectedPatient(null)} className="text-xs text-gray-400 hover:text-gray-600">Cambiar</button>
                </div>
              )}
            </div>

            {/* Cargos */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Cargos</h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 mb-3">
                <div>
                  <label className={labelClass}>Tipo</label>
                  <select value={newCharge.type} onChange={(e) => setNewCharge((p) => ({ ...p, type: e.target.value as ChargeType }))} className={inputClass}>
                    {chargeTypes.map((t) => <option key={t} value={t}>{chargeTypeLabel[t]}</option>)}
                  </select>
                </div>
                <div>
                  <label className={labelClass}>Descripción</label>
                  <input value={newCharge.description} onChange={(e) => setNewCharge((p) => ({ ...p, description: e.target.value }))} className={inputClass} placeholder="Ej: Consulta general" />
                </div>
                <div>
                  <label className={labelClass}>Cantidad</label>
                  <input type="number" value={newCharge.quantity} onChange={(e) => setNewCharge((p) => ({ ...p, quantity: Number(e.target.value) }))} min={1} className={inputClass} />
                </div>
                <div>
                  <label className={labelClass}>Precio unitario (Q)</label>
                  <input type="number" step="0.01" value={newCharge.unitPrice} onChange={(e) => setNewCharge((p) => ({ ...p, unitPrice: Number(e.target.value) }))} min={0} className={inputClass} />
                </div>
              </div>

              <button type="button" onClick={addCharge} className="flex items-center gap-1 text-sm text-medin-cyan hover:text-medin-blue mb-3">
                <PlusIcon className="h-4 w-4" /> Agregar cargo
              </button>

              {charges.length > 0 && (
                <div className="space-y-1 mb-3">
                  {charges.map((c, i) => (
                    <div key={i} className="flex items-center justify-between p-2 bg-gray-50 rounded text-sm">
                      <span>
                        <span className="font-medium">{chargeTypeLabel[c.type]}</span> — {c.description} × {c.quantity} @ Q{c.unitPrice.toFixed(2)}
                        <span className="ml-2 font-semibold text-gray-900">= Q{(c.quantity * c.unitPrice).toFixed(2)}</span>
                      </span>
                      <button type="button" onClick={() => removeCharge(i)}><TrashIcon className="h-4 w-4 text-red-400 hover:text-red-600" /></button>
                    </div>
                  ))}
                  <div className="pt-2 text-right font-semibold text-gray-900">
                    Total: Q{total.toFixed(2)}
                  </div>
                </div>
              )}

              <div className="flex justify-end pt-2">
                <button
                  type="submit"
                  disabled={creating || !selectedPatient || charges.length === 0}
                  className="px-6 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50"
                >
                  {creating ? 'Creando...' : 'Crear Factura'}
                </button>
              </div>
            </div>
          </form>
        )}
      </div>
    </MainLayout>
  );
};

export default CashierBilling;
