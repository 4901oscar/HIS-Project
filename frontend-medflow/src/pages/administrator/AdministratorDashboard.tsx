import type { FC } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainLayout } from '../../components/Layout';

interface DashCard {
  label: string;
  description: string;
  route?: string;
  color: string;
  iconPath: string;
}

const cards: DashCard[] = [
  {
    label: 'Gestión de Personal',
    description: 'Registrar, editar y desactivar cuentas del personal',
    route: '/administrator/empleados',
    color: 'bg-blue-100 text-blue-600',
    iconPath: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z',
  },
  {
    label: 'Catálogo de Medicamentos',
    description: 'Gestión de inventario de medicamentos disponibles',
    route: '/administrator/medicamentos',
    color: 'bg-green-100 text-green-600',
    iconPath: 'M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z',
  },
  {
    label: 'Catálogo de Exámenes',
    description: 'Tipos de exámenes de laboratorio disponibles',
    route: '/administrator/examenes',
    color: 'bg-purple-100 text-purple-600',
    iconPath: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01',
  },
  {
    label: 'Servicios y Precios',
    description: 'Catálogo de servicios facturables con sus tarifas',
    route: '/administrator/servicios',
    color: 'bg-yellow-100 text-yellow-600',
    iconPath: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
  },
  {
    label: 'Triaje Manchester',
    description: 'Motivos y discriminadores del sistema de triaje',
    route: '/administrator/triage',
    color: 'bg-red-100 text-red-600',
    iconPath: 'M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z',
  },
  {
    label: 'Reportes',
    description: 'Próximamente',
    color: 'bg-gray-100 text-gray-400',
    iconPath: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
  },
];

const AdministratorDashboard: FC = () => {
  const navigate = useNavigate();

  return (
    <MainLayout>
      <div className="space-y-6">
        <h2 className="text-2xl font-bold text-gray-900">Panel de Administrador</h2>
        <p className="text-gray-600">Gestione personal, catálogos y configuración del sistema</p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {cards.map(card => (
            card.route ? (
              <button
                key={card.label}
                onClick={() => navigate(card.route!)}
                className="bg-white p-6 rounded-lg shadow text-left hover:shadow-md hover:border-blue-200 border-2 border-transparent transition-all"
              >
                <div className={`h-10 w-10 rounded-lg flex items-center justify-center mb-3 ${card.color}`}>
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={card.iconPath} />
                  </svg>
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-1">{card.label}</h3>
                <p className="text-sm text-gray-600">{card.description}</p>
              </button>
            ) : (
              <div key={card.label} className="bg-white p-6 rounded-lg shadow opacity-60 cursor-not-allowed">
                <div className={`h-10 w-10 rounded-lg flex items-center justify-center mb-3 ${card.color}`}>
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={card.iconPath} />
                  </svg>
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mb-1">{card.label}</h3>
                <p className="text-sm text-gray-600">{card.description}</p>
              </div>
            )
          ))}
        </div>
      </div>
    </MainLayout>
  );
};

export default AdministratorDashboard;
