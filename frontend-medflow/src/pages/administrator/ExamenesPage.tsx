import { useState, useEffect, useCallback } from 'react';
import type { FC, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  getExamTypes, createExamType, updateExamType,
  type ExamTypeResponse, type ExamTypeRequest, type ExamTypeStatus,
} from '../../services/labCatalogService';
import { validateForm, clearFieldError } from '../../utils/formValidation';
import type { Schema, FormErrors } from '../../utils/formValidation';

interface ExamFields { code: string; name: string; testType: string; sampleType: string; }

type Modal = { type: 'create' } | { type: 'edit'; item: ExamTypeResponse } | null;

const EMPTY: ExamTypeRequest = { code: '', name: '', description: '', status: 'ACTIVE' };

const STATUS_LABELS: Record<ExamTypeStatus, string> = {
  ACTIVE: 'Activo',
  INACTIVE: 'Inactivo',
  DELETED: 'Eliminado',
};

const STATUS_BADGE: Record<ExamTypeStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-700',
  INACTIVE: 'bg-gray-100 text-gray-500',
  DELETED: 'bg-red-100 text-red-500',
};

const ExamenesPage: FC = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<ExamTypeResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [modal, setModal] = useState<Modal>(null);
  const [form, setForm] = useState<ExamTypeRequest>(EMPTY);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FormErrors<ExamFields>>({});

  const examSchema: Schema<ExamFields> = {
    code:       [{ type: 'required', message: 'El código es obligatorio.' }],
    name:       [{ type: 'required', message: 'El nombre es obligatorio.' }],
    testType:   [{ type: 'required', message: 'El tipo de examen es obligatorio.' }],
    sampleType: [{ type: 'required', message: 'El tipo de muestra es obligatorio.' }],
  };

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { setItems(await getExamTypes()); }
    catch { setError('No se pudo cargar el catálogo de exámenes.'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const filtered = items.filter(i =>
    i.name.toLowerCase().includes(search.toLowerCase()) ||
    i.code.toLowerCase().includes(search.toLowerCase())
  );

  const resetModal = () => { setFormError(null); setFieldErrors({}); };
  const openCreate = () => { setForm(EMPTY); resetModal(); setModal({ type: 'create' }); };
  const openEdit = (item: ExamTypeResponse) => {
    setForm({ code: item.code, name: item.name, description: item.description, testType: item.testType, sampleType: item.sampleType, status: item.status });
    resetModal();
    setModal({ type: 'edit', item });
  };
  const closeModal = () => setModal(null);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    const next = name === 'code' ? value.toUpperCase() : value;
    setForm(f => ({ ...f, [name]: next }));
    setFieldErrors(prev => clearFieldError(prev, name as keyof ExamFields));
    if (formError) setFormError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs = validateForm(examSchema, { code: form.code, name: form.name, testType: form.testType ?? '', sampleType: form.sampleType ?? '' });
    setFieldErrors(errs);
    if (Object.keys(errs).length > 0) return;
    setSaving(true); setFormError(null);
    try {
      if (modal?.type === 'edit') await updateExamType(modal.item.id, form);
      else await createExamType(form);
      await load(); closeModal();
    } catch { setFormError('Error al guardar.'); }
    finally { setSaving(false); }
  };

  const inputCls = (hasError = false) =>
    `w-full px-3 py-2 border ${hasError ? 'border-red-400 bg-red-50' : 'border-gray-300'} rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan text-sm`;

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex items-center gap-3">
          <button onClick={() => navigate('/administrator')} className="text-gray-400 hover:text-gray-600">
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Catálogo de Exámenes</h2>
            <p className="text-gray-500 text-sm">Gestiona los tipos de exámenes de laboratorio disponibles</p>
          </div>
        </div>

        <div className="flex flex-col sm:flex-row gap-3 justify-between">
          <input type="text" placeholder="Buscar por código o nombre..." value={search}
            onChange={e => setSearch(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-medin-cyan w-64" />
          <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm">
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" /></svg>
            Agregar examen
          </button>
        </div>

        {error && <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>}

        {loading ? (
          <div className="text-center py-12"><div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" /></div>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>{['Código', 'Nombre', 'Descripción', 'Estado', ''].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}</tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.length === 0 ? (
                  <tr><td colSpan={5} className="text-center py-10 text-gray-400 text-sm">No hay exámenes registrados</td></tr>
                ) : filtered.map(item => (
                  <tr key={item.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-mono text-sm text-gray-700">{item.code}</td>
                    <td className="px-4 py-3 font-medium text-gray-900 text-sm">{item.name}</td>
                    <td className="px-4 py-3 text-gray-500 text-sm max-w-xs truncate">{item.description || '—'}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[item.status]}`}>
                        {STATUS_LABELS[item.status]}
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
            <h3 className="text-lg font-bold text-gray-900 mb-4">
              {modal.type === 'create' ? 'Agregar examen' : 'Editar examen'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-3" noValidate>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Código <span className="text-red-500">*</span></label>
                <input name="code" className={inputCls(!!fieldErrors.code)} value={form.code}
                  onChange={handleChange} placeholder="Ej. HEM" maxLength={20} />
                {fieldErrors.code && <p className="mt-1 text-xs text-red-600">{fieldErrors.code}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre <span className="text-red-500">*</span></label>
                <input name="name" className={inputCls(!!fieldErrors.name)} value={form.name}
                  onChange={handleChange} placeholder="Ej. Hemograma Completo" maxLength={200} />
                {fieldErrors.name && <p className="mt-1 text-xs text-red-600">{fieldErrors.name}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                <input name="description" className={inputCls()} value={form.description ?? ''}
                  onChange={handleChange} placeholder="Opcional" maxLength={500} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tipo de examen <span className="text-red-500">*</span></label>
                <select name="testType" className={inputCls(!!fieldErrors.testType)} value={form.testType ?? ''} onChange={handleChange}>
                  <option value="">Seleccionar tipo de examen</option>
                  <option value="Hematología">Hematología</option>
                  <option value="Química Clínica">Química Clínica</option>
                  <option value="Uroanálisis">Uroanálisis</option>
                  <option value="Microbiología">Microbiología</option>
                  <option value="Inmunología">Inmunología</option>
                  <option value="Serología">Serología</option>
                  <option value="Coagulación">Coagulación</option>
                  <option value="Parasitología">Parasitología</option>
                  <option value="Hormonas">Hormonas</option>
                  <option value="Marcadores Tumorales">Marcadores Tumorales</option>
                  <option value="Toxicología">Toxicología</option>
                  <option value="Otro">Otro</option>
                </select>
                {fieldErrors.testType && <p className="mt-1 text-xs text-red-600">{fieldErrors.testType}</p>}
                <p className="text-xs text-gray-500 mt-1">Categoría general del examen de laboratorio</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tipo de muestra <span className="text-red-500">*</span></label>
                <select name="sampleType" className={inputCls(!!fieldErrors.sampleType)} value={form.sampleType ?? ''} onChange={handleChange}>
                  <option value="">Seleccionar tipo de muestra</option>
                  <option value="Sangre">Sangre</option>
                  <option value="Orina">Orina</option>
                  <option value="Heces">Heces</option>
                  <option value="Saliva">Saliva</option>
                  <option value="Líquido cefalorraquídeo">Líquido cefalorraquídeo</option>
                  <option value="Tejido">Tejido</option>
                  <option value="Esputo">Esputo</option>
                  <option value="Otro">Otro</option>
                </select>
                {fieldErrors.sampleType && <p className="mt-1 text-xs text-red-600">{fieldErrors.sampleType}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Estado <span className="text-red-500">*</span></label>
                <select name="status" className={inputCls()} value={form.status}
                  onChange={e => setForm(f => ({ ...f, status: e.target.value as ExamTypeStatus }))}>
                  <option value="ACTIVE">Activo</option>
                  <option value="INACTIVE">Inactivo</option>
                  <option value="DELETED">Eliminado</option>
                </select>
              </div>
              {formError && <p className="text-red-600 text-sm">{formError}</p>}
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={closeModal}
                  className="flex-1 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50">
                  Cancelar
                </button>
                <button type="submit" disabled={saving}
                  className="flex-1 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white disabled:opacity-50 transition-colors">
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

export default ExamenesPage;
