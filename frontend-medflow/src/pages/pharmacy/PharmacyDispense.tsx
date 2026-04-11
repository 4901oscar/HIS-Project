/**
 * PharmacyDispense - Despacho de medicamentos
 */

import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';

const PharmacyDispense: FC = () => {
  return (
    <MainLayout>
      <div className="space-y-6">
        <h2 className="text-2xl font-bold text-gray-900">Pharmacy Dispense</h2>
        <p className="text-gray-600">Dispense medications to patients</p>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-gray-500">Medication dispensing interface will be implemented here</p>
        </div>
      </div>
    </MainLayout>
  );
};

export default PharmacyDispense;
