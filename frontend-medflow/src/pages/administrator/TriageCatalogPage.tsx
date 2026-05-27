import { useState, useEffect, useCallback } from 'react';
import type { FC, FormEvent, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import {
  getMotifs, createMotif, updateMotif,
  getDiscriminators, createDiscriminator, updateDiscriminator,
  PRIORITY_LEVELS,
  type MotifResponse, type MotifRequest,
  type DiscriminatorResponse, type DiscriminatorRequest,
} from '../../services/clinicalCatalogService';
import { validateForm, clearFieldError } from '../../utils/formValidation';
import type { Schema, FormErrors } from '../../utils/formValidation';

interface MotifFields { code: string; description: string; }
interface DiscFields { code: string; description: string; motifId: string; }

const PRIORITY_COLORS: Record<string, string> = {
  RED: 'bg-red-100 text-red-800',
  ORANGE: 'bg-orange-100 text-orange-800',
  YELLOW: 'bg-yellow-100 text-yellow-800',
  GREEN: 'bg-green-100 text-green-800',
  BLUE: 'bg-blue-100 text-blue-800',
};

type Tab = 'motifs' | 'discriminators';
type MotifModal = { type: 'create' } | { type: 'edit'; item: MotifResponse } | null;
type DiscModal = { type: 'create' } | { type: 'edit'; item: DiscriminatorResponse } | null;

const EMPTY_MOTIF: MotifRequest = { code: '', description: '', category: '', active: true };
const EMPTY_DISC: DiscriminatorRequest = { code: '', description: '', priorityLevel: 'GREEN', active: true };

const TriageCatalogPage: FC = () => {
  const navigate = useNavigate();
  const [tab, setTab] = useState<Tab>('motifs');
  const [motifs, setMotifs] = useState<MotifResponse[]>([]);
  const [discs, setDiscs] = useState<DiscriminatorResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [motifModal, setMotifModal] = useState<MotifModal>(null);
  const [discModal, setDiscModal] = useState<DiscModal>(null);
  const [motifForm, setMotifForm] = useState<MotifRequest>(EMPTY_MOTIF);
  const [discForm, setDiscForm] = useState<DiscriminatorRequest>(EMPTY_DISC);
  const [saving, setSaving] = useState(false);
  const [motifFieldErrors, setMotifFieldErrors] = useState<FormErrors<MotifFields>>({});
  const [motifSaveError, setMotifSaveError] = useState<string | null>(null);
  const [discFieldErrors, setDiscFieldErrors] = useState<FormErrors<DiscFields>>({});
  const [discSaveError, setDiscSaveError] = useState<string | null>(null);

  const motifSchema: Schema<MotifFields> = {
    code:        [{ type: 'required', message: 'El código es obligatorio.' }],
    description: [{ type: 'required', message: 'La descripción es obligatoria.' }],
  };

  const discSchema: Schema<DiscFields> = {
    code:        [{ type: 'required', message: 'El código es obligatorio.' }],
    description: [{ type: 'required', message: 'La descripción es obligatoria.' }],
    motifId:     [{ type: 'required', message: 'Debe seleccionar un motivo asociado.' }],
  };

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try {
      const [m, d] = await Promise.all([getMotifs(), getDiscriminators()]);
      setMotifs(m); setDiscs(d);
    } catch { setError('No se pudo cargar el catálogo.'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const filteredMotifs = motifs.filter(m => m.description.toLowerCase().includes(search.toLowerCase()) || m.code.toLowerCase().includes(search.toLowerCase()));
  const filteredDiscs = discs.filter(d => d.description.toLowerCase().includes(search.toLowerCase()) || d.code.toLowerCase().includes(search.toLowerCase()));

  const handleMotifChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const next = name === 'code' ? value.toUpperCase() : value;
    setMotifForm(f => ({ ...f, [name]: next }));
    setMotifFieldErrors(prev => clearFieldError(prev, name as keyof MotifFields));
    if (motifSaveError) setMotifSaveError(null);
  };

  const handleDiscChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const next = name === 'code' ? value.toUpperCase() : value;
    setDiscForm(f => ({ ...f, [name]: next }));
    setDiscFieldErrors(prev => clearFieldError(prev, name as keyof DiscFields));
    if (discSaveError) setDiscSaveError(null);
  };

  const handleMotifSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const errs = validateForm(motifSchema, { code: motifForm.code, description: motifForm.description });
    setMotifFieldErrors(errs);
    if (Object.keys(errs).length > 0) return;
    setSaving(true); setMotifSaveError(null);
    try {
      if (motifModal?.type === 'edit') await updateMotif(motifModal.item.id, motifForm);
      else await createMotif(motifForm);
      await load(); setMotifModal(null);
    } catch { setMotifSaveError('Error al guardar.'); }
    finally { setSaving(false); }
  };

  const handleDiscSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const errs = validateForm(discSchema, { code: discForm.code, description: discForm.description, motifId: discForm.motifId ?? '' });
    setDiscFieldErrors(errs);
    if (Object.keys(errs).length > 0) return;
    setSaving(true); setDiscSaveError(null);
    try {
      if (discModal?.type === 'edit') await updateDiscriminator(discModal.item.id, discForm);
      else await createDiscriminator(discForm);
      await load(); setDiscModal(null);
    } catch { setDiscSaveError('Error al guardar.'); }
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
            <h2 className="text-2xl font-bold text-gray-900">Catálogo de Triaje Manchester</h2>
            <p className="text-gray-500 text-sm">Gestiona motivos y discriminadores del sistema de triaje</p>
          </div>
        </div>

        {/* Tabs */}
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex gap-6">
            {(['motifs', 'discriminators'] as Tab[]).map(t => (
              <button key={t} onClick={() => { setTab(t); setSearch(''); }}
                className={`py-3 px-1 border-b-2 font-medium text-sm transition-colors ${tab === t ? 'border-medin-cyan text-medin-cyan' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
                {t === 'motifs' ? `Motivos (${motifs.length})` : `Discriminadores (${discs.length})`}
              </button>
            ))}
          </nav>
        </div>

        <div className="flex flex-col sm:flex-row gap-3 justify-between">
          <input type="text" placeholder={`Buscar ${tab === 'motifs' ? 'motivo' : 'discriminador'}...`} value={search} onChange={e => setSearch(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-medin-cyan w-64" />
          <button
            onClick={() => tab === 'motifs'
              ? (setMotifForm(EMPTY_MOTIF), setMotifFieldErrors({}), setMotifSaveError(null), setMotifModal({ type: 'create' }))
              : (setDiscForm(EMPTY_DISC), setDiscFieldErrors({}), setDiscSaveError(null), setDiscModal({ type: 'create' }))}
            className="flex items-center gap-2 px-4 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors text-sm">
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" /></svg>
            Agregar {tab === 'motifs' ? 'motivo' : 'discriminador'}
          </button>
        </div>

        {error && <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>}

        {loading ? (
          <div className="text-center py-12"><div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-medin-cyan border-t-transparent" /></div>
        ) : (
          <div className="bg-white rounded-lg shadow overflow-hidden">
            {tab === 'motifs' ? (
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>{['Código', 'Descripción', 'Categoría', 'Estado', 'Acciones'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                  ))}</tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {filteredMotifs.length === 0 ? (
                    <tr><td colSpan={5} className="text-center py-10 text-gray-400 text-sm">No hay motivos registrados</td></tr>
                  ) : filteredMotifs.map(m => (
                    <tr key={m.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 font-mono text-sm text-gray-700">{m.code}</td>
                      <td className="px-4 py-3 text-sm text-gray-900">{m.description}</td>
                      <td className="px-4 py-3 text-sm text-gray-500">{m.category || '—'}</td>
                      <td className="px-4 py-3">
                        {m.active
                          ? <span className="px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">Activo</span>
                          : <span className="px-2 py-0.5 bg-gray-100 text-gray-500 rounded-full text-xs font-medium">Inactivo</span>}
                      </td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => { setMotifForm({ code: m.code, description: m.description, category: m.category, active: m.active }); setMotifFieldErrors({}); setMotifSaveError(null); setMotifModal({ type: 'edit', item: m }); }}
                          className="text-sm text-medin-cyan hover:text-medin-blue font-medium">Editar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>{['Código', 'Descripción', 'Prioridad', 'Estado', 'Acciones'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                  ))}</tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {filteredDiscs.length === 0 ? (
                    <tr><td colSpan={5} className="text-center py-10 text-gray-400 text-sm">No hay discriminadores registrados</td></tr>
                  ) : filteredDiscs.map(d => (
                    <tr key={d.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 font-mono text-sm text-gray-700">{d.code}</td>
                      <td className="px-4 py-3 text-sm text-gray-900">{d.description}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${PRIORITY_COLORS[d.priorityLevel] ?? 'bg-gray-100 text-gray-600'}`}>
                          {PRIORITY_LEVELS[d.priorityLevel] ?? d.priorityLevel}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        {d.active
                          ? <span className="px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">Activo</span>
                          : <span className="px-2 py-0.5 bg-gray-100 text-gray-500 rounded-full text-xs font-medium">Inactivo</span>}
                      </td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => { setDiscForm({ code: d.code, description: d.description, priorityLevel: d.priorityLevel, motifId: d.motifId, active: d.active }); setDiscFieldErrors({}); setDiscSaveError(null); setDiscModal({ type: 'edit', item: d }); }}
                          className="text-sm text-medin-cyan hover:text-medin-blue font-medium">Editar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>

      {/* Modal Motivo */}
      {motifModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">{motifModal.type === 'create' ? 'Agregar motivo' : 'Editar motivo'}</h3>
            <form onSubmit={handleMotifSubmit} className="space-y-3" noValidate>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Código <span className="text-red-500">*</span></label>
                  <input name="code" className={inputCls(!!motifFieldErrors.code)} value={motifForm.code}
                    onChange={handleMotifChange} placeholder="Ej. MTF01" maxLength={20} />
                  {motifFieldErrors.code && <p className="mt-1 text-xs text-red-600">{motifFieldErrors.code}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Categoría</label>
                  <input className={inputCls()} value={motifForm.category ?? ''}
                    onChange={e => setMotifForm(f => ({ ...f, category: e.target.value }))} placeholder="Ej. Trauma" maxLength={100} />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción <span className="text-red-500">*</span></label>
                <input name="description" className={inputCls(!!motifFieldErrors.description)} value={motifForm.description}
                  onChange={handleMotifChange} placeholder="Descripción del motivo" maxLength={200} />
                {motifFieldErrors.description && <p className="mt-1 text-xs text-red-600">{motifFieldErrors.description}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Estado <span className="text-red-500">*</span></label>
                <select className={inputCls()} value={motifForm.active ? 'true' : 'false'} onChange={e => setMotifForm(f => ({ ...f, active: e.target.value === 'true' }))}>
                  <option value="true">Activo</option>
                  <option value="false">Inactivo</option>
                </select>
              </div>
              {motifSaveError && <p className="text-red-600 text-sm">{motifSaveError}</p>}
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setMotifModal(null)} className="flex-1 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50">Cancelar</button>
                <button type="submit" disabled={saving} className="flex-1 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg text-sm hover:bg-medin-blue hover:text-white disabled:opacity-50 transition-colors">
                  {saving ? 'Guardando...' : 'Guardar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Discriminador */}
      {discModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-4">{discModal.type === 'create' ? 'Agregar discriminador' : 'Editar discriminador'}</h3>
            <form onSubmit={handleDiscSubmit} className="space-y-3" noValidate>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Código <span className="text-red-500">*</span></label>
                  <input name="code" className={inputCls(!!discFieldErrors.code)} value={discForm.code}
                    onChange={handleDiscChange} placeholder="Ej. D01" maxLength={20} />
                  {discFieldErrors.code && <p className="mt-1 text-xs text-red-600">{discFieldErrors.code}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Prioridad <span className="text-red-500">*</span></label>
                  <select className={inputCls()} value={discForm.priorityLevel} onChange={e => setDiscForm(f => ({ ...f, priorityLevel: e.target.value }))}>
                    {Object.entries(PRIORITY_LEVELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                  </select>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción <span className="text-red-500">*</span></label>
                <input name="description" className={inputCls(!!discFieldErrors.description)} value={discForm.description}
                  onChange={handleDiscChange} placeholder="Descripción del discriminador" maxLength={200} />
                {discFieldErrors.description && <p className="mt-1 text-xs text-red-600">{discFieldErrors.description}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Motivo asociado <span className="text-red-500">*</span></label>
                <select className={inputCls(!!discFieldErrors.motifId)} value={discForm.motifId ?? ''}
                  onChange={e => { setDiscForm(f => ({ ...f, motifId: e.target.value || undefined })); setDiscFieldErrors(prev => clearFieldError(prev, 'motifId')); if (discSaveError) setDiscSaveError(null); }}>
                  <option value="">Seleccionar motivo</option>
                  {motifs.filter(m => m.active).map(m => <option key={m.id} value={m.id}>{m.code} — {m.description}</option>)}
                </select>
                {discFieldErrors.motifId && <p className="mt-1 text-xs text-red-600">{discFieldErrors.motifId}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Estado <span className="text-red-500">*</span></label>
                <select className={inputCls()} value={discForm.active ? 'true' : 'false'} onChange={e => setDiscForm(f => ({ ...f, active: e.target.value === 'true' }))}>
                  <option value="true">Activo</option>
                  <option value="false">Inactivo</option>
                </select>
              </div>
              {discSaveError && <p className="text-red-600 text-sm">{discSaveError}</p>}
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setDiscModal(null)} className="flex-1 py-2 border border-gray-300 rounded-lg text-sm text-gray-600 hover:bg-gray-50">Cancelar</button>
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

export default TriageCatalogPage;
