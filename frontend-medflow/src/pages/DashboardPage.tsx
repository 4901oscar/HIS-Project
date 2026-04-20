/**
 * DashboardPage - Página principal del sistema
 * Ejemplo de cómo usar el MainLayout
 */

import type { FC } from 'react';
import { MainLayout } from '../components/Layout';

const DashboardPage: FC = () => {
  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Panel Principal</h2>
          <p className="mt-1 text-sm text-gray-600">
            Bienvenido al Sistema de Información Hospitalaria MedFlow
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Total de Pacientes</p>
                <p className="text-2xl font-bold text-gray-900">1,234</p>
              </div>
              <div className="h-12 w-12 bg-medin-cyan/10 rounded-full flex items-center justify-center">
                <svg className="h-6 w-6 text-medin-cyan" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Citas de Hoy</p>
                <p className="text-2xl font-bold text-gray-900">56</p>
              </div>
              <div className="h-12 w-12 bg-green-100 rounded-full flex items-center justify-center">
                <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Resultados Pendientes</p>
                <p className="text-2xl font-bold text-gray-900">12</p>
              </div>
              <div className="h-12 w-12 bg-yellow-100 rounded-full flex items-center justify-center">
                <svg className="h-6 w-6 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Personal Conectado</p>
                <p className="text-2xl font-bold text-gray-900">28</p>
              </div>
              <div className="h-12 w-12 bg-blue-100 rounded-full flex items-center justify-center">
                <svg className="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Acciones Rápidas</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <button className="p-4 border-2 border-gray-200 rounded-lg hover:border-medin-cyan hover:bg-medin-cyan/5 transition-colors text-left">
              <p className="font-medium text-gray-900">Nuevo Paciente</p>
              <p className="text-sm text-gray-600">Registrar nuevo paciente</p>
            </button>
            <button className="p-4 border-2 border-gray-200 rounded-lg hover:border-medin-cyan hover:bg-medin-cyan/5 transition-colors text-left">
              <p className="font-medium text-gray-900">Agendar Cita</p>
              <p className="text-sm text-gray-600">Reservar nueva cita</p>
            </button>
            <button className="p-4 border-2 border-gray-200 rounded-lg hover:border-medin-cyan hover:bg-medin-cyan/5 transition-colors text-left">
              <p className="font-medium text-gray-900">Ver Expedientes</p>
              <p className="text-sm text-gray-600">Acceder a archivos del paciente</p>
            </button>
            <button className="p-4 border-2 border-gray-200 rounded-lg hover:border-medin-cyan hover:bg-medin-cyan/5 transition-colors text-left">
              <p className="font-medium text-gray-900">Reportes</p>
              <p className="text-sm text-gray-600">Generar reportes</p>
            </button>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};

export default DashboardPage;
