import { useState, useEffect, useCallback } from 'react';
import type { FC, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  getExamTypes, createExamType, updateExamType, toggleExamType,
  type ExamTypeResponse, type ExamTypeRequest,
} from '../../services/labCatalogService';

type Modal = { type: 'create' } | { type: 'edit'; item: ExamTypeResponse } | null;
const EMPTY: ExamTypeRequest = { code: '', name: '', description: '' };

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

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try { setItems(await getExamTypes()); }
    catch { setError('No se pudo cargar el catálogo de exámenes.'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const filtered = items.filter(i => i.name.toLowerCase().includes(search.toLowerCase()) || i.code.toLowerCase().includes(search.toLowerCase()));

  const openCreate = () => { setForm(EMPTY); setFormError(null); setModal({ type: 'create' }); };
  const openEdit = (item: ExamTypeResponse) => { setForm({ code: item.code, name: item.name, description: item.description }); setFormError(null); setModal({ type: 'edit', item }); };
  const closeModal = () => setModal(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.code.trim() || !form.name.trim()) { setFormError('Código y nombre son obligatorios.'); return; }
    setSaving(true); setFormError(null);
    try {
      if (modal?.type === 'edit') await updateExamType(modal.item.id, form);
      else await createExamType(form);
      await load(); closeModal();
    } catch { setFormError('Error al guardar.'); }
    finally { setSaving(false); }
  };

  const handleToggle = async (id: string) => {
    try { await toggleExamType(id); await load(); }
    catch { setError('Error al cambiar estado.'); }
  };

  const inputCls = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-medin-cyan text-sm';

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
          <input type="text" placeholder="Buscar por código o nombre..." value={search} onChange={e => setSearch(e.target.value)}
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
                      {item.active
                        ? <span className="px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">Activo</span>
                        : <span className="px-2 py-0.5 bg-gray-100 text-gray-500 rounded-full text-xs font-medium">Inactivo</span>}
                    </td>
                    <td className="px-4 py-3 flex gap-3">
                      <button onClick={() => openEdit(item)} className="text-sm text-medin-cyan hover:text-medin-blue font-medium">Editar</button>
                      <button onClick={() => handleToggle(item.id)} className="text-sm text-gray-400 hover:text-gray-600 font-medium">
                        {item.active ? 'Desactivar' : 'Activar'}
                      </button>
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
            <h3 className="text-lg font-bold text-gray-900 mb-4">{modal.type === 'create' ? 'Agregar examen' : 'Editar examen'}</h3>
            <form onSubmit={handleSubmit} className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Código <span className="text-red-500">*</span></label>
                <input className={inputCls} value={form.code} onChange={e => setForm(f => ({ ...f, code: e.target.value }))} placeholder="Ej. HEM" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre <span className="text-red-500">*</span></label>
                <input className={inputCls} value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} placeholder="Ej. Hemograma Completo" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                <input className={inputCls} value={form.description ?? ''} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} placeholder="Opcional" />
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
    </MainLayout>
  );
};

export default ExamenesPage;
