import type { FC } from 'react';
import type { AppointmentListItem } from '../../services/appointmentService';
import type { PrescriptionDetailResponse } from '../../services/clinicalService';
import type { ServiceItemResponse } from '../../services/billingCatalogService';

interface PrescriptionDetailProps {
  appointment: AppointmentListItem;
  prescription: PrescriptionDetailResponse;
  onDispense: (appointmentId: string) => Promise<void>;
  onBack: () => void;
  dispensing: boolean;
  medicationCatalog?: ServiceItemResponse[];
}

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const PrescriptionDetail: FC<PrescriptionDetailProps> = ({
  appointment,
  prescription,
  onDispense,
  onBack,
  dispensing,
  medicationCatalog = [],
}) => {
  const resolveMedName = (name: string): string => {
    if (!UUID_REGEX.test(name)) return name;
    return medicationCatalog.find(m => m.id === name)?.name ?? name;
  };

  const handleDispense = async () => {
    await onDispense(appointment.id);
  };

  return (
    <div className="space-y-6">
      {/* Header with back button */}
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          <span className="text-sm font-medium">Volver a la cola</span>
        </button>
      </div>

      {/* Patient Information Card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <span className="text-xl">👤</span>
          Información del Paciente
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">Nombre Completo</p>
            <p className="text-sm font-medium text-gray-900">{prescription.patient.fullName}</p>
          </div>
          {prescription.patient.dpi && (
            <div>
              <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">DPI</p>
              <p className="text-sm font-mono font-medium text-gray-900">{prescription.patient.dpi}</p>
            </div>
          )}
          {prescription.patient.phone && (
            <div>
              <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">Teléfono</p>
              <p className="text-sm font-medium text-gray-900">{prescription.patient.phone}</p>
            </div>
          )}
          {prescription.patient.email && (
            <div>
              <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">Correo Electrónico</p>
              <p className="text-sm font-medium text-gray-900">{prescription.patient.email}</p>
            </div>
          )}
        </div>
      </div>

      {/* Prescription Metadata Card */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <span className="text-xl">📋</span>
          Información de la Receta
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">Código de Receta</p>
            <p className="text-sm font-mono font-bold text-green-700">{prescription.prescriptionCode}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">Doctor</p>
            <p className="text-sm font-medium text-gray-900">{prescription.doctor.name}</p>
            {prescription.doctor.specialty && (
              <p className="text-xs text-gray-500 mt-0.5">{prescription.doctor.specialty}</p>
            )}
          </div>
          <div>
            <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">Fecha de Emisión</p>
            <p className="text-sm font-medium text-gray-900">
              {new Date(prescription.issuedAt).toLocaleDateString('es-GT', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
              })}
            </p>
          </div>
        </div>
      </div>

      {/* Medications Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 bg-gradient-to-r from-green-500/5 to-transparent">
          <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
            <span className="text-xl">💊</span>
            Medicamentos
            <span className="ml-2 px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-semibold">
              {prescription.medications.length}
            </span>
          </h3>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
                <th className="pb-2 pr-4 pl-6 pt-3">Medicamento</th>
                <th className="pb-2 pr-4 pt-3">Dosis</th>
                <th className="pb-2 pr-4 pt-3">Frecuencia</th>
                <th className="pb-2 pr-4 pt-3">Duración</th>
                <th className="pb-2 pr-4 pt-3">Vía</th>
                <th className="pb-2 pr-6 pt-3">Cantidad Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {prescription.medications.map((med, index) => (
                <tr key={index} className="hover:bg-gray-50">
                  <td className="py-4 pr-4 pl-6">
                    <p className="font-medium text-gray-900">{resolveMedName(med.name)}</p>
                    {med.specialInstructions && (
                      <p className="text-xs text-gray-500 mt-1 italic">{med.specialInstructions}</p>
                    )}
                  </td>
                  <td className="py-4 pr-4 whitespace-nowrap">
                    <span className="font-medium">{med.dosage}</span>
                    {med.dosageAmount && med.dosageUnit && (
                      <span className="text-xs text-gray-500 ml-1">
                        ({med.dosageAmount} {med.dosageUnit})
                      </span>
                    )}
                  </td>
                  <td className="py-4 pr-4 whitespace-nowrap">
                    <span className="font-medium">{med.frequency}</span>
                    {med.frequencyHours && (
                      <span className="text-xs text-gray-500 ml-1">
                        (cada {med.frequencyHours}h)
                      </span>
                    )}
                  </td>
                  <td className="py-4 pr-4 whitespace-nowrap">
                    <span className="font-medium">{med.durationDays} días</span>
                  </td>
                  <td className="py-4 pr-4 whitespace-nowrap">
                    <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs font-medium">
                      {med.route}
                    </span>
                  </td>
                  <td className="py-4 pr-6 whitespace-nowrap">
                    {med.totalQuantity ? (
                      <span className="font-bold text-green-700">
                        {med.totalQuantity} {med.dosageUnit || 'unidades'}
                      </span>
                    ) : (
                      <span className="text-gray-400">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3 justify-end">
        <button
          onClick={onBack}
          disabled={dispensing}
          className="px-6 py-3 bg-white text-gray-700 font-semibold rounded-lg border border-gray-300 hover:bg-gray-50 hover:border-gray-400 hover:shadow-sm transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Volver a la Cola
        </button>
        <button
          onClick={handleDispense}
          disabled={dispensing || prescription.status !== 'PENDING'}
          className="px-6 py-3 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 hover:shadow-md transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed active:bg-green-800 active:scale-95 flex items-center gap-2"
          title={prescription.status !== 'PENDING' ? 'Esta receta ya fue dispensada' : 'Dispensar medicamentos al paciente'}
        >
          {dispensing ? (
            <>
              <div className="inline-block animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent" />
              <span>Dispensando...</span>
            </>
          ) : (
            <>
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
              <span>Dispensar Medicamentos</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default PrescriptionDetail;
