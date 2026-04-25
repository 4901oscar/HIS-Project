import { useState } from 'react';
import type { FC } from 'react';
import type { VitalSignsRequest } from '../../services/clinicalService';

// ─── Type Definitions ─────────────────────────────────────────────────────────

interface VitalSignsFormProps {
  patientId: string;
  onSubmit: (data: VitalSignsRequest) => Promise<void>;
  onCancel: () => void;
  submitting: boolean;
  error: string | null;
}

interface VitalSignsFormData {
  systolicPressure: string;
  diastolicPressure: string;
  heartRate: string;
  respiratoryRate: string;
  temperature: string;
  oxygenSaturation: string;
  weight: string;
  height: string;
}

// ─── Component ────────────────────────────────────────────────────────────────

const VitalSignsForm: FC<VitalSignsFormProps> = ({
  patientId,
  onSubmit,
  onCancel,
  submitting,
  error,
}) => {
  // Form state
  const [form, setForm] = useState<VitalSignsFormData>({
    systolicPressure: '',
    diastolicPressure: '',
    heartRate: '',
    respiratoryRate: '',
    temperature: '',
    oxygenSaturation: '',
    weight: '',
    height: '',
  });

  // Validation errors state
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});

  // ─── Event Handlers ───────────────────────────────────────────────────────

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    
    // Clear validation error for this field
    if (validationErrors[name]) {
      setValidationErrors((prev) => {
        const next = { ...prev };
        delete next[name];
        return next;
      });
    }
  };

  // ─── Validation Logic ─────────────────────────────────────────────────────

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    // Validate systolic pressure (50-250 mmHg)
    const systolic = Number(form.systolicPressure);
    if (!form.systolicPressure || isNaN(systolic)) {
      errors.systolicPressure = 'Campo requerido';
    } else if (systolic < 50 || systolic > 250) {
      errors.systolicPressure = 'Debe estar entre 50 y 250 mmHg';
    }

    // Validate diastolic pressure (30-150 mmHg)
    const diastolic = Number(form.diastolicPressure);
    if (!form.diastolicPressure || isNaN(diastolic)) {
      errors.diastolicPressure = 'Campo requerido';
    } else if (diastolic < 30 || diastolic > 150) {
      errors.diastolicPressure = 'Debe estar entre 30 y 150 mmHg';
    }

    // Validate heart rate (20-300 lpm)
    const heartRate = Number(form.heartRate);
    if (!form.heartRate || isNaN(heartRate)) {
      errors.heartRate = 'Campo requerido';
    } else if (heartRate < 20 || heartRate > 300) {
      errors.heartRate = 'Debe estar entre 20 y 300 lpm';
    }

    // Validate respiratory rate (5-60 rpm)
    const respiratoryRate = Number(form.respiratoryRate);
    if (!form.respiratoryRate || isNaN(respiratoryRate)) {
      errors.respiratoryRate = 'Campo requerido';
    } else if (respiratoryRate < 5 || respiratoryRate > 60) {
      errors.respiratoryRate = 'Debe estar entre 5 y 60 rpm';
    }

    // Validate temperature (30-45 °C)
    const temperature = Number(form.temperature);
    if (!form.temperature || isNaN(temperature)) {
      errors.temperature = 'Campo requerido';
    } else if (temperature < 30 || temperature > 45) {
      errors.temperature = 'Debe estar entre 30 y 45 °C';
    }

    // Validate oxygen saturation (50-100%)
    const oxygenSaturation = Number(form.oxygenSaturation);
    if (!form.oxygenSaturation || isNaN(oxygenSaturation)) {
      errors.oxygenSaturation = 'Campo requerido';
    } else if (oxygenSaturation < 50 || oxygenSaturation > 100) {
      errors.oxygenSaturation = 'Debe estar entre 50 y 100%';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // ─── Form Submission ──────────────────────────────────────────────────────

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validate()) {
      return;
    }

    // Convert string inputs to numbers
    const vitalSignsData: VitalSignsRequest = {
      patientId,
      systolicPressure: Number(form.systolicPressure),
      diastolicPressure: Number(form.diastolicPressure),
      heartRate: Number(form.heartRate),
      respiratoryRate: Number(form.respiratoryRate),
      temperature: Number(form.temperature),
      oxygenSaturation: Number(form.oxygenSaturation),
      // Optional fields
      weight: form.weight ? Number(form.weight) : undefined,
      height: form.height ? Number(form.height) : undefined,
    };

    await onSubmit(vitalSignsData);
  };

  // ─── Styling Classes ──────────────────────────────────────────────────────

  const inputClass = 'w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-medin-cyan focus:border-transparent text-sm';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1';
  const errorClass = 'text-xs text-red-600 mt-1';

  // ─── Render ───────────────────────────────────────────────────────────────

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-medin-navy">Registrar Signos Vitales</h3>
        <button
          onClick={onCancel}
          className="text-sm text-gray-500 hover:text-gray-700"
          disabled={submitting}
          aria-label="Cancelar registro de signos vitales"
        >
          Cancelar
        </button>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-sm" role="alert">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Systolic Pressure */}
          <div>
            <label htmlFor="systolicPressure" className={labelClass}>
              Sist. (mmHg) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              id="systolicPressure"
              name="systolicPressure"
              value={form.systolicPressure}
              onChange={handleChange}
              required
              min={50}
              max={250}
              className={inputClass}
              placeholder="120"
              disabled={submitting}
              aria-required="true"
              aria-invalid={!!validationErrors.systolicPressure}
              aria-describedby={validationErrors.systolicPressure ? 'systolicPressure-error' : undefined}
            />
            {validationErrors.systolicPressure && (
              <p id="systolicPressure-error" className={errorClass}>
                {validationErrors.systolicPressure}
              </p>
            )}
          </div>

          {/* Diastolic Pressure */}
          <div>
            <label htmlFor="diastolicPressure" className={labelClass}>
              Diast. (mmHg) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              id="diastolicPressure"
              name="diastolicPressure"
              value={form.diastolicPressure}
              onChange={handleChange}
              required
              min={30}
              max={150}
              className={inputClass}
              placeholder="80"
              disabled={submitting}
              aria-required="true"
              aria-invalid={!!validationErrors.diastolicPressure}
              aria-describedby={validationErrors.diastolicPressure ? 'diastolicPressure-error' : undefined}
            />
            {validationErrors.diastolicPressure && (
              <p id="diastolicPressure-error" className={errorClass}>
                {validationErrors.diastolicPressure}
              </p>
            )}
          </div>

          {/* Heart Rate */}
          <div>
            <label htmlFor="heartRate" className={labelClass}>
              FC (lpm) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              id="heartRate"
              name="heartRate"
              value={form.heartRate}
              onChange={handleChange}
              required
              min={20}
              max={300}
              className={inputClass}
              placeholder="72"
              disabled={submitting}
              aria-required="true"
              aria-invalid={!!validationErrors.heartRate}
              aria-describedby={validationErrors.heartRate ? 'heartRate-error' : undefined}
            />
            {validationErrors.heartRate && (
              <p id="heartRate-error" className={errorClass}>
                {validationErrors.heartRate}
              </p>
            )}
          </div>

          {/* Respiratory Rate */}
          <div>
            <label htmlFor="respiratoryRate" className={labelClass}>
              FR (rpm) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              id="respiratoryRate"
              name="respiratoryRate"
              value={form.respiratoryRate}
              onChange={handleChange}
              required
              min={5}
              max={60}
              className={inputClass}
              placeholder="16"
              disabled={submitting}
              aria-required="true"
              aria-invalid={!!validationErrors.respiratoryRate}
              aria-describedby={validationErrors.respiratoryRate ? 'respiratoryRate-error' : undefined}
            />
            {validationErrors.respiratoryRate && (
              <p id="respiratoryRate-error" className={errorClass}>
                {validationErrors.respiratoryRate}
              </p>
            )}
          </div>

          {/* Temperature */}
          <div>
            <label htmlFor="temperature" className={labelClass}>
              Temperatura (°C) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              step="0.1"
              id="temperature"
              name="temperature"
              value={form.temperature}
              onChange={handleChange}
              required
              min={30}
              max={45}
              className={inputClass}
              placeholder="36.5"
              disabled={submitting}
              aria-required="true"
              aria-invalid={!!validationErrors.temperature}
              aria-describedby={validationErrors.temperature ? 'temperature-error' : undefined}
            />
            {validationErrors.temperature && (
              <p id="temperature-error" className={errorClass}>
                {validationErrors.temperature}
              </p>
            )}
          </div>

          {/* Oxygen Saturation */}
          <div>
            <label htmlFor="oxygenSaturation" className={labelClass}>
              SpO2 (%) <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              id="oxygenSaturation"
              name="oxygenSaturation"
              value={form.oxygenSaturation}
              onChange={handleChange}
              required
              min={50}
              max={100}
              className={inputClass}
              placeholder="98"
              disabled={submitting}
              aria-required="true"
              aria-invalid={!!validationErrors.oxygenSaturation}
              aria-describedby={validationErrors.oxygenSaturation ? 'oxygenSaturation-error' : undefined}
            />
            {validationErrors.oxygenSaturation && (
              <p id="oxygenSaturation-error" className={errorClass}>
                {validationErrors.oxygenSaturation}
              </p>
            )}
          </div>

          {/* Weight (Optional) */}
          <div>
            <label htmlFor="weight" className={labelClass}>
              Peso (kg)
            </label>
            <input
              type="number"
              step="0.1"
              id="weight"
              name="weight"
              value={form.weight}
              onChange={handleChange}
              min={1}
              max={300}
              className={inputClass}
              placeholder="70"
              disabled={submitting}
            />
          </div>

          {/* Height (Optional) */}
          <div>
            <label htmlFor="height" className={labelClass}>
              Talla (cm)
            </label>
            <input
              type="number"
              id="height"
              name="height"
              value={form.height}
              onChange={handleChange}
              min={30}
              max={250}
              className={inputClass}
              placeholder="170"
              disabled={submitting}
            />
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onCancel}
            disabled={submitting}
            className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm font-medium disabled:opacity-50"
            aria-label="Cancelar registro de signos vitales"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="px-6 py-2 bg-medin-cyan text-white font-semibold rounded-lg hover:bg-medin-navy hover:text-white transition-colors text-sm disabled:opacity-50"
            aria-label="Guardar signos vitales del paciente"
          >
            {submitting ? 'Guardando...' : 'Guardar Signos Vitales'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default VitalSignsForm;
