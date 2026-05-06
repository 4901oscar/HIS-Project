/**
 * MainLayout - Layout principal con Sidebar para módulos según rol
 */

import { useState } from 'react';
import type { FC, ReactNode } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  CalendarIcon,
  UserGroupIcon,
  BeakerIcon,
  BuildingStorefrontIcon,
  BanknotesIcon,
  Cog6ToothIcon,
  ArrowLeftOnRectangleIcon,
  Bars3Icon,
  XMarkIcon,
  ClipboardDocumentCheckIcon,
} from '@heroicons/react/24/outline';

interface MenuItem {
  name: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  roles: string[];
}

const menuItems: MenuItem[] = [
  {
    name: 'Administración',
    path: '/administrator',
    icon: Cog6ToothIcon,
    roles: ['ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Admisión',
    path: '/admission',
    icon: CalendarIcon,
    roles: ['ADMISSION', 'ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Triaje Pendiente',
    path: '/vitals/triage',
    icon: ClipboardDocumentCheckIcon,
    roles: ['VITAL_SIGNS', 'DOCTOR'],
  },
  {
    name: 'Consultas',
    path: '/doctor/consultas',
    icon: UserGroupIcon,
    roles: ['DOCTOR', 'ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Laboratorio',
    path: '/lab',
    icon: BeakerIcon,
    roles: ['LABORATORY', 'ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Farmacia',
    path: '/pharmacy',
    icon: BuildingStorefrontIcon,
    roles: ['PHARMACY', 'ADMINISTRATOR', 'ADMIN'],
  },
  {
    name: 'Caja',
    path: '/cashier',
    icon: BanknotesIcon,
    roles: ['CASHIER', 'ADMINISTRATOR', 'ADMIN'],
  },
];

interface MainLayoutProps {
  children: ReactNode;
}

const MainLayout: FC<MainLayoutProps> = ({ children }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  const userRole = user?.roles?.[0] || 'ADMISSION';
  const userName = user?.fullName || 'Personal';

  const filteredMenu = menuItems.filter((item) =>
    item.roles.includes(userRole)
  );

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const currentPageName =
    filteredMenu.find((item) => location.pathname.startsWith(item.path))?.name ?? 'MedFlow HIS';

  const today = new Date().toLocaleDateString('es-GT', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Mobile sidebar backdrop */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-gray-600 bg-opacity-75 z-20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 bg-medin-navy transform transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
        aria-label="Navegación principal"
      >
        {/* Logo */}
        <div className="flex items-center justify-between h-16 px-6 bg-medin-navy border-b border-medin-cyan/20">
          <div className="flex items-center space-x-2">
            <img src="/icono.svg" alt="Logo MedFlow" className="h-8 w-auto" />
            <span className="text-xl font-bold">
              <span className="text-white">Med</span>
              <span className="text-medin-cyan">Flow</span>
            </span>
          </div>
          <button
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden text-gray-400 hover:text-white"
            aria-label="Cerrar menú"
          >
            <XMarkIcon className="h-6 w-6" aria-hidden="true" />
          </button>
        </div>

        {/* User Info */}
        <div className="px-6 py-4 border-b border-medin-cyan/20">
          <div className="flex items-center space-x-3">
            <div
              className="h-10 w-10 rounded-full bg-medin-cyan flex items-center justify-center text-medin-navy font-bold"
              aria-hidden="true"
            >
              {userName.charAt(0).toUpperCase()}
            </div>
            <div>
              <p className="text-white font-medium text-sm">{userName}</p>
              <p className="text-gray-400 text-xs">{userRole}</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="px-3 py-4 space-y-1" aria-label="Módulos del sistema">
          {filteredMenu.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname.startsWith(item.path);

            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                aria-current={isActive ? 'page' : undefined}
                className={`flex items-center space-x-3 px-3 py-2.5 rounded-lg transition-colors ${
                  isActive
                    ? 'bg-medin-cyan text-medin-navy font-medium'
                    : 'text-gray-300 hover:bg-medin-navy/50 hover:text-white'
                }`}
              >
                <Icon className="h-5 w-5" aria-hidden="true" />
                <span className="text-sm">{item.name}</span>
              </Link>
            );
          })}
        </nav>

        {/* Logout Button */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-medin-cyan/20">
          <button
            onClick={handleLogout}
            className="flex items-center space-x-3 px-3 py-2.5 rounded-lg text-gray-300 hover:bg-red-600/20 hover:text-red-400 transition-colors w-full"
            aria-label="Cerrar sesión"
          >
            <ArrowLeftOnRectangleIcon className="h-5 w-5" aria-hidden="true" />
            <span className="text-sm">Cerrar sesión</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="lg:pl-64">
        {/* Top bar */}
        <header className="bg-white shadow-sm h-16 flex items-center justify-between px-4 lg:px-8">
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden text-gray-600 hover:text-gray-900"
            aria-label="Abrir menú"
          >
            <Bars3Icon className="h-6 w-6" aria-hidden="true" />
          </button>

          <div className="flex-1 lg:flex-none">
            <h1 className="text-lg font-semibold text-gray-900">{currentPageName}</h1>
          </div>

          <div className="flex items-center space-x-4">
            <span className="hidden sm:block text-sm text-gray-600 capitalize">{today}</span>
          </div>
        </header>

        {/* Page Content */}
        <main className="p-4 lg:p-8">{children}</main>
      </div>
    </div>
  );
};

export default MainLayout;
