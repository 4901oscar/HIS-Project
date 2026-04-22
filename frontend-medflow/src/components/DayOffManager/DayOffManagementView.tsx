import { useState } from 'react';
import type { FC } from 'react';
import type { Doctor } from '../../services/doctorService';
import DayOffCalendar from './DayOffCalendar';
import DayOffManager from './DayOffManager';

interface DayOffManagementViewProps {
  doctor: Doctor;
  onClose?: () => void;
}

const DayOffManagementView: FC<DayOffManagementViewProps> = ({ doctor, onClose }) => {
  const [activeTab, setActiveTab] = useState<'calendar' | 'add'>('calendar');
  const [refreshKey, setRefreshKey] = useState(0);

  const handleDaysOffMarked = () => {
    setRefreshKey(prev => prev + 1);
    setActiveTab('calendar');
  };

  const handleDayOffRemoved = () => {
    setRefreshKey(prev => prev + 1);
  };

  return (
    <div className="max-w-6xl mx-auto p-6">
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">
              Gestión de Días Libres
            </h1>
            <p className="text-gray-600 mt-1">
              Administra la disponibilidad del Dr. {doctor.name}
            </p>
          </div>
          {onClose && (
            <button
              onClick={onClose}
              className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded transition-colors"
            >
              ← Volver
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="mb-6 border-b border-gray-200">
        <div className="flex space-x-4">
          <button
            onClick={() => setActiveTab('calendar')}
            className={`
              px-6 py-3 font-semibold transition-colors border-b-2
              ${activeTab === 'calendar'
                ? 'text-medin-cyan border-medin-cyan'
                : 'text-gray-500 border-transparent hover:text-gray-700'
              }
            `}
          >
            📅 Calendario
          </button>
          <button
            onClick={() => setActiveTab('add')}
            className={`
              px-6 py-3 font-semibold transition-colors border-b-2
              ${activeTab === 'add'
                ? 'text-medin-cyan border-medin-cyan'
                : 'text-gray-500 border-transparent hover:text-gray-700'
              }
            `}
          >
            ➕ Marcar Días Libres
          </button>
        </div>
      </div>

      {/* Content */}
      <div className="bg-white rounded-lg shadow-lg p-6">
        {activeTab === 'calendar' ? (
          <DayOffCalendar
            key={refreshKey}
            doctor={doctor}
            onDayOffRemoved={handleDayOffRemoved}
          />
        ) : (
          <DayOffManager
            doctor={doctor}
            onSuccess={handleDaysOffMarked}
            onCancel={() => setActiveTab('calendar')}
          />
        )}
      </div>

      {/* Help Section */}
      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="font-semibold text-gray-800 mb-2">💡 Ayuda</h3>
        <ul className="text-sm text-gray-700 space-y-1">
          <li>• <strong>Calendario:</strong> Visualiza los días disponibles (verde) y días libres (rojo) del doctor</li>
          <li>• <strong>Marcar Días Libres:</strong> Crea períodos de vacaciones o ausencias para el doctor</li>
          <li>• <strong>Eliminar Día Libre:</strong> Haz clic en un día rojo del calendario y luego en "Eliminar"</li>
          <li>• Durante los días libres, el doctor no será asignado automáticamente a nuevas citas</li>
        </ul>
      </div>
    </div>
  );
};

export default DayOffManagementView;
