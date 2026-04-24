/**
 * DashboardPage - Página principal del sistema
 */

import type { FC } from 'react';
import { MainLayout } from '../components/Layout';

const DashboardPage: FC = () => {
  return (
    <MainLayout>
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900">Bienvenido a MedFlow</h2>
          <p className="mt-2 text-sm text-gray-600">
            Seleccione una opción del menú para comenzar
          </p>
        </div>
      </div>
    </MainLayout>
  );
};

export default DashboardPage;
