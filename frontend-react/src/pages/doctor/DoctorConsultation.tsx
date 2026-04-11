/**
 * DoctorConsultation - Consulta médica y Triaje Manchester
 */

import type { FC } from 'react';
import { MainLayout } from '../../components/Layout';

const DoctorConsultation: FC = () => {
  return (
    <MainLayout>
      <div className="space-y-6">
        <h2 className="text-2xl font-bold text-gray-900">Doctor Consultation</h2>
        <p className="text-gray-600">Clinical consultations and Manchester Triage</p>
        
        <div className="bg-white p-6 rounded-lg shadow">
          <p className="text-gray-500">Consultation interface will be implemented here</p>
        </div>
      </div>
    </MainLayout>
  );
};

export default DoctorConsultation;
