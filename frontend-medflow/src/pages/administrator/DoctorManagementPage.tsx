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
          <div className="flex items-center gap-3">
            <button
              onClick={() => window.location.href = '/administrator'}
              className="text-gray-400 hover:text-gray-600"
            >
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Gestión de Doctores</h2>
              <p className="text-gray-500 text-sm">Administre doctores, turnos y días libres</p>
            </div>
          </div>
        </div>

        {/* Content */}
        {viewMode === 'list' && (
          <div className="bg-white rounded-lg shadow p-6">
            <DoctorList
              onCreateClick={handleCreateDoctor}
              onEditClick={handleEditDoctor}
              onManageDaysOff={handleManageDaysOff}
            />
          </div>
        )}

        {(viewMode === 'create' || viewMode === 'edit') && (
          <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-6">
            <DoctorForm
              doctor={selectedDoctor}
              onSuccess={handleSuccess}
              onCancel={handleCancel}
            />
          </div>
        )}

        {viewMode === 'dayoff' && selectedDoctor && (
          <div className="bg-white rounded-lg shadow p-6">
            <DayOffManagementView
              doctor={selectedDoctor}
              onClose={handleCancel}
            />
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default DoctorManagementPage;
