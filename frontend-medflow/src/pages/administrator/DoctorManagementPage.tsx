import { useState } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import DoctorList from '../../components/DoctorList/DoctorList';
import DoctorForm from '../../components/DoctorForm/DoctorForm';
import DayOffManagementView from '../../components/DayOffManager/DayOffManagementView';
import type { Doctor } from '../../services/doctorService';

type ViewMode = 'list' | 'create' | 'edit' | 'dayoff';

const DoctorManagementPage: FC = () => {
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [selectedDoctor, setSelectedDoctor] = useState<Doctor | null>(null);

  const handleCreateDoctor = () => {
    setSelectedDoctor(null);
    setViewMode('create');
  };

  const handleEditDoctor = (doctor: Doctor) => {
    setSelectedDoctor(doctor);
    setViewMode('edit');
  };

  const handleManageDaysOff = (doctor: Doctor) => {
    setSelectedDoctor(doctor);
    setViewMode('dayoff');
  };

  const handleSuccess = () => {
    setViewMode('list');
    setSelectedDoctor(null);
  };

  const handleCancel = () => {
    setViewMode('list');
    setSelectedDoctor(null);
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex justify-between items-center">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Gestión de Doctores</h2>
            <p className="text-gray-600">Administre doctores, turnos y días libres</p>
          </div>
          {viewMode === 'list' && (
            <button
              onClick={handleCreateDoctor}
              className="px-4 py-2 bg-medin-blue text-medin-navy font-semibold hover:bg-medin-blue-light transition-colors"
            >
              + VINCULAR DOCTOR
            </button>
          )}
          {viewMode !== 'list' && (
            <button
              onClick={handleCancel}
              className="px-4 py-2 bg-gray-300 text-gray-700 font-semibold hover:bg-gray-400 transition-colors"
            >
              ← VOLVER A LA LISTA
            </button>
          )}
        </div>

        {/* Content */}
        <div className="bg-white rounded-lg shadow p-6">
          {viewMode === 'list' && (
            <DoctorList
              onCreateClick={handleCreateDoctor}
              onEditClick={handleEditDoctor}
              onManageDaysOff={handleManageDaysOff}
            />
          )}

          {(viewMode === 'create' || viewMode === 'edit') && (
            <DoctorForm
              doctor={selectedDoctor}
              onSuccess={handleSuccess}
              onCancel={handleCancel}
            />
          )}

          {viewMode === 'dayoff' && selectedDoctor && (
            <DayOffManagementView
              doctor={selectedDoctor}
              onClose={handleCancel}
            />
          )}
        </div>
      </div>
    </MainLayout>
  );
};

export default DoctorManagementPage;
