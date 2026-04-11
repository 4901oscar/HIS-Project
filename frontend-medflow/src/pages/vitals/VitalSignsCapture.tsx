/**
 * VitalSignsCapture - Captura de signos vitales (Enfermería)
 */

import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';

const VitalSignsCapture: FC = () => {
  return (
    <MainLayout>
      <div className="space-y-6">
        <h2 className="text-2xl font-bold text-gray-900">Vital Signs Capture</h2>
        <p className="text-gray-600">Record patient vital signs</p>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-gray-500">Capture form will be implemented here</p>
        </div>
      </div>
    </MainLayout>
  );
};

export default VitalSignsCapture;
