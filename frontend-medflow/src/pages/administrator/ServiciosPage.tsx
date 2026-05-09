import { useState, useEffect, useCallback } from 'react';
import type { FC, FormEvent, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  getServiceItems, createServiceItem, updateServiceItem,
  SERVICE_CATEGORIES,
  type ServiceItemResponse, type ServiceItemRequest, type ServiceItemStatus,
} from '../../services/billingCatalogService';
import { getExamTypes, type ExamTypeResponse } from '../../services/labCatalogService';
import { getMedications, type MedicationResponse } from '../../services/pharmacyService';
import { validateForm, clearFieldError } from '../../utils/formValidation';
import type { Schema, FormErrors } from '../../utils/formValidation';

interface ServiceFields { code: string; name: string; price: string; }

type Modal = { type: 'create' } | { type: 'edit'; item: ServiceItemResponse } | null;
const EMPTY: ServiceItemRequest = { code: '', name: '', description: '', category: 'CONSULTATION', price: 0, status: 'ACTIVE' };

const STATUS_LABELS: Record<ServiceItemStatus, string> = {
  ACTIVE: 'Activo',
  INACTIVE: 'Inactivo',
  DELETED: 'Eliminado',
};

const STATUS_BADGE: Record<ServiceItemStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-700',
  INACTIVE: 'bg-gray-100 text-gray-500',
  DELETED: 'bg-red-100 text-red-500',
};

