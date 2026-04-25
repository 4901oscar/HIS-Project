import { useState, useEffect, useCallback } from 'react';
import type { FC, FormEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { getPatientById } from '../../services/patientService';
import type { PatientResponse } from '../../services/patientService';
import { recordVitalSigns } from '../../services/clinicalService';
import type { VitalSignsResponse } from '../../services/clinicalService';
import { extractErrorMessage } from '../../utils/errorHandler';

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

const TriageVitalSignsCapture: FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  const { appointmentId, patientId } = location.state || {};
  
  const [patient, setPatient] = useState<PatientResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [form, setForm] = useState<VitalForm>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [result, setResult] = useState<VitalSignsResponse | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);

  // Load patient data
  useEffect(() => {
    if (!patientId || !appointmentId) {
      setError('No se proporcionó información de la cita');
      setLoading(false);
      return;
    }

    const fetchPatient = async () => {
      try {
        const patientData = await getPatientById(patientId);
        setPatient(patientData);
      } catch (err) {
        console.error('Error fetching patient:', err);
        setError(extractErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchPatient();
  }, [patientId, appointmentId]);

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }, []);

  const handleSubmit = useCallback(async (e: FormEvent) => {
    e.preventDefault();
    if (!patient) return;

    setSaving(true);
    setSaveError(null);
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
      
      // Redirect back to triage pending page after 2 seconds
      setTimeout(() => {
        navigate('/vitals/triage');
      }, 2000);
    } catch (err) {
      console.error('Error recording vital signs:', err);
      setSaveError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  }, [patient, form, navigate]);

  const handleCancel = useCallback(() => {
    navigate('/vitals/triage');
  }, [navigate]);

  const inputClass = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';

  if (loading) {
    return (
      <MainLayout>
        <div className="flex justify-center items-center min-h-[60vh]">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-medin-cyan border-t-transparent" />
        </div>
      </MainLayout>
    );
  }

  if (error || !patient) {
    return (
      <MainLayout>
        <div className="space-y-6">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Registrar Signos Vitales</h2>
            <p className="mt-1 text-sm text-gray-600">Triaje</p>
          </div>
          
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">{error || 'No se pudo cargar la información del paciente'}</p>
            <button
              onClick={handleCancel}
              className="mt-4 px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
            >
              Volver a Triaje Pendiente
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Registrar Signos Vitales</h2>
            <p className="mt-1 text-sm text-gray-600">Triaje - {patient.fullName}</p>
          </div>
          <button
            onClick={handleCancel}
            className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-gray-900 transition-colors"
          >
            <ArrowLeftIcon className="h-5 w-5" />
            Volver
          </button>
        </div>

        {/* Patient Info Card */}
        <div className="bg-medin-cyan/10 border border-medin-cyan/30 rounded-lg p-4">
          <h3 className="font-semibold text-medin-navy mb-2">Información del Paciente</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
            <div>
              <span className="text-gray-600">Nombre:</span>
              <span className="ml-2 font-medium">{patient.fullName}</span>
            </div>
            <div>
              <span className="text-gray-600">DPI:</span>
              <span className="ml-2 font-medium font-mono">{patient.dpi}</span>
            </div>
            <div>
              <span className="text-gray-600">Email:</span>
              <span className="ml-2 font-medium">{patient.email}</span>
            </div>
          </div>
        </div>

        {/* Success Message */}
        {result && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="font-semibold text-green-800 mb-2">✓ Signos vitales registrados exitosamente</p>
            <p className="text-sm text-green-700">Redirigiendo a Triaje Pendiente...</p>
          </div>
        )}

        {/* Error Message */}
        {saveError && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">{saveError}</p>
          </div>
        )}

        {/* Form */}
        {!result && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Signos Vitales</h3>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div>
                  <label className={labelClass}>Sist. (mmHg) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="systolicPressure"
                    value={form.systolicPressure}
                    onChange={handleChange}
                    required
                    min={50}
                    max={250}
                    className={inputClass}
                    placeholder="120"
                  />
                </div>
                <div>
                  <label className={labelClass}>Diast. (mmHg) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="diastolicPressure"
                    value={form.diastolicPressure}
                    onChange={handleChange}
                    required
                    min={30}
                    max={150}
                    className={inputClass}
                    placeholder="80"
                  />
                </div>
                <div>
                  <label className={labelClass}>FC (lpm) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="heartRate"
                    value={form.heartRate}
                    onChange={handleChange}
                    required
                    min={20}
                    max={300}
                    className={inputClass}
                    placeholder="72"
                  />
                </div>
                <div>
                  <label className={labelClass}>FR (rpm) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="respiratoryRate"
                    value={form.respiratoryRate}
                    onChange={handleChange}
                    required
                    min={5}
                    max={60}
                    className={inputClass}
                    placeholder="16"
                  />
                </div>
                <div>
                  <label className={labelClass}>Temperatura (°C) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    step="0.1"
                    name="temperature"
                    value={form.temperature}
                    onChange={handleChange}
                    required
                    min={30}
                    max={45}
                    className={inputClass}
                    placeholder="36.5"
                  />
                </div>
                <div>
                  <label className={labelClass}>SpO2 (%) <span className="text-red-500">*</span></label>
                  <input
                    type="number"
                    name="oxygenSaturation"
                    value={form.oxygenSaturation}
                    onChange={handleChange}
                    required
                    min={50}
                    max={100}
                    className={inputClass}
                    placeholder="98"
                  />
                </div>
                <div>
                  <label className={labelClass}>Peso (kg)</label>
                  <input
                    type="number"
                    step="0.1"
                    name="weight"
                    value={form.weight}
                    onChange={handleChange}
                    min={1}
                    max={300}
                    className={inputClass}
                    placeholder="70"
                  />
                </div>
                <div>
                  <label className={labelClass}>Talla (cm)</label>
                  <input
                    type="number"
                    name="height"
                    value={form.height}
                    onChange={handleChange}
                    min={30}
                    max={250}
                    className={inputClass}
                    placeholder="170"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <button
                  type="button"
                  onClick={handleCancel}
                  disabled={saving}
                  className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-6 py-2 bg-medin-cyan text-white font-semibold rounded-lg hover:bg-medin-blue transition-colors disabled:opacity-50"
                >
                  {saving ? 'Guardando...' : 'Guardar y Continuar'}
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default TriageVitalSignsCapture;
