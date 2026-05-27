import { useState, useEffect, useCallback } from 'react';
import type { FC, FormEvent, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  getMedications, createMedication, updateMedication, updateStock,
  type MedicationResponse, type MedicationRequest, type MedicationStatus,
} from '../../services/pharmacyService';
import { validateForm, clearFieldError } from '../../utils/formValidation';
import type { Schema, FormErrors } from '../../utils/formValidation';

interface MedFields { name: string; unit: string; }
type MedErrors = Partial<Record<'name' | 'unit' | 'currentStock' | 'minStock', string>>;

type Modal =
  | { type: 'create' }
  | { type: 'edit'; med: MedicationResponse }
  | { type: 'stock'; med: MedicationResponse }
  | null;

const EMPTY_FORM: MedicationRequest = {
  name: '', description: '', unit: '', currentStock: 0, minStock: 0, status: 'ACTIVE',
};

const STATUS_LABELS: Record<MedicationStatus, string> = {
  ACTIVE: 'Activo',
  INACTIVE: 'Inactivo',
  DELETED: 'Eliminado',
};

const STATUS_BADGE: Record<MedicationStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-700',
  INACTIVE: 'bg-gray-100 text-gray-500',
  DELETED: 'bg-red-100 text-red-500',
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
  const [fieldErrors, setFieldErrors] = useState<MedErrors>({});
  const [currentStockStr, setCurrentStockStr] = useState('');
  const [minStockStr, setMinStockStr] = useState('');
  const [newStockError, setNewStockError] = useState<string | null>(null);

  const medSchema: Schema<MedFields> = {
    name: [{ type: 'required', message: 'El nombre es obligatorio.' }],
    unit: [{ type: 'required', message: 'La unidad es obligatoria.' }],
  };

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

  const resetModal = () => { setFormError(null); setFieldErrors({}); };
  const openCreate = () => {
    setForm(EMPTY_FORM);
    setCurrentStockStr('');
    setMinStockStr('');
    resetModal();
    setModal({ type: 'create' });
  };
  const openEdit = (med: MedicationResponse) => {
    setForm({ name: med.name, description: med.description ?? '', unit: med.unit, currentStock: med.currentStock, minStock: med.minStock, status: med.status });
    setCurrentStockStr(String(med.currentStock));
    setMinStockStr(String(med.minStock));
    resetModal();
    setModal({ type: 'edit', med });
  };
  const openStock = (med: MedicationResponse) => { setNewStock(String(med.currentStock)); setNewStockError(null); resetModal(); setModal({ type: 'stock', med }); };
  const closeModal = () => setModal(null);

  const handleMedChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
    setFieldErrors(prev => clearFieldError(prev as FormErrors<MedFields>, name as keyof MedFields) as MedErrors);
    if (formError) setFormError(null);
  };

  const handleStockChange = (
    field: 'currentStock' | 'minStock',
    setStr: React.Dispatch<React.SetStateAction<string>>,
    value: string,
  ) => {
    if (/[^\d]/.test(value)) {
      setFieldErrors(prev => ({ ...prev, [field]: 'Solo números.' }));
      return;
    }
    setStr(value);
    setForm(f => ({ ...f, [field]: value === '' ? 0 : parseInt(value) }));
    setFieldErrors(prev => { const next = { ...prev }; delete next[field]; return next; });
    if (formError) setFormError(null);
  };

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    const errs = validateForm(medSchema, { name: form.name, unit: form.unit });
    setFieldErrors(errs);
    if (Object.keys(errs).length > 0) return;
    setSaving(true);
    setFormError(null);
    try {
      if (modal?.type === 'edit') {
        await updateMedication(modal.med.id, form);
      } else {
        await createMedication(form);
      }
      await load();
      closeModal();
    } catch {
      setFormError('Error al guardar el medicamento.');
    } finally {
      setSaving(false);
    }
  };

  const handleNewStockChange = (value: string) => {
    if (/[^\d]/.test(value)) {
      setNewStockError('Solo números.');
      return;
    }
    setNewStock(value);
    setNewStockError(null);
  };

  const handleStock = async (e: FormEvent) => {
    e.preventDefault();
    const val = parseInt(newStock, 10);
    if (newStock === '' || isNaN(val) || val < 0) { setNewStockError('Ingresa un número válido.'); return; }
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

  const inputCls = (hasError = false) =>
    `w-full px-3 py-2 border ${hasError ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan text-sm`;

  return (
    <MainLayout>
      <div className="space-y-6">
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

        {error && <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>}

        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" />
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  {['Nombre', 'Descripción', 'Unidad', 'Stock actual', 'Stock mínimo', 'Estado', 'Acciones'].map(h => (
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
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[m.status]}`}>
                        {STATUS_LABELS[m.status]}
                      </span>
                    </td>
                    <td className="px-4 py-3 flex gap-3">
                      <button onClick={() => openEdit(m)} className="text-sm text-medin-cyan hover:text-medin-blue font-medium">Editar</button>
                      <button onClick={() => openStock(m)} className="text-sm text-gray-400 hover:text-gray-600 font-medium">Stock</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Crear / Editar */}
      {(modal?.type === 'create' || modal?.type === 'edit') && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">
              {modal.type === 'create' ? 'Agregar medicamento' : 'Editar medicamento'}
            </h3>
            <form onSubmit={handleSave} className="space-y-3" noValidate>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre <span className="text-red-500">*</span></label>
                <input name="name" className={inputCls(!!fieldErrors.name)} value={form.name}
                  onChange={handleMedChange} placeholder="Ej. Amoxicilina 500mg" />
                {fieldErrors.name && <p className="mt-1 text-xs text-red-600">{fieldErrors.name}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                <input name="description" className={inputCls()} value={form.description ?? ''}
                  onChange={handleMedChange} placeholder="Opcional" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Unidad <span className="text-red-500">*</span></label>
                <input name="unit" className={inputCls(!!fieldErrors.unit)} value={form.unit}
                  onChange={handleMedChange} placeholder="Ej. tabletas, mg, ml" />
                {fieldErrors.unit && <p className="mt-1 text-xs text-red-600">{fieldErrors.unit}</p>}
              </div>
              <div className="grid grid-cols-2 gap-3">
                {modal.type === 'create' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Stock inicial</label>
                    <input
                      type="text" inputMode="numeric" maxLength={4}
                      className={inputCls(!!fieldErrors.currentStock)}
                      value={currentStockStr}
                      onChange={e => handleStockChange('currentStock', setCurrentStockStr, e.target.value)}
                      placeholder="0"
                    />
                    {fieldErrors.currentStock && <p className="mt-1 text-xs text-red-600">{fieldErrors.currentStock}</p>}
                  </div>
                )}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Stock mínimo <span className="text-red-500">*</span></label>
                  <input
                    type="text" inputMode="numeric" maxLength={4}
                    className={inputCls(!!fieldErrors.minStock)}
                    value={minStockStr}
                    onChange={e => handleStockChange('minStock', setMinStockStr, e.target.value)}
                    placeholder="0"
                  />
                  {fieldErrors.minStock && <p className="mt-1 text-xs text-red-600">{fieldErrors.minStock}</p>}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Estado</label>
                <select name="status" className={inputCls()} value={form.status}
                  onChange={e => setForm(f => ({ ...f, status: e.target.value as MedicationStatus }))}>
                  <option value="ACTIVE">Activo</option>
                  <option value="INACTIVE">Inactivo</option>
                  <option value="DELETED">Eliminado</option>
                </select>
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

      {/* Modal Actualizar Stock */}
      {modal?.type === 'stock' && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-sm p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-1">Actualizar stock</h3>
            <p className="text-sm text-gray-500 mb-4">{modal.med.name}</p>
            <form onSubmit={handleStock} className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nuevo stock</label>
                <input
                  type="text" inputMode="numeric" maxLength={4}
                  className={inputCls(!!newStockError)}
                  value={newStock}
                  onChange={e => handleNewStockChange(e.target.value)}
                  placeholder="0"
                />
                {newStockError && <p className="mt-1 text-xs text-red-600">{newStockError}</p>}
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
