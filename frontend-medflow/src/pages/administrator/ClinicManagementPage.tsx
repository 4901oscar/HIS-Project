import { useState } from 'react';
import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';
import ClinicList from '../../components/ClinicList/ClinicList';
import ClinicForm from '../../components/ClinicForm/ClinicForm';
import type { Clinic } from '../../types/clinic';

type ViewMode = 'list' | 'create' | 'edit';

const ClinicManagementPage: FC = () => {
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [selectedClinic, setSelectedClinic] = useState<Clinic | null>(null);

  const handleCreateClinic = () => {
    setSelectedClinic(null);
    setViewMode('create');
  };

  const handleEditClinic = (clinic: Clinic) => {
    setSelectedClinic(clinic);
    setViewMode('edit');
  };

  const handleSuccess = () => {
    setViewMode('list');
    setSelectedClinic(null);
  };

  const handleCancel = () => {
    setViewMode('list');
    setSelectedClinic(null);
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
              <h2 className="text-2xl font-bold text-gray-900">Gestión de Clínicas</h2>
              <p className="text-gray-500 text-sm">Administre consultorios y espacios clínicos</p>
            </div>
          </div>
        </div>

        {/* Content */}
        {viewMode === 'list' && (
          <div className="bg-white rounded-lg shadow p-6">
            <ClinicList
              onCreateClick={handleCreateClinic}
              onEditClick={handleEditClinic}
            />
          </div>
        )}

        {(viewMode === 'create' || viewMode === 'edit') && (
          <div className="max-w-2xl mx-auto bg-white rounded-lg shadow p-6">
            <ClinicForm
              clinic={selectedClinic}
              onSuccess={handleSuccess}
              onCancel={handleCancel}
            />
          </div>
        )}
      </div>
    </MainLayout>
  );
};

export default ClinicManagementPage;
