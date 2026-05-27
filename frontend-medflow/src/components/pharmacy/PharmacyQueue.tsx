import { useState } from 'react';
import type { FC } from 'react';
import type { AppointmentListItem } from '../../services/appointmentService';

interface PharmacyQueueProps {
  appointments: AppointmentListItem[];
  loading: boolean;
  onSelect: (appointment: AppointmentListItem) => void;
  onRefresh: () => void;
}

const PharmacyQueue: FC<PharmacyQueueProps> = ({ appointments, loading, onSelect, onRefresh }) => {
  const [search, setSearch] = useState('');

  // Loading state
  if (loading) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-green-600 border-t-transparent mb-4" />
          <p className="text-gray-600">Cargando cola de farmacia...</p>
        </div>
      </div>
    );
  }

  const filtered = appointments
    .filter(a => !search || a.patient.dpi?.includes(search.trim()))
    .slice()
    .sort((a, b) => {
      const dateCompare = a.appointmentDate.localeCompare(b.appointmentDate);
      if (dateCompare !== 0) return dateCompare;
      return a.appointmentTime.localeCompare(b.appointmentTime);
    });

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      {/* Header */}
      <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gradient-to-r from-green-500/5 to-transparent">
        <div className="flex items-center gap-3">
          <span className="text-xl">💊</span>
          <div>
            <h3 className="text-sm font-semibold text-gray-900">Cola de Farmacia</h3>
            <p className="text-xs text-gray-500">Recetas pendientes de despacho</p>
          </div>
          <span className="ml-2 px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-semibold">
            {loading ? '…' : filtered.length}
          </span>
        </div>
        <div className="flex items-center gap-3">
          <input
            type="text"
            inputMode="numeric"
            value={search}
            onChange={e => { if (/^\d*$/.test(e.target.value)) setSearch(e.target.value); }}
            placeholder="Buscar por DPI..."
            className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-green-500 focus:border-transparent w-44"
          />
          <button
            onClick={onRefresh}
            disabled={loading}
            className="text-xs text-green-700 hover:text-green-500 disabled:opacity-50 transition-colors"
            title="Actualizar cola"
          >
            Actualizar
          </button>
        </div>
      </div>

      {/* Empty state */}
      {filtered.length === 0 && (
        <div className="text-center py-12 text-gray-400">
          <svg className="mx-auto h-12 w-12 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <p className="text-sm font-medium">
            {search ? 'No se encontraron recetas para ese DPI' : 'No hay recetas pendientes de despacho'}
          </p>
          {!search && <p className="text-xs mt-1">Las recetas pagadas aparecerán aquí</p>}
        </div>
      )}

      {/* Table */}
      {filtered.length > 0 && (
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="border-b border-gray-200 text-left text-xs text-gray-500 uppercase tracking-wide">
                <th className="pb-2 pr-4 pl-6">Fecha</th>
                <th className="pb-2 pr-4">Hora</th>
                <th className="pb-2 pr-4">Paciente</th>
                <th className="pb-2 pr-4">DPI</th>
                <th className="pb-2 pr-4">Cód. Receta</th>
                <th className="pb-2 px-6 text-center">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filtered.map(appt => (
                <tr key={appt.id} className="hover:bg-gray-50 transition-colors">
                  <td className="py-3 pr-4 pl-6 whitespace-nowrap text-xs">
                    {new Date(appt.appointmentDate + 'T00:00:00').toLocaleDateString('es-GT', {
                      day: '2-digit',
                      month: '2-digit',
                      year: 'numeric'
                    })}
                  </td>
                  <td className="py-3 pr-4 whitespace-nowrap font-medium">
                    {appt.appointmentTime.substring(0, 5)}
                  </td>
                  <td className="py-3 pr-4">{appt.patient.fullName}</td>
                  <td className="py-3 pr-4 font-mono text-xs">{appt.patient.dpi ?? '—'}</td>
                  <td className="py-3 pr-4 whitespace-nowrap">
                    {appt.prescriptionCode
                      ? <span className="font-mono font-bold text-green-700 text-xs">{appt.prescriptionCode}</span>
                      : <span className="text-gray-400 text-xs">—</span>
                    }
                  </td>
                  <td className="py-3 px-6 whitespace-nowrap text-center">
                    <button
                      onClick={() => onSelect(appt)}
                      className="px-4 py-2 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 hover:shadow-md transition-all duration-200 text-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 active:bg-green-800 active:scale-95"
                    >
                      Atender
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default PharmacyQueue;
