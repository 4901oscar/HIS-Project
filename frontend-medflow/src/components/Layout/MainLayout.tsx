/**
 * MainLayout - Layout principal con Sidebar para módulos según rol
 */

import { useState } from 'react';
import type { FC, ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  HomeIcon,
  CalendarIcon,
  HeartIcon,
  UserGroupIcon,
  BeakerIcon,
  BuildingStorefrontIcon,
  BanknotesIcon,
  Cog6ToothIcon,
  ArrowLeftOnRectangleIcon,
  Bars3Icon,
  XMarkIcon,
} from '@heroicons/react/24/outline';

interface MenuItem {
  name: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  roles: string[];
}

const menuItems: MenuItem[] = [
  {
    name: 'Dashboard',
    path: '/dashboard',
    icon: HomeIcon,
    roles: ['ADMINISTRATOR', 'ADMISSION', 'VITAL_SIGNS', 'DOCTOR', 'LABORATORY', 'PHARMACY', 'CASHIER'],
  },
  {
    name: 'Administrator',
    path: '/administrator',
    icon: Cog6ToothIcon,
    roles: ['ADMINISTRATOR'],
  },
  {
    name: 'Admission',
    path: '/admission',
    icon: CalendarIcon,
    roles: ['ADMISSION', 'ADMINISTRATOR'],
  },
  {
    name: 'Vital Signs',
    path: '/vitals',
    icon: HeartIcon,
    roles: ['VITAL_SIGNS', 'ADMINISTRATOR'],
  },
  {
    name: 'Doctor',
    path: '/doctor',
    icon: UserGroupIcon,
    roles: ['DOCTOR', 'ADMINISTRATOR'],
  },
  {
    name: 'Laboratory',
    path: '/lab',
    icon: BeakerIcon,
    roles: ['LABORATORY', 'ADMINISTRATOR'],
  },
  {
    name: 'Pharmacy',
    path: '/pharmacy',
    icon: BuildingStorefrontIcon,
    roles: ['PHARMACY', 'ADMINISTRATOR'],
  },
  {
    name: 'Cashier',
    path: '/cashier',
    icon: BanknotesIcon,
    roles: ['CASHIER', 'ADMINISTRATOR'],
  },
];

interface MainLayoutProps {
  children: ReactNode;
}

const MainLayout: FC<MainLayoutProps> = ({ children }) => {
  const { user, logout } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  // Valores por defecto si no hay usuario
  const userRole = user?.role || 'ADMISSION';
  const userName = user?.name || 'Staff Member';

  // Filtrar menú según rol
  const filteredMenu = menuItems.filter((item) =>
    item.roles.includes(userRole)
  );

  const handleLogout = () => {
    logout();
    // Redirigir al login se puede hacer aquí o en el componente padre
    window.location.href = '/login';
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Mobile sidebar backdrop */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-gray-600 bg-opacity-75 z-20 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 bg-medin-navy transform transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Logo */}
        <div className="flex items-center justify-between h-16 px-6 bg-medin-navy border-b border-medin-cyan/20">
          <div className="flex items-center space-x-2">
            <img src="/icono.svg" alt="MedFlow" className="h-8 w-auto" />
            <span className="text-xl font-bold">
              <span className="text-white">Med</span>
              <span className="text-medin-cyan">Flow</span>
            </span>
          </div>
          <button
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden text-gray-400 hover:text-white"
          >
            <XMarkIcon className="h-6 w-6" />
          </button>
        </div>

        {/* User Info */}
        <div className="px-6 py-4 border-b border-medin-cyan/20">
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-full bg-medin-cyan flex items-center justify-center text-medin-navy font-bold">
              {userName.charAt(0)}
            </div>
            <div>
              <p className="text-white font-medium text-sm">{userName}</p>
              <p className="text-gray-400 text-xs">{userRole}</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="px-3 py-4 space-y-1">
          {filteredMenu.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname.startsWith(item.path);
            
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setSidebarOpen(false)}
                className={`flex items-center space-x-3 px-3 py-2.5 rounded-lg transition-colors ${
                  isActive
                    ? 'bg-medin-cyan text-medin-navy font-medium'
                    : 'text-gray-300 hover:bg-medin-navy/50 hover:text-white'
                }`}
              >
                <Icon className="h-5 w-5" />
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
          >
            <ArrowLeftOnRectangleIcon className="h-5 w-5" />
            <span className="text-sm">Logout</span>
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
          >
            <Bars3Icon className="h-6 w-6" />
          </button>
          
          <div className="flex-1 lg:flex-none">
            <h1 className="text-lg font-semibold text-gray-900">
              {filteredMenu.find((item) => location.pathname.startsWith(item.path))?.name || 'MedFlow HIS'}
            </h1>
          </div>

          <div className="flex items-center space-x-4">
            <span className="hidden sm:block text-sm text-gray-600">
              {new Date().toLocaleDateString('en-US', { 
                weekday: 'long', 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
              })}
            </span>
          </div>
        </header>

        {/* Page Content */}
        <main className="p-4 lg:p-8">
          {children}
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
