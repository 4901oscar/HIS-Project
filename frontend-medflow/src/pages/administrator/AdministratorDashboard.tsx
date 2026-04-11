/**
 * AdministratorDashboard - Dashboard para rol de Administrador
 */

import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';

const AdministratorDashboard: FC = () => {
  return (
    <MainLayout>
      <div className="space-y-6">
        <h2 className="text-2xl font-bold text-gray-900">Administrator Dashboard</h2>
        <p className="text-gray-600">Create profiles, Update information, Deactivate profiles</p>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">User Management</h3>
            <p className="text-sm text-gray-600">Create and manage staff profiles</p>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">System Settings</h3>
            <p className="text-sm text-gray-600">Configure system parameters</p>
          </div>
          
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Reports</h3>
            <p className="text-sm text-gray-600">View system reports and analytics</p>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

export default AdministratorDashboard;
