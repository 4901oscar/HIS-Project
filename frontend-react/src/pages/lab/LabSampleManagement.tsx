/**
 * LabSampleManagement - Gestión de muestras de laboratorio
 */

import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';

const LabSampleManagement: FC = () => {
  return (
    <MainLayout>
      <div className="space-y-6">
        <h2 className="text-2xl font-bold text-gray-900">Laboratory Sample Management</h2>
        <p className="text-gray-600">Manage samples and results</p>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-gray-500">Sample management interface will be implemented here</p>
        </div>
      </div>
    </MainLayout>
  );
};

export default LabSampleManagement;
