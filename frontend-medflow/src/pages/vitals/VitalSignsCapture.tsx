import { useState } from 'react';
import type { FC, FormEvent } from 'react';
import { MainLayout } from '../../components/Layout';
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { searchPatients, getPatientByDpi } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import { recordVitalSigns } from '../../services/clinicalService';
import type { VitalSignsResponse } from '../../services/clinicalService';
import axios from 'axios';

interface VitalForm {
  systolicPressure: string;
  diastolicPressure: string;
  heartRate: string;
  respiratoryRate: string;
  temperature: string;
  oxygenSaturation: string;
  weight: string;
  height: string;
}

const emptyForm: VitalForm = {
  systolicPressure: '',
  diastolicPressure: '',
  heartRate: '',
  respiratoryRate: '',
  temperature: '',
  oxygenSaturation: '',
  weight: '',
  height: '',
};

const VitalSignsCapture: FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [searchResults, setSearchResults] = useState<PatientResponse[]>([]);
  const [patient, setPatient] = useState<PatientResponse | null>(null);

  const [form, setForm] = useState<VitalForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [result, setResult] = useState<VitalSignsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;
    setSearching(true);
    setSearchResults([]);
    setError(null);
    try {
      // Intento por DPI exacto primero
      if (/^\d{13}$/.test(searchQuery.trim())) {
        const found = await getPatientByDpi(searchQuery.trim());
        setPatient(found);
        setForm(emptyForm);
        setResult(null);
        return;
      }
      const results = await searchPatients(searchQuery.trim());
      setSearchResults(results);
    } catch {
      setError('No se encontró el paciente');
    } finally {
      setSearching(false);
    }
  };

  const selectPatient = (p: PatientResponse) => {
    setPatient(p);
    setSearchResults([]);
    setForm(emptyForm);
    setResult(null);
    setError(null);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!patient) return;
    setSaving(true);
    setError(null);
    setResult(null);
    try {
      const res = await recordVitalSigns({
        patientId: patient.id,
        systolicPressure: Number(form.systolicPressure),
        diastolicPressure: Number(form.diastolicPressure),
        heartRate: Number(form.heartRate),
        respiratoryRate: Number(form.respiratoryRate),
        temperature: Number(form.temperature),
        oxygenSaturation: Number(form.oxygenSaturation),
        weight: form.weight ? Number(form.weight) : undefined,
        height: form.height ? Number(form.height) : undefined,
      });
      setResult(res);
      setForm(emptyForm);
    } catch (err) {
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message || 'Error al registrar signos vitales');
      } else {
        setError('Error al conectar con el servidor');
      }
    } finally {
      setSaving(false);
    }
  };

  const inputClass = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Captura de Signos Vitales</h2>
          <p className="mt-1 text-sm text-gray-600">Registrar signos vitales del paciente</p>
        </div>

        {/* Buscar paciente */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Paciente</h3>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <MagnifyingGlassIcon className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Buscar por DPI (13 dígitos) o nombre..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm"
              />
            </div>
            <button
              onClick={handleSearch}
              disabled={searching}
              className="px-4 py-2 bg-medin-navy text-white rounded-lg text-sm disabled:opacity-50"
            >
              {searching ? 'Buscando...' : 'Buscar'}
            </button>
          </div>

          {searchResults.length > 0 && (
            <div className="mt-2 divide-y border border-gray-200 rounded-lg overflow-hidden">
              {searchResults.map((p) => (
                <button key={p.id} onClick={() => selectPatient(p)} className="w-full text-left px-4 py-3 hover:bg-gray-50">
                  <p className="font-medium text-sm text-gray-900">{p.fullName}</p>
                  <p className="text-xs text-gray-500">DPI: {p.dpi}</p>
                </button>
              ))}
            </div>
          )}

          {patient && (
            <div className="mt-3 p-3 bg-medin-cyan/10 border border-medin-cyan/30 rounded-lg flex justify-between items-start">
              <div>
                <p className="font-semibold text-gray-900">{patient.fullName}</p>
                <p className="text-xs text-gray-600">DPI: {patient.dpi} — {patient.email}</p>
              </div>
              <button onClick={() => { setPatient(null); setResult(null); }} className="text-xs text-gray-400 hover:text-gray-600">
                Cambiar
              </button>
            </div>
          )}
        </div>

        {/* Resultado registrado */}
        {result && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="font-semibold text-green-800 mb-2">Signos vitales registrados</p>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
              <div><span className="text-gray-500">TA:</span> {result.systolicPressure}/{result.diastolicPressure} mmHg</div>
              <div><span className="text-gray-500">FC:</span> {result.heartRate} lpm</div>
              <div><span className="text-gray-500">FR:</span> {result.respiratoryRate} rpm</div>
              <div><span className="text-gray-500">Temp:</span> {result.temperature} °C</div>
              <div><span className="text-gray-500">SpO2:</span> {result.oxygenSaturation}%</div>
              {result.weight && <div><span className="text-gray-500">Peso:</span> {result.weight} kg</div>}
              {result.height && <div><span className="text-gray-500">Talla:</span> {result.height} cm</div>}
              {result.bmi && <div><span className="text-gray-500">IMC:</span> {result.bmi?.toFixed(1)}</div>}
            </div>
          </div>
        )}

        {/* Formulario */}
        {patient && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Registrar Signos Vitales</h3>

            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm">{error}</div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div>
                  <label className={labelClass}>Sist. (mmHg) <span className="text-red-500">*</span></label>
                  <input type="number" name="systolicPressure" value={form.systolicPressure} onChange={handleChange} required min={50} max={250} className={inputClass} placeholder="120" />
                </div>
                <div>
                  <label className={labelClass}>Diast. (mmHg) <span className="text-red-500">*</span></label>
                  <input type="number" name="diastolicPressure" value={form.diastolicPressure} onChange={handleChange} required min={30} max={150} className={inputClass} placeholder="80" />
                </div>
                <div>
                  <label className={labelClass}>FC (lpm) <span className="text-red-500">*</span></label>
                  <input type="number" name="heartRate" value={form.heartRate} onChange={handleChange} required min={20} max={300} className={inputClass} placeholder="72" />
                </div>
                <div>
                  <label className={labelClass}>FR (rpm) <span className="text-red-500">*</span></label>
                  <input type="number" name="respiratoryRate" value={form.respiratoryRate} onChange={handleChange} required min={5} max={60} className={inputClass} placeholder="16" />
                </div>
                <div>
                  <label className={labelClass}>Temperatura (°C) <span className="text-red-500">*</span></label>
                  <input type="number" step="0.1" name="temperature" value={form.temperature} onChange={handleChange} required min={30} max={45} className={inputClass} placeholder="36.5" />
                </div>
                <div>
                  <label className={labelClass}>SpO2 (%) <span className="text-red-500">*</span></label>
                  <input type="number" name="oxygenSaturation" value={form.oxygenSaturation} onChange={handleChange} required min={50} max={100} className={inputClass} placeholder="98" />
                </div>
                <div>
                  <label className={labelClass}>Peso (kg)</label>
                  <input type="number" step="0.1" name="weight" value={form.weight} onChange={handleChange} min={1} max={300} className={inputClass} placeholder="70" />
                </div>
                <div>
                  <label className={labelClass}>Talla (cm)</label>
                  <input type="number" name="height" value={form.height} onChange={handleChange} min={30} max={250} className={inputClass} placeholder="170" />
                </div>
              </div>

              <div className="flex justify-end">
                <button
                  type="submit"
                  disabled={saving}
                  className="px-6 py-2 bg-medin-cyan text-medin-navy font-semibold rounded-lg hover:bg-medin-blue hover:text-white transition-colors disabled:opacity-50"
                >
                  {saving ? 'Guardando...' : 'Guardar Signos Vitales'}
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default VitalSignsCapture;
