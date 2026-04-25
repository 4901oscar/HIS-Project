import { useState, useEffect, useCallback } from 'react';
import type { FC, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  getMedications,
  createMedication,
  updateStock,
  type MedicationResponse,
  type MedicationRequest,
} from '../../services/pharmacyService';

type Modal =
  | { type: 'create' }
  | { type: 'stock'; med: MedicationResponse }
  | null;

const EMPTY_FORM: MedicationRequest = {
  name: '', description: '', unit: '', currentStock: 0, minStock: 0,
};

const MedicamentosPage: FC = () => {
  const navigate = useNavigate();
  const [meds, setMeds] = useState<MedicationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [filterLow, setFilterLow] = useState(false);
  const [modal, setModal] = useState<Modal>(null);
  const [form, setForm] = useState<MedicationRequest>(EMPTY_FORM);
  const [newStock, setNewStock] = useState('');
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setMeds(await getMedications());
    } catch {
      setError('No se pudo cargar el catálogo de medicamentos.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const filtered = meds.filter(m => {
    const matchSearch = m.name.toLowerCase().includes(search.toLowerCase());
    const matchLow = filterLow ? m.lowStock : true;
    return matchSearch && matchLow;
  });

  const openCreate = () => { setForm(EMPTY_FORM); setFormError(null); setModal({ type: 'create' }); };
  const openStock = (med: MedicationResponse) => { setNewStock(String(med.currentStock)); setFormError(null); setModal({ type: 'stock', med }); };
  const closeModal = () => setModal(null);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.name.trim() || !form.unit.trim()) {
      setFormError('Nombre y unidad son obligatorios.');
      return;
    }
    setSaving(true);
    setFormError(null);
    try {
      await createMedication(form);
      await load();
      closeModal();
    } catch {
      setFormError('Error al guardar el medicamento.');
    } finally {
      setSaving(false);
    }
  };

  const handleStock = async (e: FormEvent) => {
    e.preventDefault();
    const val = parseInt(newStock, 10);
    if (isNaN(val) || val < 0) { setFormError('Ingresa un número válido.'); return; }
    if (modal?.type !== 'stock') return;
    setSaving(true);
    setFormError(null);
    try {
      await updateStock(modal.med.id, val);
      await load();
      closeModal();
    } catch {
      setFormError('Error al actualizar el stock.');
    } finally {
      setSaving(false);
    }
  };

  const inputCls = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan text-sm';

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/administrator')} className="text-gray-400 hover:text-gray-600">
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Catálogo de Medicamentos</h2>
            <p className="text-gray-500 text-sm">Gestiona el inventario de medicamentos disponibles</p>
          </div>
        </div>

        {/* Actions bar */}
        <div className="flex flex-col sm:flex-row gap-3 justify-between">
          <div className="flex gap-2">
            <input
              type="text"
              placeholder="Buscar medicamento..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-medin-cyan w-56"
            />
            <button
              onClick={() => setFilterLow(f => !f)}
              className={`px-3 py-2 rounded-lg text-sm font-medium border transition-colors ${filterLow ? 'bg-red-100 border-red-300 text-red-700' : 'border-gray-300 text-gray-600 hover:bg-gray-50'}`}
            >
              Stock bajo
            </button>
          </div>
          <button
            onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Agregar medicamento
          </button>
        </div>

        {/* Error */}
        {error && <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>}

        {/* Table */}
        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" />
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  {['Nombre', 'Descripción', 'Unidad', 'Stock actual', 'Stock mínimo', 'Estado', ''].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center py-10 text-gray-400 text-sm">No hay medicamentos registrados</td>
                  </tr>
                ) : filtered.map(m => (
                  <tr key={m.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-900 text-sm">{m.name}</td>
                    <td className="px-4 py-3 text-gray-600 text-sm max-w-xs truncate">{m.description || '—'}</td>
                    <td className="px-4 py-3 text-gray-600 text-sm">{m.unit}</td>
                    <td className="px-4 py-3 text-sm">
                      <span className={`font-semibold ${m.lowStock ? 'text-red-600' : 'text-gray-900'}`}>{m.currentStock}</span>
                    </td>
                    <td className="px-4 py-3 text-gray-600 text-sm">{m.minStock}</td>
                    <td className="px-4 py-3">
                      {m.lowStock
                        ? <span className="px-2 py-0.5 bg-red-100 text-red-700 rounded-full text-xs font-medium">Stock bajo</span>
                        : <span className="px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">Disponible</span>
                      }
                    </td>
                    <td className="px-4 py-3">
                      <button
                        onClick={() => openStock(m)}
                        className="text-sm text-medin-cyan hover:text-medin-blue font-medium"
                      >
                        Actualizar stock
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal crear */}
      {modal?.type === 'create' && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">Agregar medicamento</h3>
            <form onSubmit={handleCreate} className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre <span className="text-red-500">*</span></label>
                <input className={inputCls} value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} placeholder="Ej. Amoxicilina 500mg" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                <input className={inputCls} value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} placeholder="Opcional" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Unidad <span className="text-red-500">*</span></label>
                <input className={inputCls} value={form.unit} onChange={e => setForm(f => ({ ...f, unit: e.target.value }))} placeholder="Ej. tabletas, mg, ml" />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Stock actual</label>
                  <input type="number" min={0} className={inputCls} value={form.currentStock}
                    onChange={e => setForm(f => ({ ...f, currentStock: parseInt(e.target.value) || 0 }))} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Stock mínimo</label>
                  <input type="number" min={0} className={inputCls} value={form.minStock}
                    onChange={e => setForm(f => ({ ...f, minStock: parseInt(e.target.value) || 0 }))} />
                </div>
              </div>
              {formError && <p className="text-red-600 text-sm">{formError}</p>}
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={closeModal} className="flex-1 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50">Cancelar</button>
                <button type="submit" disabled={saving} className="flex-1 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white disabled:opacity-50 transition-colors">
                  {saving ? 'Guardando...' : 'Guardar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal stock */}
      {modal?.type === 'stock' && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-sm p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-1">Actualizar stock</h3>
            <p className="text-sm text-gray-500 mb-4">{modal.med.name}</p>
            <form onSubmit={handleStock} className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nuevo stock</label>
                <input
                  type="number" min={0}
                  className={inputCls}
                  value={newStock}
                  onChange={e => setNewStock(e.target.value)}
                />
              </div>
              {formError && <p className="text-red-600 text-sm">{formError}</p>}
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={closeModal} className="flex-1 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50">Cancelar</button>
                <button type="submit" disabled={saving} className="flex-1 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white disabled:opacity-50 transition-colors">
                  {saving ? 'Guardando...' : 'Actualizar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </MainLayout>
  );
};

export default MedicamentosPage;