const ServiciosPage: FC = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<ServiceItemResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [filterCat, setFilterCat] = useState('');
  const [modal, setModal] = useState<Modal>(null);
  const [form, setForm] = useState<ServiceItemRequest>(EMPTY);
  const [priceStr, setPriceStr] = useState('0');
  const [saving, setSaving] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<FormErrors<ServiceFields>>({});
  const [saveError, setSaveError] = useState<string | null>(null);

  const serviceSchema: Schema<ServiceFields> = {
    code:  [{ type: 'required', message: 'El código es obligatorio.' }],
    name:  [{ type: 'required', message: 'El nombre es obligatorio.' }],
    price: [{ type: 'required', message: 'El precio es obligatorio.' }],
  };

  // Catálogos para selects
  const [examCatalog, setExamCatalog] = useState<ExamTypeResponse[]>([]);
  const [medCatalog, setMedCatalog] = useState<MedicationResponse[]>([]);

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { setItems(await getServiceItems()); }
    catch { setError('No se pudo cargar el catálogo de servicios.'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => {
    load();
    getExamTypes().then(data => setExamCatalog(data.filter(e => e.status === 'ACTIVE'))).catch(() => {});
    getMedications().then(data => setMedCatalog(data.filter(m => m.status === 'ACTIVE'))).catch(() => {});
  }, [load]);

  const filtered = items.filter(i => {
    const matchSearch = i.name.toLowerCase().includes(search.toLowerCase()) || i.code.toLowerCase().includes(search.toLowerCase());
    const matchCat = filterCat ? i.category === filterCat : true;
    return matchSearch && matchCat;
  });

  const resetModal = () => { setFieldErrors({}); setSaveError(null); };
  const openCreate = () => { setForm(EMPTY); setPriceStr('0'); resetModal(); setModal({ type: 'create' }); };
  const openEdit = (item: ServiceItemResponse) => {
    setForm({ code: item.code, name: item.name, description: item.description, category: item.category, price: item.price, status: item.status });
    setPriceStr(String(item.price));
    resetModal();
    setModal({ type: 'edit', item });
  };
  const closeModal = () => setModal(null);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const next = name === 'code' ? value.toUpperCase() : value;
    setForm(f => ({ ...f, [name]: next }));
    setFieldErrors(prev => clearFieldError(prev, name as keyof ServiceFields));
    if (saveError) setSaveError(null);
  };

  const handlePriceChange = (e: ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val !== '' && !/^\d*\.?\d*$/.test(val)) return;
    setPriceStr(val);
    const num = parseFloat(val);
    setForm(f => ({ ...f, price: isNaN(num) ? 0 : num }));
    setFieldErrors(prev => clearFieldError(prev, 'price'));
    if (saveError) setSaveError(null);
  };

  const handleCatalogSelect = (value: string) => {
    if (!value) return;
    if (form.category === 'LABORATORY') {
      const exam = examCatalog.find(e => e.id === value);
      if (exam) {
        setForm(f => ({ ...f, name: exam.name, code: `LAB-${exam.code}`, description: exam.description ?? '' }));
        setFieldErrors(prev => clearFieldError(clearFieldError(prev, 'code'), 'name'));
      }
    } else if (form.category === 'MEDICATION') {
      const med = medCatalog.find(m => m.id === value);
      if (med) {
        setForm(f => ({ ...f, name: med.name, code: `MED-${med.name.replace(/\s+/g, '').slice(0, 6).toUpperCase()}`, description: med.description ?? '' }));
        setFieldErrors(prev => clearFieldError(clearFieldError(prev, 'code'), 'name'));
      }
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const errs = validateForm(serviceSchema, { code: form.code, name: form.name, price: priceStr });
    if (!errs.price && (form.price <= 0 || isNaN(form.price)))
      errs.price = 'El precio debe ser mayor a 0.';
    setFieldErrors(errs);
    if (Object.keys(errs).length > 0) return;
    setSaving(true); setSaveError(null);
    try {
      if (modal?.type === 'edit') await updateServiceItem(modal.item.id, form);
      else await createServiceItem(form);
      await load(); closeModal();
    } catch { setSaveError('Error al guardar.'); }
    finally { setSaving(false); }
  };

  const inputCls = (hasError = false) =>
    `w-full px-3 py-2 border ${hasError ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan text-sm`;
  const showCatalogSelect = form.category === 'LABORATORY' || form.category === 'MEDICATION';

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/administrator')} className="text-gray-400 hover:text-gray-600">
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Servicios y Precios</h2>
            <p className="text-gray-500 text-sm">Catálogo de servicios facturables con sus precios</p>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row gap-3 justify-between">
          <div className="flex gap-2">
            <input type="text" placeholder="Buscar servicio..." value={search} onChange={e => setSearch(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-medin-cyan w-48" />
            <select value={filterCat} onChange={e => setFilterCat(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-medin-cyan">
              <option value="">Todas las categorías</option>
              {Object.entries(SERVICE_CATEGORIES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          </div>
          <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm">
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" /></svg>
            Agregar servicio
          </button>
        </div>

        {error && <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>}

        {loading ? (
          <div className="text-center py-12"><div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" /></div>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>{['Código', 'Nombre', 'Categoría', 'Precio', 'Estado', ''].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}</tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.length === 0 ? (
                  <tr><td colSpan={6} className="text-center py-10 text-gray-400 text-sm">No hay servicios registrados</td></tr>
                ) : filtered.map(item => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-mono text-sm text-gray-700">{item.code}</td>
                    <td className="px-4 py-3 font-medium text-gray-900 text-sm">{item.name}</td>
                    <td className="px-4 py-3 text-sm">
                      <span className="px-2 py-0.5 bg-blue-50 text-blue-700 rounded-full text-xs">{SERVICE_CATEGORIES[item.category] ?? item.category}</span>
                    </td>
                    <td className="px-4 py-3 font-semibold text-gray-900 text-sm">Q {Number(item.price).toFixed(2)}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[item.status as ServiceItemStatus]}`}>
                        {STATUS_LABELS[item.status as ServiceItemStatus]}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <button onClick={() => openEdit(item)} className="text-sm text-medin-cyan hover:text-medin-blue font-medium">Editar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {modal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">{modal.type === 'create' ? 'Agregar servicio' : 'Editar servicio'}</h3>
            <form onSubmit={handleSubmit} className="space-y-3" noValidate>

              {/* Categoría primero para mostrar select de catálogo si aplica */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Categoría <span className="text-red-500">*</span></label>
                <select className={inputCls()} value={form.category}
                  onChange={e => { setForm(f => ({ ...f, category: e.target.value, code: '', name: '' })); setFieldErrors(prev => clearFieldError(clearFieldError(prev, 'code'), 'name')); }}>
                  {Object.entries(SERVICE_CATEGORIES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                </select>
              </div>

              {/* Select del catálogo cuando aplica */}
              {showCatalogSelect && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    {form.category === 'LABORATORY' ? 'Seleccionar examen del catálogo' : 'Seleccionar medicamento del catálogo'}
                  </label>
                  <select className={inputCls()} defaultValue="" onChange={e => handleCatalogSelect(e.target.value)}>
                    <option value="">— Elige para autocompletar —</option>
                    {form.category === 'LABORATORY'
                      ? examCatalog.map(e => <option key={e.id} value={e.id}>{e.code} — {e.name}</option>)
                      : medCatalog.map(m => <option key={m.id} value={m.id}>{m.name} ({m.unit})</option>)
                    }
                  </select>
                  <p className="text-xs text-gray-400 mt-1">Seleccionar autocompleta el código y nombre. Puedes editarlos después.</p>
                </div>
              )}

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Código <span className="text-red-500">*</span></label>
                  <input name="code" className={inputCls(!!fieldErrors.code)} value={form.code}
                    onChange={handleChange} placeholder="Ej. CONS-GEN" maxLength={30} />
                  {fieldErrors.code && <p className="mt-1 text-xs text-red-600">{fieldErrors.code}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Precio (Q) <span className="text-red-500">*</span></label>
                  <input type="text" inputMode="decimal" className={inputCls(!!fieldErrors.price)}
                    value={priceStr} onChange={handlePriceChange} placeholder="0.00" maxLength={10} />
                  {fieldErrors.price && <p className="mt-1 text-xs text-red-600">{fieldErrors.price}</p>}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre <span className="text-red-500">*</span></label>
                <input name="name" className={inputCls(!!fieldErrors.name)} value={form.name}
                  onChange={handleChange} placeholder="Nombre del servicio" maxLength={200} />
                {fieldErrors.name && <p className="mt-1 text-xs text-red-600">{fieldErrors.name}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                <input className={inputCls()} value={form.description ?? ''}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))} placeholder="Opcional" maxLength={500} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Estado <span className="text-red-500">*</span></label>
                <select className={inputCls()} value={form.status ?? 'ACTIVE'} onChange={e => setForm(f => ({ ...f, status: e.target.value as ServiceItemStatus }))}>
                  <option value="ACTIVE">Activo</option>
                  <option value="INACTIVE">Inactivo</option>
                  <option value="DELETED">Eliminado</option>
                </select>
              </div>

              {saveError && <p className="text-red-600 text-sm">{saveError}</p>}
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
    </MainLayout>
  );
};

export default ServiciosPage;
